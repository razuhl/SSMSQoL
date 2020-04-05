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

import com.fs.graphics.Sprite;
import java.awt.Color;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Malte Schulze
 */
public class UIComponent_Sprite extends UIComponent_Base {
    protected Sprite sprite;
    protected boolean useCornerColors = false, centered = true, stretch = false, autosize = false, autosizeDirty = false;
    protected float tw = -1f, th = -1f;

    public UIComponent_Sprite(String icon, boolean centered, boolean stretch) {
        sprite = new Sprite(icon);
        sprite.setBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        this.centered = centered;
        this.stretch = stretch;
    }
    
    public Sprite getSprite() {
        return sprite;
    }

    public UIComponent_Sprite setIcon(String icon) {
        sprite.setTexture(com.fs.graphics.OooO.\u00D200000(icon));
        return this;
    }
    
    public UIComponent_Sprite setColor(Color c) {
        sprite.setColor(c);
        useCornerColors = false;
        return this;
    }
    
    /**
     * @param sFactor Source rgba modifier
     * @param dFactor Destination rgba modifier
     * @return 
     */
    public UIComponent_Sprite setBlendFunc(int sFactor, int dFactor) {
        sprite.setBlendFunc(sFactor, dFactor);
        return this;
    }
    
    public UIComponent_Sprite setAngle(float angle) {
        sprite.setAngle(angle);
        return this;
    }
    
    public UIComponent_Sprite setCornerColor(Color lowerLeft, Color upperLeft, Color upperRight, Color lowerRight) {
        sprite.setColorLL(lowerLeft);
        sprite.setColorUL(upperLeft);
        sprite.setColorLR(lowerRight);
        sprite.setColorUR(upperRight);
        useCornerColors = true;
        return this;
    }
    
    public UIComponent_Sprite setAutosize(boolean autosize) {
        this.autosize = autosize;
        autosizeDirty = autosize;
        return this;
    }

    @Override
    public void render() {
        pushStyles();
        
        int marginLeft = context.<Integer>getStyle(UIContext.StyleProperty.marginLeft);
        int marginRight = context.<Integer>getStyle(UIContext.StyleProperty.marginRight);
        int marginTop = context.<Integer>getStyle(UIContext.StyleProperty.marginTop);
        int marginBottom = context.<Integer>getStyle(UIContext.StyleProperty.marginBottom);
        
        if ( tw < 0 ) {
            tw = sprite.getWidth();
        }
        if ( th < 0 ) {
            th = sprite.getHeight();
        }
        
        if ( autosizeDirty ) {
            int w = (int) Math.ceil(tw);
            int h = (int) Math.ceil(th);
            setWidth(w,w,w);
            setHeight(h,h,h);
            autosizeDirty = false;
        }
        
        float x = layout.getX() + marginRight, y = layout.getY() + marginBottom, w = layout.getWidth() - marginRight - marginLeft, h = layout.getHeight() - marginBottom - marginTop;
        if ( !stretch ) {
            if ( centered ) {
                x = x + w * 0.5f - tw * 0.5f;
                y = y + h * 0.5f - th * 0.5f;
            } else {
                y += h - th;
            }
            w = tw;
            h = th;
        }
        
        sprite.setWidth(w);
        sprite.setHeight(h);
        if ( !useCornerColors )
            sprite.render(x, y);
        else sprite.renderAtCenterWithCornerColors(x + w * 0.5f, y + h * 0.5f);
        
        popStyles();
    }
}
