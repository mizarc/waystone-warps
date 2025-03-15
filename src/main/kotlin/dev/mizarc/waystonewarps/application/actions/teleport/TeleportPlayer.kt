package dev.mizarc.waystonewarps.application.actions.teleport

import dev.mizarc.waystonewarps.application.results.TeleportResult
import dev.mizarc.waystonewarps.application.services.PlayerAttributeService
import dev.mizarc.waystonewarps.application.services.PlayerParticleService
import dev.mizarc.waystonewarps.application.services.TeleportationService
import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.domain.warps.Warp
import java.time.Instant
import java.util.*

class TeleportPlayer(private val teleportationService: TeleportationService,
                     private val playerAttributeService: PlayerAttributeService,
                     private val playerParticleService: PlayerParticleService,
                     private val discoveryRepository: DiscoveryRepository) {

    fun execute(playerId: UUID, warp: Warp, onSuccess: () -> Unit, onPending: () -> Unit,
                onInsufficientFunds: () -> Unit, onCanceled: () -> Unit, onWorldNotFound: () -> Unit,
                onLocked: () -> Unit, onFailure: () -> Unit) {
        // Retrieve player settings
        val timer = playerAttributeService.getTeleportTimer(playerId)

        // Schedule delayed teleport
        if (timer > 0) {
            teleportationService.scheduleDelayedTeleport(
                playerId,
                warp,
                timer,
                onSuccess = {
                    onSuccess()
                    val discovery = discoveryRepository.getByWarpAndPlayer(warp.id, playerId)
                    if (discovery != null) {
                        discovery.lastVisitedTime = Instant.now()
                        discoveryRepository.update(discovery)
                    }
                    playerParticleService.removeParticles(playerId)
                },
                onPending = {
                    onPending()
                    playerParticleService.spawnParticles(playerId)
                },
                onInsufficientFunds = {
                    onInsufficientFunds()
                    playerParticleService.removeParticles(playerId)
                },
                onCanceled = {
                    onInsufficientFunds()
                    playerParticleService.removeParticles(playerId)
                },
                onWorldNotFound = {
                    onWorldNotFound()
                    playerParticleService.removeParticles(playerId)
                },
                onLocked = {
                    onLocked()
                    playerParticleService.removeParticles(playerId)
                },
                onFailure = {
                    onFailure()
                    playerParticleService.removeParticles(playerId)
                }
            )
            return
        }

        // Instant teleport
        val result = teleportationService.teleportPlayer(playerId, warp)
        when (result) {
            TeleportResult.SUCCESS -> {
                onSuccess()
                val discovery = discoveryRepository.getByWarpAndPlayer(warp.id, playerId) ?: return
                discovery.lastVisitedTime = Instant.now()
                discoveryRepository.update(discovery)
            }
            TeleportResult.INSUFFICIENT_FUNDS -> onInsufficientFunds()
            TeleportResult.WORLD_NOT_FOUND -> onWorldNotFound()
            TeleportResult.LOCKED -> onLocked()
            TeleportResult.FAILED -> onFailure()
        }
    }
}
