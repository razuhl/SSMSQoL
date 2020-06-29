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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import ssms.qol.ModPlugin;

/**
 *
 * @author Malte Schulze
 * @param <K>
 */
public class PropertiesContainerConfiguration<K> {
    static public enum CorePropertiesDialogs {
        Application("Application"),Game("Game");
        private final String id;
        private CorePropertiesDialogs(String id) {this.id = id;}
        public String getId() {return id;}
    }
    static public interface PostSetter {
        void merge(List<PropertiesContainer> loadedSettings);
    }
    protected String configurationId, label;
    protected Map<String,PropertyConfiguration<K,?>> propertiesLookup;
    protected ArrayList<PropertyConfiguration<K,?>> properties;
    protected ArrayList<PropertiesContainerMerger<K>> setters, getters;
    protected ArrayList<PropertiesContainerValidator<K>> validators;
    protected Class<K> sourceObjectClass;
    protected Collection<K> allSourceObjects;
    protected int order;
    protected PropertyValueGetter<K,String> idFromSourceObject, labelFromSourceObject;
    protected PropertyValueGetter<PropertiesContainer<K>, String> labelFromContainer;
    protected PropertyValueGetter<String,K> sourceObjectFromId;
    protected boolean usesConfigurationState, applicationScoped;
    protected String displayInDialogId;
    protected Map<String,PostSetter> postSetters;

    public void addPostSetter(String id, PostSetter postSetter) {
        if ( postSetters == null ) postSetters = new HashMap<>();
        postSetters.put(id,postSetter);
    }
    
    public PostSetter removePostSetter(String id) {
        if ( postSetters != null ) return postSetters.remove(id);
        else return null;
    }
    
    public void setUsesConfigurationState(boolean usesConfigurationState) {
        this.usesConfigurationState = usesConfigurationState;
    }

    public boolean getUsesConfigurationState() {
        return usesConfigurationState;
    }
    
    public PropertiesContainerConfiguration(Class<K> sourceObjectClass, String configurationId) {
        this.sourceObjectClass = sourceObjectClass;
        this.configurationId = configurationId;
    }
    
    public void configureApplicationScopedSingleInstance(String label, final K singleton, boolean usesConfigurationState) {
        setApplicationScoped(true);
        setLabel(label);
        setSingularSourceObject(singleton);
        setSourceObjectFromId(new PropertyValueGetter<String, K>() {
            @Override
            public K get(String sourceObject) {
                return singleton;
            }
        });
        setUsesConfigurationState(usesConfigurationState);
        setDisplayInDialogId(PropertiesContainerConfiguration.CorePropertiesDialogs.Application.getId());
    }
    
    public void configureApplicationScopedMultipleInstances(String label, Collection<K> allSourceObjects, boolean usesConfigurationState, PropertyValueGetter<K, String> idFromSource, PropertyValueGetter<String, K> sourceFromId, 
            PropertyValueGetter<K, String> labelFromSource, PropertyValueGetter<PropertiesContainer<K>, String> labelFromContainer) {
        setApplicationScoped(true);
        setLabel(label);
        setAllSourceObjects(allSourceObjects);
        setIdFromSourceObject(idFromSource);
        setSourceObjectFromId(sourceFromId);
        setLabelFromSourceObject(labelFromSource);
        setLabelFromContainer(labelFromContainer);
        setDisplayInDialogId(PropertiesContainerConfiguration.CorePropertiesDialogs.Application.getId());
    }
    
    public void configureMinorApplicationScoped(PropertyValueGetter<PropertiesContainer<K>, String> labelFromContainer) {
        setApplicationScoped(true);
        setLabelFromContainer(labelFromContainer);
    }
    
    public void configureMinorApplicationScopedMultipleInstances(PropertyValueGetter<K, String> idFromSource, PropertyValueGetter<String, K> sourceFromId, 
            PropertyValueGetter<K, String> labelFromSource, PropertyValueGetter<PropertiesContainer<K>, String> labelFromContainer) {
        setApplicationScoped(true);
        setIdFromSourceObject(idFromSource);
        setSourceObjectFromId(sourceFromId);
        setLabelFromSourceObject(labelFromSource);
        setLabelFromContainer(labelFromContainer);
    }
    
    public void configureGameScopedSingleInstance(String label, final K singleton) {
        setApplicationScoped(false);
        setLabel(label);
        setSingularSourceObject(singleton);
        setSourceObjectFromId(new PropertyValueGetter<String, K>() {
            @Override
            public K get(String sourceObject) {
                return singleton;
            }
        });
        setDisplayInDialogId(PropertiesContainerConfiguration.CorePropertiesDialogs.Game.getId());
    }
    
    public void configureGameScopedMultipleInstances(String label, Collection<K> allSourceObjects, PropertyValueGetter<K, String> idFromSource, PropertyValueGetter<String, K> sourceFromId, 
            PropertyValueGetter<K, String> labelFromSource, PropertyValueGetter<PropertiesContainer<K>, String> labelFromContainer) {
        setApplicationScoped(false);
        setLabel(label);
        setAllSourceObjects(allSourceObjects);
        setIdFromSourceObject(idFromSource);
        setSourceObjectFromId(sourceFromId);
        setLabelFromSourceObject(labelFromSource);
        setLabelFromContainer(labelFromContainer);
        setDisplayInDialogId(PropertiesContainerConfiguration.CorePropertiesDialogs.Game.getId());
    }
    
    public void configureMinorGameScoped(PropertyValueGetter<PropertiesContainer<K>, String> labelFromContainer) {
        setApplicationScoped(false);
        setLabelFromContainer(labelFromContainer);
    }
    
    public void configureMinorGameScopedMultipleInstances(PropertyValueGetter<K, String> idFromSource, PropertyValueGetter<String, K> sourceFromId, 
            PropertyValueGetter<K, String> labelFromSource, PropertyValueGetter<PropertiesContainer<K>, String> labelFromContainer) {
        setApplicationScoped(false);
        setIdFromSourceObject(idFromSource);
        setSourceObjectFromId(sourceFromId);
        setLabelFromSourceObject(labelFromSource);
        setLabelFromContainer(labelFromContainer);
    }

    public boolean displayInDialogId(String dialogId) {
        return dialogId != null && dialogId.equals(displayInDialogId);
    }
    public void setDisplayInDialogId(String displayInDialogId) {
        this.displayInDialogId = displayInDialogId;
    }
    public String getDisplayInDialogId() {
        return displayInDialogId;
    }
    public void setApplicationScoped(boolean applicationScoped) {
        this.applicationScoped = applicationScoped;
    }
    public boolean isApplicationScoped() {
        return applicationScoped;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public String getConfigurationId() {
        return configurationId;
    }
    public int getOrder() {
        return order;
    }
    public void setOrder(int order) {
        this.order = order;
    }
    public void configureSourceObjects(Collection<K> allSourceObjects, 
            PropertyValueGetter<K, String> idFromSourceObject, PropertyValueGetter<K, String> labelFromSourceObject, 
            PropertyValueGetter<String, K> sourceObjectFromId) {
        setAllSourceObjects(allSourceObjects);
        setIdFromSourceObject(idFromSourceObject);
        setLabelFromSourceObject(labelFromSourceObject);
        setSourceObjectFromId(sourceObjectFromId);
    }
    public void configureSourceObjects(K singularSourceObject) {
        setSingularSourceObject(singularSourceObject);
        setSourceObjectFromId(new PropertyValueGetter<String, K>() {
            @Override
            public K get(String sourceObject) {
                return allSourceObjects.iterator().next();
            }
        });
    }
    public PropertyValueGetter<K, String> getIdFromSourceObject() {
        return idFromSourceObject;
    }
    public void setIdFromSourceObject(PropertyValueGetter<K, String> idFromSourceObject) {
        this.idFromSourceObject = idFromSourceObject;
    }
    public PropertyValueGetter<K, String> getLabelFromSourceObject() {
        return labelFromSourceObject;
    }
    public void setLabelFromSourceObject(PropertyValueGetter<K, String> labelFromSourceObject) {
        this.labelFromSourceObject = labelFromSourceObject;
    }
    public void setLabelFromContainer(PropertyValueGetter<PropertiesContainer<K>, String> labelFromContainer) {
        this.labelFromContainer = labelFromContainer;
    }
    public PropertyValueGetter<PropertiesContainer<K>, String> getLabelFromContainer() {
        return labelFromContainer;
    }
    public PropertyValueGetter<String, K> getSourceObjectFromId() {
        return sourceObjectFromId;
    }
    public void setSourceObjectFromId(PropertyValueGetter<String, K> sourceObjectFromId) {
        this.sourceObjectFromId = sourceObjectFromId;
    }
    public Class<K> getSourceObjectClass() {
        return sourceObjectClass;
    }
    public Collection<K> getAllSourceObjects() {
        return allSourceObjects;
    }
    public void setAllSourceObjects(Collection<K> allSourceObjects) {
        this.allSourceObjects = allSourceObjects;
    }
    public void setSingularSourceObject(K singularSourceObject) {
        if ( this.allSourceObjects != null ) this.allSourceObjects.clear();
        this.allSourceObjects = new ArrayList<>(1);
        this.allSourceObjects.add(singularSourceObject);
    }
    public <T> PropertyConfiguration<K,T> getProperty(String propertyId) {
        if ( propertiesLookup != null ) return (PropertyConfiguration<K, T>) propertiesLookup.get(propertyId);
        else return null;
    }
    public List<PropertyConfiguration<K,?>> getProperties() {
        if ( properties == null ) properties = new ArrayList<>(0);
        return Collections.unmodifiableList(properties);
    }
    public <T> boolean addProperty(PropertyConfiguration<K,T> propConf) {
        if ( properties == null ) properties = new ArrayList<>(1);
        if ( propertiesLookup != null && propertiesLookup.containsKey(propConf.getId()) ) {
            Logger logger = Global.getLogger(ModPlugin.class);
            logger.log(Level.ERROR, "Attempt to register multiple properties under the same id! "+getConfigurationId()+"."+propConf.getId());
            return false;
        }
        properties.add(propConf);
        Collections.sort(properties);
        properties.trimToSize();
        if ( propertiesLookup == null ) propertiesLookup = new HashMap<>();
        propertiesLookup.put(propConf.getId(), propConf);
        return true;
    }
    public void addSetter(PropertiesContainerMerger<K> merger) {
        if ( setters == null ) setters = new ArrayList<>(1);
        setters.add(merger);
        setters.trimToSize();
    }
    public boolean set(K sourceObject, PropertiesContainer<K> container) {
        if ( sourceObject == null || !container.isValid() ) return false;
        if ( properties != null ) {
            for ( PropertyConfiguration<K,?> property : properties ) {
                PropertyValueSetter<K,Object> setter = (PropertyValueSetter<K,Object>)property.getSetter();
                if ( setter != null ) setter.set(sourceObject, container.getField(property.getId()).get());
            }
        }
        if ( setters != null && !setters.isEmpty() ) {
            for ( PropertiesContainerMerger<K> merger : setters ) {
                if ( !merger.merge(container, sourceObject) ) return false;
            }
        }
        return true;
    }
    public void addGetter(PropertiesContainerMerger<K> merger) {
        if ( getters == null ) getters = new ArrayList<>(1);
        getters.add(merger);
        getters.trimToSize();
    }
    public boolean get(PropertiesContainer<K> container, K sourceObject) {
        if ( getters != null && !getters.isEmpty() ) {
            for ( PropertiesContainerMerger<K> merger : getters ) {
                if ( !merger.merge(container, sourceObject) ) return false;
            }
        }
        return true;
    }
    public void addValidator(PropertiesContainerValidator<K> validator) {
        if ( validators == null ) validators = new ArrayList<>(1);
        validators.add(validator);
        validators.trimToSize();
    }
    public boolean isValid(PropertiesContainer<K> container) {
        if ( properties != null ) {
            for ( PropertyConfiguration property : properties ) {
                if ( !property.isValid(container.getField(property.getId()).get()) ) return false;
            }
        }
        if ( validators != null && !validators.isEmpty() ) {
            for ( PropertiesContainerValidator<K> validator : validators ) {
                if ( !validator.isValid(container) ) return false;
            }
        }
        return true;
    }
    public PropertiesContainer<K> createContainer(K sourceObject) {
        return new PropertiesContainer<>(sourceObject,this);
    }
    public PropertiesContainer<K> createContainer(JSONObject json) {
        try {
            return new PropertiesContainer<>(json,this);
        } catch (JSONException ex) {
            Logger logger = Global.getLogger(ModPlugin.class);
            logger.log(Level.WARN, "Failed to create property container from json. "+json,ex);
        }
        return null;
    }
    public void postSetters(List<PropertiesContainer> loadedSettings) {
        if ( postSetters != null ) {
            for ( PostSetter ps : postSetters.values() )
                ps.merge(loadedSettings);
        }
    }
}
