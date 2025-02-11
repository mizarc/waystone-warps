package dev.mizarc.waystonewarps

import co.aikar.commands.PaperCommandManager
import dev.mizarc.waystonewarps.application.actions.teleport.TeleportPlayer
import dev.mizarc.waystonewarps.application.actions.warp.BreakWarpBlock
import dev.mizarc.waystonewarps.application.actions.warp.CreateWarp
import dev.mizarc.waystonewarps.application.actions.warp.GetPlayerWarpAccess
import dev.mizarc.waystonewarps.application.actions.warp.GetWarpAtPosition
import dev.mizarc.waystonewarps.application.actions.warp.GetWarpPlayerAccess
import dev.mizarc.waystonewarps.application.actions.warp.RefreshAllStructures
import dev.mizarc.waystonewarps.application.actions.warp.UpdateWarpIcon
import dev.mizarc.waystonewarps.application.actions.warp.UpdateWarpName
import dev.mizarc.waystonewarps.application.services.*
import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.domain.playerstate.PlayerStateRepository
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import dev.mizarc.waystonewarps.interaction.commands.WarpMenuCommand
import dev.mizarc.waystonewarps.infrastructure.persistence.Config
import dev.mizarc.waystonewarps.infrastructure.persistence.discoveries.DiscoveryRepositorySQLite
import dev.mizarc.waystonewarps.infrastructure.persistence.playerstate.PlayerStateRepositoryMemory
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.SQLiteStorage
import dev.mizarc.waystonewarps.infrastructure.persistence.warps.WarpRepositorySQLite
import dev.mizarc.waystonewarps.infrastructure.services.BukkitSchedulerService
import dev.mizarc.waystonewarps.infrastructure.services.MovementMonitorServiceBukkit
import dev.mizarc.waystonewarps.infrastructure.services.PlayerAttributeServiceVault
import dev.mizarc.waystonewarps.infrastructure.services.StructureBuilderServiceBukkit
import dev.mizarc.waystonewarps.infrastructure.services.TeleportationServiceBukkit
import dev.mizarc.waystonewarps.interaction.listeners.*
import org.koin.core.context.startKoin
import org.koin.dsl.module

class WaystoneWarps: JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager
    private lateinit var metadata: Chat
    private val config = Config(this)

    // Storage
    private val storage = SQLiteStorage(this)

    // Repositories
    private lateinit var warpRepository: WarpRepository
    private lateinit var discoveryRepository: DiscoveryRepository
    private lateinit var playerStateRepository: PlayerStateRepository

    // Services
    private lateinit var movementMonitorService: MovementMonitorService
    private lateinit var playerAttributeService: PlayerAttributeService
    private lateinit var structureBuilderService: StructureBuilderService
    private lateinit var teleportationService: TeleportationService
    private lateinit var scheduler: Scheduler

    override fun onEnable() {
        logger.info(Chat::class.java.toString())
        val serviceProvider: RegisteredServiceProvider<Chat> = server.servicesManager
            .getRegistration(Chat::class.java)!!
        metadata = serviceProvider.provider
        commandManager = PaperCommandManager(this)
        initialiseRepositories()
        initialiseServices()
        registerDependencies()
        registerCommands()
        registerEvents()
        RefreshAllStructures(warpRepository, structureBuilderService).execute()
        logger.info("WaystoneWarps has been Enabled")
    }

    override fun onDisable() {
        logger.info("WaystoneWarps has been Disabled")
    }

    private fun initialiseRepositories() {
        warpRepository = WarpRepositorySQLite(storage)
        discoveryRepository = DiscoveryRepositorySQLite(storage)
        playerStateRepository = PlayerStateRepositoryMemory()
    }

    private fun initialiseServices() {
        movementMonitorService = MovementMonitorServiceBukkit()
        playerAttributeService = PlayerAttributeServiceVault(config, metadata)
        structureBuilderService = StructureBuilderServiceBukkit()
        scheduler = BukkitSchedulerService(this)
        teleportationService = TeleportationServiceBukkit(playerAttributeService, movementMonitorService, scheduler)
    }

    private fun registerDependencies() {
        val actions = module {
            single { CreateWarp(warpRepository, playerAttributeService, structureBuilderService,
                discoveryRepository) }
            single { GetWarpPlayerAccess(discoveryRepository) }
            single { GetPlayerWarpAccess(discoveryRepository, warpRepository) }
            single { UpdateWarpIcon(warpRepository) }
            single { UpdateWarpName(warpRepository) }
            single { GetWarpAtPosition(warpRepository) }
            single { BreakWarpBlock(warpRepository, structureBuilderService, discoveryRepository) }
            single { TeleportPlayer(teleportationService, playerAttributeService)}
        }

        startKoin { modules(actions) }
    }

    private fun registerCommands() {
        commandManager.registerCommand(WarpMenuCommand())
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(WaystoneInteractListener(), this)
        server.pluginManager.registerEvents(WaystoneDestructionListener(), this)
    }
}