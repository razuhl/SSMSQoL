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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Malte Schulze
 * @param <T>
 */
public abstract class UIComponent_ListSelect<T> extends UIComponent_Base {
    protected List<T> options;
    protected Map<T,String> labels;
    protected int scrollPos = 0, mouseOverEntry = -1;
    protected UIValueHandler<T> valueHandler;
    protected boolean draggingScrollbar = false;

    public UIComponent_ListSelect(Collection<T> options, UIValueHandler<T> valueHandler, boolean sort) {
        if ( options == null ) this.options = new ArrayList<>();
        else this.options = new ArrayList<>(options);
        
        labels = new HashMap<>();
        for ( T option : this.options ) {
            labels.put(option, getLabel(option));
        }
        if ( sort ) {
            Collections.sort(this.options, new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    String l1 = labels.get(o1), l2 = labels.get(o2);
                    if ( l1 == null ) return -1;
                    return l1.compareToIgnoreCase(l2);
                }
            });
        }
        this.valueHandler = valueHandler;
    }
    
    @Override
    public void render() {
        pushStyles();
        int lineHeight = 30;
        int marginLeft = context.<Integer>getStyle(UIContext.StyleProperty.marginLeft);
        int marginRight = context.<Integer>getStyle(UIContext.StyleProperty.marginRight);
        int marginTop = context.<Integer>getStyle(UIContext.StyleProperty.marginTop);
        int marginBottom = context.<Integer>getStyle(UIContext.StyleProperty.marginBottom);
        int horizontalGap = context.<Integer>getStyle(UIContext.StyleProperty.horizontalGap);
        int verticalGap = context.<Integer>getStyle(UIContext.StyleProperty.verticalGap);
        int scbw = context.<Integer>getStyle(UIContext.StyleProperty.scrollbarWidth);
        
        float contentHeight = (!options.isEmpty() ? (options.size()-1) : 1)*verticalGap+options.size()*lineHeight-layout.getHeight();
        
        UIUtil.getInstance().renderScrollbar(context, scrollPos, layout.getX() + layout.getWidth() - marginRight - scbw, layout.getY(), layout.getHeight(), contentHeight, mouseOverEntry == -2);
        
        int labelXMin = layout.getX() + marginLeft, 
                btn2XMax = layout.getX() + layout.getWidth() - marginRight - scbw - horizontalGap, btn2XMin = btn2XMax - lineHeight, btn1XMax = btn2XMin - horizontalGap, btn1XMin = btn1XMax - lineHeight,
                labelXMax = btn1XMin - horizontalGap;
        
        context.startScissor(layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
        int maxY = layout.getY() + layout.getHeight() - marginTop, minY = layout.getY() + verticalGap + lineHeight + marginBottom;
        try {
            int y = maxY + scrollPos;
            int entry = -1;
            for ( T o : options ) {
                entry++;
                y -= lineHeight + verticalGap;
                if ( y > maxY ) continue;

                UIUtil.getInstance().renderText(context, labels.get(o), labelXMin + 6, y, (labelXMax-labelXMin) - 6, lineHeight, mouseOverEntry == entry);

                if ( y < minY ) break;
            }
        } finally {
            context.stopScissor();
        }
        popStyles();
    }
    
    protected void startDragDropScrollbar() {
        draggingScrollbar = true;
    }
    
    protected void stopDragDropScrollbar() {
        draggingScrollbar = false;
    }
    
    protected abstract String getLabel(T o);

    @Override
    public void processInput(List<InputEventAPI> events) {
        int lineHeight = 30;
        int verticalGap = context.<Integer>getStyle(UIContext.StyleProperty.verticalGap);
        int scbw = context.<Integer>getStyle(UIContext.StyleProperty.scrollbarWidth);
        
        for  ( InputEventAPI event : events ) {
            if ( event.isConsumed() ) continue;
            if ( draggingScrollbar && event.isMouseMoveEvent() ) {
                int contentHeight = ((!options.isEmpty() ? (options.size()-1) : 1)*verticalGap+options.size()*lineHeight-layout.getHeight());
                int yContent = (int)((float)(layout.getY() + layout.getHeight() - event.getY())/layout.getHeight() * (contentHeight-layout.getHeight()));
                scrollTo(yContent);
            }
            if ( event.isMouseMoveEvent() || event.isMouseScrollEvent() ) {
                if ( layout.contains(event.getX(), event.getY()) ) {
                    if ( event.getX() < layout.getX() + layout.getWidth() - scbw ) {
                        int yOffsetView = (layout.getY() + layout.getHeight()) - event.getY() + scrollPos;
                        int entry = (yOffsetView / (lineHeight+verticalGap));
                        if ( yOffsetView - entry * (lineHeight+verticalGap) <= lineHeight ) {
                            if ( entry >= 0 && entry < options.size() ) {
                                mouseOverEntry = entry;
                            } else mouseOverEntry = -1;
                        } else mouseOverEntry = -1;
                    } else mouseOverEntry = -2;
                } else {
                    mouseOverEntry = -1;
                }
            }
            if ( event.isLMBDownEvent() ) {
                if ( layout.contains(event.getX(), event.getY()) ) {
                    UIUtil.getInstance().blur();
                    if ( event.getX() >= layout.getX() + layout.getWidth() - scbw ) {
                        //mouse is over the scrollbar
                        startDragDropScrollbar();
                    } else {
                        int yOffsetView = (layout.getY() + layout.getHeight()) - event.getY() + scrollPos;
                        int entry = (yOffsetView / (lineHeight+verticalGap));
                        if ( entry >= 0 && entry < options.size() ) {
                            valueHandler.submitValue(context,options.get(entry));
                        }
                    }
                    event.consume();
                }
            } else if ( event.isMouseScrollEvent() ) {
                if ( layout.contains(event.getX(), event.getY()) ) {
                    int scroll = -100;
                    if ( event.isShiftDown() ) scroll *= 5;
                    scrollBy(event.getEventValue() < 0 ? -scroll : scroll);
                    event.consume();
                }
            } else if ( event.isLMBUpEvent() ) {
                stopDragDropScrollbar();
            }
        }
    }
    
    protected void scrollBy(int scrollBy) {
        scrollTo(scrollPos+scrollBy);
    }
    
    protected void scrollTo(int scrollPos) {
        int lineHeight = 30;
        int verticalGap = context.<Integer>getStyle(UIContext.StyleProperty.verticalGap);
        scrollPos = Math.min((!options.isEmpty() ? (options.size()-1) : 1)*verticalGap+options.size()*lineHeight-layout.getHeight()*2, scrollPos);
        scrollPos = Math.max(0, scrollPos);
        this.scrollPos = scrollPos;
    }
}
