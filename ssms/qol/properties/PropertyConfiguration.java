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
public interface PropertyConfiguration<K,T> extends Comparable<PropertyConfiguration> {
    public String getId();
    public String getLabel();
    public String getTooltip();
    public int getOrder();
    public T getDefaultValue();
    public PropertyValueGetter<K,T> getGetter();
    public PropertyValueSetter<K,T> getSetter();
    public Class<T> getValueClass();
    public PropertyField<T> createField(K sourceObject);
    public boolean isPropertiesContainer();
    public boolean isList();
    public boolean isSelectable();
    public boolean isHidden();
    public boolean isNullable();
    public boolean isValid(T value);
    public T deepCopyValue(T obj);
    public void saveTo(JSONObject json, T value) throws JSONException;
    public void loadFrom(JSONObject json, PropertyField<T> field) throws JSONException;
}
