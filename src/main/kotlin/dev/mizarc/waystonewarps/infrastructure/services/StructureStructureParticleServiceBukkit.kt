package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.StructureParticleService
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

class StructureStructureParticleServiceBukkit(private val plugin: JavaPlugin): StructureParticleService {
    private val activeParticles: MutableMap<UUID, BukkitTask> = mutableMapOf()

    override fun spawnParticles(warp: Warp, particleName: String, spawnSpeed: Long) {
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val location = warp.position.toLocation(world)
        location.x += 0.5
        location.y += 0.5
        location.z += 0.5

        val particles = object : BukkitRunnable() {
            override fun run() {
                location.world.spawnParticle(Particle.valueOf(particleName), location, 1, 0.5, 0.5, 0.5)
            }
        }.runTaskTimer(plugin, 0L, spawnSpeed)
        activeParticles.put(warp.id, particles)
    }

    override fun removeParticles(warp: Warp) {
        val particles = activeParticles[warp.id] ?: return
        particles.cancel()
        activeParticles.remove(warp.id)
    }
}