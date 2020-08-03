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
public class PropertyConfigurationContainer<K, T> extends PropertyConfigurationBaseContainer<K, T> {
    public PropertyConfigurationContainer(String id, String label, String tooltip, T defaultValueUnwrapped, String containerConfigurationId, Class<T> valueClass, int order, final PropertyValueGetter<K, T> getter, PropertyValueSetter<K, PropertiesContainer> setter, boolean nullable) {
        super(id, label, tooltip, defaultValueUnwrapped, containerConfigurationId, valueClass, order, getter, setter, nullable);
    }
    
    @Override
    public Class<PropertiesContainer> getValueClass() {
        return PropertiesContainer.class;
    }

    @Override
    public PropertyField<PropertiesContainer> createField(K sourceObject) {
        PropertiesContainer<T> value = getter != null && sourceObject != null ? getter.get(sourceObject) : null;
        if ( value == null ) value = getDefaultValue();
        return new PropertyFieldContainer(value, false);
    }

    @Override
    public boolean isValid(PropertiesContainer value) {
        return value == null || value.isValid();
    }

    @Override
    public PropertiesContainer deepCopyValue(PropertiesContainer obj) {
        return obj;
    }

    @Override
    public void saveTo(JSONObject json, PropertiesContainer value) throws JSONException {
        if ( value == null ) json.put(id, JSONObject.NULL);
        else {
            JSONObject jsonContainer = new JSONObject();
            json.put(id, jsonContainer);
            json.put(id+"#conf", value.conf.configurationId);
            value.saveTo(jsonContainer);
        }
    }
    
    @Override
    public void loadFrom(JSONObject json, PropertyField<PropertiesContainer> field) throws JSONException {
        if ( !json.has(id) ) {
            field.setConfigured(false);
        } else {
            Object value = json.isNull(id) ? null : json.getJSONObject(id);
            if ( json.isNull(id) ) {
                //if ( isValid(null) ) {
                    field.set(null);
                    field.setConfigured(true);
                //} else field.set(defaultValue);
            } else {
                PropertiesContainer pc = field.get();
                if ( pc != null ) {
                    pc.loadFrom((JSONObject)value);
                    //if ( isValid(pc) ) {
                        field.setConfigured(true);
                    //} else field.set(defaultValue);
                } else {
                    String confId = json.getString(id+"#conf");
                    pc = new PropertiesContainer((JSONObject)value, PropertiesContainerConfigurationFactory.getInstance().getPropertiesContainerConfiguration(confId));
                    //if ( isValid(pc) ) {
                        field.set(pc);
                        field.setConfigured(true);
                    //} else field.set(defaultValue);
                }
            }
        }
    }
}
