package dev.mizarc.waystonewarps.application.actions.teleport

import dev.mizarc.waystonewarps.application.results.TeleportResult
import dev.mizarc.waystonewarps.application.services.PlayerAttributeService
import dev.mizarc.waystonewarps.application.services.PlayerCountdownService
import dev.mizarc.waystonewarps.application.services.PlayerParticleService
import dev.mizarc.waystonewarps.application.services.TeleportationService
import dev.mizarc.waystonewarps.application.services.WarpEventPublisher
import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.domain.playerstate.PlayerState
import dev.mizarc.waystonewarps.domain.playerstate.PlayerStateRepository
import dev.mizarc.waystonewarps.domain.warps.Warp
import java.time.Instant
import java.util.*

class TeleportPlayer(private val teleportationService: TeleportationService,
                     private val playerAttributeService: PlayerAttributeService,
                     private val playerParticleService: PlayerParticleService,
                     private val playerCountdownService: PlayerCountdownService,
                     private val discoveryRepository: DiscoveryRepository,
                     private val warpEventPublisher: WarpEventPublisher,
                     private val teleportPlayerImmediately: TeleportPlayerImmediately,
                     private val playerStateRepository: PlayerStateRepository) {

    fun execute(playerId: UUID, warp: Warp, onSuccess: () -> Unit, onPending: () -> Unit,
                onInsufficientFunds: () -> Unit, onCanceled: () -> Unit, onWorldNotFound: () -> Unit,
                onLocked: () -> Unit, onFailure: () -> Unit, onPermissionDenied: () -> Unit,
                onInterworldPermissionDenied: () -> Unit, onCooldown: (secondsRemaining: Int) -> Unit) {
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
                    warpEventPublisher.warpTeleported(playerId, warp)
                    val discovery = discoveryRepository.getByWarpAndPlayer(warp.id, playerId)
                    if (discovery != null) {
                        discovery.lastVisitedTime = Instant.now()
                        discoveryRepository.update(discovery)
                    }
                    playerParticleService.removeParticles(playerId)
                    playerParticleService.spawnPostParticles(playerId)
                    playerCountdownService.cancelCountdown(playerId)
                },
                onPending = {
                    onPending()
                    playerParticleService.spawnPreParticles(playerId)
                    playerCountdownService.startCountdown(playerId, warp)
                },
                onInsufficientFunds = {
                    onInsufficientFunds()
                    playerParticleService.removeParticles(playerId)
                    playerCountdownService.cancelCountdown(playerId)
                },
                onCanceled = {
                    onCanceled()
                    playerParticleService.removeParticles(playerId)
                    playerCountdownService.cancelCountdown(playerId)
                },
                onWorldNotFound = {
                    onWorldNotFound()
                    playerParticleService.removeParticles(playerId)
                    playerCountdownService.cancelCountdown(playerId)
                },
                onLocked = {
                    onLocked()
                    playerParticleService.removeParticles(playerId)
                    playerCountdownService.cancelCountdown(playerId)
                },
                onFailure = {
                    onFailure()
                    playerParticleService.removeParticles(playerId)
                    playerCountdownService.cancelCountdown(playerId)
                },
                onPermissionDenied = {
                    onPermissionDenied()
                    playerParticleService.removeParticles(playerId)
                    playerCountdownService.cancelCountdown(playerId)
                },
                onInterworldPermissionDenied = {
                    onInterworldPermissionDenied()
                    playerParticleService.removeParticles(playerId)
                    playerCountdownService.cancelCountdown(playerId)
                },
                onCooldown = { secondsRemaining ->
                    onCooldown(secondsRemaining)
                    playerParticleService.removeParticles(playerId)
                    playerCountdownService.cancelCountdown(playerId)
                }
            )
            return
        }

        // Instant teleport
        val result = teleportPlayerImmediately.execute(playerId, warp)
        when (result) {
            TeleportResult.SUCCESS -> {
                onSuccess()
                warpEventPublisher.warpTeleported(playerId, warp)
                val discovery = discoveryRepository.getByWarpAndPlayer(warp.id, playerId) ?: return
                discovery.lastVisitedTime = Instant.now()
                discoveryRepository.update(discovery)
                playerParticleService.spawnPostParticles(playerId)
            }
            TeleportResult.INSUFFICIENT_FUNDS -> onInsufficientFunds()
            TeleportResult.WORLD_NOT_FOUND -> onWorldNotFound()
            TeleportResult.LOCKED -> onLocked()
            TeleportResult.FAILED -> onFailure()
            TeleportResult.PERMISSION_DENIED -> onPermissionDenied()
            TeleportResult.INTERWORLD_PERMISSION_DENIED -> onInterworldPermissionDenied()
            TeleportResult.ON_COOLDOWN -> {
                val cooldownSeconds = playerAttributeService.getTeleportCooldown(playerId)
                val cooldownMs = cooldownSeconds * 1000L
                val lastTeleport = getPlayerState(playerId).lastTeleportTime
                val secondsRemaining = if (cooldownSeconds > 0 && lastTeleport > 0L) {
                    ((cooldownMs - (System.currentTimeMillis() - lastTeleport)) / 1000 + 1).toInt()
                } else {
                    0
                }
                onCooldown(secondsRemaining)
            }
        }
    }

    private fun getPlayerState(playerId: UUID): PlayerState {
        val existing = playerStateRepository.getById(playerId)
        if (existing != null) return existing

        val state = PlayerState(playerId)
        playerStateRepository.add(state)
        return state
    }
}
