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

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatTaskManagerAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.combat.tasks.CombatTaskManager;
import java.util.List;

/**
 *
 * @author Malte Schulze
 */
public class EveryFrameCombatPlugin_UnlimitedCommandPoints extends BaseEveryFrameCombatPlugin {
    protected CombatEngineAPI engine;
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if ( !ModPlugin.unlimitedCommandPoints ) return;
        
        if ( engine != null && engine.getContext() != null ) {
            CombatTaskManagerAPI ctmAPI = engine.getFleetManager(FleetSide.PLAYER).getTaskManager(false);
            if ( ctmAPI != null && ctmAPI.getCommandPointsLeft() < 5 && CombatTaskManager.class.isAssignableFrom(ctmAPI.getClass()) ) {
                ((CombatTaskManager) engine.getFleetManager(FleetSide.PLAYER).getTaskManager(false)).refundCP();
            }
        }
    }
}
