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
 * @param <T>
 */
public abstract class PropertyFieldBase<T> implements PropertyField<T> {
    protected T value;
    protected boolean configured;
    
    public PropertyFieldBase(T value, boolean configured) {
        this.value = value;
        this.configured = configured;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
        configured = true;
    }

    @Override
    public boolean isConfigured() {
        return configured;
    }

    @Override
    public void setConfigured(boolean configured) {
        this.configured = configured;
    }
}
