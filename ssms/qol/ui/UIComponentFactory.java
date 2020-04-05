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
public class UIComponentFactory<T extends UIComponent, K extends UIComponentFactory<T,K>> {
    static public <T extends UIComponent, K extends UIComponentFactory<T,K>> UIComponentFactory<T,K> getFactory(T comp) {
        return new UIComponentFactory<>(comp);
    }
    protected T comp;
    protected K generalizedFactory;
    protected UIComponentFactory(T comp) {
        this.comp = comp;
        this.generalizedFactory = (K) this;
    }
    public K setWidth(int min, int preferred, int max) {
        comp.setWidth(min, preferred, max);
        return generalizedFactory;
    }
    public K setHeight(int min, int preferred, int max) {
        comp.setHeight(min, preferred, max);
        return generalizedFactory;
    }
    public K setContext(UIContext context) {
        comp.setContext(context);
        return generalizedFactory;
    }
    public K addStyle(UIContext.StyleProperty property, Object value) {
        comp.addStyle(property, value);
        return generalizedFactory;
    }
    public K addStyle(String propertyId, Object value) {
        comp.addStyle(propertyId, value);
        return generalizedFactory;
    }
    public T finish() {
        return comp;
    }
}
