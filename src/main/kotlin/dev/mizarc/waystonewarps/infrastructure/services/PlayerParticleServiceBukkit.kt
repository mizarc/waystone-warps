package dev.mizarc.waystonewarps.infrastructure.services

import com.destroystokyo.paper.ParticleBuilder
import dev.mizarc.waystonewarps.application.services.PlayerAttributeService
import dev.mizarc.waystonewarps.application.services.PlayerParticleService
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*


class PlayerParticleServiceBukkit(private val plugin: JavaPlugin,
                                  private val playerAttributeService: PlayerAttributeService): PlayerParticleService {
    private val activeParticles: MutableMap<UUID, BukkitTask> = mutableMapOf()

    override fun spawnPreParticles(playerId: UUID) {
        val player = Bukkit.getPlayer(playerId) ?: return
        val particles = object : BukkitRunnable() {
            var teleportTime = playerAttributeService.getTeleportTimer(playerId) * 20
            override fun run() {
                // Cancel is player goes offline
                if (!player.isOnline) {
                    cancel()
                    return
                }

                teleportTime -= 1
                if (teleportTime == 80) {
                    player.world.playSound(player.location, Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 1.0f)
                }

                player.world.spawnParticle(Particle.PORTAL, player.location, 1)
            }
        }.runTaskTimer(plugin, 0L, 1L)
        activeParticles[player.uniqueId] = particles
    }

    override fun spawnPostParticles(playerId: UUID) {
        val player = Bukkit.getPlayer(playerId) ?: return
        val playerLocation = player.location
        ParticleBuilder(Particle.REVERSE_PORTAL)
            .location(playerLocation)
            .offset(0.5, 1.0, 0.5)
            .count(100)
            .spawn()

        // Play teleport sound
        playerLocation.world.playSound(playerLocation, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 1.0f)
    }

    override fun removeParticles(playerId: UUID) {
        val particles = activeParticles[playerId] ?: return
        particles.cancel()
        activeParticles.remove(playerId)
    }
}