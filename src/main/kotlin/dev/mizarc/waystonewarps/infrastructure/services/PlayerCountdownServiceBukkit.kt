package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.PlayerAttributeService
import dev.mizarc.waystonewarps.application.services.PlayerCountdownService
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask


class PlayerCountdownServiceBukkit(
    private val plugin: JavaPlugin,
    private val localizationProvider: LocalizationProvider,
    private val playerAttributeService: PlayerAttributeService
) : PlayerCountdownService {
    private val activeCountdowns: MutableMap<UUID, BukkitTask> = mutableMapOf()
    private val activeBossBars: MutableMap<Int, BossBar> = mutableMapOf()

    override fun startCountdown(playerId: UUID, warp: Warp) {
        val player = Bukkit.getPlayer(playerId) ?: return

        // create bossbar
        val bossBar = Bukkit.createBossBar(
            localizationProvider.get(
                player.uniqueId,
                LocalizationKeys.FEEDBACK_TELEPORT_PENDING,
                warp.name
            ),
            BarColor.GREEN,
            BarStyle.SOLID
        )
        bossBar.progress = 1.0

        // make bossbar count down
        val countdown = object : BukkitRunnable() {
            var totalTicks = playerAttributeService.getTeleportTimer(playerId) * 20
            var passedTicks = 0

            override fun run() {
                // cancel if player goes offline
                if (!player.isOnline) {
                    cancel()
                    return
                }

                passedTicks += 1
                var progress = 1 - (passedTicks.toDouble() / totalTicks.toDouble())
                // check if countdown is done
                if (progress < 0) {
                    bossBar.removeAll()
                    cancel()
                    return
                }
                bossBar.progress = progress
            }
        }.runTaskTimer(plugin, 0L, 1L)

        // show bossbar
        bossBar.addPlayer(player)

        activeCountdowns[player.uniqueId] = countdown
        activeBossBars[countdown.taskId] = bossBar
    }

    override fun cancelCountdown(playerId: UUID) {
        val countdown = activeCountdowns[playerId] ?: return
        val bossBar = activeBossBars[countdown.taskId] ?: return
        countdown.cancel()
        bossBar.removeAll()
        activeCountdowns.remove(playerId)
        activeBossBars.remove(countdown.taskId)
    }
}