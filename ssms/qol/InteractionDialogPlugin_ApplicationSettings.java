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

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import java.util.Map;
import ssms.qol.properties.PropertiesContainerConfiguration;
import ssms.qol.properties.PropertiesContainerConfigurationFactory;
import ssms.qol.properties.PropertiesContext;
import ssms.qol.ui.UIUtil;

/**
 *
 * @author Malte Schulze
 */
public class InteractionDialogPlugin_ApplicationSettings implements InteractionDialogPlugin {
    protected InteractionDialogAPI dialog;
    
    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        OptionPanelAPI options = dialog.getOptionPanel();
        TextPanelAPI text = dialog.getTextPanel();
        
        text.clear();
        text.addPara("Select mod to edit corresponding settings.");
        
        PropertiesContainerConfigurationFactory confFactory = PropertiesContainerConfigurationFactory.getInstance();
        for ( PropertiesContainerConfiguration conf : confFactory.getAllConfigurations() ) {
            if ( conf.displayInDialogId(PropertiesContainerConfiguration.CorePropertiesDialogs.Application.getId()) ) {
                options.addOption(conf.getLabel() != null ? conf.getLabel() : conf.getConfigurationId(), conf.getConfigurationId());
            }
        }
        
        options.addOption("Leave", "Leave");
        dialog.setOptionOnEscape("Leave", "Leave");
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        UIUtil.getInstance().blur();
        if ( optionData != null && String.class.isAssignableFrom(optionData.getClass()) ) {
            PropertiesContainerConfigurationFactory factory = PropertiesContainerConfigurationFactory.getInstance();
            String strData = (String)optionData;
            if ( strData.equals("Leave") ) {
                PropertiesContext.getInstance().deactivate();
                factory.MergeSettings();
                factory.save();
                dialog.dismiss();
                return;
            }
            
            PropertiesContainerConfiguration conf = factory.getPropertiesContainerConfiguration(strData);
            if ( conf != null ) {
                PropertiesContext.getInstance().deactivate();
                PropertiesContext.getInstance().activate();
                VisualPanelAPI vpAPI = dialog.getVisualPanel();
                vpAPI.showCustomPanel(800f, 600f, new CustomUIPanelPlugin_PropertiesContainer(conf, new InteractionDialog_TextPanelLogHandler(dialog)));
            }
        }
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
        
    }

    @Override
    public void advance(float amount) {
        
    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {
        
    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return null;
    }
    
}
