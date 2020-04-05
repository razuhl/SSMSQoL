# SSMSQoL

## Features

* Automatically drop surplus into cargo pods after looting.
* Unlimited command points.
* Mod menu for managing mod- and game-settings.
* UI components.

The mod menu is opened by default with F12 and allows changing all settings of this and other compatible mods.

The keys for openening the mod menu and the game menu can be configured in the setting.json file. Available key codes are listed at http://legacy.lwjgl.org/javadoc/constant-values.html in section "org.lwjgl.input.Keyboard". 

## FAQ

Can this be added to and removed from an ongoing save? Yes



###### Drop Surplus

If cargo, personel or fuel capacity is exhausted additional loot from combat, salvage or cargo pods is automatically dropped. If other cargo pods are nearby they are merged to avoid cluttering when searching the same debries a couple of times. This speeds up the looting mechanics since the player can simply hit take all and leave.

Modders can use the new command DropSurplus in the rules to trigger the functionality. In order to make it work, the rules "sal_salvageOptionSelected" and "pods_openOption" replace their commands SalvageEntity and CargoPods with the commands SalvageEntityWrapper and CargoPodsWrapper. If another mod overrides the rules they will have to be adjusted to regain the functionality.
  
###### Unlimited command points

Ensures that the player has at least 5 command points at any time.

###### Mod Menu

The mod menu provides a custom UI to edit settings in-game instead of editing configuration files. The settings are automatically saved into the file "[Starsector]/saves/common/SSMSQoL.json.data" and apply across all save games.

###### Game Menu

The game menu uses the same UI as the mod menu but it changes values in the currently running game instead of saving them seperately. This allows editing values like which resources a planet has.
