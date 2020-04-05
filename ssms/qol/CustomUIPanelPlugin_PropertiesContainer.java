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
package ssms.qol;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.ui.impl.StandardTooltipV2;
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable;
import com.fs.starfarer.util.A.B;
import com.fs.starfarer.util.A.C;
import java.awt.Color;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import org.apache.log4j.Level;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;
import ssms.qol.properties.PropertiesContainer;
import ssms.qol.properties.PropertiesContainerConfiguration;
import ssms.qol.properties.PropertiesContainerConfigurationFactory;
import ssms.qol.properties.PropertyConfiguration;
import ssms.qol.properties.PropertyConfigurationListContainer;
import ssms.qol.properties.PropertyConfigurationListPrimitive;
import ssms.qol.properties.PropertyConfigurationListSelectable;
import ssms.qol.properties.PropertyConfigurationSelectable;
import ssms.qol.properties.PropertyField;
import ssms.qol.ui.AlignmentHorizontal;
import ssms.qol.ui.AlignmentVertical;
import ssms.qol.ui.LogHandler;
import ssms.qol.ui.UIComponent;
import ssms.qol.ui.UIComponentFactory;
import ssms.qol.ui.UIComponentParentFactory;
import ssms.qol.ui.UIComponent_Button;
import ssms.qol.ui.UIComponent_Checkbox;
import ssms.qol.ui.UIComponent_Column;
import ssms.qol.ui.UIComponent_Label;
import ssms.qol.ui.UIComponent_ListInput;
import ssms.qol.ui.UIComponent_ListSelect;
import ssms.qol.ui.UIComponent_ListSelection;
import ssms.qol.ui.UIComponent_Parent;
import ssms.qol.ui.UIComponent_Row;
import ssms.qol.ui.UIComponent_Scrollpane;
import ssms.qol.ui.UIComponent_Spacer;
import ssms.qol.ui.UIComponent_Sprite;
import ssms.qol.ui.UIComponent_TextField;
import ssms.qol.ui.UIContext;
import ssms.qol.ui.UIUtil;
import ssms.qol.ui.UIValueHandler;
import ssms.qol.ui.UIValueHandler_Property;
import ssms.qol.ui.UIValueHandler_PropertyTextField;

/**
 *
 * @author Malte Schulze
 */
public class CustomUIPanelPlugin_PropertiesContainer<K> implements CustomUIPanelPlugin {
    protected PositionAPI panelPosition;
    protected UIComponent_Parent root;
    protected Stack<Breadcrumb> breadcrumbs = new Stack<>();
    protected LogHandler logHandler;
    protected static class Breadcrumb {
        PropertyConfiguration propertyConfiguration;
        PropertyField propertyField;
        PropertiesContainer container;

        public Breadcrumb(PropertyConfiguration configuration, PropertyField field) {
            this.propertyConfiguration = configuration;
            this.propertyField = field;
        }
        
        public Breadcrumb(PropertiesContainer container) {
            this.container = container;
        }
    }

    public CustomUIPanelPlugin_PropertiesContainer(PropertiesContainer<K> propertiesContainer, LogHandler logHandler) {
        this.logHandler = logHandler;
        breadcrumbs.add(new Breadcrumb(propertiesContainer));
        showCurrentSettings();
    }
    
    public CustomUIPanelPlugin_PropertiesContainer(PropertiesContainerConfiguration<K> conf, LogHandler logHandler) {
        this.logHandler = logHandler;
        showSelection(conf);
    }
    
    public void showDialog(UIComponent_Parent newRoot) {
        UIUtil.getInstance().blur();
        UIUtil.getInstance().removeTooltip();
        if ( root != null ) root.dismis();
        root = newRoot;
        if ( panelPosition != null )
            root.resize(new Rectangle((int)panelPosition.getX() + 10, (int)panelPosition.getY() + 10, (int)panelPosition.getWidth() - 20, (int)panelPosition.getHeight() - 20));
    }
    
    public void showSelection(final PropertiesContainerConfiguration<K> conf) {
        PropertiesContainerConfigurationFactory factory = PropertiesContainerConfigurationFactory.getInstance();
        if ( conf.getAllSourceObjects() != null && conf.getAllSourceObjects().size() == 1 ) {
            breadcrumbs.add(new Breadcrumb(factory.createManagedInstanceFromSource(conf, conf.getAllSourceObjects().iterator().next())));
            showCurrentSettings();
        } else {
            showDialog(UIComponentParentFactory.getFactory(new UIComponent_Column(AlignmentHorizontal.left, AlignmentVertical.top))
                    .addChild(UIComponentFactory.getFactory(new UIComponent_ListSelect<K>(conf.getAllSourceObjects(),new UIValueHandler<K>() {
                        @Override
                        public void acceptedValue(K value) {
                            PropertiesContainerConfigurationFactory factory = PropertiesContainerConfigurationFactory.getInstance();
                            breadcrumbs.add(new Breadcrumb(factory.createManagedInstanceFromSource(conf, value)));
                            showCurrentSettings();
                        }
                    }, true) {
                        @Override
                        protected String getLabel(K o) {
                            return conf.getLabelFromSourceObject().get(o);
                        }
                    })
                    .setWidth(200, 400, Integer.MAX_VALUE)
                    .setHeight(300, Integer.MAX_VALUE, Integer.MAX_VALUE)
                        .finish()).setContext(new UIContext().addLogHandler(logHandler)).finish());
        }
    }
    
    public void showCurrentSettings() {
        UIUtil.getInstance().blur();
        UIUtil.getInstance().removeTooltip();
        if ( root != null ) root.dismis();
        Breadcrumb bc = breadcrumbs.peek();
        if ( bc.container != null ) {
            root = buildContainerPage(bc.container);
        } else {
            root = buildPropertyPage(bc.propertyConfiguration,bc.propertyField);
        }
        if ( panelPosition != null )
            root.resize(new Rectangle((int)panelPosition.getX() + 10, (int)panelPosition.getY() + 10, (int)panelPosition.getWidth() - 20, (int)panelPosition.getHeight() - 20));
    }
    
    public void showPreviousSettings() {
        if ( breadcrumbs.size() > 1 ) {
            breadcrumbs.pop();
            showCurrentSettings();
        }
    }
    
    protected <T> UIComponent_Parent buildPropertyPage(PropertyConfiguration pc, final PropertyField pf) {
        UIComponent_Parent root = UIComponentParentFactory.getFactory(new UIComponent_Column(AlignmentHorizontal.left, AlignmentVertical.top))
                .finish();
        root.addChild(UIComponentParentFactory.getFactory(new UIComponent_Row(AlignmentHorizontal.left, AlignmentVertical.middle))
            .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Back") {
                    @Override
                    public void onClick() {
                        showPreviousSettings();
                    }
                })
                .setWidth(100, Integer.MAX_VALUE, Integer.MAX_VALUE)
                .setHeight(30, 30, 30)
                .finish())
            .setHeight(30, 30, 30)
            .finish());
        
        if ( PropertyConfigurationListSelectable.class.isAssignableFrom(pc.getClass()) ) {
            final PropertyConfigurationListSelectable pl = (PropertyConfigurationListSelectable) pc;
            root.addChild(UIComponentFactory.getFactory(new UIComponent_ListSelection<Object>(pl.buildOptions(),(List)pf.get(),new UIValueHandler<List<Object>>() {
                        @Override
                        public void acceptedValue(List<Object> value) {
                            pf.set(value);
                        }
                    }, pc.isNullable()) {
                        @Override
                        protected String getLabel(Object o) {
                            return pl.getOptionLabel(o);
                        }
                    })
                    .setWidth(200, Integer.MAX_VALUE, Integer.MAX_VALUE)
                    .setHeight(300, Integer.MAX_VALUE, Integer.MAX_VALUE)
                    .finish());
        } else if ( PropertyConfigurationListContainer.class.isAssignableFrom(pc.getClass()) ) {
            final PropertyConfigurationListContainer pp = (PropertyConfigurationListContainer)pc;
            root.addChild(UIComponentFactory.getFactory(new UIComponent_ListInput<PropertiesContainer>(PropertiesContainer.class,(List)pf.get(),new UIValueHandler<List<PropertiesContainer>>() {
                        @Override
                        public void acceptedValue(List<PropertiesContainer> value) {
                            pf.set(value);
                        }
                    }, pp.isAddingAllowed(), pp.isRemovalAllowed(), false, new Callable<PropertiesContainer>() {
                        @Override
                        public PropertiesContainer call() throws Exception {
                            return pp.createNewEntry();
                        }
                    }) {
                        @Override
                        public void onSelect(PropertiesContainer option) {
                            if ( option != null ) {
                                breadcrumbs.push(new Breadcrumb(option));
                                showCurrentSettings();
                            }
                        }
                    })
                    .setWidth(200, Integer.MAX_VALUE, Integer.MAX_VALUE)
                    .setHeight(300, Integer.MAX_VALUE, Integer.MAX_VALUE)
                    .finish());
        } else if ( PropertyConfigurationListPrimitive.class.isAssignableFrom(pc.getClass()) ) {
            final PropertyConfigurationListPrimitive pp = (PropertyConfigurationListPrimitive)pc;
            root.addChild(UIComponentFactory.getFactory(new UIComponent_ListInput<Object>(pp.getInnerType(),(List)pf.get(),new UIValueHandler<List<Object>>() {
                        @Override
                        public void acceptedValue(List<Object> value) {
                            pf.set(value);
                        }
                    }, pp.isAddingAllowed(), pp.isRemovalAllowed(), pp.isInnerNullable(), new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            return pp.createNewEntry();
                        }
                    }))
                    .setWidth(200, Integer.MAX_VALUE, Integer.MAX_VALUE)
                    .setHeight(300, Integer.MAX_VALUE, Integer.MAX_VALUE)
                    .finish());
        } else if ( PropertyConfigurationSelectable.class.isAssignableFrom(pc.getClass()) ) {
            final PropertyConfigurationSelectable ps = (PropertyConfigurationSelectable)pc;
            root.addChild(UIComponentFactory.getFactory(new UIComponent_ListSelect<Object>(ps.buildOptions(),new UIValueHandler<Object>() {
                        @Override
                        public void acceptedValue(Object value) {
                            pf.set(value);
                            showCurrentSettings();
                        }
                    }, true) {
                        @Override
                        protected String getLabel(Object o) {
                            return ps.getOptionLabel(o);
                        }
                    })
                    .setWidth(200, Integer.MAX_VALUE, Integer.MAX_VALUE)
                    .setHeight(300, Integer.MAX_VALUE, Integer.MAX_VALUE)
                    .finish());
        }
        root.setContext(new UIContext().addLogHandler(logHandler));
        return root;
    }
    
    protected <T> UIComponent_Parent buildContainerPage(final PropertiesContainer<T> pc) {
        UIComponent_Parent root = UIComponentParentFactory.getFactory(new UIComponent_Column(AlignmentHorizontal.left, AlignmentVertical.top))
                .finish();
        if ( breadcrumbs.size() > 1 ) {
            root.addChild(UIComponentParentFactory.getFactory(new UIComponent_Row(AlignmentHorizontal.left, AlignmentVertical.middle))
                    .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Back") {
                            @Override
                            public void onClick() {
                                showPreviousSettings();
                            }
                        })
                        .setWidth(100, Integer.MAX_VALUE, Integer.MAX_VALUE)
                        .setHeight(30, 30, 30)
                        .finish())
                    .finish());
        } else {
            if ( pc.getConf().getAllSourceObjects() != null && pc.getConf().getAllSourceObjects().size() > 1 ) {
                root.addChild(UIComponentParentFactory.getFactory(new UIComponent_Row(AlignmentHorizontal.left, AlignmentVertical.middle))
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Back") {
                                @Override
                                public void onClick() {
                                    breadcrumbs.pop();
                                    showSelection((PropertiesContainerConfiguration<K>)pc.getConf());
                                }
                            })
                            .setWidth(100, Integer.MAX_VALUE, Integer.MAX_VALUE)
                            .setHeight(30, 30, 30)
                            .finish())
                        .finish());
            }
        }
        for ( final PropertyConfiguration prop : pc.getConf().getProperties() ) {
            if ( prop.isList() ) {
                root.addChild(UIComponentParentFactory.getFactory(new UIComponent_Row(AlignmentHorizontal.left, AlignmentVertical.middle))
                    .addChild(UIComponentFactory.getFactory(new UIComponent_Button(prop.getLabel()) {
                            @Override
                            public void onClick() {
                                breadcrumbs.add(new Breadcrumb(prop,pc.getField(prop.getId())));
                                showCurrentSettings();
                            }
                        }.setTextTooltip(prop.getTooltip()))
                        .setWidth(200, Integer.MAX_VALUE, Integer.MAX_VALUE)
                        .setHeight(30, 30, 30)
                        .finish())
                    .finish());
            } else if ( prop.isPropertiesContainer() && pc.getFieldValue(prop.getId(), PropertiesContainer.class) != null ) {
                root.addChild(UIComponentParentFactory.getFactory(new UIComponent_Row(AlignmentHorizontal.left, AlignmentVertical.middle))
                    .addChild(UIComponentFactory.getFactory(new UIComponent_Button(prop.getLabel()) {
                            @Override
                            public void onClick() {
                                PropertiesContainer p = pc.getFieldValue(prop.getId(), PropertiesContainer.class);
                                if ( p != null ) {
                                    breadcrumbs.push(new Breadcrumb(p));
                                    showCurrentSettings();
                                }
                            }
                        }.setTextTooltip(prop.getTooltip()))
                        .setWidth(200, Integer.MAX_VALUE, Integer.MAX_VALUE)
                        .setHeight(30, 30, 30)
                        .finish())
                    .finish());
            } else if ( prop.isSelectable() ) {
                final PropertyConfigurationSelectable ps = (PropertyConfigurationSelectable)prop;
                root.addChild(UIComponentParentFactory.getFactory(new UIComponent_Row(AlignmentHorizontal.left, AlignmentVertical.middle))
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Label(prop.getLabel()).setTextTooltip(prop.getTooltip()))
                            .setWidth(100, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button(ps.getOptionLabel(pc.getField(prop.getId()).get())) {
                            @Override
                            public void onClick() {
                                breadcrumbs.push(new Breadcrumb(prop, pc.getField(prop.getId())));
                                showCurrentSettings();
                            }
                        })
                        .setWidth(100, Integer.MAX_VALUE, Integer.MAX_VALUE)
                        .setHeight(30, 30, 30)
                        .finish())
                    .finish());
            } else if ( prop.getValueClass() == String.class ) {
                root.addChild(UIComponentParentFactory.getFactory(new UIComponent_Row(AlignmentHorizontal.left, AlignmentVertical.middle))
                    .addChild(UIComponentFactory.getFactory(new UIComponent_Label(prop.getLabel()).setTextTooltip(prop.getTooltip()))
                        .setWidth(100, 200, 200)
                        .setHeight(30, 30, 30)
                        .finish())
                    .addChild(UIComponentFactory.getFactory(new UIComponent_TextField(pc.getFieldValue(prop.getId(), String.class),
                        new UIValueHandler_PropertyTextField(prop) {
                            @Override
                            public void acceptedValue(String value) {
                                pc.setFieldValue(prop.getId(), value);
                            }
                        }))
                        .setWidth(100, Integer.MAX_VALUE, Integer.MAX_VALUE)
                        .setHeight(30, 30, 30)
                        .finish())
                    .finish());
            } else if ( prop.getValueClass() == Float.class ) {
                root.addChild(UIComponentParentFactory.getFactory(new UIComponent_Row(AlignmentHorizontal.left, AlignmentVertical.middle))
                    .addChild(UIComponentFactory.getFactory(new UIComponent_Label(prop.getLabel()).setTextTooltip(prop.getTooltip()))
                        .setWidth(100, 200, 200)
                        .setHeight(30, 30, 30)
                        .finish())
                    .addChild(UIComponentFactory.getFactory(new UIComponent_TextField(pc.getFieldValue(prop.getId(), Float.class),
                        new UIValueHandler_PropertyTextField(prop) {
                            @Override
                            public void acceptedValue(String value) {
                                pc.setFieldValue(prop.getId(), value != null ? Float.parseFloat(value) : null);
                            }
                        }))
                        .setWidth(100, Integer.MAX_VALUE, Integer.MAX_VALUE)
                        .setHeight(30, 30, 30)
                        .finish())
                    .finish());
            } else if ( prop.getValueClass() == Integer.class ) {
                root.addChild(UIComponentParentFactory.getFactory(new UIComponent_Row(AlignmentHorizontal.left, AlignmentVertical.middle))
                    .addChild(UIComponentFactory.getFactory(new UIComponent_Label(prop.getLabel()).setTextTooltip(prop.getTooltip()))
                        .setWidth(100, 200, 200)
                        .setHeight(30, 30, 30)
                        .finish())
                    .addChild(UIComponentFactory.getFactory(new UIComponent_TextField(pc.getFieldValue(prop.getId(), Integer.class),
                        new UIValueHandler_PropertyTextField(prop) {
                            @Override
                            public void acceptedValue(String value) {
                                pc.setFieldValue(prop.getId(), value != null ? Integer.parseInt(value) : null);
                            }
                        }))
                        .setWidth(100, Integer.MAX_VALUE, Integer.MAX_VALUE)
                        .setHeight(30, 30, 30)
                        .finish())
                    .finish());
            } else if ( prop.getValueClass() == Boolean.class ) {
                root.addChild(UIComponentParentFactory.getFactory(new UIComponent_Row(AlignmentHorizontal.left, AlignmentVertical.middle))
                    .addChild(UIComponentFactory.getFactory(new UIComponent_Label(prop.getLabel()).setTextTooltip(prop.getTooltip()))
                        .setWidth(100, 200, 200)
                        .setHeight(30, 30, 30)
                        .finish())
                    .addChild(UIComponentFactory.getFactory(new UIComponent_Checkbox(pc.getFieldValue(prop.getId(), Boolean.class),
                        new UIValueHandler_Property<Boolean>(prop) {
                            @Override
                            public void acceptedValue(Boolean value) {
                                pc.setFieldValue(prop.getId(), value);
                            }
                        }, prop.isNullable()))
                        .setWidth(20, 20, 20)
                        .setHeight(20, 20, 20)
                        .finish())
                    .addChild(UIComponentFactory.getFactory(new UIComponent_Spacer())
                        .setWidth(0, Integer.MAX_VALUE, Integer.MAX_VALUE)
                        .setHeight(30, 30, 30)
                        .finish())
                    .finish());
            } else {
                continue;
            }
            
            //add configuration checkbox
            UIComponent uiLastPropertyRow = root.getChilds().get(root.getChilds().size()-1);
            if ( pc.getUsesConfigurationState() && UIComponent_Parent.class.isAssignableFrom(uiLastPropertyRow.getClass()) ) {
                ((UIComponent_Parent)uiLastPropertyRow).addChild(UIComponentFactory.getFactory(new UIComponent_Checkbox(new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                return pc.getField(prop.getId()).isConfigured();
                            }
                        },
                        new UIValueHandler<Boolean>() {
                            @Override
                            public void acceptedValue(Boolean value) {
                                if ( value ) {
                                    pc.markFieldAsConfigured(prop.getId(),true,false);
                                } else {
                                    pc.markFieldAsNotConfigured(prop.getId(),false);
                                }
                            }
                        }))
                        .setWidth(20, 20, 20)
                        .setHeight(30, 30, 30).finish());
            }
        }
        return UIComponentParentFactory.getFactory(new UIComponent_Scrollpane()).addChild(root).setContext(new UIContext().addLogHandler(logHandler)).finish();
    }

    @Override
    public void positionChanged(PositionAPI position) {
        this.panelPosition = position;
        root.resize(new Rectangle((int)position.getX() + 10, (int)position.getY() + 10, (int)position.getWidth() - 20, (int)position.getHeight() - 20));
    }
    
    @Override
    public void render(float alphaMult) {
        float xMin = panelPosition.getX(), yMin = panelPosition.getY(), xMax = panelPosition.getX() + panelPosition.getWidth(), yMax = panelPosition.getY() + panelPosition.getHeight();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        Color color = Color.black;
        GL11.glColor4ub((byte)color.getRed(),
            (byte)color.getGreen(),
            (byte)color.getBlue(),
            (byte)(color.getAlpha() * alphaMult * 0.25f));
        //Background for the panel
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(xMin, yMin);
        GL11.glVertex2f(xMin, yMax);
        GL11.glVertex2f(xMax, yMax);
        GL11.glVertex2f(xMax, yMin);
        GL11.glEnd();
        
        if ( root.isLayoutDirty() ) {
            root.pack();
        }
        root.getContext().pushStyle(UIContext.StyleProperty.alphaFactor, alphaMult);
        root.render();
        root.getContext().popStyle(UIContext.StyleProperty.alphaFactor);
        StandardTooltipV2Expandable tooltip = UIUtil.getInstance().getTooltip();
        if ( tooltip != null ) {
            UIUtil.getInstance().positionTooltipOnMouse(tooltip);
            tooltip.render(alphaMult);
        }
    }

    @Override
    public void advance(float amount) {
        root.advance(amount);
        StandardTooltipV2Expandable tooltip = UIUtil.getInstance().getTooltip();
        if ( tooltip != null ) tooltip.advance(amount);
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        if ( root.getContext() != null ) {
            root.processInput(events);
            root.getContext().executeDelayedActions();
        }
        
        StandardTooltipV2Expandable tooltip = UIUtil.getInstance().getTooltip();
        if ( tooltip != null ) {
            B evs = new B();
            for ( InputEventAPI ev : events ) evs.add((C)ev);
            tooltip.processInput(evs);
        }
    }
}
