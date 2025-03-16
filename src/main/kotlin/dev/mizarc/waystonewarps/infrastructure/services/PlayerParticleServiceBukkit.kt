package dev.mizarc.waystonewarps.infrastructure.services

import com.destroystokyo.paper.ParticleBuilder
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

    override fun spawnPreParticles(playerId: UUID) {
        val player = Bukkit.getPlayer(playerId) ?: return
        val particles = object : BukkitRunnable() {
            override fun run() {
                // Cancel is player goes offline
                if (!player.isOnline) {
                    cancel()
                    return
                }

                player.world.spawnParticle(Particle.PORTAL, player.location, 1)
            }
        }.runTaskTimer(plugin, 0L, 1L)
        activeParticles.put(player.uniqueId, particles)
    }

    override fun spawnPostParticles(playerId: UUID) {
        val player = Bukkit.getPlayer(playerId) ?: return
        val playerLocation = player.location
        ParticleBuilder(Particle.REVERSE_PORTAL)
            .location(playerLocation)
            .offset(0.5, 1.0, 0.5)
            .count(100)
            .spawn()
    }

    override fun removeParticles(playerId: UUID) {
        val particles = activeParticles[playerId] ?: return
        particles.cancel()
        activeParticles.remove(playerId)
    }
}