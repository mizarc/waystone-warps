package dev.mizarc.waystonewarps.domain.discoveries

import java.time.Instant
import java.time.LocalDateTime
import java.util.*

/**
 * Represents a waystone that a player has discovered.
 *
 * @param warpId Unique identifier for the warp.
 * @param playerId Unique identifier for the player.
 * @param firstDiscoveredTime Time the waystone was discovered.
 * @param lastVisitedTime Time the waystone was last teleported to.
 * @param isFavourite If the waystone is marked as favourite.
 */
class Discovery(var warpId: UUID, var playerId: UUID, var firstDiscoveredTime: Instant,
                var lastVisitedTime: Instant, var isFavourite: Boolean) {

    /**
     * Constructs a discovery using the minimum required details.
     *
     * @param warpId Unique identifier for the warp.
     * @param playerId Unique identifier for the player.
     * @param firstDiscoveredTime Time the waystone was discovered.
     */
    constructor(warpId: UUID, playerId: UUID, firstDiscoveredTime: Instant): this(
        warpId, playerId, firstDiscoveredTime, firstDiscoveredTime, false)
}