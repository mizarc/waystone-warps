# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project adheres to Semantic Versioning.

## [0.3.3]

### Fixed
- Warp owner unable to teleport to their own warp when locked.
- Incorrect message when teleport is cancelled due to movement.

## [0.3.2]

### Added
- Player hand swings when interacting with a waystone or changing the base block.

### Fixed
- Trying to incorrectly find `list_menu_via_compass` and `list_menu_via_waystone` instead of `warps_menu_via_compass` and `warps_menu_via_compass`

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