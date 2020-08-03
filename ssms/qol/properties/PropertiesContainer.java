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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Malte Schulze
 * @param <K>
 */
public class PropertiesContainer<K> {
    protected boolean dirty, active;
    protected Map<String,PropertyField> fields;
    protected PropertiesContainerConfiguration<K> conf;
    protected String id;
    protected PropertiesContainer parentObject;
    protected boolean usesConfigurationState = false;

    public PropertiesContainer(K sourceObject, PropertiesContainerConfiguration<K> conf) {
        //TODO Keeping the instance of the used sourceObject for a propertiesContainer can be done in the game scope(since it is never persisted). 
        //That allows to ease up the initialization of uninitialized sourceObject via a default value.
        this.conf = conf;
        fields = new HashMap<>();
        for ( PropertyConfiguration<K,?> property : conf.getProperties() ) {
            fields.put(property.getId(), property.createField(sourceObject));
            attachFieldIfPossible(property.getId());
        }
        conf.get(this, sourceObject);
        id = conf.getIdFromSourceObject() != null ? conf.getIdFromSourceObject().get(sourceObject) : null;
        if ( conf.getUsesConfigurationState() )
            initFieldConfigurationStatusHierachy(false);
        dirty = false;
        active = true;
    }
    
    public PropertiesContainer(JSONObject json, PropertiesContainerConfiguration<K> conf) throws JSONException {
        this.conf = conf;
        fields = new HashMap<>();
        for ( PropertyConfiguration<K,?> property : conf.getProperties() ) {
            fields.put(property.getId(), property.createField(null));
            attachFieldIfPossible(property.getId());
        }
        loadFrom(json);
        for ( PropertyConfiguration<K,?> property : conf.getProperties() ) {
            attachFieldIfPossible(property.getId());
        }
        dirty = false;
        active = true;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return conf.getLabelFromContainer().get(this);
    }

    public PropertiesContainerConfiguration<K> getConf() {
        return conf;
    }
    
    public boolean isDirty() { return dirty; }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    public PropertyField getField(String fieldId) {
        return fields.get(fieldId);
    }
    
    public <T> T getFieldValue(String fieldId, Class<T> clz) {
        PropertyField<T> field = fields.get(fieldId);
        if ( field != null ) {
            if ( field.getFieldClass() == clz ) {
                return field.get();
            }
        }
        return null;
    }
    
    public <T> void setFieldValue(String fieldId, T value) {
        setFieldValue(fieldId,value,false);
    }
    
    public <T> void setFieldValue(String fieldId, T value, boolean useDefault) {
        PropertyField<T> field = fields.get(fieldId);
        if ( field != null ) {
            if ( useDefault && value == null ) value = conf.<T>getProperty(fieldId).getDefaultValue();
            field.set(value);
            markFieldAsConfigured(fieldId);
            attachFieldIfPossible(fieldId);
            markAsChanged();
        }
    }
    
    public void markContainerFieldAsChanged(String fieldId) {
        markFieldAsConfigured(fieldId);
        attachFieldIfPossible(fieldId);
        markAsChanged();
    }
    
    public void attach(PropertiesContainer parentObject) { 
        this.parentObject = parentObject;
    }
    
    protected void attachFieldIfPossible(String fieldId) {
        PropertyField field = fields.get(fieldId);
        if ( field != null ) {
            PropertyConfiguration fieldSettings = conf.getProperty(fieldId);
            if ( fieldSettings.isPropertiesContainer() ) {
                Object value = field.get();
                if ( value != null ) {
                    if ( !fieldSettings.isList() ) {
                        ((PropertiesContainer)value).attach(this);
                    } else {
                        for ( PropertiesContainer container : (List<PropertiesContainer>)value ) {
                            container.attach(this);
                        }
                    }
                }
            }
        }
    }
    
    public PropertiesContainer getParentObject() { 
        return parentObject; 
    }
    
    public void markAsChanged() {
        if ( parentObject == null ) {
            active = true;
            dirty = true;
        } else {
            parentObject.markAsChanged();
        }
    }
    
    public void markFieldAsConfigured(String fieldId) {
        markFieldAsConfigured(fieldId,true,true);
    }
    
    public void initFieldConfigurationStatus(boolean presetState) {
        initFieldConfigurationStatus(presetState, true);
    }
    
    public void initFieldConfigurationStatus(boolean presetState, boolean bubble) {
        usesConfigurationState = true;
        for ( PropertyConfiguration property : (List<PropertyConfiguration<K,?>>)conf.getProperties() ) {
            if ( "Version".equals(property.getId()) ) {
                markFieldAsConfigured(property.getId(), false, false);
            } else {
                if ( presetState )
                    markFieldAsConfigured(property.getId(), bubble, false);
                else 
                    markFieldAsNotConfigured(property.getId(), false);
            }
        }
    }

    public void initFieldConfigurationStatusHierachy(boolean presetState) {
        //We scan for strong linked setting instances and recur the call. In this instance we initialize the configuration status if it hasn't been set yet.
        for ( PropertyConfiguration property : (List<PropertyConfiguration<K,?>>)conf.getProperties() ) {
            if ( property.isPropertiesContainer() ) {
                if ( property.isList() ) {
                    //Check if the list can add or remove entries, if it isn't static in size and position it isn't strong linked and can not use configuration status.
                    if ( false ) {
                        List child = (List)getField(property.getId()).get();
                        if ( child != null ) {
                            for ( PropertiesContainer container : (List<PropertiesContainer>)child ) {
                                container.initFieldConfigurationStatusHierachy(presetState);
                                break;
                            }
                        }
                    }
                } else {
                    PropertiesContainer child = getFieldValue(property.getId(),PropertiesContainer.class);
                    if ( child != null ) child.initFieldConfigurationStatusHierachy(presetState);
                }
            }
        }

        initFieldConfigurationStatus(presetState, false);
    }
    
    public void markFieldAsConfigured(String fieldId, boolean bubble, boolean triggerMarkAsChanged) {
        if ( getUsesConfigurationState() ) {
            fields.get(fieldId).setConfigured(true);
        }

        //This has to bubble up if possible so that parent settings are also marked as configured
        if ( bubble && parentObject != null ) {
            for ( PropertyConfiguration property : (List<PropertyConfiguration>)parentObject.conf.getProperties() ) {
                if ( property.isPropertiesContainer() ) {
                    if ( property.isList() ) {
                        List child = (List)parentObject.getField(property.getId()).get();
                        if ( child != null ) {
                            for ( PropertiesContainer container : (List<PropertiesContainer>)child ) {
                                if ( container == this ) {
                                    parentObject.markFieldAsConfigured(property.getId());
                                    break;
                                }
                            }
                        }
                    } else {
                        PropertiesContainer container = (PropertiesContainer)parentObject.getField(property.getId()).get();
                        if ( container == this ) {
                            parentObject.markFieldAsConfigured(property.getId());
                        }
                    }
                }
            }
        }

        if ( triggerMarkAsChanged ) markAsChanged();
    }
    
    public void markFieldAsNotConfigured(String fieldId) {
        markFieldAsNotConfigured(fieldId, true);
    }
    
    public void markFieldAsNotConfigured(String fieldId, boolean triggerMarkAsChanged) {
        if ( !getUsesConfigurationState() ) return;
        PropertyField pf = getField(fieldId);
        if ( !pf.isConfigured() ) return;
        if ( "Version".equals(fieldId) ) return;
        getField(fieldId).setConfigured(false);

        if ( triggerMarkAsChanged ) markAsChanged();
    }
    
    public boolean getUsesConfigurationState() {
        return usesConfigurationState;
    }
    
    public boolean isValid() {
        return conf.isValid(this);
    }
    
    public boolean set() {
        return set(conf.getSourceObjectFromId().get(id));
    }
    
    public boolean set(K sourceObject) {
        if ( sourceObject != null ) return conf.set(sourceObject, this);
        return false;
    }
    
    public boolean copyTo(PropertiesContainer pc) {
        return copyTo(pc, false);
    }
    
    public boolean copyTo(PropertiesContainer pc, boolean onlyNonConfiguredFields) {
        if ( pc != this && ( (id == null && pc.getId() == null) || (id != null && id.equals(pc.getId())) ) ) {
            for ( Map.Entry<String,PropertyField> entry : fields.entrySet() ) {
                if ( onlyNonConfiguredFields && (!pc.getUsesConfigurationState() || pc.getField(entry.getKey()).isConfigured()) ) continue;
                pc.setFieldValue(entry.getKey(), conf.getProperty(entry.getKey()).deepCopyValue(entry.getValue().get()), false);
                if ( onlyNonConfiguredFields ) pc.markFieldAsNotConfigured(entry.getKey(), false);
            }
            pc.dirty = dirty;
            pc.active = active;
            return true;
        }
        return false;
    }
    
    public void saveTo(JSONObject json) throws JSONException {
        if ( id == null ) json.put("id", JSONObject.NULL);
        else json.put("id", id);
        json.put("usesConfigurationState", usesConfigurationState);
        boolean useConfigured = usesConfigurationState;
        for ( Map.Entry<String,PropertyField> entry : fields.entrySet() ) {
            if ( !useConfigured || entry.getValue().isConfigured() ) conf.getProperty(entry.getKey()).saveTo(json,entry.getValue().get());
        }
    }
    
    public void loadFrom(JSONObject json) throws JSONException {
        id = json.isNull("id") ? null : json.getString("id");
        usesConfigurationState = json.getBoolean("usesConfigurationState");
        for ( Map.Entry<String,PropertyField> entry : fields.entrySet() ) {
            conf.getProperty(entry.getKey()).loadFrom(json,entry.getValue());
        }
    }
}
