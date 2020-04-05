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
public class PropertyConfigurationInteger<K> extends PropertyConfigurationBase<K, Integer> {
    protected Integer min, max;

    public PropertyConfigurationInteger(String id, String label, String tooltip, Integer defaultValue, int order, PropertyValueGetter<K, Integer> getter, PropertyValueSetter<K, Integer> setter, boolean nullable, Integer min, Integer max) {
        super(id, label, tooltip, defaultValue, order, getter, setter, nullable);
        this.min = min;
        this.max = max;
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }

    @Override
    public Class<Integer> getValueClass() {
        return Integer.class;
    }

    @Override
    public PropertyField<Integer> createField(K sourceObject) {
        Integer value = sourceObject != null ? getter.get(sourceObject) : null;
        if ( value == null ) value = defaultValue;
        return new PropertyFieldInteger(value, false);
    }

    @Override
    public Integer deepCopyValue(Integer obj) {
        return obj;
    }

    @Override
    public boolean isValid(Integer value) {
        if ( value == null ) return true;
        return ( min == null || min.compareTo(value) <= 0 ) && ( max == null || max.compareTo(value) >= 0 );
    }
    
    @Override
    public void loadFrom(JSONObject json, PropertyField<Integer> field) throws JSONException {
        if ( !json.has(id) ) {
            field.setConfigured(false);
        } else {
            setFrom(json.isNull(id) ? null : json.getInt(id),field);
        }
    }
}
