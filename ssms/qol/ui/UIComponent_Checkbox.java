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

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.log4j.Level;
import ssms.qol.ModPlugin;

/**
 *
 * @author Malte Schulze
 */
public class UIComponent_Checkbox extends UIComponent_Base {
    protected Boolean checkmark;
    protected Callable<Boolean> valueGetter;
    protected boolean allowsNull = false;
    protected UIValueHandler<Boolean> valueHandler;

    public Boolean getCheckmark() {
        return checkmark;
    }

    public UIComponent_Checkbox setCheckmark(Boolean checkmark) {
        this.checkmark = checkmark;
        return this;
    }
    static protected float[] 
        colorBorder1 = new float[]{42.0f/255.0f, 48.0f/255.0f, 49.0f/255.0f}, 
        colorBorder2 = new float[]{132.0f/255.0f, 140.0f/255.0f, 140.0f/255.0f},
        colorBorder3 = new float[]{208.0f/255.0f, 216.0f/255.0f, 216.0f/255.0f},
        colorBorder4 = new float[]{51.0f/255.0f, 41.0f/255.0f, 41.0f/255.0f},
        colorBorder5 = new float[]{98.0f/255.0f, 62.0f/255.0f, 54.0f/255.0f},
        colorBG = new float[]{36.0f/255.0f, 22.0f/255.0f, 20.0f/255.0f};
    
    static protected float[] colorBorder4Checked = new float[]{74.0f/255.0f, 113.0f/255.0f, 116.0f/255.0f},
            colorBorder5Checked = new float[]{61.0f/255.0f, 210.0f/255.0f, 239.0f/255.0f},
            colorBorder6Checked = new float[]{30.0f/255.0f, 107.0f/255.0f, 122.0f/255.0f},
            colorBGChecked = new float[]{75.0f/255.0f, 254.0f/255.0f, 255.0f/255.0f};
    
    static protected float[] colorBorderInvalid = new float[]{255.0f/255.0f, 50.0f/255.0f, 50.0f/255.0f};

    public UIComponent_Checkbox(Boolean value, UIValueHandler<Boolean> valueHandler) {
        this(value, valueHandler, false);
    }
    
    public UIComponent_Checkbox(Boolean value, UIValueHandler<Boolean> valueHandler, boolean allowsNull) {
        this.checkmark = value;
        this.valueHandler = valueHandler;
        this.allowsNull = allowsNull;
    }
    
    public UIComponent_Checkbox(Callable<Boolean> valueGetter, UIValueHandler<Boolean> valueHandler) {
        this(valueGetter, valueHandler, false);
    }
    
    public UIComponent_Checkbox(Callable<Boolean> valueGetter, UIValueHandler<Boolean> valueHandler, boolean allowsNull) {
        if ( valueGetter == null ) throw new IllegalArgumentException("valueGetter may not be null!");
        this.valueGetter = valueGetter;
        this.valueHandler = valueHandler;
        this.allowsNull = allowsNull;
    }

    @Override
    public void render() {
        if ( valueGetter != null ) {
            try {
                checkmark = valueGetter.call();
            } catch (Exception ex) {
                valueGetter = null;
                Global.getLogger(ModPlugin.class).log(Level.INFO, "valueGetter threw an exception and will be deactivated.", ex);
            }
        }
        
        pushStyles();
        float[] gradient;
        if ( checkmark == null ) gradient = context.<float[]>getStyle(UIContext.StyleProperty.checkboxGradientNotSet);
        else if ( checkmark ) gradient = context.<float[]>getStyle(UIContext.StyleProperty.checkboxGradientChecked);
        else gradient = context.<float[]>getStyle(UIContext.StyleProperty.checkboxGradientUnchecked);
        UIUtil.getInstance().renderCheckbox(gradient, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
        popStyles();
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        for  ( InputEventAPI event : events ) {
            if ( event.isConsumed() ) continue;
            if ( event.isLMBDownEvent() ) {
                SimpleEntry<Integer,Integer> coords = context.getTranslatedCoordinates(event.getX(), event.getY());
                if ( layout.contains(coords.getKey(), coords.getValue()) ) {
                    UIUtil.getInstance().blur();
                    Boolean v = getCheckmark();
                    if ( v == null ) v = Boolean.TRUE;
                    else if ( v ) v = Boolean.FALSE;
                    else if ( allowsNull ) v = null;
                    else v = Boolean.TRUE;
                    if ( valueHandler == null || valueHandler.submitValue(context,v) || !valueHandler.validate(context,getCheckmark()) ) {
                        setCheckmark(v);
                    }
                    event.consume();
                }
            }
        }
    }
}
