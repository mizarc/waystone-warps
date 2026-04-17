package dev.mizarc.waystonewarps.application.services

import java.util.*

/**
 * Provides service for determining if worlds are in the same world group.
 */
interface WorldGroupService {
    /**
     * Checks if worlds are in the same world group.
     *
     * @param current The id of world the user is currently in.
     * @param target The id of world the user wants to teleport to.
     */
    fun inSameGroup(current: UUID, target: UUID): Boolean

    /**
     * Checks if worlds are in the same world group.
     *
     * @param current The name of world the user is currently in.
     * @param target The name of world the user wants to teleport to.
     */
    fun inSameGroup(current: String, target: String): Boolean
}
