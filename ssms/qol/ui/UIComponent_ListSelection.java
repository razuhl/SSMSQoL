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
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.util.Pair;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Malte Schulze
 * @param <T>
 * @param <K>
 */
public abstract class UIComponent_ListSelection<T> extends UIComponent_Base {
    protected List<T> optionsOriginalOrder;
    protected List<Pair<T,Boolean>> options;
    protected Color textColor = new Color(170.0f/255.0f, 221.0f/255.0f, 254.0f/255.0f),
            textColorMouseOver = Color.white;
    protected float[] colorBGSelected = new float[]{26.0f/255.0f, 58.0f/255.0f, 69.0f/255.0f};
    protected float[] colorScrollbarMarker = new float[]{54.0f/255.0f, 155.0f/255.0f, 197.0f/255.0f};
    protected float[] colorScrollbarMarkerMouseOver = new float[]{255.0f/255.0f, 255.0f/255.0f, 255.0f/255.0f};
    protected float[] colorIcon = new float[]{254.0f/255.0f, 209.0f/255.0f, 0.0f/255.0f};
    protected int lineHeight = 30, marginBetween = 2, marginLeft = 2, marginRight = 2, marginText = 6;
    protected int scrollPos = 0, mouseOverEntry = -1;
    protected static int scrollbarWidth = 5;
    protected UIValueHandler<List<T>> valueHandler;
    protected List<T> selection;
    protected boolean coerceEmptyToNull = true;
    protected boolean draggingScrollbar = false;
    
    protected float[] colorIconBorder1 = new float[]{0.0f/255.0f, 221.0f/255.0f, 247.0f/255.0f},
            colorIconBorder2 = new float[]{0.0f/255.0f, 82.0f/255.0f, 93.0f/255.0f},
            colorIconBorder3 = new float[]{0.0f/255.0f, 160.0f/255.0f, 180.0f/255.0f},
            colorIconFill = new float[]{0.0f/255.0f, 219.0f/255.0f, 245.0f/255.0f},
            colorIconShadow = new float[]{0.0f/255.0f, 152.0f/255.0f, 171.0f/255.0f};

    public UIComponent_ListSelection(List<T> options, List<T> selection, UIValueHandler<List<T>> valueHandler, boolean coerceEmptyToNull) {
        this(options, selection, valueHandler, coerceEmptyToNull, true);
    }
    
    public UIComponent_ListSelection(List<T> options, List<T> selection, UIValueHandler<List<T>> valueHandler, boolean coerceEmptyToNull, boolean sort) {
        if ( options == null ) options = new ArrayList<>();
        if ( selection == null ) this.selection = new ArrayList<>();
        else this.selection = new ArrayList<>(selection);
        this.optionsOriginalOrder = new ArrayList<>(options);
        this.valueHandler = valueHandler;
        this.coerceEmptyToNull = coerceEmptyToNull;
        if ( sort ) {
            Collections.sort(this.optionsOriginalOrder, new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    String l1 = getLabel(o1), l2 = getLabel(o2);
                    if ( l1 == null ) return -1;
                    return l1.compareToIgnoreCase(l2);
                }
            });
        }
        rebuildOptions();
    }
    
    protected void rebuildOptions() {
        if ( this.options == null )
            this.options = new ArrayList<>(optionsOriginalOrder.size());
        HashSet<T> selLookup = new HashSet<>(this.selection);
        int i = 0;
        for ( T option : this.selection ) {
            if ( i < this.options.size() ) {
                Pair<T,Boolean> p = this.options.get(i);
                p.one = option; p.two = true;
            } else {
                this.options.add(new Pair<>(option,true));
            }
            i++;
        }
        for ( T option : optionsOriginalOrder ) {
            if ( selLookup.contains(option) ) continue;
            if ( i < this.options.size() ) {
                Pair<T,Boolean> p = this.options.get(i);
                p.one = option; p.two = false;
            } else {
                this.options.add(new Pair<>(option,false));
            }
            i++;
        }
    }
    
    @Override
    public void render() {
        //Improving the view mode to allow ordering of the selected entries.
        //All selected entries appear at the top in the order they were selected.
        //All options that have not been selected yet appear below that in the initial order of the options.
        //Selected options have up/down marker besides them to change their ordering. Must be a rendered area otherwise clicking beside it will deselect the option.
        //A filter that filters both selected and unselected options based on their label.
        //Essentially a picklist that is rolled into one vertical list.
        //A scrollbar on the right indicating the current scroll position
        //managing an internal list with the ordered options for this component is the best option to support large lists.
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        int maxY = layout.getY() + layout.getHeight();
        GL11.glLineWidth(1f);
        GL11.glColor3f(colorBGSelected[0],colorBGSelected[1],colorBGSelected[2]);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(layout.getX() + layout.getWidth() - marginRight - 2.5f, layout.getY());
        GL11.glVertex2f(layout.getX() + layout.getWidth() - marginRight - 2.5f, layout.getY() + layout.getHeight());
        GL11.glEnd();
        
        float contentHeight = (!options.isEmpty() ? (options.size()-1) : 1)*marginBetween+options.size()*lineHeight-layout.getHeight();
        float yMarkerMax = ((contentHeight-scrollPos)/contentHeight)*(layout.getHeight()-lineHeight) + layout.getY() + lineHeight;
        float yMarkerMin = yMarkerMax - lineHeight;
        if ( mouseOverEntry == -2 ) {
            GL11.glColor3f(colorScrollbarMarkerMouseOver[0],colorScrollbarMarkerMouseOver[1],colorScrollbarMarkerMouseOver[2]);
        } else {
            GL11.glColor3f(colorScrollbarMarker[0],colorScrollbarMarker[1],colorScrollbarMarker[2]);
        }
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(layout.getX() + layout.getWidth() - marginRight - 5f, yMarkerMin);
        GL11.glVertex2f(layout.getX() + layout.getWidth() - marginRight - 5f, yMarkerMax);
        GL11.glVertex2f(layout.getX() + layout.getWidth() - marginRight, yMarkerMax);
        GL11.glVertex2f(layout.getX() + layout.getWidth() - marginRight, yMarkerMin);
        GL11.glEnd();
        
        int labelXMin = layout.getX() + marginLeft, 
                btn2XMax = layout.getX() + layout.getWidth() - marginRight - scrollbarWidth - marginBetween, btn2XMin = btn2XMax - lineHeight, btn1XMax = btn2XMin - marginBetween, btn1XMin = btn1XMax - lineHeight,
                labelXMax = btn1XMin - marginBetween;
        
        GL11.glScissor(layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int y = maxY + scrollPos;
        int entry = -1;
        for ( Pair<T,Boolean> o : options ) {
            entry++;
            y -= lineHeight + marginBetween;
            int yMax = y + lineHeight;
            if ( y > maxY ) continue;
            
            if ( o.two ) {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glLineWidth(1f);
                
                GL11.glColor3f(colorBGSelected[0],colorBGSelected[1],colorBGSelected[2]);
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f(labelXMin, y);
                GL11.glVertex2f(labelXMin, yMax);
                GL11.glVertex2f(labelXMax, yMax);
                GL11.glVertex2f(labelXMax, y);
                GL11.glEnd();

                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f(btn1XMin, y);
                GL11.glVertex2f(btn1XMin, yMax);
                GL11.glVertex2f(btn1XMax, yMax);
                GL11.glVertex2f(btn1XMax, y);
                GL11.glEnd();
                
                //iconsize 24x18 looks best
                renderUpButton(btn1XMin + (btn1XMax - btn1XMin - 24) / 2,
                        btn1XMax - (btn1XMax - btn1XMin - 24) / 2,
                        y + (yMax - y - 18) / 2,
                        yMax - (yMax - y - 18) / 2);
                
                GL11.glColor3f(colorBGSelected[0],colorBGSelected[1],colorBGSelected[2]);
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f(btn2XMin, y);
                GL11.glVertex2f(btn2XMin, yMax);
                GL11.glVertex2f(btn2XMax, yMax);
                GL11.glVertex2f(btn2XMax, y);
                GL11.glEnd();
                
                //iconsize 24x18 looks best
                renderDownButton(btn2XMin + (btn2XMax - btn2XMin - 24) / 2,
                        btn2XMax - (btn2XMax - btn2XMin - 24) / 2,
                        y + (yMax - y - 18) / 2,
                        yMax - (yMax - y - 18) / 2);
            }
            
            UIUtil.getInstance().renderText(context, getLabel(o.one), labelXMin + marginText, y, (labelXMax-labelXMin) - marginText, lineHeight, mouseOverEntry == entry);
            
            if ( y < layout.getY() ) break;
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
    
    protected void renderDownButton(int xMin, int xMax, int yMin, int yMax) {
        GL11.glColor3f(colorIconBorder1[0],colorIconBorder1[1],colorIconBorder1[2]);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(xMin + 2.5f, yMax - 2.5f);
        GL11.glVertex2f(xMax - 2.5f, yMax - 2.5f);
        GL11.glVertex2f(xMax - 2.5f, yMin + 12.5f);
        GL11.glVertex2f(xMax - 11.5f, yMin + 3.5f);
        GL11.glVertex2f(xMin + 11.5f, yMin + 3.5f);
        GL11.glVertex2f(xMin + 2.5f, yMin + 12.5f);
        GL11.glEnd();
        GL11.glColor3f(colorIconBorder2[0],colorIconBorder2[1],colorIconBorder2[2]);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(xMin + 3.5f, yMax - 3.5f);
        GL11.glVertex2f(xMax - 3.5f, yMax - 3.5f);
        GL11.glVertex2f(xMax - 3.5f, yMin + 12.5f);
        GL11.glVertex2f(xMax - 11.5f, yMin + 4.5f);
        GL11.glVertex2f(xMin + 11.5f, yMin + 4.5f);
        GL11.glVertex2f(xMin + 3.5f, yMin + 12.5f);
        GL11.glEnd();
        GL11.glColor3f(colorIconBorder3[0],colorIconBorder3[1],colorIconBorder3[2]);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(xMin + 4.5f, yMax - 4.5f);
        GL11.glVertex2f(xMax - 4.5f, yMax - 4.5f);
        GL11.glVertex2f(xMax - 4.5f, yMin + 12.5f);
        GL11.glVertex2f(xMax - 11.5f, yMin + 5.5f);
        GL11.glVertex2f(xMin + 11.5f, yMin + 5.5f);
        GL11.glVertex2f(xMin + 4.5f, yMin + 12.5f);
        GL11.glEnd();
        GL11.glColor3f(colorIconFill[0],colorIconFill[1],colorIconFill[2]);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(xMin + (xMax - xMin) / 2f, yMin + (yMax - yMin) / 2f);
        GL11.glVertex2f(xMin + 5f, yMax - 5f);
        GL11.glVertex2f(xMax - 5f, yMax - 5f);
        GL11.glVertex2f(xMax - 5f, yMin + 12f);
        GL11.glVertex2f(xMax - 11f, yMin + 6f);
        GL11.glVertex2f(xMin + 11f, yMin + 6f);
        GL11.glVertex2f(xMin + 5f, yMin + 12f);
        GL11.glVertex2f(xMin + 5f, yMax - 5f);
        GL11.glEnd();
        GL11.glColor3f(colorIconShadow[0],colorIconShadow[1],colorIconShadow[2]);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex2f(xMax - 2.5f, yMin + 11.5f);
        GL11.glVertex2f(xMax - 11.5f, yMin + 2.5f);
        GL11.glVertex2f(xMin + 11.5f, yMin + 2.5f);
        GL11.glVertex2f(xMin + 2.5f, yMin + 11.5f);
        GL11.glEnd();
    }
    
    protected void renderUpButton(int xMin, int xMax, int yMin, int yMax) {
        GL11.glColor3f(colorIconBorder1[0],colorIconBorder1[1],colorIconBorder1[2]);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(xMin + 2.5f, yMin + 2.5f);
        GL11.glVertex2f(xMax - 2.5f, yMin + 2.5f);
        GL11.glVertex2f(xMax - 2.5f, yMax - 12.5f);
        GL11.glVertex2f(xMax - 11.5f, yMax - 3.5f);
        GL11.glVertex2f(xMin + 11.5f, yMax - 3.5f);
        GL11.glVertex2f(xMin + 2.5f, yMax - 12.5f);
        GL11.glEnd();
        GL11.glColor3f(colorIconBorder2[0],colorIconBorder2[1],colorIconBorder2[2]);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(xMin + 3.5f, yMin + 3.5f);
        GL11.glVertex2f(xMax - 3.5f, yMin + 3.5f);
        GL11.glVertex2f(xMax - 3.5f, yMax - 12.5f);
        GL11.glVertex2f(xMax - 11.5f, yMax - 4.5f);
        GL11.glVertex2f(xMin + 11.5f, yMax - 4.5f);
        GL11.glVertex2f(xMin + 3.5f, yMax - 12.5f);
        GL11.glEnd();
        GL11.glColor3f(colorIconBorder3[0],colorIconBorder3[1],colorIconBorder3[2]);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(xMin + 4.5f, yMin + 4.5f);
        GL11.glVertex2f(xMax - 4.5f, yMin + 4.5f);
        GL11.glVertex2f(xMax - 4.5f, yMax - 12.5f);
        GL11.glVertex2f(xMax - 11.5f, yMax - 5.5f);
        GL11.glVertex2f(xMin + 11.5f, yMax - 5.5f);
        GL11.glVertex2f(xMin + 4.5f, yMax - 12.5f);
        GL11.glEnd();
        GL11.glColor3f(colorIconFill[0],colorIconFill[1],colorIconFill[2]);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(xMin + (xMax - xMin) / 2f, yMin + (yMax - yMin) / 2f);
        GL11.glVertex2f(xMin + 5f, yMin + 5f);
        GL11.glVertex2f(xMax - 5f, yMin + 5f);
        GL11.glVertex2f(xMax - 5f, yMax - 12f);
        GL11.glVertex2f(xMax - 11f, yMax - 6f);
        GL11.glVertex2f(xMin + 11f, yMax - 6f);
        GL11.glVertex2f(xMin + 5f, yMax - 12f);
        GL11.glVertex2f(xMin + 5f, yMin + 5f);
        GL11.glEnd();
        GL11.glColor3f(colorIconShadow[0],colorIconShadow[1],colorIconShadow[2]);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex2f(xMax - 2.5f, yMax - 11.5f);
        GL11.glVertex2f(xMax - 11.5f, yMax - 2.5f);
        GL11.glVertex2f(xMin + 11.5f, yMax - 2.5f);
        GL11.glVertex2f(xMin + 2.5f, yMax - 11.5f);
        GL11.glEnd();
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
        for  ( InputEventAPI event : events ) {
            if ( event.isConsumed() ) continue;
            if ( draggingScrollbar && event.isMouseMoveEvent() ) {
                int contentHeight = ((!options.isEmpty() ? (options.size()-1) : 1)*marginBetween+options.size()*lineHeight-layout.getHeight());
                int yContent = (int)((float)(layout.getY() + layout.getHeight() - event.getY())/layout.getHeight() * contentHeight);
                scrollTo(yContent);
            }
            if ( event.isMouseMoveEvent() || event.isMouseScrollEvent() ) {
                if ( layout.contains(event.getX(), event.getY()) ) {
                    if ( event.getX() < layout.getX() + layout.getWidth() - scrollbarWidth ) {
                        int yOffsetView = (layout.getY() + layout.getHeight()) - event.getY() + scrollPos;
                        int entry = (yOffsetView / (lineHeight+marginBetween));
                        if ( yOffsetView - entry * (lineHeight+marginBetween) <= lineHeight ) {
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
                    if ( event.getX() >= layout.getX() + layout.getWidth() - scrollbarWidth ) {
                        //mouse is over the scrollbar
                        startDragDropScrollbar();
                    } else {
                        int yOffsetView = (layout.getY() + layout.getHeight()) - event.getY() + scrollPos;
                        int entry = (yOffsetView / (lineHeight+marginBetween));
                        if ( entry >= 0 && entry < options.size() ) {
                            Pair<T,Boolean> p = options.get(entry);
                            //check which section was clicked
                            int xOff = event.getX();
                            int labelXMin = layout.getX() + marginLeft, 
                                btn2XMax = layout.getX() + layout.getWidth() - marginRight - scrollbarWidth - marginBetween, btn2XMin = btn2XMax - lineHeight, btn1XMax = btn2XMin - marginBetween, btn1XMin = btn1XMax - lineHeight,
                                labelXMax = btn1XMin - marginBetween;
                            if ( p.two && xOff >= btn1XMin && xOff <= btn1XMax ) {
                                //up
                                int indx = selection.indexOf(p.one);
                                if ( indx > 0 ) {
                                    selection.remove(p.one);
                                    selection.add(indx-1, p.one);
                                }
                            } else if ( p.two && xOff >= btn2XMin && xOff <= btn2XMax ) {
                                //down
                                int indx = selection.indexOf(p.one);
                                if ( indx >= 0 && indx < selection.size() - 1 ) {
                                    selection.remove(p.one);
                                    selection.add(indx+1, p.one);
                                }
                            } else if ( !p.two || xOff >= labelXMin && xOff <= labelXMax ) {
                                //select/deselect
                                p.two = !p.two;
                                if ( p.two ) {
                                    selection.add(p.one);
                                } else {
                                    selection.remove(p.one);
                                }
                            }
                            rebuildOptions();
                            if ( selection.isEmpty() && coerceEmptyToNull )
                                valueHandler.submitValue(context,null);
                            else
                                valueHandler.submitValue(context,selection);
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
        scrollPos = Math.min((!options.isEmpty() ? (options.size()-1) : 1)*marginBetween+options.size()*lineHeight-layout.getHeight(), scrollPos);
        scrollPos = Math.max(0, scrollPos);
        this.scrollPos = scrollPos;
    }
}
