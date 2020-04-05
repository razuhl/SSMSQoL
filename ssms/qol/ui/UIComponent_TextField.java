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

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import java.awt.Color;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.log4j.Level;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ssms.qol.ModPlugin;

/**
 *
 * @author Malte Schulze
 */
public class UIComponent_TextField extends UIComponent_Base implements Focusable {
    protected String text, lastValidText; 
    protected boolean focused = false;
    protected Fader carrot;
    protected UIValueHandler<String> valueHandler;
    protected Callable<String> valueGetter;

    public String getText() {
        return text;
    }

    public UIComponent_TextField setText(String text) {
        if ( text != null && text.length() == 0 ) {
            if ( valueHandler == null || valueHandler.validate(context, null) ) {
                text = null;
            }
        }
        this.text = text;
        return this;
    }
    static protected float[] colorBorder = new float[]{38.0f/255.0f, 68.0f/255.0f, 79.0f/255.0f}, 
        colorBG = new float[]{18.0f/255.0f, 44.0f/255.0f, 52.0f/255.0f};
    static protected Color colorFont = new Color(170.0f/255.0f, 221.0f/255.0f, 254.0f/255.0f);
    static protected float[] colorBorderFocused = new float[]{77.0f/255.0f, 136.0f/255.0f, 159.0f/255.0f}, 
        colorBGFocused = new float[]{26.0f/255.0f, 58.0f/255.0f, 69.0f/255.0f};
    static protected float[] colorBorderInvalid = new float[]{255.0f/255.0f, 50.0f/255.0f, 50.0f/255.0f};

    public UIComponent_TextField(String value, UIValueHandler<String> valueHandler) {
        carrot = new Fader(0, 0.5f, 0.5f, true, true);
        this.text = value;
        this.valueHandler = valueHandler;
    }
    
    public UIComponent_TextField(Float value, UIValueHandler<String> valueHandler) {
        this(value != null ? Float.toString(value) : null, valueHandler);
    }
    
    public UIComponent_TextField(Integer value, UIValueHandler<String> valueHandler) {
        this(value != null ? Integer.toString(value) : null, valueHandler);
    }
    
    public UIComponent_TextField(Callable<String> valueGetter, UIValueHandler<String> valueHandler) {
        carrot = new Fader(0, 0.5f, 0.5f, true, true);
        this.valueGetter = valueGetter;
        this.valueHandler = valueHandler;
    }

    @Override
    public void render() {
        //while focused we must use the current value since it gets edited by the user.
        if ( !focused && valueGetter != null ) {
            try {
                text = valueGetter.call();
            } catch (Exception ex) {
                valueGetter = null;
                Global.getLogger(ModPlugin.class).log(Level.INFO, "valueGetter threw an exception and will be deactivated.", ex);
            }
        }
        
        float xMin = layout.getX(), xMax = layout.getX() + layout.getWidth(),
                yMin = layout.getY(), yMax = layout.getY() + layout.getHeight();
        
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        if ( focused )
            GL11.glColor3f(colorBGFocused[0],colorBGFocused[1],colorBGFocused[2]);
        else GL11.glColor3f(colorBG[0],colorBG[1],colorBG[2]);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(xMin, yMin);
        GL11.glVertex2f(xMin, yMax);
        GL11.glVertex2f(xMax, yMax);
        GL11.glVertex2f(xMax, yMin);
        GL11.glEnd();
        if ( valueHandler == null || valueHandler.validate(context, text) ) {
            if ( focused )
                GL11.glColor3f(colorBorderFocused[0],colorBorderFocused[1],colorBorderFocused[2]);
            else 
                GL11.glColor3f(colorBorder[0],colorBorder[1],colorBorder[2]);
        } else {
            GL11.glColor3f(colorBorderInvalid[0],colorBorderInvalid[1],colorBorderInvalid[2]);
        }
        GL11.glLineWidth(1f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(xMin + 0.5f, yMin + 0.5f);
        GL11.glVertex2f(xMin + 0.5f, yMax - 0.5f);
        GL11.glVertex2f(xMax - 0.5f, yMax - 0.5f);
        GL11.glVertex2f(xMax - 0.5f, yMin + 0.5f);
        GL11.glEnd();
        
        if ( focused && carrot.isFadingOut() ) {
            if ( text != null )
                UIUtil.getInstance().renderText(new StringBuilder(text).append("_").toString(), colorFont, xMin + 6, yMin, xMax - xMin, yMax - yMin, Alignment.LMID);
            else UIUtil.getInstance().renderText("_", colorFont, xMin + 6, yMin, xMax - xMin, yMax - yMin, Alignment.LMID);
        } else UIUtil.getInstance().renderText(text, colorFont, xMin + 6, yMin, xMax - xMin, yMax - yMin, Alignment.LMID);
    }

    @Override
    public void advance(float amount) {
        if ( focused ) carrot.advance(amount);
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        for  ( InputEventAPI event : events ) {
            if ( event.isConsumed() ) continue;
            if ( event.isLMBDownEvent() ) {
                SimpleEntry<Integer,Integer> coords = context.getTranslatedCoordinates(event.getX(), event.getY());
                if ( layout.contains(coords.getKey(), coords.getValue()) ) {
                    if ( !focused ) focus();
                    event.consume();
                } else if ( focused ) {
                    blur();
                }
            } else if ( focused && event.isKeyboardEvent() && ( event.isKeyDownEvent() || event.isRepeat() ) && !event.isModifierKey() ) {
                char c = event.getEventChar();
                if ( Keyboard.KEY_BACK == event.getEventValue() ) {
                    if ( text != null && text.length() > 0 ) setText(text.substring(0, text.length()-1));
                    event.consume();
                } else if ( UIUtil.getInstance().isValidKeyForText(c) ) {
                    if ( text != null ) setText(new StringBuilder(text).append(c).toString());
                    else setText(Character.toString(c));
                    event.consume();
                }
            }
        }
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void focus() {
        Focusable old = UIUtil.getInstance().setFocus(this);
        if ( old != null ) old.blur();
        focused = true;
        lastValidText = text;
        carrot.forceIn();
    }

    @Override
    public void blur() {
        UIUtil.getInstance().removeFocus(this);
        focused = false;
        if ( valueHandler != null ) {
            if ( !valueHandler.submitValue(context, getText()) ) {
                setText(lastValidText);
            }
        }
    }

    @Override
    public void dismis() {
        super.dismis();
        if ( focused ) blur();
    }
}
