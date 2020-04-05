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
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import ssms.qol.properties.PropertiesContainer;
import ssms.qol.properties.PropertiesContainerConfiguration;
import ssms.qol.properties.PropertiesContainerConfigurationFactory;
import ssms.qol.properties.PropertiesContext;
import ssms.qol.ui.UIUtil;

/**
 *
 * @author Malte Schulze
 */
public class InteractionDialogPlugin_GameSettings implements InteractionDialogPlugin {
    protected InteractionDialogAPI dialog;
    
    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        OptionPanelAPI options = dialog.getOptionPanel();
        TextPanelAPI text = dialog.getTextPanel();
        
        text.clear();
        text.addPara("Select settings to change for the current save.");
        
        PropertiesContainerConfigurationFactory confFactory = PropertiesContainerConfigurationFactory.getInstance();
        for ( PropertiesContainerConfiguration conf : confFactory.getAllConfigurations() ) {
            if ( conf.displayInDialogId(PropertiesContainerConfiguration.CorePropertiesDialogs.Game.getId()) ) {
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
                deactivateContext();
                dialog.dismiss();
                return;
            }
            
            PropertiesContainerConfiguration conf = factory.getPropertiesContainerConfiguration(strData);
            if ( conf != null ) {
                deactivateContext();
                PropertiesContext.getInstance().activate();
                PropertiesContext.getInstance().setProperty("configurationId",conf.getConfigurationId());
                VisualPanelAPI vpAPI = dialog.getVisualPanel();
                vpAPI.showCustomPanel(800f, 600f, new CustomUIPanelPlugin_PropertiesContainerTransient(conf, new InteractionDialog_TextPanelLogHandler(dialog)));
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

    private void deactivateContext() {
        Logger logger = Global.getLogger(ModPlugin.class);
        PropertiesContext context = PropertiesContext.getInstance();
        Map<String,PropertiesContainer> changedProperties = (Map<String,PropertiesContainer>) context.getProperty(context.getProperty("configurationId"));
        if ( changedProperties != null ) {
            for ( PropertiesContainer pc : changedProperties.values() ) {
                if ( pc.isValid() ) {
                    if ( !pc.set() ) {
                        logger.log(Level.FATAL, "Setting could not be applied, data might be corrupted!");
                    }
                } else logger.log(Level.WARN, "Setting was invalid, ignoring changes.");
            }
            changedProperties.clear();
        }
        context.deactivate();
    }
}
