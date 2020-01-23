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
import com.fs.starfarer.api.InteractionDialogImageVisual;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import ssms.qol.events.GlobalEvents;
import ssms.qol.events.SSEventPhases;
import ssms.qol.events.ShowLoot;

/**
 *
 * @author Malte Schulze
 */
public class VisualPanelAPIWrapper implements VisualPanelAPI {
    protected InteractionDialogAPI dialog;
    protected VisualPanelAPI originalAPI;

    public VisualPanelAPIWrapper(InteractionDialogAPI dialog, VisualPanelAPI originalApi) {
        this.dialog = dialog;
        this.originalAPI = originalApi;
    }
    
    public boolean isWrapping(VisualPanelAPI api) {
        return originalAPI == api;
    }

    @Override
    public void setVisualFade(float in, float out) {
        originalAPI.setVisualFade(in, out);
    }

    @Override
    public void showFleetInfo(String titleOne, CampaignFleetAPI one, String titleTwo, CampaignFleetAPI two) {
        originalAPI.showFleetInfo(titleOne, one, titleTwo, two);
    }

    @Override
    public void showFleetInfo(String titleOne, CampaignFleetAPI one, String titleTwo, CampaignFleetAPI two, FleetEncounterContextPlugin context) {
        originalAPI.showFleetInfo(titleOne, one, titleTwo, two, context);
    }

    @Override
    public void showPersonInfo(PersonAPI person) {
        originalAPI.showPersonInfo(person);
    }

    @Override
    public void showPlanetInfo(SectorEntityToken planet) {
        originalAPI.showPlanetInfo(planet);
    }

    @Override
    public void showFleetMemberInfo(FleetMemberAPI member) {
        originalAPI.showFleetMemberInfo(member);
    }

    @Override
    public void showImagePortion(String category, String id, float x, float y, float w, float h, float xOffset, float yOffset, float displayWidth, float displayHeight) {
        originalAPI.showImagePortion(category, id, x, y, w, h, xOffset, yOffset, displayWidth, displayHeight);
    }

    @Override
    public void showImagePortion(String category, String id, float w, float h, float xOffset, float yOffset, float displayWidth, float displayHeight) {
        originalAPI.showImagePortion(category, id, w, h, xOffset, yOffset, displayWidth, displayHeight);
    }

    @Override
    public void showImageVisual(InteractionDialogImageVisual visual) {
        originalAPI.showImageVisual(visual);
    }

    @Override
    public void showCustomPanel(float width, float height, CustomUIPanelPlugin plugin) {
        originalAPI.showCustomPanel(width, height, plugin);
    }

    @Override
    public void fadeVisualOut() {
        originalAPI.fadeVisualOut();
    }

    @Override
    public void showLoot(String title, CargoAPI otherCargo, boolean generatePods, final CoreInteractionListener listener) {
        GlobalEvents.RaiseEvent(new ShowLoot(VisualPanelAPIWrapper.this.dialog,SSEventPhases.prefix));
        originalAPI.showLoot(title, otherCargo, generatePods, new CoreInteractionListener() {
            @Override
            public void coreUIDismissed() {
                listener.coreUIDismissed();
                GlobalEvents.RaiseEvent(new ShowLoot(VisualPanelAPIWrapper.this.dialog,SSEventPhases.postfix));
            }
        });
    }

    @Override
    public void showLoot(String title, CargoAPI otherCargo, boolean canLeavePersonnel, boolean revealMode, boolean generatePods, final CoreInteractionListener listener) {
        GlobalEvents.RaiseEvent(new ShowLoot(VisualPanelAPIWrapper.this.dialog,SSEventPhases.prefix));
        originalAPI.showLoot(title, otherCargo, canLeavePersonnel, revealMode, generatePods, new CoreInteractionListener() {
            @Override
            public void coreUIDismissed() {
                listener.coreUIDismissed();
                GlobalEvents.RaiseEvent(new ShowLoot(VisualPanelAPIWrapper.this.dialog,SSEventPhases.postfix));
            }
        });
    }

    @Override
    public void showCore(CoreUITabId tabId, SectorEntityToken other, boolean noCost, CoreInteractionListener listener) {
        originalAPI.showCore(tabId, other, noCost, listener);
    }

    @Override
    public void showCore(CoreUITabId tabId, SectorEntityToken other, CoreInteractionListener listener) {
        originalAPI.showCore(tabId, other, listener);
    }

    @Override
    public void showCore(CoreUITabId tabId, SectorEntityToken other, CampaignUIAPI.CoreUITradeMode mode, CoreInteractionListener listener) {
        originalAPI.showCore(tabId, other, mode, listener);
    }

    @Override
    public void hideCore() {
        originalAPI.hideCore();
    }

    @Override
    public void showNewGameOptionsPanel(CharacterCreationData data) {
        originalAPI.showNewGameOptionsPanel(data);
    }

    @Override
    public void showPersonInfo(PersonAPI person, boolean minimalMode) {
        originalAPI.showPersonInfo(person, minimalMode);
    }

    @Override
    public void showPreBattleJoinInfo(String playerTitle, CampaignFleetAPI playerFleet, String titleOne, String titleTwo, FleetEncounterContextPlugin context) {
        originalAPI.showPreBattleJoinInfo(playerTitle, playerFleet, titleOne, titleTwo, context);
    }

    @Override
    public void showFleetMemberInfo(FleetMemberAPI member, boolean recoveryMode) {
        originalAPI.showFleetMemberInfo(member, recoveryMode);
    }

    @Override
    public void showFleetInfo(String titleOne, CampaignFleetAPI one, String titleTwo, CampaignFleetAPI two, FleetEncounterContextPlugin context, boolean recoveryMode) {
        originalAPI.showFleetInfo(titleOne, one, titleTwo, two, context, recoveryMode);
    }

    @Override
    public void finishFadeFast() {
        originalAPI.finishFadeFast();
    }

    @Override
    public void saveCurrentVisual() {
        originalAPI.saveCurrentVisual();
    }

    @Override
    public void restoreSavedVisual() {
        originalAPI.restoreSavedVisual();
    }

    @Override
    public void closeCoreUI() {
        originalAPI.closeCoreUI();
    }
    
}
