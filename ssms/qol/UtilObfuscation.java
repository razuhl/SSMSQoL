/*
 * Copyright (C) 2021 Malte Schulze.
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
package ssms.qol;

import com.fs.graphics.D;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable;
import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.apache.log4j.Level;
import ssms.qol.ui.TextStyle;
import ssms.qol.ui.UIUtil;

/**
 *
 * @author Malte Schulze
 */
public class UtilObfuscation {

    public static D GetTexture(String icon) {
        return com.fs.graphics.H.o00000().get(icon);
    }
    
    protected static Method mProcessInput = null;
    protected static Class cEventCollection = null;
    
    public static boolean InitProcessInput() {
        if ( mProcessInput == null ) {
            try {
                cEventCollection = Class.forName("com.fs.starfarer.util.A.B");
                mProcessInput = StandardTooltipV2Expandable.class.getMethod("processInput", cEventCollection);
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
                Global.getLogger(ModPlugin.class).log(Level.ERROR, "Error reflecting types for processing input on tooltips!", ex);
            }
        }
        return true;
    }

    static void ProcessInput(StandardTooltipV2Expandable tooltip, List<InputEventAPI> events) {
        if ( !InitProcessInput() ) return;
        try {
            List<Object> collection = (List<Object>) cEventCollection.newInstance();
            for ( InputEventAPI ev : events ) {
                collection.add(ev);
            }
            mProcessInput.invoke(tooltip, collection);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Global.getLogger(ModPlugin.class).log(Level.ERROR, "Error creating new event collection for processing on tooltip!", ex);
        }
    }
    
    static public class LabelRendererWrapper {
        static protected Method mSetColor, mGetColor, mSetShadow, mGetWidth, mGetHeight, mSetOutline, mSetText;
        static protected Method mRenderRightAbove, mRenderCenterAbove, mRenderRightCenter, mRenderLeftCenter,
                mRenderCenterCenter, mRenderCenterBelow, mRenderLeftAbove, mRenderLeftBelow, mRenderRightBelow;
        static protected Constructor conStringString;
        static boolean initialized = false;
        protected Object original;

        public LabelRendererWrapper(String s, String font) {
            if ( !initialized ) {
                try {
                    Class c = UIUtil.class.getClassLoader().loadClass("com.fs.graphics.super.C");
                    conStringString = c.getConstructor(String.class,String.class);
                    for ( Method m : c.getMethods() ) {
                        if ( m.getName().equals("\u00F4O0000") && m.getParameterTypes().length == 0 ) mGetColor = m;
                        else if ( m.getName().equals("\u00D200000") && m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == Color.class ) mSetColor = m;
                        else if ( m.getName().equals("int") && m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == boolean.class ) mSetOutline = m;
                        else if ( m.getName().equals("Object") && m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == boolean.class ) mSetShadow = m;
                        else if ( m.getName().equals("\u00D500000") && m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == String.class && m.getReturnType() == float.class ) mGetWidth = m;
                        else if ( m.getName().equals("\u00D200000") && m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == String.class && m.getReturnType() == float.class ) mGetHeight = m;
                        else if ( m.getName().equals("o00000") && m.getParameterTypes().length == 2 && m.getParameterTypes()[0] == String.class && m.getParameterTypes()[1] == boolean.class ) mSetText = m;
                        else if ( m.getParameterTypes().length == 2 && m.getParameterTypes()[0] == float.class && m.getParameterTypes()[1] == float.class ) {
                            switch ( m.getName() ) {
                                case "o00000": mRenderRightAbove = m; break;
                                case "\u00F800000": mRenderLeftAbove = m; break;
                                case "\u00D400000": mRenderLeftBelow = m; break;
                                case "Object": mRenderRightBelow = m; break;
                                case "\u00F400000": mRenderCenterBelow = m; break;
                                case "\u00F600000": mRenderRightCenter = m; break;
                                case "return": mRenderLeftCenter = m; break;
                                case "\u00D800000": mRenderCenterAbove = m; break;
                                case "for": mRenderCenterCenter = m; break;
                            }
                        }
                    }
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
                    Global.getLogger(ModPlugin.class).log(Level.ERROR, "Error populating wrapper for label renderer!", ex);
                }
                initialized = true;
            }
            try {
                this.original = conStringString.newInstance(s,font);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Global.getLogger(ModPlugin.class).log(Level.ERROR, "Error instantiating native label renderer in wrapper!", ex);
            }
        }

        /**
         * 
         * @param alpha 0-255
         */
        public void setAlpha(int alpha) {
            try {
                Color c = (Color)mGetColor.invoke(original);
                mSetColor.invoke(original, new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            }
        }

        public void setColor(Color color) {
            try {
                mSetColor.invoke(original, color);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            }
        }

        public void setText(String text) {
            try {
                mSetText.invoke(original, text, false);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            }
        }

        public void setShadow(boolean drawShadow) {
            try {
                mSetShadow.invoke(original, drawShadow);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            }
        }

        public void setOutline(boolean drawShadow) {
            try {
                mSetOutline.invoke(original, drawShadow);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            }
        }

        public void renderText(String font, String text, Color textColor, float x, float y, float w, float h, Alignment alignment, TextStyle style) {
            setText(text);
            setColor(textColor);
            switch ( style ) {
                case Normal: setOutline(false); setShadow(false); break;
                case Outline: setOutline(true); setShadow(false); break;
                case Shadow: setOutline(false); setShadow(true); break;
            }
            try {
                switch (alignment) {
                  case BR: mRenderLeftAbove.invoke(original, x + w, y); break;
                  case BMID: mRenderCenterAbove.invoke(original, x + w / 2.0f, y); break;
                  case MID: mRenderCenterCenter.invoke(original, x + w / 2.0f, y + h / 2.0f); break;
                  case TR: mRenderLeftBelow.invoke(original, x + w, y + h); break;
                  case TL: mRenderRightBelow.invoke(original, x, y + h); break;
                  case TMID: mRenderCenterBelow.invoke(original, x + w / 2.0f, y + h); break;
                  case RMID: mRenderLeftCenter.invoke(original, x + w, y + h / 2.0f); break;
                  case LMID: mRenderRightCenter.invoke(original, x, y + h / 2.0f); break;
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            }
        }

        public float getTextWidth(String text) {
            try {
                return (float)mGetWidth.invoke(original, text);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            }
            return 100f;
        }

        public float getTextHeight(String text) {
            try {
                return (float)mGetHeight.invoke(original, text);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            }
            return 100f;
        }
    }
}
