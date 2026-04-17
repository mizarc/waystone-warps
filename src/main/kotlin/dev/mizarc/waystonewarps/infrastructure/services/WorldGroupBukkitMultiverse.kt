package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.WorldGroupService
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.mvplugins.multiverse.inventories.MultiverseInventoriesApi
import java.util.UUID

/**
 * Implementation of WorldGroupService used when world group is provided by Multiverse.
 */
class WorldGroupBukkitMultiverse(private val plugin: JavaPlugin) : WorldGroupService {
    override fun inSameGroup(current: String, target: String): Boolean {
        // plugin.logger.info("Checking if $current in same group as $target")
        val api = MultiverseInventoriesApi.get()
        for (g in api.worldGroupManager.getGroupsForWorld(current)) {
            if (target in g.applicableWorlds) {
                // plugin.logger.info("Found both in group ${g.name}")
                return true
            }
        }
        return false
    }

    override fun inSameGroup(current: UUID, target: UUID): Boolean {
        val currentWorld = Bukkit.getWorld(current) ?: return false
        val targetWorld = Bukkit.getWorld(target) ?: return false
        return inSameGroup(currentWorld.name, targetWorld.name)
    }
}