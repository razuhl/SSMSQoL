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
public class PropertyConfigurationBoolean<K> extends PropertyConfigurationBase<K, Boolean> {
    public PropertyConfigurationBoolean(String id, String label, String tooltip, Boolean defaultValue, int order, PropertyValueGetter<K, Boolean> getter, PropertyValueSetter<K, Boolean> setter, boolean nullable) {
        super(id, label, tooltip, defaultValue, order, getter, setter, nullable);
    }
    
    @Override
    public Class<Boolean> getValueClass() {
        return Boolean.class;
    }

    @Override
    public PropertyField<Boolean> createField(K sourceObject) {
        Boolean value = getter != null && sourceObject != null ? getter.get(sourceObject) : null;
        if ( value == null ) value = defaultValue;
        return new PropertyFieldBoolean(value, false);
    }

    @Override
    public Boolean deepCopyValue(Boolean obj) {
        return obj;
    }
    
    @Override
    public void loadFrom(JSONObject json, PropertyField<Boolean> field) throws JSONException {
        if ( !json.has(id) ) {
            field.setConfigured(false);
        } else {
            setFrom(json.isNull(id) ? null : json.getBoolean(id),field);
        }
    }
}
