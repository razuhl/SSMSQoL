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
import java.util.AbstractMap;
import java.util.List;

/**
 *
 * @author Malte Schulze
 */
public class UIComponent_Label extends UIComponent_Base {
    protected String text, textTooltip;
    protected StandardTooltipV2Expandable tooltip;

    public UIComponent_Label(String text) {
        this.text = text;
    }
    
    public String getText() {
        return text;
    }

    public UIComponent_Label setText(String text) {
        this.text = text;
        return this;
    }
    
    public UIComponent_Label setTextTooltip(String text) {
        this.textTooltip = text;
        return this;
    }

    @Override
    public void render() {
        pushStyles();
        UIUtil.getInstance().renderText(context, text, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight(), false);
        popStyles();
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        if ( textTooltip == null ) return;
        for  ( InputEventAPI event : events ) {
            if ( event.isMouseMoveEvent() ) {
                AbstractMap.SimpleEntry<Integer,Integer> coords = context.getTranslatedCoordinates(event.getX(), event.getY());
                if ( layout.contains(coords.getKey(), coords.getValue()) ) {
                    if ( tooltip == null ) {
                        tooltip = UIUtil.getInstance().createTooltip(textTooltip);
                        UIUtil.getInstance().setTooltip(tooltip);
                    }
                } else if ( tooltip != null ) {
                    UIUtil.getInstance().removeTooltip(tooltip);
                    dismissTooltip(tooltip);
                    tooltip = null;
                }
            }
        }
    }
    
    @Override
    public void dismis() {
        super.dismis();
        if ( tooltip != null ) {
            UIUtil.getInstance().removeTooltip(tooltip);
            dismissTooltip(tooltip);
            tooltip = null;
        }
    }
}
