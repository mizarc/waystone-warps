package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.StructureParticleService
import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.whitelist.WhitelistRepository
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*


class StructureParticleServiceBukkit(private val plugin: JavaPlugin,
                                     private val playerDiscoveryRepository: DiscoveryRepository,
                                     private val whitelistRepository: WhitelistRepository): StructureParticleService {
    private val activeParticles: MutableMap<UUID, BukkitTask> = mutableMapOf()

    override fun spawnParticles(warp: Warp) {
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val location = warp.position.toLocation(world)
        location.x += 0.5
        location.y += 0.5
        location.z += 0.5

        val particles = object : BukkitRunnable() {
            override fun run() {
                for (player in Bukkit.getOnlinePlayers()) {
                    if (player.location.world == location.world && player.location.distance(location)
                        <= Bukkit.getServer().viewDistance * 16
                    ) {
                        val discovered = playerDiscoveryRepository.getByWarpAndPlayer(warp.id, player.uniqueId)
                        val whitelisted = whitelistRepository.isWhitelisted(warp.id, player.uniqueId)

                        if (warp.isLocked && !whitelisted) {
                            location.world.spawnParticle(Particle.WAX_ON, location, 1, 0.5, 0.5, 0.5)
                        } else {
                            val particle = if (discovered != null) Particle.SCRAPE else Particle.WAX_OFF
                            player.spawnParticle(particle, location, 1, 0.5, 0.5, 0.5)
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L)
        activeParticles.put(warp.id, particles)
    }

    override fun removeParticles(warp: Warp) {
        val particles = activeParticles[warp.id] ?: return
        particles.cancel()
        activeParticles.remove(warp.id)
    }
}