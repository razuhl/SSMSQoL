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

/**
 *
 * @author Malte Schulze
 */
public class SizeConstraint {
    protected int min, preferred, max;

    public SizeConstraint(int min, int preferred, int max) {
        this.min = min;
        this.preferred = preferred;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getPreferred() {
        return preferred;
    }

    public int getMax() {
        return max;
    }
    
    @Override
    public String toString() {
        return new StringBuilder().append("(").append(min).append(",").append(preferred).append(",").append(max).append(")").toString();
    }
}
