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
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.settings.StarfarerSettings;
import com.fs.starfarer.ui.impl.StandardTooltipV2;
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable;
import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

/**
 *
 * @author Malte Schulze
 */
public class UIUtil {
    static protected volatile UIUtil instance;
    protected WeakReference<Focusable> focusedObject;
    protected WeakReference<StandardTooltipV2Expandable> tooltipComponent;
    protected Map<String,com.fs.graphics.A.Object> labelRenderers;
    
    protected UIUtil() {
        labelRenderers = new HashMap<>();
    }
    
    static public UIUtil getInstance() {
        UIUtil localInstance = UIUtil.instance;
        if ( localInstance == null ) {
            synchronized(UIUtil.class) {
                localInstance = UIUtil.instance;
                if ( localInstance == null ) {
                    UIUtil.instance = localInstance = new UIUtil();
                }
            }
        }
        return localInstance;
    }
    
    protected com.fs.graphics.A.Object getLabelRenderer(String font) {
        com.fs.graphics.A.Object labelRenderer = labelRenderers.get(font);
        if ( labelRenderer == null ) {
            labelRenderer = new com.fs.graphics.A.Object("", font);
            //Alpha
            labelRenderer.o00000(1f);
            //Outline
            //labelRenderer.\u00F400000(true);
            //Shadow
            labelRenderer.\u00D200000(true);
            labelRenderers.put(font, labelRenderer);
        }
        return labelRenderer;
    }
    
    public boolean isMouseOver(float x, float y, float w, float h) {
        int mx = Mouse.getX();
        int my = Mouse.getY();
        return x <= mx && y <= my && mx < x + w && my < y + h;
    }
    
    public boolean isMouseOver(Rectangle r) {
        return r.contains(Mouse.getX(), Mouse.getY());
    }
    
    public boolean isMouseOverAbsolute(float x1, float y1, float x2, float y2) {
        int mx = Mouse.getX();
        int my = Mouse.getY();
        return x1 <= mx && y1 <= my && mx < x2 && my < y2;
    }
    
    public void renderText(String text, Color textColor, float x, float y, float w, float h, Alignment alignment) {
        renderText(Fonts.INSIGNIA_LARGE,text,textColor, x, y, w, h, alignment, TextStyle.Shadow);
    }
    
    public void renderText(UIContext context, String text, float x, float y, float w, float h) {
        renderText(context, text, x, y, w, h, false);
    }
    
    public void renderText(UIContext context, String text, float x, float y, float w, float h, boolean useMouseOver) {
        Color c;
        if ( useMouseOver ) {
            int mx = Mouse.getX();
            int my = Mouse.getY();
            if ( x <= mx && y <= my && mx <= x + w && my <= y + h ) {
                c = context.<Color>getStyle(UIContext.StyleProperty.textMouseOverColor);
            } else {
                c = context.<Color>getStyle(UIContext.StyleProperty.textColor);
            }
        } else c = context.<Color>getStyle(UIContext.StyleProperty.textColor);
        renderText(context.<String>getStyle(UIContext.StyleProperty.font), text, c, x, y, w, h, 
            context.<Alignment>getStyle(UIContext.StyleProperty.textAlignment), context.<TextStyle>getStyle(UIContext.StyleProperty.textStyle));
    }
    
    public void renderText(String font, String text, Color textColor, float x, float y, float w, float h, Alignment alignment, TextStyle style) {
        com.fs.graphics.A.Object labelRenderer = getLabelRenderer(font);
        labelRenderer.Object(text);
        labelRenderer.Ò00000(textColor);
        switch ( style ) {
            case Normal: labelRenderer.\u00F400000(false); labelRenderer.\u00D200000(false); break;
            case Outline: labelRenderer.\u00F400000(true); labelRenderer.\u00D200000(false); break;
            case Shadow: labelRenderer.\u00F400000(false); labelRenderer.\u00D200000(true); break;
        }
        switch (alignment) {
          case BR:
            labelRenderer.ø00000(x + w, y);
            break;
          case BMID:
            labelRenderer.Ø00000(x + w / 2.0f, y);
            break;
          case MID:
            //labelRenderer.for(label.getPosition().getX() + label.getPosition().getWidth() / 2.0F, label.getPosition().getY() + label.getPosition().getHeight() / 2.0F);
            break;
          case TR:
            labelRenderer.Ô00000(x + w, y + h);
            break;
          case TL:
            labelRenderer.Object(x, y + h);
            break;
          case TMID:
            labelRenderer.ô00000(x + w / 2.0f, y + h);
            break;
          case RMID:
            labelRenderer.Ô00000(x + w, y + h / 2.0f + labelRenderer.ÔO0000() / 2.0f);
            break;
          case LMID:
            labelRenderer.ö00000(x, y + h / 2.0f);
            break;
        } 
    }
    
    public float getTextWidth(String text) {
        return getTextWidth(Fonts.INSIGNIA_LARGE,text);
    }
    
    public float getTextWidth(String font, String text) {
        return getLabelRenderer(font).\u00D500000(text);
    }
    
    public float getTextHeight(String text) {
        return getTextHeight(Fonts.INSIGNIA_LARGE, text);
    }
    
    public float getTextHeight(String font, String text) {
        return getLabelRenderer(font).\u00D200000(text);
    }
    
    public Focusable setFocus(Focusable focusedObject) {
        Focusable oldFocus = this.focusedObject != null ? this.focusedObject.get() : null;
        if (focusedObject != null ) this.focusedObject = new WeakReference<>(focusedObject);
        else this.focusedObject = null;
        return oldFocus;
    }
    
    public Focusable getFocus() {
        if ( focusedObject == null ) return null;
        return focusedObject.get();
    }
    
    public boolean isValidKeyForText(char c) {
        int i = c;
        return ( 32 <= i && i <= 93 ) || ( 95 <= i && i <= 126 ) || ( 160 <= i && i <= 163 ) || ( 191 <= i && i <= 207 ) || 
                ( 209 <= i && i <= 215 ) || ( 217 <= i && i <= 221 ) || ( 223 <= i && i <= 239 ) || ( 241 <= i && i <= 247 ) || ( 249 <= i && i <= 253 ) ||
                168 == i || 176 == i || 180 == i || 182 == i || 184 == i || 255 == i;
    }
    
    public void renderCheckbox(float[] gradient, float x, float y, float w, float h) {
        float xMin = x, xMax = x + w,
                yMin = y, yMax = y + h;
        
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        GL11.glColor3f(gradient[0],gradient[1],gradient[2]);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(xMin + 5f, yMin + 5f);
        GL11.glVertex2f(xMin + 5f, yMax - 5f);
        GL11.glVertex2f(xMax - 5f, yMax - 5f);
        GL11.glVertex2f(xMax - 5f, yMin + 5f);
        GL11.glEnd();
        
        GL11.glLineWidth(1f);
        GL11.glColor3f(gradient[3],gradient[4],gradient[5]);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(xMin + 3.5f, yMin + 0.5f);
        GL11.glVertex2f(xMax - 3.5f, yMin + 0.5f);
        GL11.glVertex2f(xMax - 0.5f, yMin + 3.5f);
        GL11.glVertex2f(xMax - 0.5f, yMax - 3.5f);
        GL11.glVertex2f(xMax - 3.5f, yMax - 0.5f);
        GL11.glVertex2f(xMin + 3.5f, yMax - 0.5f);
        GL11.glVertex2f(xMin + 0.5f, yMax - 3.5f);
        GL11.glVertex2f(xMin + 0.5f, yMin + 3.5f);
        GL11.glEnd();
        
        GL11.glColor3f(gradient[6],gradient[7],gradient[8]);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(xMin + 3.5f, yMin + 1.5f);
        GL11.glVertex2f(xMax - 3.5f, yMin + 1.5f);
        GL11.glVertex2f(xMax - 1.5f, yMin + 3.5f);
        GL11.glVertex2f(xMax - 1.5f, yMax - 3.5f);
        GL11.glVertex2f(xMax - 3.5f, yMax - 1.5f);
        GL11.glVertex2f(xMin + 3.5f, yMax - 1.5f);
        GL11.glVertex2f(xMin + 1.5f, yMax - 3.5f);
        GL11.glVertex2f(xMin + 1.5f, yMin + 3.5f);
        GL11.glEnd();
        
        GL11.glColor3f(gradient[9],gradient[10],gradient[11]);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(xMin + 3.5f, yMin + 2.5f);
        GL11.glVertex2f(xMax - 3.5f, yMin + 2.5f);
        GL11.glVertex2f(xMax - 2.5f, yMin + 3.5f);
        GL11.glVertex2f(xMax - 2.5f, yMax - 3.5f);
        GL11.glVertex2f(xMax - 3.5f, yMax - 2.5f);
        GL11.glVertex2f(xMin + 3.5f, yMax - 2.5f);
        GL11.glVertex2f(xMin + 2.5f, yMax - 3.5f);
        GL11.glVertex2f(xMin + 2.5f, yMin + 3.5f);
        GL11.glEnd();
        
        GL11.glColor3f(gradient[12],gradient[13],gradient[14]);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(xMin + 3.5f, yMin + 3.5f);
        GL11.glVertex2f(xMax - 3.5f, yMin + 3.5f);
        GL11.glVertex2f(xMax - 3.5f, yMax - 3.5f);
        GL11.glVertex2f(xMin + 3.5f, yMax - 3.5f);
        GL11.glEnd();
        
        GL11.glColor3f(gradient[9],gradient[10],gradient[11]);
        GL11.glBegin(GL11.GL_POINTS);
        GL11.glVertex2f(xMin + 3.5f, yMin + 3.5f);
        GL11.glVertex2f(xMin + 3.5f, yMax - 3.5f);
        GL11.glVertex2f(xMax - 3.5f, yMin + 3.5f);
        GL11.glVertex2f(xMax - 3.5f, yMax - 3.5f);
        GL11.glEnd();
        
        GL11.glColor3f(gradient[15],gradient[16],gradient[17]);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(xMin + 4.5f, yMin + 4.5f);
        GL11.glVertex2f(xMax - 4.5f, yMin + 4.5f);
        GL11.glVertex2f(xMax - 4.5f, yMax - 4.5f);
        GL11.glVertex2f(xMin + 4.5f, yMax - 4.5f);
        GL11.glEnd();
        
        GL11.glColor3f(gradient[18],gradient[19],gradient[20]);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(xMin + 5.5f, yMin + 5.5f);
        GL11.glVertex2f(xMax - 5.5f, yMin + 5.5f);
        GL11.glVertex2f(xMax - 5.5f, yMax - 5.5f);
        GL11.glVertex2f(xMin + 5.5f, yMax - 5.5f);
        GL11.glEnd();
    }
    
    public void renderScrollbar(UIContext context, float scrollPos, float x, float y, float viewHeight, float contentHeight, boolean mouseOver) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1f);
        
        float[] color = context.<float[]>getStyle(UIContext.StyleProperty.scrollbarBackgroundColor);
        int scbw = context.<Integer>getStyle(UIContext.StyleProperty.scrollbarWidth);
        GL11.glColor3f(color[0],color[1],color[2]);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x + scbw / 2f, y);
        GL11.glVertex2f(x + scbw / 2f, y + viewHeight);
        GL11.glEnd();
        
        float markerHeight = viewHeight == 0 ? 0 : viewHeight * viewHeight / Math.max(viewHeight, contentHeight);
        float yMarkerMax = contentHeight == 0 ? 0 : ((float)(contentHeight-viewHeight-scrollPos)/(contentHeight-viewHeight))*(viewHeight-markerHeight) + y + markerHeight;
        float yMarkerMin = yMarkerMax - markerHeight;
        if ( mouseOver ) {
            color = context.<float[]>getStyle(UIContext.StyleProperty.scrollbarMarkerActiveColor);
        } else {
            color = context.<float[]>getStyle(UIContext.StyleProperty.scrollbarMarkerColor);
        }
        GL11.glColor3f(color[0],color[1],color[2]);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x + scbw, yMarkerMin);
        GL11.glVertex2f(x + scbw, yMarkerMax);
        GL11.glVertex2f(x, yMarkerMax);
        GL11.glVertex2f(x, yMarkerMin);
        GL11.glEnd();
    }
    
    public void renderTextButton(UIContext context, String text, float x, float y, float w, float h) {
        float xMin = x, xMax = x + w, yMin = y, yMax = y + h;
        
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        float[] gradient = context.<float[]>getStyle(UIContext.StyleProperty.buttonGradient);
        GL11.glColor3f(gradient[0],gradient[1],gradient[2]);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(xMin, yMin);
        GL11.glVertex2f(xMin, yMax);
        GL11.glVertex2f(xMax, yMax);
        GL11.glVertex2f(xMax, yMin);
        GL11.glEnd();
        GL11.glColor3f(gradient[1],gradient[2],gradient[3]);
        GL11.glLineWidth(1f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(xMin + 0.5f, yMin + 0.5f);
        GL11.glVertex2f(xMin + 0.5f, yMax - 0.5f);
        GL11.glVertex2f(xMax - 0.5f, yMax - 0.5f);
        GL11.glVertex2f(xMax - 0.5f, yMin + 0.5f);
        GL11.glEnd();
        
        UIUtil.getInstance().renderText(context, text, xMin + 6, yMin, xMax - xMin, yMax - yMin, UIUtil.getInstance().isMouseOverAbsolute(xMin, yMin, xMax, yMax));
    }
    
    public StandardTooltipV2Expandable createTooltip(String content) {
        StandardTooltipV2Expandable tooltip = StandardTooltipV2Expandable.createTextTooltip(Math.min(400f,getTextWidth("graphics/fonts/orbitron12condensed.fnt",content)), content);
        tooltip.beforeShown();
        tooltip.getFader().forceIn();
        tooltip.getPosition().setRoundCoordinates(true);
        return tooltip;
    }
    
    public void positionTooltipOnMouse(StandardTooltipV2Expandable tooltip) {
        if ( tooltip == null ) return;
        int x = Mouse.getX(), y = Mouse.getY();
        if ( x + tooltip.getPosition().getWidth() > Global.getSettings().getScreenWidth() ) {
            x = (int)Global.getSettings().getScreenWidth() - (int)tooltip.getPosition().getWidth();
        }
        if ( y + tooltip.getPosition().getHeight() > Global.getSettings().getScreenHeight() ) {
            y = (int)Global.getSettings().getScreenHeight() - (int)tooltip.getPosition().getHeight();
        }
        tooltip.getPosition().setLocation(x, y);
    }

    public void blur() {
        if ( focusedObject != null ) {
            Focusable focus = focusedObject.get();
            if ( focus != null ) {
                focusedObject = null;
                focus.blur();
            }
        }
    }
    
    public void removeFocus(Focusable removeFocusIfCurrent) {
        if ( focusedObject != null ) {
            Focusable focus = focusedObject.get();
            if ( focus != null && focus.equals(removeFocusIfCurrent) ) {
                focusedObject = null;
            }
        }
    }
    
    public StandardTooltipV2Expandable setTooltip(StandardTooltipV2Expandable tooltipComponent) {
        StandardTooltipV2Expandable oldTooltip = this.tooltipComponent != null ? this.tooltipComponent.get() : null;
        if (tooltipComponent != null ) this.tooltipComponent = new WeakReference<>(tooltipComponent);
        else this.tooltipComponent = null;
        return oldTooltip;
    }
    
    public StandardTooltipV2Expandable getTooltip() {
        if ( tooltipComponent == null ) return null;
        return tooltipComponent.get();
    }
    
    public void removeTooltip(StandardTooltipV2Expandable removeTooltipIfCurrent) {
        if ( tooltipComponent != null ) {
            StandardTooltipV2Expandable tooltip = tooltipComponent.get();
            if ( tooltip != null && tooltip.equals(removeTooltipIfCurrent) ) {
                tooltipComponent = null;
            }
        }
    }
    
    public void removeTooltip() {
        tooltipComponent = null;
    }
}
