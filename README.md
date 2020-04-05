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

How do I add settings for my mod to the menu? Add this mods jar as a library reference, do not include the jar. Then look at the showcase project under https://github.com/razuhl/SSMSQoLTest . It contains the java code for every type of settings that can be used. Remind your users that they have to manage the mod load order to ensure your mod loads after this one.

How do I change the load order? The order of mods is lexical. Adding prefixes to mod names which are defined in the respective mod_info.json file allows reordering. Putting a zero in front of the name should promote it to the top.

###### Drop Surplus

If cargo, personel or fuel capacity is exhausted additional loot from combat, salvage or cargo pods is automatically dropped. If other cargo pods are nearby they are merged to avoid cluttering when searching the same debries a couple of times. This speeds up the looting mechanics since the player can simply hit take all and leave.

Modders can use the new command DropSurplus in the rules to trigger the functionality. In order to make it work, the rules "sal_salvageOptionSelected" and "pods_openOption" replace their commands SalvageEntity and CargoPods with the commands SalvageEntityWrapper and CargoPodsWrapper. If another mod overrides the rules they will have to be adjusted to regain the functionality.
  
###### Unlimited command points

Ensures that the player has at least 5 command points at any time.

###### Mod Menu (F12)

The mod menu provides a custom UI to edit settings in-game instead of editing configuration files. The settings are automatically saved into the file "[Starsector]/saves/common/SSMSQoL.json.data" and apply across all save games.

Authors can modify Integer, Float, Boolean, String, Compositions and List of those types. They can either be edited directly with textboxes etc. or values are chosen from a dynamic list of options.

###### Game Menu (F10)

The game menu uses the same UI as the mod menu but it changes values in the currently running game instead of saving them seperately. This allows editing values like which resources a planet has.
