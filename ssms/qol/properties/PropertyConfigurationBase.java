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
 * @param <T>
 */
public abstract class PropertyConfigurationBase<K,T> implements PropertyConfiguration<K, T> {
    protected String id, label, tooltip;
    protected T defaultValue;
    protected int order;
    protected PropertyValueGetter<K, T> getter;
    protected PropertyValueSetter<K, T> setter;
    protected boolean nullable;

    public PropertyConfigurationBase(String id, String label, String tooltip, T defaultValue, int order, PropertyValueGetter<K, T> getter, PropertyValueSetter<K, T> setter, boolean nullable) {
        this.id = id;
        this.label = label;
        this.tooltip = tooltip;
        this.defaultValue = defaultValue;
        this.order = order;
        this.getter = getter;
        this.setter = setter;
        this.nullable = nullable;
    }

    @Override
    public int compareTo(PropertyConfiguration o) {
        if ( o == null ) return -1;
        return Integer.compare(order, o.getOrder());
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getTooltip() {
        return tooltip;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public PropertyValueGetter<K, T> getGetter() {
        return getter;
    }
    
    @Override
    public PropertyValueSetter<K, T> getSetter() {
        return setter;
    }

    @Override
    public boolean isPropertiesContainer() {
        return false;
    }

    public boolean isNullable() {
        return nullable;
    }

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public boolean isHidden() {
        return false;
    }
    
    @Override
    public boolean isValid(T value) {
        return true;
    }
    
    @Override
    public void saveTo(JSONObject json, T value) throws JSONException {
        if ( value == null ) json.put(id, JSONObject.NULL);
        else json.put(id, value);
    }
    
    @Override
    public void loadFrom(JSONObject json, PropertyField<T> field) throws JSONException {
        if ( !json.has(id) ) {
            field.setConfigured(false);
        } else {
            Object value = json.get(id);
            if ( value == JSONObject.NULL ) {
                value = null;
            }
            setFrom((T)value,field);
        }
    }
    
    protected void setFrom(T value, PropertyField<T> field) {
        //if ( isValid((T)value) ) {
            field.set((T) value);
            field.setConfigured(true);
        /*} else {
            field.set(defaultValue);
        }*/
    }
}
