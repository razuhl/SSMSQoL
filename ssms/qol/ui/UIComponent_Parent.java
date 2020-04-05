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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.lwjgl.util.Rectangle;

/**
 *
 * @author Malte Schulze
 */
public abstract class UIComponent_Parent extends UIComponent_Base {
    protected ArrayList<UIComponent> childs;
    protected List<UIComponent> childsUnmodifiable;
    protected boolean layoutDirty, constraintsDirty;
    protected SizeConstraint manualHeightConstraint = new SizeConstraint(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE), manualWidthConstraint = new SizeConstraint(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    public UIComponent_Parent() {
        childs = new ArrayList<>(0);
        childsUnmodifiable = Collections.unmodifiableList(childs);
        layoutDirty = constraintsDirty = false;
    }
    
    public void addChild(UIComponent child) {
        if ( childs.contains(child) ) return;
        childs.add(child);
        childs.trimToSize();
        childsUnmodifiable = Collections.unmodifiableList(childs);
        layoutDirty = constraintsDirty = true;
        child.setParentComponent(this);
    }
    
    public boolean removeChild(UIComponent child) {
        boolean removed = childs.remove(child);
        if ( removed ) {
            childs.trimToSize();
            childsUnmodifiable = Collections.unmodifiableList(childs);
            layoutDirty = constraintsDirty = true;
            child.setParentComponent(null);
        }
        return removed;
    }
    
    public List<UIComponent> getChilds() {
        return childsUnmodifiable;
    }
    
    @Override
    public void setContext(UIContext context) {
        if ( this.context != context ) {
            this.context = context;
            for ( UIComponent c : getChilds() ) {
                c.setContext(context);
            }
        }
    }
    
    @Override
    public boolean resize(Rectangle rect) {
        updateConstraintsIfNecessary();
        boolean accepted = super.resize(rect);
        layoutDirty = true;
        return accepted;
    }
    
    @Override
    public void setWidth(int min, int pref, int max) {
        pref = Math.max(min, pref);
        max = Math.max(pref, max);
        manualWidthConstraint = new SizeConstraint(min, pref, max);
        if ( parentComponent != null ) parentComponent.setDirty(true, true);
    }
    
    @Override
    public void setWidth(SizeConstraint con) {
        con.preferred = Math.max(con.min, con.preferred);
        con.max = Math.max(con.preferred, con.max);
        this.manualWidthConstraint = con;
        if ( parentComponent != null ) parentComponent.setDirty(true, true);
    }
    
    @Override
    public void setHeight(int min, int pref, int max) {
        layoutDirty = constraintsDirty = true;
        pref = Math.max(min, pref);
        max = Math.max(pref, max);
        manualHeightConstraint = new SizeConstraint(min, pref, max);
        if ( parentComponent != null ) parentComponent.setDirty(true, true);
    }
    
    @Override
    public void setHeight(SizeConstraint con) {
        layoutDirty = constraintsDirty = true;
        con.preferred = Math.max(con.min, con.preferred);
        con.max = Math.max(con.preferred, con.max);
        this.manualHeightConstraint = con;
        if ( parentComponent != null ) parentComponent.setDirty(true, true);
    }
    
    public void setDirty(boolean constraints, boolean layout) {
        if ( !constraintsDirty && constraints ) constraintsDirty = true;
        if ( !layoutDirty && layout ) layoutDirty = true;
        if ( parentComponent != null ) parentComponent.setDirty(constraints, layout);
    }
    
    protected void updateConstraintsIfNecessary() {
        if ( constraintsDirty ) {
            calculateConstraints();
            constraintsDirty = false;
        }
    }
    
    protected void updateChildLayoutIfNecessary() {
        if ( layoutDirty ) {
            layoutChildren();
            layoutDirty = false;
        }
    }

    public boolean isLayoutDirty() {
        return layoutDirty;
    }

    public boolean isConstraintsDirty() {
        return constraintsDirty;
    }
    
    public abstract void layoutChildren();
    public abstract void calculateConstraints();
    public void cacheStyles() {};
    public void renderPreChilds() {};
    public void renderPostChilds() {};

    @Override
    public void render() {
        pushStyles();
        cacheStyles();
        updateConstraintsIfNecessary();
        updateChildLayoutIfNecessary();
        renderPreChilds();
        for ( UIComponent child : childs ) {
            child.render();
        }
        renderPostChilds();
        popStyles();
    }
    
    @Override
    public void pack() {
        pushStyles();
        cacheStyles();
        setDirty(true,true);
        for ( UIComponent child : childs ) {
            child.pack();
        }
        popStyles();
    }

    @Override
    public void advance(float amount) {
        for ( UIComponent child : childs ) {
            child.advance(amount);
        }
        super.advance(amount);
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        for ( UIComponent child : childs ) {
            child.processInput(events);
        }
        super.processInput(events);
    }

    @Override
    public void dismis() {
        for ( UIComponent child : childs ) {
            child.dismis();
        }
        super.dismis();
        childs.clear();
        childs = null;
    }

    @Override
    public SizeConstraint getWidth() {
        updateConstraintsIfNecessary();
        return super.getWidth();
    }

    @Override
    public SizeConstraint getHeight() {
        updateConstraintsIfNecessary();
        return super.getHeight();
    }
}
