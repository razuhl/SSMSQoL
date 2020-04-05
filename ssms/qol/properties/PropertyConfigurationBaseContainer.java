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

/**
 *
 * @author Malte Schulze
 * @param <K>
 * @param <T>
 */
public abstract class PropertyConfigurationBaseContainer<K, T> extends PropertyConfigurationBase<K, PropertiesContainer> {
    protected PropertiesContainerConfiguration<T> conf;
    protected T defaultValueUnwrapped;
    

    public PropertyConfigurationBaseContainer(String id, String label, String tooltip, T defaultValueUnwrapped, String containerConfigurationId, Class<T> containerClass, int order, final PropertyValueGetter<K, T> getter, final PropertyValueSetter<K, PropertiesContainer> setter, boolean nullable) {
        super(id, label, tooltip, null, order, null, null, nullable);
        this.conf = PropertiesContainerConfigurationFactory.getInstance().getPropertiesContainerConfiguration(containerConfigurationId, containerClass);
        this.defaultValueUnwrapped = defaultValueUnwrapped;
        this.getter = new PropertyValueGetter<K, PropertiesContainer>() {
            @Override
            public PropertiesContainer<T> get(K sourceObject) {
                T value = getter.get(sourceObject);
                return value != null ? conf.createContainer(value) : null;
            }
        };
        this.setter = new PropertyValueSetter<K, PropertiesContainer>() {
            @Override
            public void set(K sourceObject, PropertiesContainer value) {
                conf.set(getter.get(sourceObject), value);
                setter.set(sourceObject, value);
            }
        };
    }

    @Override
    public PropertiesContainer<T> getDefaultValue() {
        if ( defaultValueUnwrapped == null ) return null;
        return conf.createContainer(defaultValueUnwrapped);
    }

    @Override
    public boolean isPropertiesContainer() {
        return true;
    }
}
