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
import ssms.qol.properties.PropertyConfigurationFloat;

/**
 *
 * @author Malte Schulze
 */
public abstract class UIValueHandler_PropertyTextField extends UIValueHandler<String> implements UIValueValidator<String> {
    protected PropertyConfiguration prop;
    
    public UIValueHandler_PropertyTextField(PropertyConfiguration prop) {
        super();
        this.prop = prop;
        setValidator(this);
    }
    
    @Override
    public boolean validate(UIContext context, String value) {
        if ( Float.class.isAssignableFrom(prop.getValueClass()) ) {
            if ( value == null ) return prop.isNullable();
            try {
                return prop.isValid(Float.parseFloat(value));
            } catch (NumberFormatException ex) {
                return false;
            }
        } else if ( Integer.class.isAssignableFrom(prop.getValueClass()) ) {
            if ( value == null ) return prop.isNullable();
            try {
                return prop.isValid(Integer.parseInt(value));
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        return prop.isValid(value);
    }
}
