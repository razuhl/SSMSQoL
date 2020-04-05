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
package ssms.qol.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Malte Schulze
 */
public class PropertiesContext {
    public static enum PropertiesContextEvent {
        PreActivate, PostActivate, PreDeactivate, PostDeactivate
    }
    private static volatile PropertiesContext instance;
    protected Map<Object, Object> properties = new HashMap<>();
    protected boolean active = false;
    protected Map<PropertiesContextEvent,List<Runnable>> listeners = new HashMap<>();
    private PropertiesContext() {}
    
    static public PropertiesContext getInstance() {
        PropertiesContext localInstance = PropertiesContext.instance;
        if ( localInstance == null ) {
            synchronized(PropertiesContext.class) {
                localInstance = PropertiesContext.instance;
                if ( localInstance == null ) {
                    PropertiesContext.instance = localInstance = new PropertiesContext();
                }
            }
        }
        return localInstance;
    }
    
    public boolean activate() {
        if ( !active ) {
            raiseEvent(PropertiesContextEvent.PreActivate);
            active = true;
            raiseEvent(PropertiesContextEvent.PostActivate);
            return true;
        }
        return false;
    }
    
    public boolean deactivate() {
        if ( active ) {
            raiseEvent(PropertiesContextEvent.PreDeactivate);
            properties.clear();
            active = false;
            raiseEvent(PropertiesContextEvent.PostDeactivate);
            return true;
        }
        return false;
    }
    public boolean isActive() { return active; }
    public Object getProperty(Object key) {
        if ( active ) return properties.get(key);
        return null;
    }
    public <T> T getProperty(Object key, Class<T> cls) {
        return (T)getProperty(key);
    }
    public void setProperty(Object key, Object value) {
        if ( active ) properties.put(key, value);
    }
    
    public void addListener(PropertiesContextEvent event, Runnable callback) {
        List<Runnable> lst = listeners.get(event);
        if ( lst == null ) {
            lst = new ArrayList<>();
            listeners.put(event, lst);
        }
        lst.add(callback);
    }
    public boolean removeListener(PropertiesContextEvent event, Runnable callback) {
        List<Runnable> lst = listeners.get(event);
        if ( lst != null ) {
            return lst.remove(callback);
        }
        return false;
    }
    public int removeListener(Runnable callback) {
        int removedInstances = 0;
        for ( Map.Entry<PropertiesContextEvent,List<Runnable>> e : listeners.entrySet() ) {
            while ( e.getValue().remove(callback) ) {
                removedInstances++;
            }
        }
        return removedInstances;
    }
    public void raiseEvent(PropertiesContextEvent event) {
        List<Runnable> lst = listeners.get(event);
        if ( lst != null ) {
            for ( Runnable r : lst ) {
                r.run();
            }
        }
    }
}
