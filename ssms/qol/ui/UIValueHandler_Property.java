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
package ssms.qol.ui;

import ssms.qol.properties.PropertyConfiguration;

/**
 *
 * @author Malte Schulze
 */
public abstract class UIValueHandler_Property<T> extends UIValueHandler<T> implements UIValueValidator<T> {
    protected PropertyConfiguration prop;
    
    public UIValueHandler_Property(PropertyConfiguration prop) {
        super();
        this.prop = prop;
        setValidator(this);
    }
    
    @Override
    public boolean validate(UIContext context, T value) {
        return prop.isValid(value);
    }
}
