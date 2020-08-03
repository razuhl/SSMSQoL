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
package ssms.qol.properties;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.settings.StarfarerSettings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ssms.qol.ModPlugin;

/**
 *
 * @author Malte Schulze
 */
public class PropertiesContainerConfigurationFactory {
    private static volatile PropertiesContainerConfigurationFactory instance;
    protected Map<String,PropertiesContainerConfiguration<?>> configurations = new HashMap<>();
    protected Map<String,Map<String,PropertiesContainer>> managedInstances = new HashMap<>();
    protected Map<String,Map<String,PropertiesContainer>> overriddenInstances = new HashMap<>();
    private PropertiesContainerConfigurationFactory() {}
    
    static public PropertiesContainerConfigurationFactory getInstance() {
        PropertiesContainerConfigurationFactory localInstance = PropertiesContainerConfigurationFactory.instance;
        if ( localInstance == null ) {
            synchronized(PropertiesContainerConfigurationFactory.class) {
                localInstance = PropertiesContainerConfigurationFactory.instance;
                if ( localInstance == null ) {
                    PropertiesContainerConfigurationFactory.instance = localInstance = new PropertiesContainerConfigurationFactory();
                }
            }
        }
        return localInstance;
    }
    
    public <K> PropertiesContainerConfiguration<K> getPropertiesContainerConfiguration(String id, Class<K> sourceObjectClass) {
        PropertiesContainerConfiguration<?> conf = configurations.get(id);
        if ( conf != null ) {
            if ( conf.getSourceObjectClass() == sourceObjectClass ) {
                return (PropertiesContainerConfiguration<K>)conf;
            }
        }
        return null;
    }
    
    public <K> PropertiesContainerConfiguration getPropertiesContainerConfiguration(String id) {
        PropertiesContainerConfiguration<?> conf = configurations.get(id);
        if ( conf != null ) {
            return conf;
        }
        return null;
    }
    
    public <K> PropertiesContainerConfiguration<K> getOrCreatePropertiesContainerConfiguration(String id, Class<K> sourceObjectClass) {
        PropertiesContainerConfiguration<?> conf = configurations.get(id);
        if ( conf != null ) {
            if ( conf.getSourceObjectClass() == sourceObjectClass ) {
                return (PropertiesContainerConfiguration<K>)conf;
            }
        } else {
            PropertiesContainerConfiguration<K> newConf = new PropertiesContainerConfiguration<>(sourceObjectClass,id);
            configurations.put(id, newConf);
            return newConf;
        }
        return null;
    }
    
    public List<PropertiesContainerConfiguration<?>> getAllConfigurations() {
        List<PropertiesContainerConfiguration<?>> lst = new ArrayList<>(configurations.values());
        Collections.sort(lst, new Comparator<PropertiesContainerConfiguration<?>>() {
            @Override
            public int compare(PropertiesContainerConfiguration<?> o1, PropertiesContainerConfiguration<?> o2) {
                return Integer.compare(o1.getOrder(),o2.getOrder());
            }
        });
        return lst;
    }
    
    public void MergeSettings() {
        MergeSettings(false);
    }
    
    public void MergeSettings(boolean reload) {
        Logger logger = Global.getLogger(ModPlugin.class);
        
        for ( PropertiesContainerConfiguration props : configurations.values() ) {
            List<PropertiesContainer> loadedSettings = new ArrayList<>();
            if ( props.getAllSourceObjects() != null ) {
                Map<String,PropertiesContainer> instances = managedInstances.get(props.getConfigurationId());
                if ( instances == null ) {
                    instances = new HashMap<>();
                }
                Map<String,PropertiesContainer> overriddenSettings = overriddenInstances.get(props.getConfigurationId());
                if ( overriddenSettings == null ) {
                    overriddenSettings = new HashMap<>();
                    overriddenInstances.put(props.getConfigurationId(), overriddenSettings);
                }

                Map<String,PropertiesContainer> instancesAndOverridden = new HashMap<>(instances);
                //all overridden settings must be present so we find the ones that we have to reset, not the case if the list got purged from non active settings.
                for ( PropertiesContainer setting : overriddenSettings.values() ) {
                    if ( !instancesAndOverridden.containsKey(setting.getId()) ) {
                        instancesAndOverridden.put(setting.getId(),setting);
                    }
                }

                for ( PropertiesContainer setting : instancesAndOverridden.values() ) {
                    if ( setting.isActive() && (setting.isDirty()|| reload) ) {
                        if ( !overriddenSettings.containsKey(setting.getId()) ) {
                            //before we apply a new value for a def for the first time we save the original data
                            try {
                                overriddenSettings.put(setting.getId(), props.createContainer(props.getSourceObjectFromId().get(setting.getId())));
                            } catch (Throwable t) {
                                logger.log(Level.WARN, "setting \""+props.getConfigurationId()+"\".\""+setting.getId()+"\" failed to find overriden object. Deactivating entry.", t);
                                setting.setActive(false);
                                instances.remove(setting.getId());
                                continue;
                            }
                        }
                    } else if ( !setting.isActive() && overriddenSettings.containsKey(setting.getId()) ) {
                        //no longer active but previously overridden
                        overriddenSettings.get(setting.getId()).copyTo(setting);
                        setting.setDirty(true);
                        overriddenSettings.remove(setting.getId());
                    } else {
                        //not an active setting that needs to be merged, nor a reset setting
                        continue;
                    }
                    
                    boolean isValid;
                    try {
                        isValid = setting.isValid();
                    } catch (Throwable t) {
                        logger.log(Level.WARN,"Validation for setting \""+props.getConfigurationId()+"\".\""+setting.getId()+"\" failed.",t);
                        isValid = false;
                    }
                    if ( isValid ) {
                        //only when using active settings do we want to discard unconfigured fields otherwise we already have the overridden one
                        if ( setting.isActive() ) {
                            overriddenSettings.get(setting.getId()).copyTo(setting,true);
                        }
                        
                        loadedSettings.add(setting);
                        try {
                            if ( !setting.set() ) {
                                throw new Exception("Set method returned false, this should be handled via a validator!");
                            }
                        } catch (Throwable t) {									
                            setting.setActive(false);
                            instances.remove(setting.getId());
                            logger.log(Level.ERROR,"Unable to write changes into source object for setting \""+props.getConfigurationId()+"\".\""+setting.getId()+"\". Deactivating entry but residual changes might already be present.",t);
                        }
                    } else {
                        setting.setActive(false);
                        overriddenSettings.remove(setting.getId());
                        instances.remove(setting.getId());
                        logger.log(Level.WARN,"Setting \""+props.getConfigurationId()+"\".\""+setting.getId()+"\" was marked as invalid. Deactivating entry.");
                    }
                    setting.setDirty(false);
                }
            }
            props.postSetters(loadedSettings);
        }
    }

    public <K> PropertiesContainer<K> createManagedInstanceFromSource(PropertiesContainerConfiguration<K> conf, K sourceObject) {
        Map<String, PropertiesContainer> instances = managedInstances.get(conf.getConfigurationId());
        if ( instances == null ) {
            instances = new HashMap<>();
            managedInstances.put(conf.getConfigurationId(), instances);
        }
        String id = conf.getIdFromSourceObject() != null ? conf.getIdFromSourceObject().get(sourceObject) : null;
        PropertiesContainer<K> container = instances.get(id);
        if ( container != null ) return container;
        container = conf.createContainer(sourceObject);
        instances.put(container.id, container);
        return container;
    }
    
    public <K> PropertiesContainer<K> createManagedInstanceFromJSON(PropertiesContainerConfiguration<K> conf, JSONObject json) {
        Map<String, PropertiesContainer> instances = managedInstances.get(conf.getConfigurationId());
        if ( instances == null ) {
            instances = new HashMap<>();
            managedInstances.put(conf.getConfigurationId(), instances);
        }
        PropertiesContainer<K> container;
        String id;
        try {
            id = json.isNull("id") ? null : json.getString("id");
            container = instances.get(id);
            if ( container != null ) return container;
        } catch (JSONException ex) {
            Global.getLogger(ModPlugin.class).log(Level.ERROR,"Failed to read id from json while creating a managed instance! JSON: "+json);
        }
        container = conf.createContainer(json);
        instances.put(container.id, container);
        return container;
    }

    public boolean save() {
        Logger logger = Global.getLogger(ModPlugin.class);
        JSONObject json = new JSONObject();
        
        for ( Map.Entry<String,Map<String,PropertiesContainer>> entry : managedInstances.entrySet() ) {
            JSONArray arr = new JSONArray();
            for ( Map.Entry<String,PropertiesContainer> container : entry.getValue().entrySet() ) {
                try {
                    JSONObject jsonContainer = new JSONObject();
                    container.getValue().saveTo(jsonContainer);
                    arr.put(jsonContainer);
                } catch (JSONException ex) {
                    logger.log(Level.ERROR, "Failed to create JSON data for properties container "+entry.getKey()+"/"+container.getKey()+"!", ex);
                }
            }
            try {
                json.put(entry.getKey(), arr);
            } catch (JSONException ex) {
                logger.log(Level.ERROR, "Failed to create JSON data for properties configuration "+entry.getKey()+"!", ex);
            }
        }
        
        try {
            Global.getSettings().writeTextFileToCommon("SSMSQoL.json", json.toString(2));
            return true;
        } catch (JSONException ex) {
            logger.log(Level.ERROR, "Failed to create JSON data for saving configured settings!", ex);
        } catch (IOException ex) {
            logger.log(Level.ERROR, "Failed to use file when saving configured settings!", ex);
        }
        return false;
    }

    public void load() {
        Logger logger = Global.getLogger(ModPlugin.class);
        String str;
        if ( !Global.getSettings().fileExistsInCommon("SSMSQoL.json") ) return;
        try {
            str = Global.getSettings().readTextFileFromCommon("SSMSQoL.json");
        } catch (IOException ex) {
            logger.log(Level.ERROR, "Failed to read file: SSMSQoL.json",ex);
            return;
        }
        JSONObject json;
        try {
            json = new JSONObject(str);
        } catch (JSONException ex) {
            logger.log(Level.ERROR, "File SSMSQoL.json contains invalid json syntax!",ex);
            return;
        }
        
        deleteAllManagedInstances();
        
        Iterator it = json.keys();
        while ( it.hasNext() ) {
            String confId = (String) it.next();
            PropertiesContainerConfiguration conf = getPropertiesContainerConfiguration(confId);
            if ( conf == null ) {
                logger.log(Level.WARN, "Found obsolete configuration id "+confId+" ignoring entries.");
                continue;
            }
            JSONArray jsonContainers;
            try {
                jsonContainers = json.getJSONArray(confId);
            } catch (JSONException ex) {
                logger.log(Level.ERROR, "Failed to read array of managed properties containers! Skipping configurations for id \""+confId+"\".",ex);
                continue;
            }
            for ( int i = 0; i < jsonContainers.length(); i++ ) {
                JSONObject jsonContainer;
                try {
                    jsonContainer = jsonContainers.getJSONObject(i);
                } catch (JSONException ex) {
                    logger.log(Level.ERROR,"Failed to read managed properties container! Skipping entry for configurarion id \""+confId+"\" at position "+i+".",ex);
                    continue;
                }
                createManagedInstanceFromJSON(conf,jsonContainer).markAsChanged();
            }
        }
    }
    
    public void deleteManagedInstance(PropertiesContainer container) {
        Map<String,PropertiesContainer> containers = managedInstances.get(container.getConf().getConfigurationId());
        if ( containers != null ) {
            PropertiesContainer removedContainer = containers.remove(container.id);
            if ( removedContainer != null ) {
                resetManagedInstance(removedContainer);
            }
        }
    }
    
    protected void resetManagedInstance(PropertiesContainer container) {
        Map<String,PropertiesContainer> containers = overriddenInstances.get(container.getConf().getConfigurationId());
        if ( containers != null ) {
            PropertiesContainer removedContainer = containers.remove(container.id);
            if ( removedContainer != null ) {
                removedContainer.set();
            }
        }
    }

    public <K> PropertiesContainer<K> getManagedInstance(PropertiesContainerConfiguration<K> conf, String id) {
        Map<String,PropertiesContainer> containers = managedInstances.get(conf.getConfigurationId());
        if ( containers != null ) {
            return containers.get(id);
        }
        return null;
    }
    
    public void deleteAllManagedInstances(PropertiesContainerConfiguration conf) {
        if ( conf == null ) return;
        deleteAllManagedInstances(conf.getConfigurationId());
    }

    public void deleteAllManagedInstances(String configurationId) {
        if ( configurationId == null ) return;
        Map<String,PropertiesContainer> containers = managedInstances.get(configurationId);
        if ( containers != null ) {
            for ( PropertiesContainer container : containers.values() ) {
                resetManagedInstance(container);
            }
            containers.clear();
        }
    }
    
    public void deleteAllManagedInstances() {
        for ( String configurationId : managedInstances.keySet() ) {
            deleteAllManagedInstances(configurationId);
        }
    }

    public void removePropertiesContainerConfigurations(String ...ids) {
        for ( String id : ids ) {
            deleteAllManagedInstances(id);
            configurations.remove(id);
        }
    }

    public void removeGameScopeConfigurations() {
        ArrayList<String> lst = new ArrayList<>();
        for ( PropertiesContainerConfiguration conf : configurations.values() ) {
            if ( !conf.isApplicationScoped() ) lst.add(conf.getConfigurationId());
        }
        removePropertiesContainerConfigurations(lst.toArray(new String[0]));
    }
}
