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

import org.lwjgl.util.Rectangle;

/**
 *
 * @author Malte Schulze
 */
public class UIComponent_Row extends UIComponent_Parent {
    protected enum ChildLayout { mip,pma,ma }
    protected AlignmentVertical alignmentVertically;
    protected AlignmentHorizontal alignmentHorizontal;
    protected boolean allowVerticalGrowth, allowHorizontalGrowth;
    protected SizeConstraint widthConstraintFromChildren, heightConstraintFromChildren;
    protected int marginLeft, marginRight, marginTop, marginBottom, horizontalGap;

    public UIComponent_Row(AlignmentHorizontal alignmentHorizontal, AlignmentVertical alignmentVertically) {
        this.alignmentHorizontal = alignmentHorizontal;
        this.alignmentVertically = alignmentVertically;
        widthConstraintFromChildren = new SizeConstraint(0, 0, 0);
        heightConstraintFromChildren = new SizeConstraint(0, 0, 0);
    }
    
    public AlignmentVertical getAlignmentVertically() {
        return alignmentVertically;
    }

    public void setAlignmentVertically(AlignmentVertical alignmentVertically) {
        this.alignmentVertically = alignmentVertically;
        layoutDirty = true;
    }

    public AlignmentHorizontal getAlignmentHorizontal() {
        return alignmentHorizontal;
    }

    public void setAlignmentHorizontal(AlignmentHorizontal alignmentHorizontal) {
        this.alignmentHorizontal = alignmentHorizontal;
        layoutDirty = true;
    }
    
    public UIComponent_Row setGrowHorizontal(boolean allowHorizontalGrowth) {
        if ( this.allowHorizontalGrowth != allowHorizontalGrowth ) {
            this.allowHorizontalGrowth = allowHorizontalGrowth;
            constraintsDirty = layoutDirty = true;
        }
        return this;
    }
    
    public boolean getGrowHorizontal() {
        return this.allowHorizontalGrowth;
    }
    
    public UIComponent_Row setGrowVertical(boolean allowVerticalGrowth) {
        if ( this.allowVerticalGrowth != allowVerticalGrowth ) {
            this.allowVerticalGrowth = allowVerticalGrowth;
            constraintsDirty = layoutDirty = true;
        }
        return this;
    }
    
    public boolean getGrowVertical() {
        return this.allowVerticalGrowth;
    }
    
    @Override
    public void calculateConstraints() {
        int wmi = 0, wp = 0, wma = 0, hmi = 0, hp = 0, hma = 0;
        for ( UIComponent c : childs ) {
            SizeConstraint wc = c.getWidth();
            SizeConstraint hc = c.getHeight();
            
            wmi = addInt(wmi,wc.min);
            wp = addInt(wp,wc.preferred);
            wma = addInt(wma,wc.max);
            hmi = Math.max(hmi, hc.min);
            hp = Math.max(hp, hc.preferred);
            hma = Math.max(hma, hc.max);
        }
        widthConstraint.max = allowHorizontalGrowth ? Integer.MAX_VALUE : wma;
        widthConstraintFromChildren.max = wma;
        widthConstraintFromChildren.min = widthConstraint.min = wmi;
        widthConstraintFromChildren.preferred = widthConstraint.preferred = wp;
        
        heightConstraint.max = allowVerticalGrowth ? Integer.MAX_VALUE : Math.min(manualHeightConstraint.max, hma);
        heightConstraintFromChildren.max = hma;
        heightConstraintFromChildren.min = heightConstraint.min = Math.min(manualHeightConstraint.min, hmi);
        heightConstraintFromChildren.preferred = heightConstraint.preferred = Math.min(manualHeightConstraint.preferred, hp);
        
        if ( widthConstraint.max != Integer.MAX_VALUE ) widthConstraint.max += (childs.isEmpty() ? 0 : childs.size()-1) * horizontalGap + marginLeft + marginRight;
        if ( widthConstraint.min != Integer.MAX_VALUE ) widthConstraint.min += (childs.isEmpty() ? 0 : childs.size()-1) * horizontalGap + marginLeft + marginRight;
        if ( widthConstraint.preferred != Integer.MAX_VALUE ) widthConstraint.preferred += (childs.isEmpty() ? 0 : childs.size()-1) * horizontalGap + marginLeft + marginRight;
        if ( heightConstraint.max != Integer.MAX_VALUE ) heightConstraint.max += marginTop + marginBottom;
        if ( heightConstraint.min != Integer.MAX_VALUE ) heightConstraint.min += marginTop + marginBottom;
        if ( heightConstraint.preferred != Integer.MAX_VALUE ) heightConstraint.preferred += marginTop + marginBottom;
    }

    @Override
    public void layoutChildren() {
        int w = layout.getWidth(), h = layout.getHeight();
        int wmi, wp, wma, hmi, hma;
        wma = widthConstraintFromChildren.max;
        wmi = widthConstraintFromChildren.min;
        wp = widthConstraintFromChildren.preferred;
        hma = heightConstraintFromChildren.max;
        hmi = heightConstraintFromChildren.min;
        
        //the limits on max sizes can be lifted
        if ( w < wmi ) {
            w = wmi;
        } else if ( w > wma && !allowHorizontalGrowth ) {
            w = wma;
        }
        if ( h < hmi ) {
            h = hmi;
        } else if ( h > hma && !allowVerticalGrowth ) {
            h = hma;
        }
        
        Rectangle r = new Rectangle();
        r.setX(layout.getX() + marginLeft);
        int xOffset = 0, adjustedHeight = h - marginTop - marginBottom, adjustedY = layout.getY() + marginBottom,
                adjustedWidth = w - marginLeft - marginRight - (childs.isEmpty() ? 0 : (childs.size()-1) * horizontalGap);
        
        int surplus, surplusLeft; ChildLayout cl;
        if ( adjustedWidth >= wmi && adjustedWidth < wp ) {
            surplus = adjustedWidth - wmi;
            surplusLeft = surplus;
            cl = ChildLayout.mip;
        } else if ( adjustedWidth >= wp && adjustedWidth <= wma ) {
            surplus = adjustedWidth - wp;
            surplusLeft = surplus;
            cl = ChildLayout.pma;
        } else {
            int surplusMaximum = adjustedWidth - wma;
            //surplus per child
            surplus = childs.isEmpty() ? surplusMaximum : surplusMaximum/childs.size();
            surplusLeft = surplus/2;
            cl = ChildLayout.ma;
        }
        int i = 0;
        
        for ( UIComponent c : childs ) {
            r.setX(r.getX()+r.getWidth()+xOffset);
            if ( i == 0 ) xOffset = horizontalGap;
            SizeConstraint wc = c.getWidth();
            
            switch ( cl ) {
                case mip:
                    //if width is between minimum and preferred children are grown front to back up to their preferred size until the surplus width is used up.
                    if ( wc.preferred - wc.min < surplusLeft ) {
                        r.setWidth(wc.preferred);
                        surplusLeft -= (wc.preferred - wc.min);
                    } else {
                        r.setWidth(wc.min + surplusLeft);
                        surplusLeft = 0;
                    }
                    break;
                case pma:
                    //if width is between preferred and maximum children are grown front to back up to their maximum size until the surplus width is used up.
                    if ( wc.max - wc.preferred < surplusLeft ) {
                        r.setWidth(wc.max);
                        surplusLeft -= (wc.max - wc.preferred);
                    } else {
                        r.setWidth(wc.preferred + surplusLeft);
                        surplusLeft = 0;
                    }
                    break;
                case ma:
                    //Width exceeds maximum width of children. We layout children with their maximum width and according to the horizontal alignment. Everyone getting the same amount of surplus
                    r.setWidth(wc.max);
                    switch ( alignmentHorizontal ) {
                        case left: xOffset = surplus + horizontalGap; break;
                        case right: r.setX(r.getX() + surplus); xOffset = horizontalGap; break;
                        case middle: r.setX(r.getX() + surplusLeft); xOffset = surplus - surplusLeft + horizontalGap; break;
                    }
                    break;
            }
            
            SizeConstraint hc = c.getHeight();
            if ( adjustedHeight <= hc.max ) {
                r.setHeight(adjustedHeight);
                r.setY(adjustedY);
            } else {
                r.setHeight(hc.max);
                switch ( alignmentVertically ) {
                    case top: r.setY(adjustedY+adjustedHeight-hc.max);break;
                    case bottom: r.setY(adjustedY);break;
                    case middle: r.setY(adjustedY+(adjustedHeight-hc.max)/2);break;
                }
            }
            
            c.resize(r);
            i++;
        }
    }

    @Override
    public void cacheStyles() {
        marginLeft = context.<Integer>getStyle(UIContext.StyleProperty.marginLeft);
        marginRight = context.<Integer>getStyle(UIContext.StyleProperty.marginRight);
        marginTop = context.<Integer>getStyle(UIContext.StyleProperty.marginTop);
        marginBottom = context.<Integer>getStyle(UIContext.StyleProperty.marginBottom);
        horizontalGap = context.<Integer>getStyle(UIContext.StyleProperty.horizontalGap);
    }
}
