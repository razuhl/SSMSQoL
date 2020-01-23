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
package ssms.qol;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import ssms.qol.events.GlobalEvents;
import ssms.qol.events.SSEvent;
import ssms.qol.events.SSEventCallback;
import ssms.qol.events.InteractionDialogDismissed;
import ssms.qol.events.SSEventPhases;
import ssms.qol.events.ShowLoot;
import ssms.qol.rule.DropSurplus;

public final class ModPlugin extends BaseModPlugin {
    static public List<String> commoditiesToDrop, fuelToDrop, personelToDrop;
    static public boolean unlimitedCommandPoints, mergeCargoPodsOnDrop;
    static public float mergeCargoPodsOnDropRadius;
    
    protected void loadSettings() throws Exception {
        JSONObject settings = Global.getSettings().loadJSON("settings.json");
        JSONObject settingsDropSurplus = settings.optJSONObject("dropSurplus");
        if ( settingsDropSurplus != null ) {
            commoditiesToDrop = new ArrayList<>();
            personelToDrop = new ArrayList<>();
            fuelToDrop = new ArrayList<>();
            JSONArray arr = settingsDropSurplus.optJSONArray("commoditiesToDrop");
            if ( arr != null ) {
                for ( int i = 0; i < arr.length(); i++ ) {
                    commoditiesToDrop.add(arr.getString(i));
                }
            }
            arr = settingsDropSurplus.optJSONArray("personelToDrop");
            if ( arr != null ) {
                for ( int i = 0; i < arr.length(); i++ ) {
                    personelToDrop.add(arr.getString(i));
                }
            }
            arr = settingsDropSurplus.optJSONArray("fuelToDrop");
            if ( arr != null ) {
                for ( int i = 0; i < arr.length(); i++ ) {
                    fuelToDrop.add(arr.getString(i));
                }
            }
            mergeCargoPodsOnDrop = settingsDropSurplus.optBoolean("mergeCargoPods", false);
            mergeCargoPodsOnDropRadius = settingsDropSurplus.optInt("mergeCargoPodsRadius", 300);
        } else {
            commoditiesToDrop = new ArrayList<>();
            personelToDrop = new ArrayList<>();
            fuelToDrop = new ArrayList<>();
            mergeCargoPodsOnDrop = false;
            mergeCargoPodsOnDropRadius = 300f;
        }
        unlimitedCommandPoints = settings.optBoolean("unlimitedCommandPoints", false);
    }
    
    @Override
    public void onApplicationLoad() throws Exception {
        final Logger logger = Global.getLogger(ModPlugin.class);
        logger.setLevel(Level.INFO);
        loadSettings();
        
        //drop loot after custom events for salvage and cargo pods are raised
        GlobalEvents.AddEventListener(ShowLoot.class, new SSEventCallback() {
            @Override
            public void callback(SSEvent event) {
                //logger.log(Level.INFO, "ShowLoot Event");
                InteractionDialogAPI dialog = (InteractionDialogAPI) event.getData().get("dialog");
                SSEventPhases phase = (SSEventPhases) event.getData().get("phase");
                if ( dialog != null && SSEventPhases.postfix == phase ) {
                    InteractionDialogPlugin plugin = dialog.getPlugin();
                    if ( plugin != null ) {
                        Map<String, MemoryAPI> mm = plugin.getMemoryMap();
                        if ( mm != null ) {
                            MemoryAPI ma = mm.get("global");
                            if ( ma != null ) {
                                if ( "SALVAGE".equals(ma.getString("$tradePanelMode")) ) {
                                    DropSurplus.dropSurplus();
                                }/* else {
                                    for ( Map.Entry<String, MemoryAPI> entry : plugin.getMemoryMap().entrySet() ) {
                                        logger.log(Level.INFO,"mainKey: "+entry.getKey());
                                        for ( String subKey : entry.getValue().getKeys() ) {
                                            logger.log(Level.INFO,subKey+": "+entry.getValue().get(subKey));
                                        }
                                    }
                                }*/
                            }
                        }
                    }
                }
            }
        });
        /*GlobalEvents.AddEventListener(InteractionDialogDismissed.class, new SSEventCallback() {
            @Override
            public void callback(SSEvent event) {
                logger.log(Level.INFO, "Dismissed Event");
            }
        });*/
    }

    @Override
    public void onNewGame() {
        Global.getSector().getListenerManager().addListener(new FleetEventListenerImpl());
        Global.getSector().registerPlugin(new CampaignPlugin());
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().getListenerManager().addListener(new FleetEventListenerImpl());
        Global.getSector().registerPlugin(new CampaignPlugin());
    }
}