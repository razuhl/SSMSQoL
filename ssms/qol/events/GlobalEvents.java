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
package ssms.qol.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Malte Schulze
 */
public class GlobalEvents {
    static protected Map<Class<? extends SSEvent>,List<SSEventCallback>> listeners = new HashMap<>();
    
    static public void AddEventListener(Class<? extends SSEvent> eventClass, SSEventCallback callback) {
        List<SSEventCallback> lst = listeners.get(eventClass);
        if ( lst == null ) {
            lst = new ArrayList<>();
            listeners.put(eventClass, lst);
        }
        lst.add(callback);
    }
    
    static public boolean RaiseEvent(SSEvent event) {
        if ( event == null ) return false;
        
        Class<? extends SSEvent> currentEventClass = event.getClass();
        outer: while ( currentEventClass != null && !event.isCancelled() ) {
            List<SSEventCallback> lst = listeners.get(currentEventClass);
            if ( lst != null ) {
                for ( SSEventCallback cb : lst ) {
                    cb.callback(event);
                    if ( event.isCancelled() ) break outer;
                }
            }
            Class cls = currentEventClass.getSuperclass();
            if ( cls != null && SSEvent.class.isAssignableFrom(cls) )
                currentEventClass = cls;
            else currentEventClass = null;
        }
        
        return true;
    }
}
