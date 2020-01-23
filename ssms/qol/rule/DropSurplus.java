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
package ssms.qol.rule;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CargoPodsEntityPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.Map;
import ssms.qol.ModPlugin;
import static ssms.qol.ModPlugin.commoditiesToDrop;
import static ssms.qol.ModPlugin.fuelToDrop;
import static ssms.qol.ModPlugin.personelToDrop;

/**
 *
 * @author Malte Schulze
 */
public class DropSurplus extends BaseCommandPlugin {
    static public void dropSurplus() {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if ( player != null ) {
            CargoAPI cargo = player.getCargo();
            CargoAPI cargoToDrop = Global.getFactory().createCargo(true);
            int freeFuelSpace = cargo.getFreeFuelSpace();
            if ( freeFuelSpace < 0 && fuelToDrop != null && !fuelToDrop.isEmpty() ) {
                //cargoToDrop.addFuel(-freeFuelSpace);
                freeFuelSpace = -freeFuelSpace;
                for ( String id : fuelToDrop ) {
                    CommoditySpecAPI csa = Global.getSettings().getCommoditySpec(id);
                    if ( csa != null && csa.isFuel() ) {
                        float availableQuantity = cargo.getCommodityQuantity(id);
                        if ( availableQuantity > 0f ) {
                            float dropQuantity = Math.min(freeFuelSpace, availableQuantity);
                            cargoToDrop.addCommodity(id, dropQuantity);
                            freeFuelSpace -= dropQuantity;
                            if ( freeFuelSpace <= 0f ) break;
                        }
                    }
                }
            }
            float freeCargoSpace = cargo.getMaxCapacity() - cargo.getSpaceUsed();
            if ( freeCargoSpace < 0f && commoditiesToDrop != null && !commoditiesToDrop.isEmpty() ) {
                float cargoSpaceToFreeUp = -freeCargoSpace;
                for ( String id : commoditiesToDrop ) {
                    CommoditySpecAPI csa = Global.getSettings().getCommoditySpec(id);
                    if ( csa != null && csa.getCargoSpace() > 0f ) {
                        float availableQuantity = cargo.getCommodityQuantity(id);
                        if ( availableQuantity > 0f ) {
                            float dropQuantity = Math.min(cargoSpaceToFreeUp, availableQuantity*csa.getCargoSpace())/csa.getCargoSpace();
                            //dropQuantity = (float) Math.ceil(dropQuantity);
                            cargoToDrop.addCommodity(id, dropQuantity);
                            cargoSpaceToFreeUp -= dropQuantity * csa.getCargoSpace();
                            if ( cargoSpaceToFreeUp <= 0f ) break;
                        }
                    }
                }
            }
            int freePersonel = cargo.getFreeCrewSpace();
            if ( freePersonel < 0 && personelToDrop != null && !personelToDrop.isEmpty() ) {
                float personelToFire = -freePersonel;
                for ( String id : personelToDrop ) {
                    CommoditySpecAPI csa = Global.getSettings().getCommoditySpec(id);
                    if ( csa != null && csa.isPersonnel() ) {
                        float availableQuantity = cargo.getCommodityQuantity(id);
                        if ( availableQuantity > 0f ) {
                            float dropQuantity = Math.min(personelToFire, availableQuantity);
                            cargoToDrop.addCommodity(id, dropQuantity);
                            personelToFire -= dropQuantity;
                            if ( personelToFire <= 0f ) break;
                        }
                    }
                }
            }
            if ( !cargoToDrop.isEmpty() ) {
                //remove cargo from fleet
                cargo.removeAll(cargoToDrop);
                //spawn cargo pod with cargoToDrop
                CustomCampaignEntityAPI cargoPod = null;
                if ( ModPlugin.mergeCargoPodsOnDrop ) {
                    List<CustomCampaignEntityAPI> exPods = Global.getSector().getCurrentLocation().getEntities(CustomCampaignEntityAPI.class);
                    if ( exPods != null ) {
                        float radius = ModPlugin.mergeCargoPodsOnDropRadius;
                        //final Logger logger = Global.getLogger(ModPlugin.class);
                        for ( CustomCampaignEntityAPI pod : exPods ) {
                            CustomCampaignEntityPlugin p = pod.getCustomPlugin();
                            if ( p != null && CargoPodsEntityPlugin.class.isAssignableFrom(p.getClass()) ) {
                                CargoPodsEntityPlugin plugin = ((CargoPodsEntityPlugin)p);
                                //don't merge with stabilized or old cargo pods
                                if ( plugin.getExtraDays() > 0f || plugin.getElapsed() > 1f ) continue;
                                //only close by
                                if ( Misc.getDistance(player, pod) < radius ) {
                                    cargoPod = pod;
                                    break;
                                }
                                //logger.log(Level.INFO, "Distance: "+Misc.getDistance(player, pod));
                                //logger.log(Level.INFO, "Days: "+plugin.getElapsed()+" "+plugin.getDaysLeft()+" "+plugin.getExtraDays());
                            }
                        }
                    }
                }
                if ( cargoPod == null ) {
                    if ( !player.isInHyperspace() )
                        cargoPod = Misc.addCargoPods(player.getContainingLocation(), player.getLocation());
                    else cargoPod = Misc.addCargoPods(Global.getSector().getHyperspace(), player.getLocationInHyperspace());
                }
                cargoPod.getCargo().addAll(cargoToDrop);
            }
        }
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        try {
            dropSurplus();
        } catch (Throwable t) {
            Logger logger = Logger.getLogger(ModPlugin.class);
            logger.log(Level.ERROR, "DropSurplus cmd failed to execute!", t);
        }
        
        return true;
    }
    
}
