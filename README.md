# SSMSQoL

## Features

All features can be configured in the setting.json file.

* Automatically drop surplus into cargo pods after looting.
* Unlimited command points.

## FAQ

Can this be added to and removed from an ongoing save? Yes

###### DropSurplus

If cargo, personel or fuel capacity is exhausted additional loot from combat, salvage or cargo pods is automatically dropped. If other cargo pods are nearby they are merged to avoid cluttering when searching the same debries a couple of times. This speeds up the looting mechanics since the player can simply hit take all and leave.

Modders can use the new command DropSurplus in the rules to trigger the functionality. In order to make it work, the rules "sal_salvageOptionSelected" and "pods_openOption" replace their commands SalvageEntity and CargoPods with the commands SalvageEntityWrapper and CargoPodsWrapper. If another mod overrides the rules they will have to be adjusted to regain the functionality.

```json
dropSurplus: {
	commoditiesToDrop: ["metals","ore","food","organics","domestic_goods","rare_ore","rare_metals","volatiles","heavy_machinery","supplies"],
	personelToDrop: ["marines","crew"],
	fuelToDrop: ["fuel"],
	mergeCargoPods: true,
	mergeCargoPodsRadius: 300
}
```

* commoditiesToDrop, personelToDrop, fuelToDrop
  * The list contains the ids for items that will be dropped if the respective storage is full. Items with the first id will be dropped first before the second id is considered. A list of available ids can be found in the games file "starsector-core/data/campaign/commodities.csv". Modded items should appear in the respective mods file structure at "data/campaign/commodities.csv".
* mergeCargoPods
  * Whether existing cargo pods should be filled instead of creating new ones. This only happens if the cargo pods are newly made(within one day) and if they have not been stabilized.
* mergeCargoPodsRadius
  * The maximum range for a cargo pod to be away from the ship to be eligible for merging. The measure is the same as sensor range.
  
###### DropSurplus

Ensures that the player has at least 5 command points at any time.

```json
unlimitedCommandPoints: true
```

* unlimitedCommandPoints
  * Whether or not this feature is active.
  
