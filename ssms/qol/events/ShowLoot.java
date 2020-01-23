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
package ssms.qol.events;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;

/**
 *
 * @author Malte Schulze
 */
public class ShowLoot extends SSEvent {
    public ShowLoot(InteractionDialogAPI dialog, SSEventPhases phase) {
        super.data.put("dialog",dialog);
        super.data.put("phase",phase);
    }
}
