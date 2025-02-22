package dev.mizarc.waystonewarps.interaction.utils

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.Plugin
import java.util.concurrent.CompletableFuture

/**
 * Creates a player head texture from a given player.
 *
 * @param player The player to take the head skin from.
 * @return The ItemStack of the head.
 */
fun createHead(player: OfflinePlayer, plugin: Plugin): CompletableFuture<ItemStack> {
    val future = CompletableFuture<ItemStack>()
    val playerProfile = Bukkit.createProfile(player.uniqueId)
    playerProfile.update().thenAcceptAsync(
        { complete ->
            val skull = ItemStack(Material.PLAYER_HEAD)
            val meta = skull.itemMeta as SkullMeta
            meta.playerProfile = complete
            skull.itemMeta = meta
            future.complete(skull)
        },
        Bukkit.getScheduler().getMainThreadExecutor(plugin)
    )
    return future
}