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
import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;
import ssms.qol.events.GlobalEvents;
import ssms.qol.events.InitUIContext;

/**
 *
 * @author Malte Schulze
 */
public class UIContext {
    public static enum StyleProperty {
        font("font",Fonts.INSIGNIA_LARGE), textStyle("textStyle",TextStyle.Shadow), textColor("textColor",new Color(170.0f/255.0f, 221.0f/255.0f, 254.0f/255.0f)),
        textMouseOverColor("textMouseOverColor", Color.white), textAlignment("textAlignment",Alignment.LMID),
        checkboxGradientUnchecked("checkboxGradientUnchecked",new float[]{
            36.0f/255.0f, 22.0f/255.0f, 20.0f/255.0f,//bg
            42.0f/255.0f, 48.0f/255.0f, 49.0f/255.0f,//border 1
            132.0f/255.0f, 140.0f/255.0f, 140.0f/255.0f,//border 2
            208.0f/255.0f, 216.0f/255.0f, 216.0f/255.0f,//border 3
            51.0f/255.0f, 41.0f/255.0f, 41.0f/255.0f,//border 4
            98.0f/255.0f, 62.0f/255.0f, 54.0f/255.0f,//border 5
            36.0f/255.0f, 22.0f/255.0f, 20.0f/255.0f//border 6(bg color to hide this border line)
        }),
        checkboxGradientChecked("checkboxGradientChecked",new float[]{
            75.0f/255.0f, 254.0f/255.0f, 255.0f/255.0f,//bg
            42.0f/255.0f, 48.0f/255.0f, 49.0f/255.0f,//border 1
            132.0f/255.0f, 140.0f/255.0f, 140.0f/255.0f,//border 2
            208.0f/255.0f, 216.0f/255.0f, 216.0f/255.0f,//border 3
            74.0f/255.0f, 113.0f/255.0f, 116.0f/255.0f,//border 4
            61.0f/255.0f, 210.0f/255.0f, 239.0f/255.0f,//border 5
            30.0f/255.0f, 107.0f/255.0f, 122.0f/255.0f//border 6
        }),
        checkboxGradientNotSet("checkboxGradientNotSet",new float[]{
            208.0f/255.0f, 216.0f/255.0f, 216.0f/255.0f,//bg(border 3 color to form a solid center)
            42.0f/255.0f, 48.0f/255.0f, 49.0f/255.0f,//border 1
            132.0f/255.0f, 140.0f/255.0f, 140.0f/255.0f,//border 2
            208.0f/255.0f, 216.0f/255.0f, 216.0f/255.0f,//border 3
            208.0f/255.0f, 216.0f/255.0f, 216.0f/255.0f,//border 4(border 3 color to form a solid center)
            208.0f/255.0f, 216.0f/255.0f, 216.0f/255.0f,//border 5(border 3 color to form a solid center)
            208.0f/255.0f, 216.0f/255.0f, 216.0f/255.0f//border 6(border 3 color to form a solid center)
        }),
        buttonGradient("buttonGradient",new float[]{
            18.0f/255.0f, 44.0f/255.0f, 52.0f/255.0f,
            38.0f/255.0f, 68.0f/255.0f, 79.0f/255.0f
        }),
        scrollbarBackgroundColor("scrollbarBackgroundColor",new float[]{26.0f/255.0f, 58.0f/255.0f, 69.0f/255.0f}),
        scrollbarMarkerColor("scrollbarMarkerColor",new float[]{54.0f/255.0f, 155.0f/255.0f, 197.0f/255.0f}),
        scrollbarMarkerActiveColor("scrollbarMarkerActiveColor",new float[]{255.0f/255.0f, 255.0f/255.0f, 255.0f/255.0f}),
        scrollbarWidth("scrollbarWidth",Integer.valueOf(5)),
        marginLeft("marginLeft",Integer.valueOf(2)), marginRight("marginRight",Integer.valueOf(2)),
        marginTop("marginTop",Integer.valueOf(2)), marginBottom("marginBottom",Integer.valueOf(2)),
        horizontalGap("horizontalGap",Integer.valueOf(2)), verticalGap("verticalGap",Integer.valueOf(2)), alphaFactor("alphaFactor",Float.valueOf(1f));
        public final String id; public final Object initialValue;
        private StyleProperty(String id, Object initialValue) {
            this.id = id;
            this.initialValue = initialValue;
        }
    }
    protected Map<String,WeakReference<Object>> weakProperties;
    protected Map<String,Object> properties;
    protected Map<String,Stack<Object>> styles;
    protected IterationItem currentIteration;
    protected Stack<Rectangle> scissors = new Stack<>();
    protected Stack<int[]> translation = new Stack<>();
    protected List<Runnable> delayedActions = new ArrayList<>();
    protected Logger logger;
    protected List<LogHandler> logHandlers = new ArrayList<>(0);
    
    public UIContext() {
        logger = Global.getLogger(UIContext.class);
        styles = new HashMap<>();
        for ( StyleProperty style : StyleProperty.values() ) {
            Stack stack = new Stack(); stack.push(style.initialValue);
            styles.put(style.id, stack);
        }
        translation.push(new int[]{0,0});
        GlobalEvents.RaiseEvent(new InitUIContext(this));
    }
    
    public Object setWeakProperty(String id, Object value) {
        if ( weakProperties == null ) weakProperties = new HashMap<>();
        WeakReference<Object> wr = weakProperties.put(id, new WeakReference(value));
        return wr != null ? wr.get() : null;
    }
    public Object getWeakProperty(String id) {
        if ( weakProperties != null ) {
            WeakReference<Object> wr = weakProperties.get(id);
            return wr != null ? wr.get() : null;
        }
        return null;
    }
    public Object removeWeakProperty(String id) {
        if ( weakProperties != null ) {
            WeakReference<Object> wr = weakProperties.remove(id);
            return wr != null ? wr.get() : null;
        }
        return null;
    }
    public Object setProperty(String id, Object value) {
        if ( properties == null ) properties = new HashMap<>();
        return properties.put(id, value);
    }
    public Object getProperty(String id) {
        if ( properties != null ) {
            return properties.get(id);
        }
        return null;
    }
    public Object removeProperty(String id) {
        if ( properties != null ) {
            return properties.remove(id);
        }
        return null;
    }
    public void pushStyle(StyleProperty property, Object value) {
        pushStyle(property.id, value);
    }
    public void pushStyle(String propertyId, Object value) {
        styles.get(propertyId).push(value);
    }
    public void pushStyles(Map<String,Object> additionalStyles) {
        for ( Map.Entry<String,Object> e : additionalStyles.entrySet() ) {
            styles.get(e.getKey()).push(e.getValue());
        }
    }
    public void popStyle(StyleProperty property) {
        popStyle(property.id);
    }
    public void popStyle(String propertyId) {
        styles.get(propertyId).pop();
    }
    public void popStyles(Collection<String> additionalStyles) {
        for ( String propertyId : additionalStyles ) {
            styles.get(propertyId).pop();
        }
    }
    public <T> T getStyle(StyleProperty property) {
        return getStyle(property.id);
    }
    public <T> T getStyle(String propertyId) {
        return (T) styles.get(propertyId).peek();
    }
    protected void beginIteration(Iterator iterator) {
        currentIteration = new IterationItem(iterator,currentIteration);
    }
    protected boolean endIteration() {
        if ( currentIteration == null ) return false;
        currentIteration = currentIteration.getParentIteration();
        return true;
    }
    protected boolean advanceIteration() {
        return currentIteration.next();
    }
    public IterationItem getCurrentIteration() {
        return currentIteration;
    }
    public void startScissor(int x, int y, int width, int height) {
        Rectangle r = new Rectangle(x, y, width, height);
        if ( scissors.isEmpty() ) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        } else {
            r = r.intersection(scissors.peek(), r);
        }
        scissors.push(r);
        GL11.glScissor(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    public void stopScissor() {
        if ( !scissors.isEmpty() ) {
            scissors.pop();
            if ( scissors.isEmpty() ) {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }
        }
    }
    public void pushTranslation(int xOff, int yOff) {
        int []last = translation.peek();
        translation.push(new int[]{xOff + last[0],yOff + last[1]});
    }
    public void popTranslation() {
        translation.pop();
    }
    public SimpleEntry<Integer,Integer> getTranslatedCoordinates(int x, int y) {
        int[] off = translation.peek();
        return new SimpleEntry<>(x + off[0], y + off[1]);
    }
    public void executeDelayedActions() {
        while ( !delayedActions.isEmpty() ) {
            List<Runnable> lst = new ArrayList<>(delayedActions);
            delayedActions.clear();
            for ( Runnable r : lst ) r.run();
        }
    }
    public void addDelayedAction(Runnable runnable) {
        delayedActions.add(runnable);
    }
    
    public void log(Level level, String msg) {
        log(level, msg, null);
    }
    public void log(Level level, String msg, Throwable t) {
        logger.log(level, msg, t);
        for ( LogHandler handler : logHandlers ) {
            handler.log(level, msg, t);
        }
    }
    public UIContext addLogHandler(LogHandler handler) {
        if ( handler != null ) logHandlers.add(handler);
        return this;
    }
    public boolean removeLogHandler(LogHandler handler) {
        return logHandlers.remove(handler);
    }
    public UIContext clearLogHandlers() {
        logHandlers.clear();
        return this;
    }
}
