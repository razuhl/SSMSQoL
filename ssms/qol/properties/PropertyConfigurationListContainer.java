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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.log4j.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ssms.qol.ModPlugin;

/**
 *
 * @author Malte Schulze
 */
public class PropertyConfigurationListContainer<K> extends PropertyConfigurationList<K> {
    protected PropertiesContainerConfiguration conf;
    protected List defaultValueUnwrapped;
    protected boolean addingAllowed, removalAllowed;
    protected Callable createNewEntry;
    
    public PropertyConfigurationListContainer(String id, String label, String tooltip, List defaultValueUnwrapped, int order, final PropertyValueGetter<K, List> getter, 
            final PropertyValueSetter<K, List> setter, boolean nullable, String containerConfigurationId, boolean addingAllowed, boolean removalAllowed, Callable createNewEntry) {
        super(id, label, tooltip, null, order, null, null, nullable);
        conf = PropertiesContainerConfigurationFactory.getInstance().getPropertiesContainerConfiguration(containerConfigurationId);
        this.defaultValueUnwrapped = defaultValueUnwrapped;
        this.addingAllowed = addingAllowed;
        this.removalAllowed = removalAllowed;
        this.createNewEntry = createNewEntry;
        
        this.getter = new PropertyValueGetter<K, List>() {
            @Override
            public List get(K sourceObject) {
                List value = getter.get(sourceObject);
                if ( value == null ) return null;
                List<PropertiesContainer> lstContainers = new ArrayList<>();
                for ( Object o : value ) {
                    lstContainers.add(o != null ? conf.createContainer(o) : null);
                }
                return lstContainers;
            }
        };
        this.setter = new PropertyValueSetter<K, List>() {
            @Override
            public void set(K sourceObject, List value) {
                if ( value == null ) {
                    if ( setter != null ) setter.set(sourceObject, null);
                } else {
                    List sourceObjects = getter.get(sourceObject);
                    if ( sourceObjects == null ) {
                        sourceObjects = new ArrayList<>();
                        if ( setter != null ) setter.set(sourceObject, sourceObjects);
                    }
                    int sourceObjectsCount = sourceObjects.size(); int containerCounts = value.size();
                    for ( int i = sourceObjectsCount; i < containerCounts; i++ ) {
                        try {
                            sourceObjects.add(PropertyConfigurationListContainer.this.createNewEntry.call());
                        } catch (Exception ex) {
                            Global.getLogger(ModPlugin.class).log(Level.ERROR, "Failed to spawn new source entry for property container list!", ex);
                        }
                    }
                    sourceObjectsCount = sourceObjects.size();
                    for ( int i = 0; i < sourceObjectsCount && i < containerCounts; i++ ) {
                        conf.set(sourceObjects.get(i), (PropertiesContainer)value.get(i));
                    }
                }
            }
        };
    }
    
    public boolean isAddingAllowed() {
        return addingAllowed;
    }

    public boolean isRemovalAllowed() {
        return removalAllowed;
    }
    
    public PropertiesContainer createNewEntry() {
        if ( createNewEntry != null ) {
            try {
                Object obj = createNewEntry.call();
                if ( obj == null || PropertiesContainer.class.isAssignableFrom(obj.getClass()) ) {
                    return (PropertiesContainer)obj;
                }
                return conf.createContainer(obj);
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }
    
    @Override
    public List getDefaultValue() {
        if ( defaultValueUnwrapped == null ) return null;
        List<PropertiesContainer> lstContainers = new ArrayList<>();
        for ( Object o : defaultValueUnwrapped ) {
            lstContainers.add(o != null ? conf.createContainer(o) : null);
        }
        return lstContainers;
    }
    
    @Override
    public void saveTo(JSONObject json, List value) throws JSONException {
        if ( value != null ) {
            JSONArray arr = new JSONArray();
            int i = 0;
            for ( Object entry : value ) {
                if ( entry == null ) arr.put(i, JSONObject.NULL);
                else {
                    PropertiesContainer con = (PropertiesContainer) entry;
                    JSONObject jsonContainer = new JSONObject();
                    json.put(id, jsonContainer);
                    con.saveTo(jsonContainer);
                    arr.put(i, jsonContainer);
                }
                i++;
            }
            json.put(id, arr);
        } else json.put(id, JSONObject.NULL);
    }

    @Override
    public void loadFrom(JSONObject json, PropertyField<List> field) throws JSONException {
        if ( !json.has(id) ) {
            field.setConfigured(false);
        } else {
            if ( json.isNull(id) ) {
                //if ( isValid(null) ) {
                    field.set(null);
                    field.setConfigured(true);
                //} else field.set(defaultValue);
            } else {
                JSONArray arr = json.getJSONArray(id);
                
                List<PropertiesContainer> lst = new ArrayList<>();
                for ( int i = 0; i < arr.length(); i++ ) {
                    lst.add(loadInnerType(arr,i,conf));
                }
                //if ( isValid(lst) ) {
                    field.set(lst);
                    field.setConfigured(true);
                //} else field.set(defaultValue);
            }
        }
    }

    @Override
    public boolean isPropertiesContainer() {
        return true;
    }
}
