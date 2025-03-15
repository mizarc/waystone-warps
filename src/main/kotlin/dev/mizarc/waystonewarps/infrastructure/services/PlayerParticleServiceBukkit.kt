package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.PlayerParticleService
import dev.mizarc.waystonewarps.application.services.StructureParticleService
import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.whitelist.WhitelistRepository
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.math.cos
import kotlin.math.sin


class PlayerParticleServiceBukkit(private val plugin: JavaPlugin): PlayerParticleService {
    private val activeParticles: MutableMap<UUID, BukkitTask> = mutableMapOf()

    override fun spawnParticles(playerId: UUID) {
        val player = Bukkit.getPlayer(playerId) ?: return
        val radius = 1.0
        val height = 2.0
        val speed = 0.2
        val particleColor = Color.fromRGB(222, 122, 250)
        val particleSize = 0.5f
        val particleOptions = DustOptions(particleColor, particleSize)

        val particles = object : BukkitRunnable() {
            var goingUp = true
            var angle = 0.0

            override fun run() {
                // Cancel is player goes offline
                if (!player.isOnline) {
                    cancel()
                    return
                }

                // Spinning maths
                angle += speed
                val x = radius * cos(angle)
                val z = radius * sin(angle)
                var y = (height * (angle / (2 * Math.PI))) % height

                // Flip direction once on top/bottom
                if (angle >= 2 * Math.PI) {
                    angle = 0.0
                    goingUp = !goingUp
                }

                // Modify y value based on the direction
                y = if (!goingUp) {
                    height - y % height
                }else{
                    y % height
                }

                // Spawn particles at location
                val particleLocation1 = player.location.clone().add(x, y, z)
                player.world.spawnParticle(Particle.DUST, particleLocation1, 1, particleOptions)
                val particleLocation2 = player.location.clone().add(-x, y, -z)
                player.world.spawnParticle(Particle.DUST, particleLocation2, 1, particleOptions)
                player.world.spawnParticle(Particle.PORTAL, player.location, 1)
            }
        }.runTaskTimer(plugin, 0L, 1L)
        activeParticles.put(player.uniqueId, particles)
    }

    override fun removeParticles(playerId: UUID) {
        val particles = activeParticles[playerId] ?: return
        particles.cancel()
        activeParticles.remove(playerId)
    }
}