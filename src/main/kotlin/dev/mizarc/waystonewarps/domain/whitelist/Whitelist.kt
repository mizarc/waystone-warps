package dev.mizarc.waystonewarps.domain.whitelist

import java.util.UUID

/**
 * Maps warps to players to store whitelist information.
 *
 * @property warpId The unique identifier for the warp.
 * @property playerId The unique identifier for a player.
 */
class Whitelist(var warpId: UUID, var playerId: UUID)