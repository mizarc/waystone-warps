package dev.mizarc.waystonewarps.api

import dev.mizarc.waystonewarps.WaystoneWarps
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import org.bukkit.Location
import org.bukkit.World
import java.util.*

/**
 * The main API class for WaystoneWarps. Provides access to waystone data and events.
 * 
 * To use the API, get an instance via [WaystoneWarps.getAPI()].
 */
class WaystoneWarpsAPI(private val warpRepository: WarpRepository) {
    /**
     * Gets all waystones in the server.
     * @return A set of all waystones
     */
    fun getAllWarps(): Set<Warp> = warpRepository.getAll()

    /**
     * Gets a waystone by its unique ID.
     * @param id The UUID of the waystone
     * @return The waystone, or null if not found
     */
    fun getWarpById(id: UUID): Warp? = warpRepository.getById(id)

    /**
     * Gets all waystones owned by a player.
     * @param playerId The UUID of the player
     * @return A list of waystones owned by the player
     */
    fun getWarpsByPlayer(playerId: UUID): List<Warp> = warpRepository.getByPlayer(playerId)

    /**
     * Gets all waystones in a specific world.
     * @param world The world
     * @return A list of waystones in the world
     */
    fun getWarpsInWorld(world: World): List<Warp> = warpRepository.getByWorld(world.uid)

    /**
     * Gets a waystone at a specific location.
     * @param location The location to check
     * @return The waystone at the location, or null if none exists
     */
    fun getWarpAtLocation(location: Location): Warp? {
        return warpRepository.getByPosition(
            location.toPosition3D(),
            location.world?.uid ?: return null
        )
    }
}
