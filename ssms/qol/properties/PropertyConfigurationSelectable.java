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
import java.util.List;
import org.apache.log4j.Level;
import org.json.JSONException;
import org.json.JSONObject;
import ssms.qol.ModPlugin;

/**
 *
 * @author Malte Schulze
 */
public abstract class PropertyConfigurationSelectable<K,T> extends PropertyConfigurationBase<K,T> {
    protected Class<T> valueClass;

    public PropertyConfigurationSelectable(String id, String label, String tooltip, T defaultValue, int order, Class<T> valueClass, PropertyValueGetter<K, T> getter, PropertyValueSetter<K, T> setter, boolean nullable) {
        super(id, label, tooltip, defaultValue, order, getter, setter, nullable);
        this.valueClass = valueClass;
    }

    @Override
    public Class getValueClass() {
        return valueClass;
    }

    @Override
    public PropertyField<T> createField(K sourceObject) {
        T value = sourceObject != null ? getter.get(sourceObject) : null;
        if ( value == null ) value = defaultValue;
        if ( Float.class == valueClass ) {
            return (PropertyField<T>) new PropertyFieldFloat((Float)value, false);
        } else if ( String.class == valueClass ) {
            return (PropertyField<T>) new PropertyFieldString((String)value, false);
        } else if ( Boolean.class == valueClass ) {
            return (PropertyField<T>) new PropertyFieldBoolean((Boolean)value, false);
        } else if ( Integer.class == valueClass ) {
            return (PropertyField<T>) new PropertyFieldInteger((Integer)value, false);
        } else {
            Global.getLogger(ModPlugin.class).log(Level.ERROR, "Unsupported value class "+valueClass+" for select property!");
            return null;
        }
    }

    @Override
    public T deepCopyValue(T obj) {
        return obj;
    }
    
    public abstract List<T> buildOptions();
    public abstract String getOptionLabel(T o);
    
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
    public void loadFrom(JSONObject json, PropertyField<T> field) throws JSONException {
        if ( !json.has(id) ) {
            field.setConfigured(false);
        } else {
            //based on value class different load methods must be used
            if ( json.isNull(id) ) {
                setFrom(null,field);
            } else if ( Float.class == valueClass ) {
                setFrom((T)Float.valueOf(Double.valueOf(json.getDouble(id)).floatValue()),field);
            } else if ( String.class == valueClass ) {
                setFrom((T)json.getString(id),field);
            } else if ( Boolean.class == valueClass ) {
                setFrom((T)Boolean.valueOf(json.getBoolean(id)),field);
            } else if ( Integer.class == valueClass ) {
                setFrom((T)Integer.valueOf(json.getInt(id)),field);
            } else {
                Global.getLogger(ModPlugin.class).log(Level.ERROR, "Unsupported value class "+valueClass+" for select property!");
            }
        }
    }
}
