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
 * @param <T>
 */
public abstract class UIValueHandler<T> {
    protected UIValueValidator<T> validator;
    public UIValueHandler() {
        this(null);
    }
    public UIValueHandler(UIValueValidator<T> validator) {
        this.validator = validator;
    }
    public UIValueHandler<T> setValidator(UIValueValidator<T> validator) {
        this.validator = validator;
        return this;
    }
    public UIValueValidator<T> getValidator() {
        return validator;
    }
    public boolean submitValue(UIContext context, final T value) {
        if ( validate(context, value) ) {
            context.addDelayedAction(new Runnable() {
                @Override
                public void run() {
                    acceptedValue(value);
                }
            });
            return true;
        }
        return false;
    }
    public abstract void acceptedValue(T value);
    public boolean validate(UIContext context, T value) {
        if ( validator != null ) return validator.validate(context, value);
        return true;
    }
}
