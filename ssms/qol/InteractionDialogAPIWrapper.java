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

import com.fs.starfarer.api.campaign.CampaignEntityPickerListener;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoPickerListener;
import com.fs.starfarer.api.campaign.CommDirectoryAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomProductionPickerDelegate;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.GroundRaidTargetPickerDelegate;
import com.fs.starfarer.api.campaign.IndustryPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import java.awt.Color;
import java.util.List;
import ssms.qol.events.GlobalEvents;
import ssms.qol.events.InteractionDialogDismissed;

/**
 *
 * @author Malte Schulze
 */
public class InteractionDialogAPIWrapper implements InteractionDialogAPI {
    protected InteractionDialogAPI originalApi;
    protected VisualPanelAPIWrapper visualPanelApiWrapper;
    
    public InteractionDialogAPIWrapper(InteractionDialogAPI originalApi) {
        this.originalApi = originalApi;
    }

    @Override
    public void setTextWidth(float width) {
        originalApi.setTextWidth(width);
    }

    @Override
    public void setTextHeight(float height) {
        originalApi.setTextHeight(height);
    }

    @Override
    public void setXOffset(float xOffset) {
        originalApi.setXOffset(xOffset);
    }

    @Override
    public void setYOffset(float yOffset) {
        originalApi.setYOffset(yOffset);
    }

    @Override
    public void setPromptText(String promptText) {
        originalApi.setPromptText(promptText);
    }

    @Override
    public void hideTextPanel() {
        originalApi.hideTextPanel();
    }

    @Override
    public void showTextPanel() {
        originalApi.showTextPanel();
    }

    @Override
    public float getTextWidth() {
        return originalApi.getTextWidth();
    }

    @Override
    public float getTextHeight() {
        return originalApi.getTextHeight();
    }

    @Override
    public float getXOffset() {
        return originalApi.getXOffset();
    }

    @Override
    public float getYOffset() {
        return originalApi.getYOffset();
    }

    @Override
    public String getPromptText() {
        return originalApi.getPromptText();
    }

    @Override
    public void flickerStatic(float in, float out) {
        originalApi.flickerStatic(in, out);
    }

    @Override
    public OptionPanelAPI getOptionPanel() {
        return originalApi.getOptionPanel();
    }

    @Override
    public TextPanelAPI getTextPanel() {
        return originalApi.getTextPanel();
    }

    @Override
    public VisualPanelAPI getVisualPanel() {
        if ( visualPanelApiWrapper == null || !visualPanelApiWrapper.isWrapping(originalApi.getVisualPanel()) ) {
            visualPanelApiWrapper = new VisualPanelAPIWrapper(this,originalApi.getVisualPanel());
        }
        return visualPanelApiWrapper;
    }

    @Override
    public SectorEntityToken getInteractionTarget() {
        return originalApi.getInteractionTarget();
    }

    @Override
    public InteractionDialogPlugin getPlugin() {
        return originalApi.getPlugin();
    }

    @Override
    public void setOptionOnEscape(String text, Object optionId) {
        originalApi.setOptionOnEscape(text, optionId);
    }

    @Override
    public void startBattle(BattleCreationContext context) {
        originalApi.startBattle(context);
    }

    @Override
    public void dismiss() {
        originalApi.dismiss();
        GlobalEvents.RaiseEvent(new InteractionDialogDismissed(originalApi));
    }

    @Override
    public void dismissAsCancel() {
        originalApi.dismissAsCancel();
        GlobalEvents.RaiseEvent(new InteractionDialogDismissed(originalApi));
    }

    @Override
    public void showFleetMemberPickerDialog(String title, String okText, String cancelText, int rows, int cols, float iconSize, boolean canPickNotReady, boolean canPickMultiple, List<FleetMemberAPI> pool, FleetMemberPickerListener listener) {
        originalApi.showFleetMemberPickerDialog(title, okText, cancelText, rows, cols, iconSize, canPickNotReady, canPickMultiple, pool, listener);
    }

    @Override
    public void hideVisualPanel() {
        originalApi.hideVisualPanel();
    }

    @Override
    public void showCommDirectoryDialog(CommDirectoryAPI dir) {
        originalApi.showCommDirectoryDialog(dir);
    }

    @Override
    public void setOptionOnConfirm(String text, Object optionId) {
        originalApi.setOptionOnConfirm(text, optionId);
    }

    @Override
    public void setOpacity(float opacity) {
        originalApi.setOpacity(opacity);
    }

    @Override
    public void setBackgroundDimAmount(float backgroundDimAmount) {
        originalApi.setBackgroundDimAmount(backgroundDimAmount);
    }

    @Override
    public void setPlugin(InteractionDialogPlugin plugin) {
        originalApi.setPlugin(plugin);
    }

    @Override
    public void setInteractionTarget(SectorEntityToken interactionTarget) {
        originalApi.setInteractionTarget(interactionTarget);
    }

    @Override
    public void showFleetMemberRecoveryDialog(String title, List<FleetMemberAPI> pool, FleetMemberPickerListener listener) {
        originalApi.showFleetMemberRecoveryDialog(title, pool, listener);
    }

    @Override
    public void showCargoPickerDialog(String title, String okText, String cancelText, boolean small, float textPanelWidth, CargoAPI cargo, CargoPickerListener listener) {
        originalApi.showCargoPickerDialog(title, okText, cancelText, small, textPanelWidth, cargo, listener);
    }
    
    @Override
    public void showCargoPickerDialog(String title, String okText, String cancelText, boolean small, float textPanelWidth, float width, float height, CargoAPI cargo, CargoPickerListener listener) {
        originalApi.showCargoPickerDialog(title, okText, cancelText, small, textPanelWidth, cargo, listener);
    }

    @Override
    public void makeOptionOpenCore(String optionId, CoreUITabId tabId, CampaignUIAPI.CoreUITradeMode mode) {
        originalApi.makeOptionOpenCore(optionId, tabId, mode);
    }
    
    @Override
    public void makeOptionOpenCore(String optionId, CoreUITabId tabId, CampaignUIAPI.CoreUITradeMode mode, boolean onlyShowTargetTabShortcut) {
        originalApi.makeOptionOpenCore(optionId, tabId, mode, onlyShowTargetTabShortcut);
    }

    @Override
    public void showIndustryPicker(String title, String okText, MarketAPI market, List<Industry> industries, IndustryPickerListener listener) {
        originalApi.showIndustryPicker(title, okText, market, industries, listener);
    }

    @Override
    public void showCustomDialog(float customPanelWidth, float customPanelHeight, CustomDialogDelegate delegate) {
        originalApi.showCustomDialog(customPanelWidth, customPanelHeight, delegate);
    }

    @Override
    public void setOptionColor(Object optionId, Color color) {
        originalApi.setOptionColor(optionId, color);
    }

    @Override
    public void makeStoryOption(Object optionId, int storyPoints, float bonusXPFraction, String soundId) {
        originalApi.makeStoryOption(optionId, storyPoints, bonusXPFraction, soundId);
    }

    @Override
    public void addOptionSelectedText(Object optionId) {
        originalApi.addOptionSelectedText(optionId);
    }

    @Override
    public void addOptionSelectedText(Object optionId, boolean allowPrintingStoryOption) {
        originalApi.addOptionSelectedText(optionId, allowPrintingStoryOption);
    }

    @Override
    public void showFleetMemberRecoveryDialog(String title, List<FleetMemberAPI> pool, List<FleetMemberAPI> storyPool, FleetMemberPickerListener listener) {
        originalApi.showFleetMemberRecoveryDialog(title, pool, storyPool, listener);
    }

    @Override
    public void showGroundRaidTargetPicker(String title, String okText, MarketAPI market, List<GroundRaidObjectivePlugin> data, GroundRaidTargetPickerDelegate listener) {
        originalApi.showGroundRaidTargetPicker(title, okText, market, data, listener);
    }

    @Override
    public void showVisualPanel() {
        originalApi.showVisualPanel();
    }

    @Override
    public void showCustomProductionPicker(CustomProductionPickerDelegate delegate) {
        originalApi.showCustomProductionPicker(delegate);
    }

    @Override
    public void showCampaignEntityPicker(String title, String selectedText, String okText, FactionAPI factionForUIColors, List<SectorEntityToken> entities, CampaignEntityPickerListener listener) {
        originalApi.showCampaignEntityPicker(title, selectedText, okText, factionForUIColors, entities, listener);
    }

    @Override
    public boolean isCurrentOptionHadAConfirm() {
        return originalApi.isCurrentOptionHadAConfirm();
    }

    @Override
    public void showCustomVisualDialog(float customPanelWidth, float customPanelHeight, CustomVisualDialogDelegate delegate) {
        originalApi.showCustomVisualDialog(customPanelWidth, customPanelHeight, delegate);
    }
}
