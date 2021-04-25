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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.log4j.Level;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;
import ssms.qol.ModPlugin;
import ssms.qol.properties.PropertiesContainer;
import ssms.qol.properties.PropertyValueGetter;

/**
 *
 * @author Malte Schulze
 * @param <T>
 */
public class UIComponent_ListInput<T> extends UIComponent_Base {
    protected List<T> options;
    protected int scrollPos = 0, mouseOverEntry = -1;
    protected UIValueHandler<List<T>> valueHandler;
    protected boolean addingAllowed = false, removalAllowed = false, innerNullable = false;
    protected boolean draggingScrollbar = false, layoutDirty = true;
    protected Callable<T> createNewEntry;
    protected List<UIComponent> optionsUI;
    protected Class<T> innerClass;
    protected UIValueValidator<String> validator;
    //style information for general use across the component
    protected int contentHeight = 0, viewHeight = 0, lineHeight = 30, marginLeft, marginRight, marginTop, marginBottom, 
            horizontalGap, verticalGap, scbw;

    public UIComponent_ListInput(Class<T> innerClass, List<T> options, UIValueHandler<List<T>> valueHandler, boolean addingAllowed, boolean removalAllowed, boolean innerNullable, Callable<T> createNewEntry) {
        if ( options == null ) this.options = new ArrayList<>();
        else this.options = new ArrayList<>(options);
        this.valueHandler = valueHandler;
        this.addingAllowed = addingAllowed;
        this.removalAllowed = removalAllowed;
        this.innerNullable = innerNullable;
        this.createNewEntry = createNewEntry;
        this.innerClass = innerClass;
        if ( Integer.class.isAssignableFrom(innerClass) ) {
            validator = new UIValueValidator<String>() {
                @Override
                public boolean validate(UIContext context, String value) {
                    if ( value == null ) return UIComponent_ListInput.this.innerNullable;
                    try {
                        Integer.parseInt(value);
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                    return true;
                }
            };
        } else if ( Float.class.isAssignableFrom(innerClass) ) {
            validator = new UIValueValidator<String>() {
                @Override
                public boolean validate(UIContext context, String value) {
                    if ( value == null ) return UIComponent_ListInput.this.innerNullable;
                    try {
                        Float.parseFloat(value);
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                    return true;
                }
            };
        } else { 
            validator = null; 
        }
    }

    @Override
    public void setContext(UIContext context) {
        super.setContext(context);
        if ( optionsUI != null ) {
            for ( UIComponent comp : optionsUI ) {
                comp.setContext(context);
            }
        }
    }
    
    public void onSelect(T option) {
        
    }
    
    protected void buildOptionComponents() {
        //initialize
        if ( optionsUI == null ) optionsUI = new ArrayList<>();
        //remove surplus
        boolean removed = false;
        for ( int i = optionsUI.size() - 1; i >= options.size(); i-- ) {
            UIComponent comp = optionsUI.get(i);
            if ( comp != null ) comp.dismis();
            optionsUI.remove(i);
            if ( !removed ) removed = true;
        }
        //making sure the scroll position is not exceeding the newly established maximum
        if ( removed ) scrollTo(scrollPos);
        //add missing
        for ( int i = optionsUI.size(); i < options.size(); i++ ) {
            UIComponent comp;
            if ( String.class.isAssignableFrom(innerClass) ) {
                comp = new UIComponent_TextField(new ValueGetterText(i), new ValueSetterText(i));
            } else if ( Integer.class.isAssignableFrom(innerClass) ) {
                comp = new UIComponent_TextField(new ValueGetterText(i), new ValueSetterText(i));
            } else if ( Float.class.isAssignableFrom(innerClass) ) {
                comp = new UIComponent_TextField(new ValueGetterText(i), new ValueSetterText(i));
            } else if ( Boolean.class.isAssignableFrom(innerClass) ) {
                comp = new UIComponent_Checkbox(new ValueGetterDirect<Boolean>(i), new ValueSetterDirect<Boolean>(i), innerNullable);
            } else if ( PropertiesContainer.class.isAssignableFrom(innerClass) ) {
                comp = new SelectButton(i);
            } else {
                Global.getLogger(ModPlugin.class).log(Level.ERROR, "Unsupported inner class "+innerClass+" for primitive list!");
                return;
            }
            
            if ( !Boolean.class.isAssignableFrom(innerClass) ) {
                comp = UIComponentParentFactory.getFactory(new UIComponent_Row(AlignmentHorizontal.left, AlignmentVertical.middle))
                    .addStyle(UIContext.StyleProperty.marginTop, Integer.valueOf(0))
                    .addStyle(UIContext.StyleProperty.marginBottom, Integer.valueOf(0))
                    .addChild(UIComponentFactory.getFactory(comp).setWidth(0, 0, Integer.MAX_VALUE).setHeight(0, 0, Integer.MAX_VALUE).finish())
                    .addChild(UIComponentFactory.getFactory(new UpButton(i)).setWidth(24, 24, 24).setHeight(0, 0, Integer.MAX_VALUE).finish())
                    .addChild(UIComponentFactory.getFactory(new DownButton(i)).setWidth(24, 24, 24).setHeight(0, 0, Integer.MAX_VALUE).finish())
                    .finish();
            } else {
                comp = UIComponentParentFactory.getFactory(new UIComponent_Row(AlignmentHorizontal.left, AlignmentVertical.middle))
                    .addStyle(UIContext.StyleProperty.marginTop, Integer.valueOf(0))
                    .addStyle(UIContext.StyleProperty.marginBottom, Integer.valueOf(0))
                    .addChild(UIComponentFactory.getFactory(comp).setWidth(20, 20, 20).setHeight(20, 20, 20).finish())
                    .addChild(UIComponentFactory.getFactory(new UIComponent_Spacer()).setWidth(0, Integer.MAX_VALUE, Integer.MAX_VALUE).setHeight(0, 0, 0).finish())
                    .addChild(UIComponentFactory.getFactory(new UpButton(i)).setWidth(24, 24, 24).setHeight(0, 0, Integer.MAX_VALUE).finish())
                    .addChild(UIComponentFactory.getFactory(new DownButton(i)).setWidth(24, 24, 24).setHeight(0, 0, Integer.MAX_VALUE).finish())
                    .finish();
            }
            if ( removalAllowed ) 
                ((UIComponent_Row)comp).addChild(UIComponentFactory.getFactory(new RemoveButton(i)).setWidth(24, 24, 24).setHeight(0, 0, Integer.MAX_VALUE).finish());
            comp.setContext(context);
            optionsUI.add(comp);
        }
        layoutDirty = true;
    }

    @Override
    public void dismis() {
        super.dismis();
        if ( options != null ) {
            options.clear();
            options = null;
        }
        if ( optionsUI != null ) {
            for ( UIComponent comp : optionsUI ) {
                comp.dismis();
            }
            optionsUI.clear();
            optionsUI = null;
        }
    }

    @Override
    public boolean resize(Rectangle rect) {
        try {
            return super.resize(rect);
        } finally {
            layoutDirty = true;
        }
    }
    
    protected class UpButton extends UIComponent_Button {
        int i;

        public UpButton(int i) {
            super("A");
            this.i = i;
        }
        
        @Override
        public void onClick() {
            if ( i > 0 ) {
                T option = UIComponent_ListInput.this.options.remove(i);
                UIComponent_ListInput.this.options.add(i - 1, option);
                valueHandler.submitValue(context, new ArrayList<>(options));
            }
        }
    }
    
    protected class DownButton extends UIComponent_Button {
        int i;

        public DownButton(int i) {
            super("V");
            this.i = i;
        }
        
        @Override
        public void onClick() {
            if ( i < UIComponent_ListInput.this.options.size() - 1 ) {
                T option = UIComponent_ListInput.this.options.remove(i);
                UIComponent_ListInput.this.options.add(i + 1, option);
                valueHandler.submitValue(context, new ArrayList<>(options));
            }
        }
    }
    
    protected class RemoveButton extends UIComponent_Button {
        int i;

        public RemoveButton(int i) {
            super("-");
            this.i = i;
        }
        
        @Override
        public void onClick() {
            if ( i >= 0 && i < UIComponent_ListInput.this.options.size() ) {
                UIComponent_ListInput.this.options.remove(i);
                valueHandler.submitValue(context, new ArrayList<>(options));
                buildOptionComponents();
            }
        }
    }
    
    protected class SelectButton extends UIComponent_Button implements Callable<String> {
        int i;

        public SelectButton(int i) {
            super((Callable<String>)null);
            this.i = i;
            setText(this);
        }
        
        @Override
        public void onClick() {
            onSelect(UIComponent_ListInput.this.options.get(i));
        }
        
        @Override
        public String call() throws Exception {
            PropertiesContainer con = (PropertiesContainer)UIComponent_ListInput.this.options.get(i);
            return con != null ? con.getLabel() : "unknown";
        }
    }
    
    protected class ValueGetterText implements Callable<String> {
        int i;

        public ValueGetterText(int i) {
            this.i = i;
        }
        
        @Override
        public String call() throws Exception {
            Object v = UIComponent_ListInput.this.options.get(i);
            if ( v == null ) return null;
            if ( String.class.isAssignableFrom(UIComponent_ListInput.this.innerClass) ) {
                return (String)v;
            } else if ( Integer.class.isAssignableFrom(UIComponent_ListInput.this.innerClass) ) {
                return Integer.toString((Integer)v);
            } else if ( Float.class.isAssignableFrom(UIComponent_ListInput.this.innerClass) ) {
                return Float.toString((Float)v);
            }
            return null;
        }
    }
    
    protected class ValueGetterDirect<K> implements Callable<K> {
        int i;

        public ValueGetterDirect(int i) {
            this.i = i;
        }
        
        @Override
        public K call() throws Exception {
            return (K) UIComponent_ListInput.this.options.get(i);
        }
    }
    
    protected class ValueSetterText extends UIValueHandler<String> {
        int i;

        public ValueSetterText(int i) {
            super(UIComponent_ListInput.this.validator);
            this.i = i;
        }
        
        @Override
        public void acceptedValue(String value) {
            if ( String.class.isAssignableFrom(UIComponent_ListInput.this.innerClass) ) {
                UIComponent_ListInput.this.options.set(i, (T) value);
            } else if ( Integer.class.isAssignableFrom(UIComponent_ListInput.this.innerClass) ) {
                UIComponent_ListInput.this.options.set(i, (T) (value == null ? null : Integer.valueOf(Integer.parseInt(value))));
            } else if ( Float.class.isAssignableFrom(UIComponent_ListInput.this.innerClass) ) {
                UIComponent_ListInput.this.options.set(i, (T) (value == null ? null : Float.valueOf(Float.parseFloat(value))));
            }
            valueHandler.submitValue(context, new ArrayList<>(options));
        }
    }
    
    protected class ValueSetterDirect<K> extends UIValueHandler<K> {
        int i;

        public ValueSetterDirect(int i) {
            super();
            this.i = i;
        }
        
        @Override
        public void acceptedValue(K value) {
            UIComponent_ListInput.this.options.set(i, (T) value);
            valueHandler.submitValue(context, new ArrayList<>(options));
        }
    }
    
    protected void layoutOptionComponents(int x, int yTop, int width, int lineHeight, int verticalGap) {
        int y = yTop - lineHeight;
        for ( UIComponent comp : optionsUI ) {
            comp.setWidth(width, width, width);
            comp.setHeight(lineHeight, lineHeight, lineHeight);
            comp.resize(new Rectangle(x, y, width, lineHeight));
            y -= lineHeight + verticalGap;
        }
        
        layoutDirty = false;
    }
    
    @Override
    public void render() {
        if ( optionsUI == null ) {
            buildOptionComponents();
        }
        pushStyles();
        
        lineHeight = 30;
        marginLeft = context.<Integer>getStyle(UIContext.StyleProperty.marginLeft);
        marginRight = context.<Integer>getStyle(UIContext.StyleProperty.marginRight);
        marginTop = context.<Integer>getStyle(UIContext.StyleProperty.marginTop);
        marginBottom = context.<Integer>getStyle(UIContext.StyleProperty.marginBottom);
        horizontalGap = context.<Integer>getStyle(UIContext.StyleProperty.horizontalGap);
        verticalGap = context.<Integer>getStyle(UIContext.StyleProperty.verticalGap);
        scbw = context.<Integer>getStyle(UIContext.StyleProperty.scrollbarWidth);
        
        contentHeight = (options.isEmpty() ? 0 : -verticalGap)+options.size()*(lineHeight+verticalGap);
        viewHeight = layout.getHeight() - marginTop - marginBottom - (addingAllowed ? verticalGap + lineHeight : 0);
        int labelXMin = layout.getX() + marginLeft, labelXMax;
        int yTopView = layout.getY() + layout.getHeight() - marginTop;
        int yBottomView = yTopView - viewHeight;
        
        
        labelXMax = layout.getX() + layout.getWidth() - marginRight - scbw - horizontalGap;
        
        if ( layoutDirty ) {
            layoutOptionComponents(labelXMin, yTopView, labelXMax - labelXMin, lineHeight, verticalGap);
            scrollTo(scrollPos);
        }
        
        UIUtil.getInstance().renderScrollbar(context, scrollPos, layout.getX() + layout.getWidth() - marginRight - scbw, yBottomView, viewHeight, contentHeight, mouseOverEntry == -2);
        
        context.startScissor(layout.getX(), yBottomView, layout.getWidth(), viewHeight);
        GL11.glTranslatef(0f, scrollPos, 0);
        try {
            int y = yTopView - lineHeight, yFrom = yTopView - scrollPos, yTo = yBottomView - scrollPos - lineHeight;
            for ( UIComponent component : optionsUI ) {
                if ( y <= yFrom ) {
                    component.render();
                }

                y -= lineHeight + verticalGap;
                if ( y < yTo ) break;
            }
        } finally {
            GL11.glTranslatef(0f, -scrollPos, 0);
            context.stopScissor();
        }
        
        if ( addingAllowed ) 
            UIUtil.getInstance().renderTextButton(context, "+", layout.getX() + marginLeft, layout.getY() + marginBottom, layout.getWidth() - marginRight - marginLeft, lineHeight);
        
        popStyles();
    }
    
    protected void startDragDropScrollbar() {
        draggingScrollbar = true;
    }
    
    protected void stopDragDropScrollbar() {
        draggingScrollbar = false;
    }
    
    protected void addNewEntry() {
        T entry;
        if ( createNewEntry != null ) {
            try {
                entry = createNewEntry.call();
            } catch (Exception ex) {
                Global.getLogger(ModPlugin.class).log(Level.ERROR, "Failed to create new entry for primitve list property!", ex);
                entry = null;
            }
        } else {
            entry = null;
        }
        options.add(entry);
        valueHandler.submitValue(context, new ArrayList<>(options));
        buildOptionComponents();
    }

    @Override
    public void processInput(List<InputEventAPI> events) {        
        //priority for buttons on this component before deferring to the children
        for  ( InputEventAPI event : events ) {
            if ( event.isConsumed() ) continue;
            if ( event.isLMBDownEvent() ) {
                AbstractMap.SimpleEntry<Integer,Integer> coords = context.getTranslatedCoordinates(event.getX(), event.getY());
                if ( layout.contains(coords.getKey(), coords.getValue()) ) {
                    UIUtil.getInstance().blur();
                    if ( coords.getKey() >= layout.getX() + layout.getWidth() - scbw ) {
                        //mouse is over the scrollbar
                        startDragDropScrollbar();
                        event.consume();
                    } else if ( addingAllowed && coords.getValue() <= layout.getY() + marginBottom + lineHeight ) {
                        if ( coords.getValue() > layout.getY() + marginBottom ) {
                            context.addDelayedAction(new Runnable() {
                                @Override
                                public void run() {
                                    addNewEntry();
                                    valueHandler.submitValue(context, new ArrayList<>(options));
                                }
                            });
                            event.consume();
                        }
                    }
                }
            }
        }
        
        //we have to consider translation due to scrolling
        context.pushTranslation(0,-scrollPos);
        try {
            for ( UIComponent child : optionsUI ) {
                child.processInput(events);
            }
        } finally {
            context.popTranslation();
        }
        
        for  ( InputEventAPI event : events ) {
            if ( event.isConsumed() ) continue;
            AbstractMap.SimpleEntry<Integer,Integer> coords = context.getTranslatedCoordinates(event.getX(), event.getY());
            if ( draggingScrollbar && event.isMouseMoveEvent() ) {
                if ( layout.getHeight() > 0 ) {
                    int yContent = (int)((float)(layout.getY() + layout.getHeight() - coords.getValue())/layout.getHeight() * (contentHeight-viewHeight));
                    scrollTo(yContent);
                }
            }
            if ( event.isMouseMoveEvent() || event.isMouseScrollEvent() ) {
                if ( layout.contains(coords.getKey(), coords.getValue()) ) {
                    if ( coords.getKey() < layout.getX() + layout.getWidth() - scbw ) {
                        int yOffsetView = (layout.getY() + layout.getHeight()) - coords.getValue() + scrollPos;
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
            if ( event.isMouseScrollEvent() ) {
                if ( layout.contains(coords.getKey(), coords.getValue()) ) {
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
        scrollPos = Math.min(contentHeight-viewHeight, scrollPos);
        scrollPos = Math.max(0, scrollPos);
        this.scrollPos = scrollPos;
    }
}
