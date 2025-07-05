package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.application.services.HologramService
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.TextDisplay

class HologramServiceBukkit(private val configService: ConfigService): HologramService {
    override fun spawnHologram(warp: Warp) {
        if (!configService.hologramsEnabled()) return
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val location = warp.position.toLocation(world)

        // Create the display component consisting of a name and coordinates
        val nameComponent = Component.text(warp.name)
            .color(NamedTextColor.GOLD)
            .decorate(TextDecoration.BOLD)
        val coordinatesComponent = Component.text("X: ${warp.position.x}  Y: ${warp.position.y}  Z: ${warp.position.z}")
            .color(NamedTextColor.GRAY)
        val componentsToJoin = listOf(nameComponent, coordinatesComponent)
        val combinedText = Component.join(
            JoinConfiguration.separator(Component.newline()), componentsToJoin)

        // Display the hologram
        val display: TextDisplay = world.spawn(location.add(0.5, 1.5, 0.5), TextDisplay::class.java) { entity ->
            entity.text(combinedText)
            entity.billboard = Display.Billboard.VERTICAL
        }
        display.customName(Component.text((warp.id.toString())))
    }

    override fun updateHologram(warp: Warp) {
        removeHologram(warp)
        spawnHologram(warp)
    }

    override fun removeHologram(warp: Warp) {
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val entities: MutableList<Entity> = world.entities
        for (entity in entities) {
            if (entity !is TextDisplay) continue
            val customName = entity.customName() ?: continue
            if (customName is TextComponent && customName.content() == warp.id.toString()) {
                entity.remove()
            }
        }
    }
}