# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project adheres to Semantic Versioning.

## [Latest]

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

## [0.1.0]
- Initial Pre-Release for Minecraft 1.21.4. Watch out for bugs!