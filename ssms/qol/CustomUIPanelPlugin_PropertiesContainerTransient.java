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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ssms.qol.properties.PropertiesContainer;
import ssms.qol.properties.PropertiesContainerConfiguration;
import ssms.qol.properties.PropertiesContext;
import ssms.qol.ui.AlignmentHorizontal;
import ssms.qol.ui.AlignmentVertical;
import ssms.qol.ui.LogHandler;
import ssms.qol.ui.UIComponentFactory;
import ssms.qol.ui.UIComponentParentFactory;
import ssms.qol.ui.UIComponent_Column;
import ssms.qol.ui.UIComponent_ListSelect;
import ssms.qol.ui.UIContext;
import ssms.qol.ui.UIValueHandler;

/**
 *
 * @author Malte Schulze
 */
public class CustomUIPanelPlugin_PropertiesContainerTransient<K> extends CustomUIPanelPlugin_PropertiesContainer<K> {
    public CustomUIPanelPlugin_PropertiesContainerTransient(PropertiesContainerConfiguration conf, LogHandler logHandler) {
        super(conf, logHandler);
    }
    
    @Override
    public void showSelection(final PropertiesContainerConfiguration<K> conf) {
        if ( conf.getAllSourceObjects() != null && conf.getAllSourceObjects().size() == 1 ) {
            breadcrumbs.add(new Breadcrumb(createTransientInstance(conf, conf.getAllSourceObjects().iterator().next())));
            showCurrentSettings();
        } else {
            showDialog(UIComponentParentFactory.getFactory(new UIComponent_Column(AlignmentHorizontal.left, AlignmentVertical.top))
                    .addChild(UIComponentFactory.getFactory(new UIComponent_ListSelect<K>(conf.getAllSourceObjects(),new UIValueHandler<K>() {
                        @Override
                        public void acceptedValue(K value) {
                            breadcrumbs.add(new Breadcrumb(createTransientInstance(conf, value)));
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

    private PropertiesContainer<K> createTransientInstance(PropertiesContainerConfiguration<K> conf, K sourceObject) {
        Map<String,PropertiesContainer<K>> alteredInstances = (Map<String,PropertiesContainer<K>>) PropertiesContext.getInstance().getProperty(conf.getConfigurationId());
        if ( alteredInstances == null ) {
            alteredInstances = new HashMap<>();
            PropertiesContext.getInstance().setProperty(conf.getConfigurationId(), alteredInstances);
        }
        String id = conf.getIdFromSourceObject() != null ? conf.getIdFromSourceObject().get(sourceObject) : null;
        if ( alteredInstances.containsKey(id) ) {
            return alteredInstances.get(id);
        }
        PropertiesContainer<K> pc = conf.createContainer(sourceObject);
        alteredInstances.put(pc.getId(),pc);
        return pc;
    }
}
