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

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Malte Schulze
 */
public class UtilItems {
    public static class ItemId implements Comparable<ItemId> {
        public static final ItemId UNKNOWN = new ItemId("unknown", "", "UNKNOWN");
        public final String uniqueId, id, label;

        public ItemId(String id, String prefix, String label) {
            this.id = id;
            this.uniqueId = new StringBuilder(prefix).append("_").append(id).toString();
            this.label = label != null ? label : uniqueId;
        }

        @Override
        public int compareTo(ItemId o) {
            return this.label.compareTo(o.label);
        }
    }
    protected static volatile UtilItems instance;
    protected Map<Object,ItemId> idBySpec;
    protected Map<String,ItemId> idByUniqueId;
    protected boolean dirty;
    
    static public UtilItems getInstance() {
        UtilItems localInstance = UtilItems.instance;
        if ( localInstance == null ) {
            synchronized(UtilItems.class) {
                localInstance = UtilItems.instance;
                if ( localInstance == null ) {
                    UtilItems.instance = localInstance = new UtilItems();
                }
            }
        }
        return localInstance;
    }
    
    protected UtilItems() {
        refreshItemIdCache();
    }
    
    public void refreshItemIdCache() {
        Map<Object, ItemId> m = new HashMap<>();
        for ( CommoditySpecAPI ss : Global.getSettings().getAllCommoditySpecs() ) {
            m.put(ss, new ItemId(ss.getId(),"c",ss.getName()));
        }
        for ( WeaponSpecAPI ss : Global.getSettings().getAllWeaponSpecs() ) {
            m.put(ss, new ItemId(ss.getWeaponId(),"w",ss.getWeaponName()));
        }
        for ( SpecialItemSpecAPI ss : Global.getSettings().getAllSpecialItemSpecs() ) {
            m.put(ss, new ItemId(ss.getId(),"s",ss.getName()));
        }
        for ( FighterWingSpecAPI ss : Global.getSettings().getAllFighterWingSpecs() ) {
            m.put(ss, new ItemId(ss.getId(),"f",ss.getWingName()));
        }
        for ( HullModSpecAPI ss : Global.getSettings().getAllHullModSpecs() ) {
            m.put(ss, new ItemId(ss.getId(),"h",ss.getDisplayName()));
        }
        idBySpec = m;
        Map<String, ItemId> ml = new HashMap<>();
        for ( ItemId ii : idBySpec.values() ) {
            ml.put(ii.uniqueId, ii);
        }
        idByUniqueId = ml;
    }
    
    public ItemId getItemId(Object spec) {
        ItemId id = idBySpec.get(spec);
        if ( id == null ) return ItemId.UNKNOWN;
        return id;
    }

    public ItemId getItemId(CargoStackAPI stack) {
        return stack.isCommodityStack() ? getItemId(stack.getResourceIfResource()) : 
            stack.isWeaponStack() ? getItemId(stack.getWeaponSpecIfWeapon()) : 
            stack.isSpecialStack() ? getItemId(stack.getSpecialItemSpecIfSpecial()) : 
            stack.isFighterWingStack() ? getItemId(stack.getFighterWingSpecIfWing()) : 
            stack.getHullModSpecIfHullMod() != null ? getItemId(stack.getHullModSpecIfHullMod()) : ItemId.UNKNOWN;
    }
    
    public Collection<ItemId> getAllItemIds() {
        return idBySpec.values();
    }
    
    public ItemId getItemIdForUniqueId(String uqId) {
        ItemId id = idByUniqueId.get(uqId);
        if ( id == null ) return ItemId.UNKNOWN;
        return id;
    }
}
