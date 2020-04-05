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
import java.util.concurrent.Callable;
import org.apache.log4j.Level;

/**
 *
 * @author Malte Schulze
 */
public abstract class UIComponent_Button extends UIComponent_Base {
    protected String text, textTooltip;
    protected StandardTooltipV2Expandable tooltip;
    protected Callable<String> textGetter;

    public String getText() {
        return text;
    }

    public UIComponent_Button setText(String text) {
        this.text = text;
        this.textGetter = null;
        return this;
    }
    
    public UIComponent_Button setText(Callable<String> textGetter) {
        this.textGetter = textGetter;
        this.text = null;
        return this;
    }
    
    public UIComponent_Button setTextTooltip(String text) {
        this.textTooltip = text;
        return this;
    }

    public UIComponent_Button(String text) {
        this.text = text;
    }
    
    public UIComponent_Button(Callable<String> textGetter) {
        this.textGetter = textGetter;
    }

    @Override
    public void render() {
        pushStyles();
        if ( textGetter == null )
            UIUtil.getInstance().renderTextButton(context, text, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
        else {
            try {
                UIUtil.getInstance().renderTextButton(context, textGetter.call(), layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
            } catch (Exception ex) {
                context.log(Level.ERROR, "Failed to determine text for button!", ex);
                textGetter = null;
            }
        }
        popStyles();
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        for  ( InputEventAPI event : events ) {
            if ( textTooltip != null && event.isMouseMoveEvent() ) {
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
            if ( event.isConsumed() ) continue;
            if ( event.isLMBDownEvent() ) {
                AbstractMap.SimpleEntry<Integer,Integer> coords = context.getTranslatedCoordinates(event.getX(), event.getY());
                if ( layout.contains(coords.getKey(), coords.getValue()) ) {
                    UIUtil.getInstance().blur();
                    getContext().addDelayedAction(new Runnable() {
                        @Override
                        public void run() {
                            onClick();
                        }
                    });
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
    
    public abstract void onClick();
}
