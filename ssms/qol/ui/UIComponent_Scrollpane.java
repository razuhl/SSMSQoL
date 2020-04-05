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
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

/**
 *
 * @author Malte Schulze
 */
public class UIComponent_Scrollpane extends UIComponent_Parent {
    protected float[] colorScrollbarMarker = new float[]{54.0f/255.0f, 155.0f/255.0f, 197.0f/255.0f};
    protected float[] colorScrollbarMarkerMouseOver = new float[]{255.0f/255.0f, 255.0f/255.0f, 255.0f/255.0f};
    protected boolean draggingScrollbar = false, mouseOverScrollbar = false;
    protected int scrollPos = 0, contentHeight = 0, viewHeight = 0, marginLeft, marginRight, marginTop, marginBottom, scbw, horizontalGap;

    public UIComponent_Scrollpane() {
        super();
        manualWidthConstraint.min = 0;
        manualWidthConstraint.preferred = 0;
        manualWidthConstraint.max = Integer.MAX_VALUE;
        manualHeightConstraint.min = 0;
        manualHeightConstraint.preferred = 0;
        manualHeightConstraint.max = Integer.MAX_VALUE;
    }

    @Override
    public void addChild(UIComponent child) {
        if ( childs.contains(child) ) return;
        if ( childs.size() >= 1 ) {
            while ( !childs.isEmpty() ) {
                removeChild(childs.get(0));
            }
        }
        super.addChild(child);
    }

    @Override
    public void layoutChildren() {
        if ( childs.size() >= 1 ) {
            UIComponent c = childs.get(0);
            c.resize(new Rectangle(layout.getX() + marginLeft, layout.getY() + layout.getHeight() - marginTop - contentHeight, layout.getWidth() - scbw - marginRight - marginLeft - horizontalGap, contentHeight));
        }
    }

    @Override
    public void calculateConstraints() {
        int contentWidth;
        if ( childs.size() >= 1 ) {
            UIComponent c = childs.get(0);
            contentHeight = c.getHeight().preferred;
            contentWidth = c.getWidth().preferred;
        } else {
            contentHeight = 0;
            contentWidth = 0;
        }
        contentHeight += marginTop + marginBottom;
        
        widthConstraint.min = Math.max(scbw + horizontalGap + marginLeft + marginRight, manualWidthConstraint.min);
        widthConstraint.preferred = Math.min(contentWidth + widthConstraint.min, manualWidthConstraint.preferred);
        widthConstraint.max = Math.max(widthConstraint.preferred, manualWidthConstraint.max);
        
        heightConstraint.min = Math.max(marginTop + marginBottom, manualHeightConstraint.min);
        heightConstraint.preferred = Math.min(contentHeight + heightConstraint.min, manualHeightConstraint.preferred);
        heightConstraint.max = Math.max(heightConstraint.preferred, manualHeightConstraint.max);
    }
    
    protected void startDragDropScrollbar() {
        draggingScrollbar = true;
    }
    
    protected void stopDragDropScrollbar() {
        draggingScrollbar = false;
    }
    
    protected void scrollBy(int scrollBy) {
        scrollTo(scrollPos+scrollBy);
    }
    
    protected void scrollTo(int scrollPos) {
        scrollPos = Math.min(contentHeight-layout.getHeight(), scrollPos);
        scrollPos = Math.max(0, scrollPos);
        this.scrollPos = scrollPos;
    }
    
    @Override
    public void processInput(List<InputEventAPI> events) {
        for  ( InputEventAPI event : events ) {
            if ( event.isConsumed() ) continue;
            
            SimpleEntry<Integer,Integer> coords = context.getTranslatedCoordinates(event.getX(), event.getY());
            if ( event.isMouseMoveEvent() || event.isMouseScrollEvent() || event.isLMBDownEvent() ) {
                int xMin = layout.getX() + layout.getWidth() - scbw - marginRight;
                int xMax = xMin + scbw;
                int yMin = layout.getY() + marginBottom;
                int yMax = layout.getY() + layout.getHeight() - marginTop;
                if ( coords.getKey() >= xMin && coords.getKey() < xMax && coords.getValue() >= yMin && coords.getValue() < yMax ) {
                    //mouse is over the scrollbar
                    if ( event.isLMBDownEvent() ) {
                        UIUtil.getInstance().blur();
                        startDragDropScrollbar();
                        event.consume();
                    } else {
                        mouseOverScrollbar = true;
                    }
                } else {
                    mouseOverScrollbar = false;
                }
            }
        }
        
        //we have to consider translation due to scrolling
        context.pushTranslation(0,-scrollPos);
        try {
            super.processInput(events);
        } finally {
            context.popTranslation();
        }
        
        for  ( InputEventAPI event : events ) {
            if ( event.isLMBUpEvent() ) {
                stopDragDropScrollbar();
                continue;
            }
            if ( event.isConsumed() ) continue;
            if ( draggingScrollbar && event.isMouseMoveEvent() ) {
                if ( layout.getHeight() > 0 ) {
                    SimpleEntry<Integer,Integer> coords = context.getTranslatedCoordinates(event.getX(), event.getY());
                    int yContent = (int)((float)(layout.getY() + layout.getHeight() - coords.getValue())/layout.getHeight() * (contentHeight-viewHeight));
                    scrollTo(yContent);
                }
            } else if ( event.isMouseScrollEvent() ) {
                SimpleEntry<Integer,Integer> coords = context.getTranslatedCoordinates(event.getX(), event.getY());
                if ( layout.contains(coords.getKey(), coords.getValue()) ) {
                    int scroll = -100;
                    if ( event.isShiftDown() ) scroll *= 5;
                    scrollBy(event.getEventValue() < 0 ? -scroll : scroll);
                    event.consume();
                }
            }
        }
    }

    @Override
    public void cacheStyles() {
        marginLeft = context.<Integer>getStyle(UIContext.StyleProperty.marginLeft);
        marginRight = context.<Integer>getStyle(UIContext.StyleProperty.marginRight);
        marginTop = context.<Integer>getStyle(UIContext.StyleProperty.marginTop);
        marginBottom = context.<Integer>getStyle(UIContext.StyleProperty.marginBottom);
        horizontalGap = context.<Integer>getStyle(UIContext.StyleProperty.horizontalGap);
        scbw = context.<Integer>getStyle(UIContext.StyleProperty.scrollbarWidth);
    }

    @Override
    public void renderPreChilds() {
        viewHeight = layout.getHeight() - marginTop - marginBottom;
        
        UIUtil.getInstance().renderScrollbar(context, scrollPos, layout.getX() + layout.getWidth() - marginRight - scbw, 
                layout.getY() + marginBottom, viewHeight, contentHeight, mouseOverScrollbar);
        
        context.startScissor(layout.getX() + marginLeft, layout.getY() + marginBottom, layout.getWidth() - marginLeft - marginRight - scbw - horizontalGap, viewHeight);
        GL11.glTranslatef(0f, scrollPos, 0);
    }

    @Override
    public void renderPostChilds() {
        GL11.glTranslatef(0f, -scrollPos, 0);
        context.stopScissor();
    }
}
