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
import java.util.List;
import org.lwjgl.util.Rectangle;

/**
 *
 * @author Malte Schulze
 */
public interface UIComponent {
    public UIComponent_Parent parentComponent();
    public void setParentComponent(UIComponent_Parent parent);
    public boolean resize(Rectangle rect);
    public Rectangle getLayout();
    public void pack();
    public void render();
    public void advance(float amount);
    public void processInput(List<InputEventAPI> events);
    public void dismis();
    public void setWidth(int min, int pref, int max);
    public void setWidth(SizeConstraint con);
    public void setWidthMin(int width);
    public void setWidthPref(int width);
    public void setWidthMax(int width);
    public void setHeight(int min, int pref, int max);
    public void setHeight(SizeConstraint con);
    public void setHeightMin(int height);
    public void setHeightPref(int height);
    public void setHeightMax(int height);
    public SizeConstraint getWidth();
    public SizeConstraint getHeight();
    public void setContext(UIContext context);
    public UIContext getContext();
    public void addStyle(String propertyId, Object value);
    public void addStyle(UIContext.StyleProperty property, Object value);
    public void removeStyle(String propertyId);
    public void removeStyle(UIContext.StyleProperty property);
}
