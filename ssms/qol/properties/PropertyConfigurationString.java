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

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Malte Schulze
 * @param <K>
 */
public class PropertyConfigurationString<K> extends PropertyConfigurationBase<K, String> {
    public PropertyConfigurationString(String id, String label, String tooltip, String defaultValue, int order, PropertyValueGetter<K, String> getter, PropertyValueSetter<K, String> setter, boolean nullable) {
        super(id, label, tooltip, defaultValue, order, getter, setter, nullable);
    }
    
    @Override
    public Class<String> getValueClass() {
        return String.class;
    }

    @Override
    public PropertyField<String> createField(K sourceObject) {
        String value = getter != null && sourceObject != null ? getter.get(sourceObject) : null;
        if ( value == null ) value = defaultValue;
        return new PropertyFieldString(value, false);
    }

    @Override
    public String deepCopyValue(String obj) {
        return obj;
    }
    
    @Override
    public void loadFrom(JSONObject json, PropertyField<String> field) throws JSONException {
        if ( !json.has(id) ) {
            field.setConfigured(false);
        } else {
            setFrom(json.isNull(id) ? null : json.getString(id),field);
        }
    }
}
