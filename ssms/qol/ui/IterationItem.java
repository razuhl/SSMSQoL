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

import java.util.Iterator;

/**
 *
 * @author Malte Schulze
 */
public class IterationItem {
    
    protected IterationItem parentIteration;
    protected int index;
    protected Object element;
    protected Iterator iterator;

    public IterationItem(Iterator iterator, IterationItem parentItem) {
        this(iterator, parentItem, -1);
    }

    public IterationItem(Iterator iterator, IterationItem parentIteration, int index) {
        this.iterator = iterator;
        this.parentIteration = parentIteration;
        this.index = index;
    }

    public boolean next() {
        if (iterator.hasNext()) {
            element = iterator.next();
            index++;
            return true;
        } else {
            element = null;
            index = -1;
            return false;
        }
    }

    public int getIndex() {
        return index;
    }

    public Object getElement() {
        return element;
    }

    public IterationItem getParentIteration() {
        return parentIteration;
    }
    
}
