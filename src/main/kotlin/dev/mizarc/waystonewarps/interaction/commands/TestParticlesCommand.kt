package dev.mizarc.waystonewarps.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import dev.mizarc.waystonewarps.application.services.StructureParticleService
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import org.bukkit.entity.Player

@CommandAlias("particle")
@CommandPermission("worldwidewarps.command.warpmenu")
class TestParticlesCommand(private val warpRepository: WarpRepository,
                           private val structureParticleService: StructureParticleService): BaseCommand() {

    @Default
    fun onWarp(player: Player, particleName: String, spawnSpeed: Long) {
        for (warp in warpRepository.getAll()) {
            structureParticleService.removeParticles(warp)
            structureParticleService.spawnParticles(warp)
        }

    }
}