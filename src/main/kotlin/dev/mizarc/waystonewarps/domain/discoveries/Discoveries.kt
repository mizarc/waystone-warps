package dev.mizarc.waystonewarps.domain.discoveries

import java.util.*

/**
 * Maps waystones to players. This is used to represent what players have access to what waystones.
 * @property waystoneId The unique identifier for the claim.
 * @property playerId The unique identifier for a player.
 */
class Discoveries(var waystoneId: UUID, var playerId: UUID)