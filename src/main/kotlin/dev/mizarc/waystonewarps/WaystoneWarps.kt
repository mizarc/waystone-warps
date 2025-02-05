package dev.mizarc.waystonewarps

import co.aikar.commands.PaperCommandManager
import dev.mizarc.waystonewarps.application.actions.warp.CreateWarp
import dev.mizarc.waystonewarps.application.actions.warp.GetPlayersWithAccessToWarp
import dev.mizarc.waystonewarps.application.actions.warp.UpdateWarpIcon
import dev.mizarc.waystonewarps.application.actions.warp.UpdateWarpName
import dev.mizarc.waystonewarps.application.services.*
import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import dev.mizarc.waystonewarps.interaction.commands.WarpMenuCommand
import dev.mizarc.waystonewarps.infrastructure.persistence.Config
import dev.mizarc.waystonewarps.infrastructure.persistence.discoveries.DiscoveryRepositorySQLite
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.SQLiteStorage
import dev.mizarc.waystonewarps.infrastructure.persistence.warps.WarpRepositorySQLite
import dev.mizarc.waystonewarps.infrastructure.services.MessagingServiceBukkit
import dev.mizarc.waystonewarps.infrastructure.services.MovementMonitorServiceBukkit
import dev.mizarc.waystonewarps.infrastructure.services.PlayerAttributeServiceVault
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

    // Services
    private lateinit var messagingService: MessagingService
    private lateinit var movementMonitorService: MovementMonitorService
    private lateinit var playerAttributeService: PlayerAttributeService
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
        logger.info("WaystoneWarps has been Enabled")
    }

    override fun onDisable() {
        logger.info("WaystoneWarps has been Disabled")
    }

    private fun initialiseRepositories() {
        warpRepository = WarpRepositorySQLite(storage)
        discoveryRepository = DiscoveryRepositorySQLite(storage)
    }

    private fun initialiseServices() {
        messagingService = MessagingServiceBukkit()
        movementMonitorService = MovementMonitorServiceBukkit()
        playerAttributeService = PlayerAttributeServiceVault(config, metadata)
    }

    private fun registerDependencies() {
        val actions = module {
            single { CreateWarp(warpRepository, playerAttributeService) }
            single { GetPlayersWithAccessToWarp(discoveryRepository) }
            single { UpdateWarpIcon(warpRepository) }
            single { UpdateWarpName(warpRepository) }
        }

        startKoin { modules(actions) }
    }

    private fun registerDependencies2() {
        commandManager.registerDependency(Config::class.java, config)
        commandManager.registerDependency(DatabaseStorage::class.java, storage)
        commandManager.registerDependency(PlayerStateRepository::class.java, players)
        commandManager.registerDependency(Teleporter::class.java, teleporter)
        commandManager.registerDependency(WarpRepositorySQLite::class.java, waystoneRepositorySQLite)
    }

    private fun registerCommands() {
        commandManager.registerCommand(WarpMenuCommand())
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(PlayerRegistrationListener(homeRepository, players, config, metadata), this)
        server.pluginManager.registerEvents(TeleportCancelListener(players), this)
        server.pluginManager.registerEvents(WarpInteractListener(waystoneRepositorySQLite, warpAccessRepository), this)
        server.pluginManager.registerEvents(WarpDestructionListener(waystoneRepositorySQLite, warpAccessRepository), this)
        server.pluginManager.registerEvents(WarpMoveToolListener(waystoneRepositorySQLite), this)
        server.pluginManager.registerEvents(WarpMoveToolRemovalListener(), this)
    }
}