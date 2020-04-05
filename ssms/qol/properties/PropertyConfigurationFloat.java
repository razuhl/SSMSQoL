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
public class PropertyConfigurationFloat<K> extends PropertyConfigurationBase<K, Float> {
    protected Float min, max;
    
    public PropertyConfigurationFloat(String id, String label, String tooltip, Float defaultValue, int order, PropertyValueGetter<K, Float> getter, PropertyValueSetter<K, Float> setter, boolean allowingNull, Float min, Float max) {
        super(id, label, tooltip, defaultValue, order, getter, setter, allowingNull);
        this.min = min;
        this.max = max;
    }

    public Float getMin() {
        return min;
    }

    public Float getMax() {
        return max;
    }

    @Override
    public Class<Float> getValueClass() {
        return Float.class;
    }

    @Override
    public PropertyField<Float> createField(K sourceObject) {
        Float value = sourceObject != null ? getter.get(sourceObject) : null;
        if ( value == null ) value = defaultValue;
        return new PropertyFieldFloat(value, false);
    }

    @Override
    public Float deepCopyValue(Float obj) {
        return obj;
    }

    @Override
    public boolean isValid(Float value) {
        if ( value == null ) return true;
        return ( min == null || min.compareTo(value) <= 0 ) && ( max == null || max.compareTo(value) >= 0 );
    }
    
    @Override
    public void loadFrom(JSONObject json, PropertyField<Float> field) throws JSONException {
        if ( !json.has(id) ) {
            field.setConfigured(false);
        } else {
            setFrom(json.isNull(id) ? null : Double.valueOf(json.getDouble(id)).floatValue(),field);
        }
    }
}
