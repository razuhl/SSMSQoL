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

/**
 *
 * @author Malte Schulze
 */
public class PropertyFieldList extends PropertyFieldBase<List> {
    public PropertyFieldList(List value, boolean configured) {
        super(value, configured);
    }
    
    @Override
    public Class<List> getFieldClass() {
        return List.class;
    }

    @Override
    public void set(List value) {
        //creating a copy to avoid unintentional changes when modifying the list later
        if ( value != null ) super.set(new ArrayList<>(value));
        else super.set(value);
    }
}
