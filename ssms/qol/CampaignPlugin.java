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

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.CoreCampaignPluginImpl;
import com.fs.starfarer.campaign.CampaignEngine;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Malte Schulze
 */
public class CampaignPlugin extends BaseCampaignPlugin {
    protected static CoreCampaignPluginImpl defaultImpl = new CoreCampaignPluginImpl();
    protected boolean injectOption = true;
    
    @Override
    public String getId() {
        return "ssms.qol.CampaignPlugin";
    }
    
    @Override
    public boolean isTransient() {
        return true;
    }

    /*@Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        if ( injectOption ) {
            try {
                injectOption = false;
                final InteractionDialogPlugin result = CampaignEngine.getInstance().getModAndPluginData().pickInteractionDialogPlugin(interactionTarget);
                if ( result == null ) return null;
                InteractionDialogPlugin newResult = new InteractionDialogPlugin() {
                    @Override
                    public void init(InteractionDialogAPI dialog) {
                        //if the option should not be last then wrapping the interactionDialogAPI and inside the option panel to gain access to the option list is necessary.
                        result.init(dialog);
                        dialog.getOptionPanel().addOption("Injected", "MyOption");
                    }

                    @Override
                    public void optionSelected(String optionText, Object optionData) {
                        result.optionSelected(optionText, optionData);
                    }

                    @Override
                    public void optionMousedOver(String optionText, Object optionData) {
                        result.optionMousedOver(optionText, optionData);
                    }

                    @Override
                    public void advance(float amount) {
                        result.advance(amount);
                    }

                    @Override
                    public void backFromEngagement(EngagementResultAPI battleResult) {
                        result.backFromEngagement(battleResult);
                    }

                    @Override
                    public Object getContext() {
                        return result.getContext();
                    }

                    @Override
                    public Map<String, MemoryAPI> getMemoryMap() {
                        return result.getMemoryMap();
                    }
                };
                return new PluginPick<>(newResult,PickPriority.HIGHEST);
            } finally {
                injectOption = true;
            }
        }
        return null;
    }*/
}
