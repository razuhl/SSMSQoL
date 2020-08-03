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
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.List;
import org.apache.log4j.Level;
import ssms.qol.properties.PropertiesContainerConfigurationFactory;

/**
 *
 * @author Malte Schulze
 */
public class EveryFrameCombatPlugin_PostOnApplicationLoadPhase extends BaseEveryFrameCombatPlugin {
    protected CombatEngineAPI engine;
    static protected boolean initialized = false;
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if ( !initialized ) {
            Global.getLogger(ModPlugin.class).log(Level.INFO, "loading settings");
            initialized = true;
            PropertiesContainerConfigurationFactory fac = PropertiesContainerConfigurationFactory.getInstance();
            fac.load();
            fac.MergeSettings();
        }
        
        if ( engine != null ) engine.removePlugin(this);
    }
}
