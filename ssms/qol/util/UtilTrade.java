/*
 * Copyright (C) 2020 Malte Schulze.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library;  If not, see 
 * <https://www.gnu.org/licenses/>.
 */
package ssms.qol.util;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.LocalResourcesSubmarketPlugin;
import com.fs.starfarer.campaign.CampaignEngine;
import com.fs.starfarer.settings.StarfarerSettings;
import java.util.List;

/**
 *
 * @author Malte Schulze
 */
public class UtilTrade {
    protected static volatile UtilTrade instance;
    protected float nonEconItemBuyPriceMult, nonEconItemSellPriceMult, shipWeaponSellPriceMult;
    
    static public UtilTrade getInstance() {
        UtilTrade localInstance = UtilTrade.instance;
        if ( localInstance == null ) {
            synchronized(UtilItems.class) {
                localInstance = UtilTrade.instance;
                if ( localInstance == null ) {
                    UtilTrade.instance = localInstance = new UtilTrade();
                }
            }
        }
        return localInstance;
    }
    
    protected UtilTrade() {
        nonEconItemBuyPriceMult = StarfarerSettings.o00000("nonEconItemBuyPriceMult");
        nonEconItemSellPriceMult = StarfarerSettings.o00000("nonEconItemSellPriceMult");
        shipWeaponSellPriceMult = StarfarerSettings.o00000("shipWeaponSellPriceMult");
    }
    
    public float priceForBuying(CargoStackAPI stack, MarketAPI market) {
        return priceForBuying(stack, stack.getSize(), market);
    }
    
    public float priceForBuying(CargoStackAPI stack, float quantity, MarketAPI market) {
        if ( stack.isCommodityStack() ) {
            return market.getSupplyPrice(stack.getCommodityId(), quantity, true);
        } else {
            float stackPrice = stack.getBaseValuePerUnit() * quantity;
            stackPrice *= nonEconItemBuyPriceMult;
            return stackPrice;
        }
    }
    
    public void doTransaction(PlayerMarketTransaction transaction, CargoAPI tradepartner) {
        if ( transaction == null ) return;
        computeTransactionValue(transaction);
        CargoAPI cargo = transaction.getSold();
        SubmarketAPI submarket = transaction.getSubmarket();
        submarket.getPlugin().updateCargoPrePlayerInteraction();
        
        if ( cargo != null ) {
            submarket.getCargo().addAll(cargo);
            tradepartner.removeAll(cargo);
        }
        cargo = transaction.getBought();
        if ( cargo != null ) {
            tradepartner.addAll(cargo);
            submarket.getCargo().removeAll(cargo);
        }
        
        //stockpiles add the goods taken to the monthly report as costs when reporting the transaction.
        if ( !submarket.getSpecId().equals("local_resources") ) {
            tradepartner.getCredits().add(transaction.getCreditValue());
        }
        
        submarket.getPlugin().reportPlayerMarketTransaction(transaction);
        CampaignEngine.getInstance().reportPlayerMarketTransaction(transaction);
    }
    
    public void computeTransactionValue(PlayerMarketTransaction transaction) {
        List<CargoStackAPI> sold = transaction.getSold().getStacksCopy();
        float creditValue = 0;
        if ( sold != null ) {
            for ( CargoStackAPI stack : sold ) {
                if ( stack.isCommodityStack() ) {
                    creditValue += transaction.getMarket().getDemandPrice(stack.getCommodityId(), stack.getSize(), true);
                } else {
                    float stackPrice = stack.getBaseValuePerUnit() * stack.getSize();
                    if (stack.isWeaponStack() || stack.isFighterWingStack()) {
                        stackPrice *= shipWeaponSellPriceMult;
                    } else {
                        stackPrice *= nonEconItemSellPriceMult;
                    }
                    creditValue += Math.floor(stackPrice);
                }
            }
        }
        
        List<CargoStackAPI> bought = transaction.getBought().getStacksCopy();
        if ( bought != null ) {
            for ( CargoStackAPI stack : bought ) {
                if ( stack.isCommodityStack() ) {
                    if ( LocalResourcesSubmarketPlugin.class.isAssignableFrom(transaction.getSubmarket().getPlugin().getClass()) ) {
                        creditValue -= LocalResourcesSubmarketPlugin.getStockpilingUnitPrice(stack.getResourceIfResource(), false) * stack.getSize();
                    } else creditValue -= transaction.getMarket().getSupplyPrice(stack.getCommodityId(), stack.getSize(), true);
                } else {
                    float stackPrice = stack.getBaseValuePerUnit() * stack.getSize();
                    stackPrice *= nonEconItemBuyPriceMult;
                    creditValue -= stackPrice;
                }
            }
        }

        creditValue -= Math.abs(creditValue) * transaction.getSubmarket().getTariff();
        transaction.setCreditValue(creditValue > 0 ? (float)Math.floor(creditValue) : (float)Math.ceil(creditValue));
    }
}
