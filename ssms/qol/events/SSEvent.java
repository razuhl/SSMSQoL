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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Malte Schulze
 */
public abstract class SSEvent {
    protected boolean cancelled = false;
    protected Map<String,Object> data = new HashMap<>();
    
    public Map<String,Object> getData() {
        return data;
    }
    
    public boolean raise() {
        return GlobalEvents.RaiseEvent(this);
    }
    
    public void cancel() {
        cancelled = true;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
}
