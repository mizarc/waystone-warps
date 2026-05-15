# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project adheres to Semantic Versioning.

## [1.1.0]

### Added
- Support for Minecraft 26.1.
- New `/warpcreate` command to create a new waystone via command. Build a waystone as usual but type this command while looking at the lodestone block. Permission to use: `waystonewarps.command.warpcreate`
- New "Global" access type for warps. Warps with this access can be used without discovery.
- Boss bar teleport timer. Can be optionally toggled via config.
- Waystones can no longer be moved into areas protected by land protection plugins.
- Teleport cost can be dynamic based on distance, config options provided to tweak.
- Warp groups feature, optionally enabled via config. Allows players to put their warp under a group for easier browsing. Groups are admin-defined.
- Teleporting can now be restricted by a config cooldown or a player override with a metadata value: `waystonewarps.teleport_cooldown`
- Player override values can be set without a metadata provider such as Vault. Permissions are as follows:
  - `waystonewarps.warp_limit.<number>`: Sets a player's waystone creation limit, for example `waystonewarps.warp_limit.4` or `waystonewarps.warp_limit.19`. Highest matching value wins.
  - `waystonewarps.warp_limit.*`: Allows unlimited waystone creation.
  - `waystonewarps.teleport_cost.<number>`: Sets a player's teleport cost multiplier, for example `waystonewarps.teleport_cost.0.5` for half price. Lowest matching value wins.
  - `waystonewarps.teleport_cost.*`: Allows free teleportation (zero cost).
  - `waystonewarps.teleport_timer.<number>`: Sets a player's teleport timer in seconds, for example `waystonewarps.teleport_timer.3` for 3-second timer. Lowest matching value wins.
  - `waystonewarps.teleport_timer.*`: Allows instant teleportation (zero timer).
  - `waystonewarps.teleport_cooldown.<number>`: Sets a player's teleport cooldown in seconds, for example `waystonewarps.teleport_cooldown.20` for 20-second cooldown. Lowest matching value wins.
  - `waystonewarps.teleport_cooldown.*`: Allows instant teleportation (zero timer).
- Towny support, which allows for free travel between two of the same town.
- Simplified Chinese (zh_cn) localization.

### Changed
- Waystone skin menu tooltip now provides clearer information on how to use the blocks to apply the skins.

### Fixed
- Teleport particles and sound not appearing when teleportation is instant.
- Shift clicking items into menus deletes items.
- Check failure error when an item being placed does not have an attached ItemMeta.

### Removed
- `waystonewarps.teleport.cooldown_bypass` permission.

## [1.0.0]

### Added
- Support for Minecraft 1.21.11.
- Waystone creation is covered by protections that protect against block placing.
- Icons for waystones now support metadata including custom model data, armour dyes, trims, firework colours, potion types.
- Automatic database schema migrations on startup, including schema version tracking.
- New permissions to allow administrators to manage other players' waystones:
  - `waystonewarps.bypass.open_menu`: Allows access to open the management menu.
  - `waystonewarps.bypass.access_control`: Allows access to change the access control.
  - `waystonewarps.bypass.manage_players`: Allows access to manage players.
  - `waystonewarps.bypass.rename`: Allows access to rename the waystone.
  - `waystonewarps.bypass.icon`: Allows access to change the waystone icon.
  - `waystonewarps.bypass.relocate`: Allows access to relocate the waystone.
- New command system to remove invalid warp world data:
  - `/waystonewarps invalids list`: Lists missing worlds containing warps.
  - `/waystonewarps invalids remove <id>` Removes warps for a given world.
  - `/waystonewarps invalids removeall`: Removes all warps for missing worlds.
- New permissions to use the invalids command system:
  - `waystonewarps.admin.invalids.list`: Allows usage of the list command.
  - `waystonewarps.admin.invalids.remove`: Allows usage of remove command.
  - `waystonewarps.admin.invalids.removeall`: Allows usage of removeall command.
- New API to get warps and listen to events:
  - `WaystoneWarpsAPI`: Provides getters to get warp data.
  - `WarpCreateEvent`: Called when a warp is created.
  - `WarpDeleteEvent`: Called when a warp is deleted.
  - `WarpUpdateEvent`: Called when a warp is updated.
- New permissions to disallow teleportation:
  - `waystonewarps.teleport`: Disallows teleportation entirely
  - `waystonewarps.teleport.interworld`: Disallows teleportation to undiscovered warps
  - `waystonewarps.teleport.cooldown_bypass`: Bypasses the set cooldown timer
- New permission to disallow the creation of warps `waystonewarps.create`
- New permission to disallow the discovery of warps `waystonewarps.discover`
- New permission to use the /warpmenu command `waystonewarps.command.warpmenu`
- Localisation support, with languages now contained in properties files in the lang folder. Currently only supports English.

### Changed
- Primary menu action buttons are now gold with a grey description for standardisation.
- Menu buttons that denote alternative actions are as described:
  - Info only: BLUE
  - Confirm: GREEN
  - Cancel/Back: RED
  - Unavailable: GREY

### Fixed
- Config not being read on the first launch, required restart before the plugin could actually let you do anything.
- Waystones not being covered by protection plugins even though the block break itself is prevented.
- Whitelists not being removed from the database when the waystone is removed.
- Pagination for warps and player access were not showing correctly.

## [0.3.5]

### Added
- Support for Minecraft 1.21.7, 1.21.8, 1.21.9, and 1.21.10.

### Changed
- Waystones are now reverted back to their original state when the plugin is disabled.

## [0.3.4]

### Added
- Support for Minecraft 1.21.5 and 1.21.6.
- Waystones can now display holographic text information above them containing their name and coordinates.
- New config value to toggle whether waystones will have a hologram.

## [0.3.3]

### Fixed
- Waystone creation not checking if the top block is a Lodestone, resulting in the menu popping up for any block on top of a valid base block.
- Waystone creation with base bypassing removal of config specified base block.

## [0.3.2]

### Added
- Player hand swings when interacting with a waystone or changing the base block.

### Fixed
- Trying to incorrectly find `list_menu_via_compass` and `list_menu_via_waystone` instead of `warps_menu_via_compass` and `warps_menu_via_compass`
- Warp owner unable to teleport to their own warp when locked.
- Incorrect message when teleport is cancelled due to movement.

## [0.3.1]

### Fixed
- Error handling when invalid cost type in the config. Falls back to ITEM if set to an invalid type.
- Error when using invalid materials in the config. Invalid materials are now skipped over or set to default when required.

## [0.3.0]

### Added
- Warp menu contains filters to switch between discovered, favourites, and owned.
- Search can be used to narrow down the list of discovered warps.
- Warp sub-menu can be used to delete, favourite, or point the currently held compass to the direction of the warp.
- Waystones can have their appearance changed by clicking on the base of the block with a valid block.
- Waystones can also have their appearance changed on creation by placing down the lodestone on a valid block type.
- Valid block skins are displayed on the "View Available Skins" submenu of the management menu.
- Waystone skin types can be specified in the config with a required structure.
- Particles are displayed on the waystone depending on personal state. Bright green for owner, dull green for discovered, orange for locked, white for undiscovered.
- Particles are displayed to indicate the player being teleported and arriving at their destination.
- Sounds are played for vault creation and discovery.
- Sounds are played for the player being teleported and arriving at their destination.
- Sounds are played for changing the skin of the waystone.
- Config toggle to enable the functionality of using the compass to open the warp list menu.
- Config toggle to enable the functionality of using the waystone to open the warp list menu, moving the management menu to shift+right click.
- A sample-config.yml file is generated fresh on every launch to ensure that the most update to date base config format is known to administrators.

### Changed
- The base of the waystone is now a slab rather than a barrier, making the space above the slab clickable to set the skin.
- Lists of players and warps are sorted alphabetically.

## [0.2.0]

### Added
- Waystones can now be locked, disallowing discovery and teleportation to the public by toggling the torch icon.
- There's now a paginated menu where you can view players, listed by discovery, whitelisted, or all online players.
- Player search menu can now be used to narrow down the list of players.
- Access to teleport to the warp can be revoked via the new player menu by right-clicking their icon, which opens up a confirmation prompt.
- Whitelist can be toggled by left-clicking the player icon in the same menu.
- Being whitelisted allows players to still discover and teleport to a waystone set to private mode.
- Error message output in text input menus can now be dismissed by clicking on them.
- Teleport area is cleared out on teleport to prevent suffocation.
- Teleport area has a platform built on teleport to replace missing or hollow blocks to prevent falling.
- Fluids are prevented from flowing into the teleport zone, as they can fill in the space after the blocks are cleared out.
- Falling blocks are broken as they fall in to prevent filling the space back in with blocks after the existing blocks are cleared out.
- Players are now given Resistance 5 on teleport for temporary invulnerability.
- Warp menu can now be opened by right-clicking a compass.

### Changes
- Text input in menus now defaults to blank.
- Text input keeps the existing text in the box when the confirmation button is pressed and an error appears.
- Warp list menu makes use of a new pagination system.
- Actions are now cancelled when interacting with a waystone with item in hand.

## [0.1.1]

### Fixed
- Default config value for cost amount should be `teleport_cost_amount` instead of `teleport_cost`.

## [0.1.0]
- Initial Pre-Release for Minecraft 1.21.4. Watch out for bugs!
