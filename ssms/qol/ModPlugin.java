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
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import ssms.qol.events.GlobalEvents;
import ssms.qol.events.SSEvent;
import ssms.qol.events.SSEventCallback;
import ssms.qol.events.SSEventPhases;
import ssms.qol.events.ShowLoot;
import ssms.qol.rule.DropSurplus;
import org.lwjgl.input.Keyboard;
import ssms.qol.properties.PropertiesContainer;
import ssms.qol.properties.PropertiesContainerConfiguration;
import ssms.qol.properties.PropertiesContainerConfigurationFactory;
import ssms.qol.properties.PropertiesContainerMerger;
import ssms.qol.properties.PropertyConfigurationBoolean;
import ssms.qol.properties.PropertyConfigurationContainer;
import ssms.qol.properties.PropertyConfigurationFloat;
import ssms.qol.properties.PropertyConfigurationListContainer;
import ssms.qol.properties.PropertyConfigurationListSelectable;
import ssms.qol.properties.PropertyConfigurationString;
import ssms.qol.properties.PropertyValueGetter;
import ssms.qol.properties.PropertyValueSetter;

public final class ModPlugin extends BaseModPlugin {
    static public List<String> commoditiesToDrop = Arrays.asList("metals","ore","food","organics","domestic_goods","rare_ore","rare_metals","volatiles","heavy_machinery","supplies"), 
            fuelToDrop = Arrays.asList("fuel"), personelToDrop = Arrays.asList("marines","crew");
    static public boolean unlimitedCommandPoints = true, mergeCargoPodsOnDrop = true;
    static public float mergeCargoPodsOnDropRadius = 300f;
    static public int modMenuKey, gameMenuKey;
    
    protected void loadSettings() throws Exception {
        JSONObject settings = Global.getSettings().loadJSON("settings.json");
        modMenuKey = settings.optInt("modMenuKey", Keyboard.KEY_F12);
        gameMenuKey = settings.optInt("gameMenuKey", Keyboard.KEY_F10);
    }
    
    protected void configureSettingsApplication() {
        PropertiesContainerConfigurationFactory confFactory = PropertiesContainerConfigurationFactory.getInstance();
        PropertiesContainerConfiguration<ModPlugin> confDropSurplus = confFactory.getOrCreatePropertiesContainerConfiguration("SSMSDropSurplus", ModPlugin.class);
        
        confDropSurplus.addProperty(new PropertyConfigurationListSelectable<ModPlugin>("commoditiesToDrop","Commodities To Drop","Which commodieties can be placed in cargo pods if the fleets inventory is full. The order determines which commodities to drop first.",
                new ArrayList<>(Arrays.asList("metals","ore","food","organics","domestic_goods","rare_ore","rare_metals","volatiles","heavy_machinery","supplies")),
                10,new PropertyValueGetter<ModPlugin, List>() {
            @Override
            public List get(ModPlugin sourceObject) {
                return ModPlugin.commoditiesToDrop;
            }
        },new PropertyValueSetter<ModPlugin, List>() {
            @Override
            public void set(ModPlugin sourceObject, List value) {
                ModPlugin.commoditiesToDrop = new ArrayList<>(value);
            }
        }, false,String.class) {
            protected Map<String,String> labels;
            
            @Override
            public List buildOptions() {
                List<String> ids = new ArrayList<>();
                for ( CommoditySpecAPI api : Global.getSettings().getAllCommoditySpecs() ) {
                    ids.add(api.getId());
                }
                return ids;
            }

            @Override
            public String getOptionLabel(Object o) {
                if ( labels == null ) {
                    labels = new HashMap<>();
                    for ( CommoditySpecAPI api : Global.getSettings().getAllCommoditySpecs() ) {
                        labels.put(api.getId(), api.getName());
                    }
                }
                return labels.get((String)o);
            }
        });
        confDropSurplus.addProperty(new PropertyConfigurationListSelectable<ModPlugin>("personelToDrop","Personel To Drop","Which personel type cargo can be placed in cargo pods if the fleets inventory is full. The order determines which commodities to drop first.",
                new ArrayList<>(Arrays.asList("marines","crew")),
                20,new PropertyValueGetter<ModPlugin, List>() {
            @Override
            public List get(ModPlugin sourceObject) {
                return ModPlugin.personelToDrop;
            }
        },new PropertyValueSetter<ModPlugin, List>() {
            @Override
            public void set(ModPlugin sourceObject, List value) {
                ModPlugin.personelToDrop = new ArrayList<>(value);
            }
        }, false,String.class) {
            protected Map<String,String> labels;
            
            @Override
            public List buildOptions() {
                List<String> ids = new ArrayList<>();
                for ( CommoditySpecAPI api : Global.getSettings().getAllCommoditySpecs() ) {
                    if ( api.isPersonnel() )
                        ids.add(api.getId());
                }
                return ids;
            }

            @Override
            public String getOptionLabel(Object o) {
                if ( labels == null ) {
                    labels = new HashMap<>();
                    for ( CommoditySpecAPI api : Global.getSettings().getAllCommoditySpecs() ) {
                        if ( api.isPersonnel() ) 
                            labels.put(api.getId(), api.getName());
                    }
                }
                return labels.get((String)o);
            }
        });
        confDropSurplus.addProperty(new PropertyConfigurationListSelectable<ModPlugin>("fuelToDrop","Fuel To Drop","Which fuel type cargo can be placed in cargo pods if the fleets inventory is full. The order determines which commodities to drop first.",
                new ArrayList<>(Arrays.asList("fuel")),
                30,new PropertyValueGetter<ModPlugin, List>() {
            @Override
            public List get(ModPlugin sourceObject) {
                return ModPlugin.fuelToDrop;
            }
        },new PropertyValueSetter<ModPlugin, List>() {
            @Override
            public void set(ModPlugin sourceObject, List value) {
                ModPlugin.fuelToDrop = new ArrayList<>(value);
            }
        }, false,String.class) {
            protected Map<String,String> labels;
            
            @Override
            public List buildOptions() {
                List<String> ids = new ArrayList<>();
                for ( CommoditySpecAPI api : Global.getSettings().getAllCommoditySpecs() ) {
                    if ( api.isFuel() )
                        ids.add(api.getId());
                }
                return ids;
            }

            @Override
            public String getOptionLabel(Object o) {
                if ( labels == null ) {
                    labels = new HashMap<>();
                    for ( CommoditySpecAPI api : Global.getSettings().getAllCommoditySpecs() ) {
                        if ( api.isFuel() ) 
                            labels.put(api.getId(), api.getName());
                    }
                }
                return labels.get((String)o);
            }
        });
        confDropSurplus.addProperty(new PropertyConfigurationBoolean<>("mergeCargoPodsOnDrop","Merge CPs","Spawns dropped surplus into nearby cargo pods that arent stabilized.",true,40, new PropertyValueGetter<ModPlugin, Boolean>() {
            @Override
            public Boolean get(ModPlugin sourceObject) {
                return ModPlugin.mergeCargoPodsOnDrop;
            }
        }, new PropertyValueSetter<ModPlugin, Boolean>() {
            @Override
            public void set(ModPlugin sourceObject, Boolean value) {
                ModPlugin.mergeCargoPodsOnDrop = value;
            }
        }, false));
        confDropSurplus.addProperty(new PropertyConfigurationFloat<>("mergeCargoPodsOnDropRadius","Merge radius","Maximal distance between fleet and mergable cargo pod.",300f,50, new PropertyValueGetter<ModPlugin, Float>() {
            @Override
            public Float get(ModPlugin sourceObject) {
                return ModPlugin.mergeCargoPodsOnDropRadius;
            }
        }, new PropertyValueSetter<ModPlugin, Float>() {
            @Override
            public void set(ModPlugin sourceObject, Float value) {
                ModPlugin.mergeCargoPodsOnDropRadius = value;
            }
        }, false, 0f, Float.MAX_VALUE));
        
        PropertiesContainerConfiguration<ModPlugin> conf = confFactory.getOrCreatePropertiesContainerConfiguration("SSMSQoLSettings", ModPlugin.class);
        conf.addProperty(new PropertyConfigurationBoolean<>("unlimitedCommandPoints","Unlimited CP","Prevents Command Points from falling below 5 during combat.",true,10, new PropertyValueGetter<ModPlugin, Boolean>() {
            @Override
            public Boolean get(ModPlugin sourceObject) {
                return ModPlugin.unlimitedCommandPoints;
            }
        }, new PropertyValueSetter<ModPlugin, Boolean>() {
            @Override
            public void set(ModPlugin sourceObject, Boolean value) {
                ModPlugin.unlimitedCommandPoints = value;
            }
        }, false));
        conf.addProperty(new PropertyConfigurationContainer<>("dropSurplus","Drop Surplus...","Change the drop surplus settings.",this,"SSMSDropSurplus",ModPlugin.class,0,new PropertyValueGetter<ModPlugin, ModPlugin>() {
            @Override
            public ModPlugin get(ModPlugin sourceObject) {
                return sourceObject;
            }
        }, new PropertyValueSetter<ModPlugin, PropertiesContainer>() {
            @Override
            public void set(ModPlugin sourceObject, PropertiesContainer value) {
                //already done in the properties since we use the same sourceObject for this layer
            }
        }, false));
        conf.configureApplicationScopedSingleInstance("SSMSQoL",this,true);
    }
    
    
    
    protected void configureSettingsGame() {
        PropertiesContainerConfigurationFactory confFactory = PropertiesContainerConfigurationFactory.getInstance();
        //Game settings are configured every time a save is loaded(or new game is started). To avoid pollution between games we remove all previously configured game settings.
        confFactory.removeGameScopeConfigurations();
    }
    
    @Override
    public void onApplicationLoad() throws Exception {
        final Logger logger = Global.getLogger(ModPlugin.class);
        logger.setLevel(Level.INFO);
        loadSettings();
        configureSettingsApplication();
        
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
    public void onGameLoad(boolean newGame) {
        configureSettingsGame();
        
        Global.getSector().getListenerManager().addListener(new FleetEventListenerImpl());
        Global.getSector().registerPlugin(new CampaignPlugin());
        
        Global.getSector().addTransientScript(new EveryFrameScript() {
            boolean done = false;
            
            @Override
            public boolean isDone() {
                return done;
            }

            @Override
            public boolean runWhilePaused() {
                return true;
            }

            @Override
            public void advance(float amount) {
                done = true;
                Global.getLogger(ModPlugin.class).log(Level.INFO, "loading settings");
                PropertiesContainerConfigurationFactory fac = PropertiesContainerConfigurationFactory.getInstance();
                fac.load();
                fac.MergeSettings();
            }
        });
        Global.getSector().addTransientScript(new EveryFrameScript() {
            @Override
            public void advance(float amount) {
                if (Keyboard.isKeyDown(modMenuKey)) {
                    InteractionDialogAPI idAPI = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
                    if ( idAPI == null || idAPI.getPlugin().getClass() != InteractionDialogPlugin_ApplicationSettings.class ) {
                        Global.getSector().getCampaignUI().showInteractionDialog(new InteractionDialogPlugin_ApplicationSettings(), null);
                    }
                } else if (Keyboard.isKeyDown(gameMenuKey)) {
                    InteractionDialogAPI idAPI = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
                    if ( idAPI == null || idAPI.getPlugin().getClass() != InteractionDialogPlugin_GameSettings.class ) {
                        Global.getSector().getCampaignUI().showInteractionDialog(new InteractionDialogPlugin_GameSettings(), null);
                    }
                }
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public boolean runWhilePaused() {
                return true;
            }
        });
    }
}