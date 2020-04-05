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

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.util.Rectangle;

/**
 *
 * @author Malte Schulze
 */
public abstract class UIComponent_Base implements UIComponent {
    protected UIComponent_Parent parentComponent;
    protected Rectangle layout;
    protected SizeConstraint widthConstraint, heightConstraint;
    protected UIContext context;
    protected Map<String,Object> styles;

    public UIComponent_Base() {
        widthConstraint = new SizeConstraint(0, 0, 0);
        heightConstraint = new SizeConstraint(0, 0, 0);
    }
    
    @Override
    public UIComponent_Parent parentComponent() {
        return this.parentComponent;
    }

    @Override
    public void setParentComponent(UIComponent_Parent parent) {
        UIComponent_Parent oldParent = this.parentComponent;
        if ( oldParent == parent ) return;
        this.parentComponent = parent;
        if ( parent != null ) {
            parent.addChild(this);
            setContext(parent.getContext());
        } else {
            setContext(null);
        }
        if ( oldParent != null ) oldParent.removeChild(this);
    }

    @Override
    public UIContext getContext() {
        return context;
    }
    
    @Override
    public void setContext(UIContext context) {
        this.context = context;
    }

    @Override
    public boolean resize(Rectangle rect) {
        boolean accepted = true;
        int w = rect.getWidth(), h = rect.getHeight();
        if ( widthConstraint.min > w ) {
            rect.setWidth(widthConstraint.min);
            accepted = false;
        } else if ( widthConstraint.max < w ) {
            rect.setWidth(widthConstraint.max);
            accepted = false;
        }
        //if height gets cut we need to change y so that it anchors at the top of the rectangle
        if ( heightConstraint.min > h ) {
            rect.setY(rect.getY()-(heightConstraint.min-h));
            rect.setHeight(heightConstraint.min);
            accepted = false;
        } else if ( heightConstraint.max < h ) {
            rect.setY(rect.getY()+(h-heightConstraint.max));
            rect.setHeight(heightConstraint.max);
            accepted = false;
        }
        layout = new Rectangle(rect);
        return accepted;
    }

    @Override
    public Rectangle getLayout() {
        return layout;
    }

    @Override
    public void advance(float amount) {
        
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        
    }
    
    @Override
    public void dismis() {
        parentComponent = null;
        if ( styles != null ) {
            styles.clear();
            styles = null;
        }
        context = null;
    }

    @Override
    public void setWidth(int min, int pref, int max) {
        pref = Math.max(min, pref);
        max = Math.max(pref, max);
        widthConstraint = new SizeConstraint(min, pref, max);
        if ( parentComponent != null ) parentComponent.setDirty(true, true);
    }
    
    @Override
    public void setWidth(SizeConstraint con) {
        con.preferred = Math.max(con.min, con.preferred);
        con.max = Math.max(con.preferred, con.max);
        this.widthConstraint = con;
        if ( parentComponent != null ) parentComponent.setDirty(true, true);
    }

    @Override
    public SizeConstraint getWidth() {
        return widthConstraint;
    }
    
    @Override
    public void setWidthMin(int widthMin) {
        setWidth(widthMin, widthConstraint.preferred, widthConstraint.max);
    }
    
    @Override
    public void setWidthPref(int widthPref) {
        setWidth(widthConstraint.min, widthPref, widthConstraint.max);
    }
    
    @Override
    public void setWidthMax(int widthMax) {
        setWidth(widthConstraint.min, widthConstraint.preferred, widthMax);
    }

    @Override
    public void setHeight(int min, int pref, int max) {
        pref = Math.max(min, pref);
        max = Math.max(pref, max);
        heightConstraint = new SizeConstraint(min, pref, max);
        if ( parentComponent != null ) parentComponent.setDirty(true, true);
    }
    
    @Override
    public void setHeight(SizeConstraint con) {
        con.preferred = Math.max(con.min, con.preferred);
        con.max = Math.max(con.preferred, con.max);
        this.heightConstraint = con;
        if ( parentComponent != null ) parentComponent.setDirty(true, true);
    }
    
    @Override
    public SizeConstraint getHeight() {
        return heightConstraint;
    }
    
    @Override
    public void setHeightMin(int heightMin) {
        setHeight(heightMin, heightConstraint.preferred, heightConstraint.max);
    }
    
    @Override
    public void setHeightPref(int heightPref) {
        setHeight(heightConstraint.min, heightPref, heightConstraint.max);
    }
    
    @Override
    public void setHeightMax(int heightMax) {
        setHeight(heightConstraint.min, heightConstraint.preferred, heightMax);
    }
    
    protected int addInt(int a, int b) {
        long l = (long)a + (long)b;
        if ( l > Integer.MAX_VALUE ) return Integer.MAX_VALUE;
        else if ( l < Integer.MIN_VALUE ) return Integer.MIN_VALUE;
        else return (int)l;
    }

    @Override
    public void addStyle(String propertyId, Object value) {
        if ( styles == null ) styles = new HashMap<>();
        styles.put(propertyId,value);
    }

    @Override
    public void addStyle(UIContext.StyleProperty property, Object value) {
        addStyle(property.id, value);
    }

    @Override
    public void removeStyle(String propertyId) {
        styles.remove(propertyId);
    }

    @Override
    public void removeStyle(UIContext.StyleProperty property) {
        removeStyle(property.id);
    }

    public Map<String, Object> getStyles() {
        return styles;
    }
    
    public void pushStyles() {
        if ( styles != null && context != null ) {
            context.pushStyles(styles);
        }
    }
    
    public void popStyles() {
        if ( styles != null && context != null ) {
            context.popStyles(styles.keySet());
        }
    }
    
    public void dismissTooltip(StandardTooltipV2Expandable tooltip) {
        tooltip.clearChildren();
    }
    
    @Override
    public void pack() {
        
    }
}
