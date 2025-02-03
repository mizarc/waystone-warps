package dev.mizarc.waystonewarps.domain.discoveries

import java.util.*

/**
 * Maps waystones to players. This is used to represent what players have access to what waystones.
 * @property warpId The unique identifier for the warp.
 * @property playerId The unique identifier for a player.
 */
class Discovery(var warpId: UUID, var playerId: UUID)