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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Malte Schulze
 */
public class PropertyConfigurationListPrimitive<K> extends PropertyConfigurationList<K> {
    protected Class innerType;
    protected boolean addingAllowed, removalAllowed, innerNullable;
    protected Callable createNewEntry;
    
    public PropertyConfigurationListPrimitive(String id, String label, String tooltip, List defaultValue, int order, PropertyValueGetter<K, List> getter, PropertyValueSetter<K, List> setter, boolean nullable, Class innerType, boolean addingAllowed, boolean removalAllowed, boolean innerNullable, Callable createNewEntry) {
        super(id, label, tooltip, defaultValue, order, getter, setter, nullable);
        this.addingAllowed = addingAllowed;
        this.removalAllowed = removalAllowed;
        this.innerNullable = innerNullable;
        this.createNewEntry = createNewEntry;
        this.innerType = innerType;
    }
    
    public PropertyConfigurationListPrimitive<K> setInnerType(Class innerType) {
        this.innerType = innerType;
        return this;
    }

    public Class getInnerType() {
        return innerType;
    }

    public boolean isAddingAllowed() {
        return addingAllowed;
    }

    public boolean isRemovalAllowed() {
        return removalAllowed;
    }

    public boolean isInnerNullable() {
        return innerNullable;
    }
    
    public Object createNewEntry() {
        if ( createNewEntry != null ) {
            try {
                return createNewEntry.call();
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }
    
    @Override
    public void saveTo(JSONObject json, List value) throws JSONException {
        if ( value != null ) {
            JSONArray arr = new JSONArray();
            for ( Object entry : value ) {
                arr.put(entry);
            }
            json.put(id, arr);
        } else json.put(id, JSONObject.NULL);
    }

    @Override
    public void loadFrom(JSONObject json, PropertyField<List> field) throws JSONException {
        if ( !json.has(id) ) {
            field.setConfigured(false);
        } else {
            if ( json.isNull(id) ) {
                //if ( isValid(null) ) {
                    field.set(null);
                    field.setConfigured(true);
                //} else field.set(defaultValue);
            } else {
                JSONArray arr = json.getJSONArray(id);
                
                List lst = new ArrayList<>();
                for ( int i = 0; i < arr.length(); i++ ) {
                    lst.add(loadInnerType(arr,i,innerType));
                }
                //if ( isValid(lst) ) {
                    field.set(lst);
                    field.setConfigured(true);
                //} else field.set(defaultValue);
            }
        }
    }
}
