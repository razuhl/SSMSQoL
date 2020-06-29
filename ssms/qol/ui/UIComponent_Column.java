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

import org.apache.log4j.Level;
import org.lwjgl.util.Rectangle;

/**
 *
 * @author Malte Schulze
 */
public class UIComponent_Column extends UIComponent_Parent {
    protected enum ChildLayout { mip,pma,ma }
    protected AlignmentVertical alignmentVertically;
    protected AlignmentHorizontal alignmentHorizontal;
    protected boolean allowVerticalGrowth, allowHorizontalGrowth;
    protected SizeConstraint widthConstraintFromChildren, heightConstraintFromChildren;
    protected int marginLeft, marginRight, marginTop, marginBottom, verticalGap;

    public UIComponent_Column(AlignmentHorizontal alignmentHorizontal, AlignmentVertical alignmentVertically) {
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
    
    public UIComponent_Column setGrowHorizontal(boolean allowHorizontalGrowth) {
        if ( this.allowHorizontalGrowth != allowHorizontalGrowth ) {
            this.allowHorizontalGrowth = allowHorizontalGrowth;
            constraintsDirty = layoutDirty = true;
        }
        return this;
    }
    
    public boolean getGrowHorizontal() {
        return this.allowHorizontalGrowth;
    }
    
    public UIComponent_Column setGrowVertical(boolean allowVerticalGrowth) {
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
        int gapVertical = (childs.isEmpty() ? 0 : childs.size()-1) * verticalGap + marginTop + marginBottom;
        int gapHorizontal = marginLeft + marginRight;
        int wmi = 0, wp = 0, wma = 0, hmi = gapVertical, hp = gapVertical, hma = gapVertical;
        for ( UIComponent c : childs ) {
            SizeConstraint wc = c.getWidth();
            SizeConstraint hc = c.getHeight();
            
            hmi = addInt(hmi,hc.min);
            hp = addInt(hp,hc.preferred);
            hma = addInt(hma,hc.max);
            wmi = Math.max(wmi, addInt(wc.min, gapHorizontal));
            wp = Math.max(wp, addInt(wc.preferred, gapHorizontal));
            wma = Math.max(wma, addInt(wc.max, gapHorizontal));
        }
        widthConstraint.max = allowHorizontalGrowth ? Integer.MAX_VALUE : wma;
        widthConstraintFromChildren.max = wma;
        widthConstraintFromChildren.min = widthConstraint.min = wmi;
        widthConstraintFromChildren.preferred = widthConstraint.preferred = wp;
        heightConstraint.max = allowVerticalGrowth ? Integer.MAX_VALUE : hma;
        heightConstraintFromChildren.max = hma;
        heightConstraintFromChildren.min = heightConstraint.min = hmi;
        heightConstraintFromChildren.preferred = heightConstraint.preferred = hp;
    }

    @Override
    public void layoutChildren() {
        int w = layout.getWidth(), h = layout.getHeight();
        int wmi, wp, wma, hmi, hma, hp;
        wma = widthConstraintFromChildren.max;
        wmi = widthConstraintFromChildren.min;
        wp = widthConstraintFromChildren.preferred;
        hma = heightConstraintFromChildren.max;
        hmi = heightConstraintFromChildren.min;
        hp = heightConstraintFromChildren.preferred;
        
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
        r.setY(layout.getY() + h - marginTop);
        int yOffset = 0, adjustedWidth = w - marginLeft - marginRight, adjustedX = layout.getX() + marginLeft, 
                adjustedHeight = h;
        
        int surplus, surplusLeft; ChildLayout cl;
        if ( adjustedHeight >= hmi && adjustedHeight < hp ) {
            surplus = adjustedHeight - hmi;
            surplusLeft = surplus;
            cl = ChildLayout.mip;
        } else if ( adjustedHeight >= hp && adjustedHeight <= hma ) {
            surplus = adjustedHeight - hp;
            surplusLeft = surplus;
            cl = ChildLayout.pma;
        } else {
            int surplusMaximum = adjustedHeight - hma;
            //surplus per child
            surplus = childs.isEmpty() ? surplusMaximum : surplusMaximum/childs.size();
            surplusLeft = surplus/2;
            cl = ChildLayout.ma;
        }
        int i = 0;
        
        for ( UIComponent c : childs ) {
            r.setY(r.getY()-yOffset);
            if ( i == 0 ) yOffset = verticalGap;
            SizeConstraint hc = c.getHeight();
            
            switch ( cl ) {
                case mip:
                    //if height is between minimum and preferred children are grown front to back up to their preferred size until the surplus is used up.
                    if ( hc.preferred - hc.min < surplusLeft ) {
                        r.setHeight(hc.preferred);
                        surplusLeft -= (hc.preferred - hc.min);
                    } else {
                        r.setHeight(hc.min + surplusLeft);
                        surplusLeft = 0;
                    }
                    break;
                case pma:
                    //if height is between preferred and maximum children are grown front to back up to their maximum size until the surplus is used up.
                    if ( hc.max - hc.preferred < surplusLeft ) {
                        r.setHeight(hc.max);
                        surplusLeft -= (hc.max - hc.preferred);
                    } else {
                        r.setHeight(hc.preferred + surplusLeft);
                        surplusLeft = 0;
                    }
                    break;
                case ma:
                    //Height exceeds maximum height of children. We layout children with their maximum height and according to the vertical alignment. Everyone getting the same amount of surplus
                    r.setHeight(hc.max);
                    switch ( alignmentVertically ) {
                        case bottom: r.setY(r.getY() - surplus); yOffset = verticalGap; break;
                        case top: yOffset = surplus + verticalGap; break;
                        case middle: r.setY(r.getY() - surplusLeft); yOffset = surplus - surplusLeft + verticalGap; break;
                    }
                    break;
            }
            //rendering y from top(high number) to bottom(low number)
            r.setY(r.getY()-r.getHeight());
            
            SizeConstraint wc = c.getWidth();
            if ( adjustedWidth <= wc.max ) {
                r.setWidth(adjustedWidth);
                r.setX(adjustedX);
            } else {
                r.setWidth(wc.max);
                switch ( alignmentHorizontal ) {
                    case right: r.setX(adjustedX+adjustedWidth-wc.max);break;
                    case left: r.setX(adjustedX);break;
                    case middle: r.setX(adjustedX+(adjustedWidth-wc.max)/2);break;
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
        verticalGap = context.<Integer>getStyle(UIContext.StyleProperty.verticalGap);
    }
}
