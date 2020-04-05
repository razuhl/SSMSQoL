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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Malte Schulze
 */
public abstract class PropertyConfigurationListSelectable<K> extends PropertyConfigurationList<K> {
    protected Class innerType;
    public PropertyConfigurationListSelectable(String id, String label, String tooltip, List defaultValue, int order, PropertyValueGetter<K, List> getter, PropertyValueSetter<K, List> setter, boolean nullable, Class innerType) {
        super(id, label, tooltip, defaultValue, order, getter, setter, nullable);
        this.innerType = innerType;
    }
    
    public abstract List buildOptions();
    public abstract String getOptionLabel(Object o);
    public PropertyConfigurationListSelectable<K> setInnerType(Class innerType) {
        this.innerType = innerType;
        return this;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }
    
    public List getOptions() {
        //optimizing mass processing if a context is active
        PropertiesContext context = PropertiesContext.getInstance();
        List options;
        if ( context.isActive() ) {
            options = context.getProperty(this, List.class);
            if ( options == null ) {
                options = buildOptions();
                context.setProperty(this, options);
            }
        } else options = buildOptions();
        return options;
    }

    @Override
    public void saveTo(JSONObject json, List value) throws JSONException {
        if ( value != null ) {
            JSONArray arr = new JSONArray();
            for ( Object entry : value ) {
                arr.put(entry);
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
                List options = getOptions();
                
                List lst = new ArrayList<>();
                for ( int i = 0; i < arr.length(); i++ ) {
                    Object entry = loadInnerType(arr,i,innerType);
                    if ( options.contains(entry) ) lst.add(entry);
                }
                //if ( isValid(lst) ) {
                    field.set(lst);
                    field.setConfigured(true);
                //} else field.set(defaultValue);
            }
        }
    }
}
