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

/**
 *
 * @author Malte Schulze
 * @param <T>
 * @param <K>
 */
public class UIComponentParentFactory<T extends UIComponent_Parent, K extends UIComponentParentFactory<T,K>> extends UIComponentFactory<T,K> {
    static public <T extends UIComponent_Parent, K extends UIComponentParentFactory<T,K>> UIComponentParentFactory<T,K> getFactory(T comp) {
        return new UIComponentParentFactory<>(comp);
    }
    
    protected UIComponentParentFactory(T comp) {
        super(comp);
    }
    public K addChild(UIComponent child) {
        comp.addChild(child);
        return generalizedFactory;
    }
}
