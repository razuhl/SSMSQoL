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
 * @param <K>
 */
public abstract class PropertyConfigurationList<K> extends PropertyConfigurationBase<K, List> {
    public PropertyConfigurationList(String id, String label, String tooltip, List defaultValue, int order, PropertyValueGetter<K, List> getter, PropertyValueSetter<K, List> setter, boolean nullable) {
        super(id, label, tooltip, defaultValue, order, getter, setter, nullable);
    }
    
    @Override
    public Class<List> getValueClass() {
        return List.class;
    }

    @Override
    public PropertyField<List> createField(K sourceObject) {
        List value = getter != null && sourceObject != null ? getter.get(sourceObject) : null;
        if ( value == null ) value = defaultValue;
        return new PropertyFieldList(value, false);
    }

    @Override
    public List deepCopyValue(List obj) {
        if ( obj == null ) return null;
        return new ArrayList<>(obj);
    }

    @Override
    public List getDefaultValue() {
        return defaultValue != null ? new ArrayList<>(defaultValue) : defaultValue;
    }
    @Override
    public boolean isList() {
        return true;
    }
    
    protected <T> T loadInnerType(JSONArray arr, int i, Class<T> innerType) throws JSONException {
        return (T) loadInnerType(arr, i, innerType, null);
    }
    
    protected PropertiesContainer loadInnerType(JSONArray arr, int i, PropertiesContainerConfiguration conf) throws JSONException {
        return (PropertiesContainer) loadInnerType(arr,i,PropertiesContainer.class,conf);
    }
    
    protected Object loadInnerType(JSONArray arr, int i, Class innerType, PropertiesContainerConfiguration conf) throws JSONException {
        if ( arr.isNull(i) ) {
            return null;
        }
        if ( innerType == null ) return arr.get(i);
        if ( Float.class.isAssignableFrom(innerType) ) {
            return Double.valueOf(arr.getDouble(i)).floatValue();
        } else if ( Integer.class.isAssignableFrom(innerType) ) {
            return arr.getInt(i);
        } else if ( String.class.isAssignableFrom(innerType) ) {
            return arr.getString(i);
        } else if ( Boolean.class.isAssignableFrom(innerType) ) {
            return arr.getBoolean(i);
        } else if ( PropertiesContainer.class.isAssignableFrom(innerType) ) { 
            JSONObject json = arr.getJSONObject(i);
            if ( json == null ) {
                return null;
            } else {
                return new PropertiesContainer(json, conf);
            }
        }
        return arr.get(i);
    }
}
