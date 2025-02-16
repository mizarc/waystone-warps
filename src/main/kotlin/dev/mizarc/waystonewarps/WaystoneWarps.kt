package dev.mizarc.waystonewarps

import co.aikar.commands.PaperCommandManager
import co.aikar.idb.Database
import dev.mizarc.waystonewarps.application.actions.discovery.DiscoverWarp
import dev.mizarc.waystonewarps.application.actions.teleport.LogPlayerMovement
import dev.mizarc.waystonewarps.application.actions.teleport.TeleportPlayer
import dev.mizarc.waystonewarps.application.actions.world.BreakWarpBlock
import dev.mizarc.waystonewarps.application.actions.world.CreateWarp
import dev.mizarc.waystonewarps.application.actions.discovery.GetPlayerWarpAccess
import dev.mizarc.waystonewarps.application.actions.world.GetWarpAtPosition
import dev.mizarc.waystonewarps.application.actions.discovery.GetWarpPlayerAccess
import dev.mizarc.waystonewarps.application.actions.world.RefreshAllStructures
import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpIcon
import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpName
import dev.mizarc.waystonewarps.application.actions.world.MoveWarp
import dev.mizarc.waystonewarps.application.services.*
import dev.mizarc.waystonewarps.application.services.scheduling.SchedulerService
import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.domain.playerstate.PlayerStateRepository
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import dev.mizarc.waystonewarps.interaction.commands.WarpMenuCommand
import dev.mizarc.waystonewarps.infrastructure.services.ConfigServiceBukkit
import dev.mizarc.waystonewarps.infrastructure.persistence.discoveries.DiscoveryRepositorySQLite
import dev.mizarc.waystonewarps.infrastructure.persistence.playerstate.PlayerStateRepositoryMemory
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.SQLiteStorage
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.Storage
import dev.mizarc.waystonewarps.infrastructure.persistence.warps.WarpRepositorySQLite
import dev.mizarc.waystonewarps.infrastructure.services.MovementMonitorServiceBukkit
import dev.mizarc.waystonewarps.infrastructure.services.PlayerAttributeServiceVault
import dev.mizarc.waystonewarps.infrastructure.services.StructureBuilderServiceBukkit
import dev.mizarc.waystonewarps.infrastructure.services.teleportation.TeleportationServiceBukkit
import dev.mizarc.waystonewarps.infrastructure.services.scheduling.SchedulerServiceBukkit
import dev.mizarc.waystonewarps.interaction.listeners.*
import net.milkbowl.vault.economy.Economy
import org.koin.core.context.startKoin
import org.koin.dsl.module

class WaystoneWarps: JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager
    private lateinit var metadata: Chat
    private var economy: Economy? = null

    // Storage
    private lateinit var storage: Storage<Database>

    // Repositories
    private lateinit var warpRepository: WarpRepository
    private lateinit var discoveryRepository: DiscoveryRepository
    private lateinit var playerStateRepository: PlayerStateRepository

    // Services
    private lateinit var movementMonitorService: MovementMonitorService
    private lateinit var playerAttributeService: PlayerAttributeService
    private lateinit var structureBuilderService: StructureBuilderService
    private lateinit var teleportationService: TeleportationService
    private lateinit var configService: ConfigService
    private lateinit var scheduler: SchedulerService

    override fun onEnable() {
        logger.info(Chat::class.java.toString())

        // Get Vault metadata
        val chatServiceProvider: RegisteredServiceProvider<Chat> = server.servicesManager
            .getRegistration(Chat::class.java)!!
        metadata = chatServiceProvider.provider

        // Get Vault economy
        val economyServiceProvider: RegisteredServiceProvider<Economy>? = server.servicesManager
            .getRegistration(Economy::class.java)
        if (economyServiceProvider != null) {
            economy = economyServiceProvider.provider
        }
        // Create plugin folder
        if (!dataFolder.exists()) dataFolder.mkdir()

        storage = SQLiteStorage(this)
        commandManager = PaperCommandManager(this)
        saveDefaultConfig()
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
        configService = ConfigServiceBukkit(this, this.config)
        playerAttributeService = PlayerAttributeServiceVault(configService, metadata)
        structureBuilderService = StructureBuilderServiceBukkit(this)
        scheduler = SchedulerServiceBukkit(this)
        teleportationService = TeleportationServiceBukkit(playerAttributeService, configService,
            movementMonitorService, scheduler, economy)
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
            single { LogPlayerMovement(movementMonitorService) }
            single { DiscoverWarp(discoveryRepository) }
            single { MoveWarp(warpRepository, structureBuilderService) }
        }

        startKoin { modules(actions) }
    }

    private fun registerCommands() {
        commandManager.registerCommand(WarpMenuCommand())
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(WaystoneInteractListener(), this)
        server.pluginManager.registerEvents(WaystoneDestructionListener(), this)
        server.pluginManager.registerEvents(PlayerMovementListener(), this)
        server.pluginManager.registerEvents(MoveToolListener(), this)
        server.pluginManager.registerEvents(ToolRemovalListener(), this)
    }
}