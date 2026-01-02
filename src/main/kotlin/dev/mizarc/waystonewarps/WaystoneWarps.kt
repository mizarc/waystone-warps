package dev.mizarc.waystonewarps

import co.aikar.commands.PaperCommandManager
import co.aikar.idb.Database
import dev.mizarc.waystonewarps.application.actions.administration.ListInvalidWarps
import dev.mizarc.waystonewarps.application.actions.administration.RemoveAllInvalidWarps
import dev.mizarc.waystonewarps.application.actions.administration.RemoveInvalidWarpsForWorld
import dev.mizarc.waystonewarps.application.actions.discovery.DiscoverWarp
import dev.mizarc.waystonewarps.application.actions.discovery.GetFavouritedWarpAccess
import dev.mizarc.waystonewarps.application.actions.teleport.LogPlayerMovement
import dev.mizarc.waystonewarps.application.actions.teleport.TeleportPlayer
import dev.mizarc.waystonewarps.application.actions.world.BreakWarpBlock
import dev.mizarc.waystonewarps.application.actions.world.CreateWarp
import dev.mizarc.waystonewarps.application.actions.discovery.GetPlayerWarpAccess
import dev.mizarc.waystonewarps.application.actions.world.GetWarpAtPosition
import dev.mizarc.waystonewarps.application.actions.discovery.GetWarpPlayerAccess
import dev.mizarc.waystonewarps.application.actions.discovery.IsPlayerFavouriteWarp
import dev.mizarc.waystonewarps.application.actions.discovery.RevokeDiscovery
import dev.mizarc.waystonewarps.application.actions.discovery.ToggleFavouriteDiscovery
import dev.mizarc.waystonewarps.application.actions.management.GetAllWarpSkins
import dev.mizarc.waystonewarps.application.actions.management.GetOwnedWarps
import dev.mizarc.waystonewarps.application.actions.management.ToggleLock
import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpIcon
import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpName
import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpSkin
import dev.mizarc.waystonewarps.application.actions.whitelist.ToggleWhitelist
import dev.mizarc.waystonewarps.application.actions.whitelist.GetWhitelistedPlayers
import dev.mizarc.waystonewarps.application.actions.world.AddAllDisplays
import dev.mizarc.waystonewarps.application.actions.world.IsPositionInTeleportZone
import dev.mizarc.waystonewarps.application.actions.world.IsValidWarpBase
import dev.mizarc.waystonewarps.application.actions.world.MoveWarp
import dev.mizarc.waystonewarps.application.actions.world.RemoveAllDisplays
import dev.mizarc.waystonewarps.application.services.*
import dev.mizarc.waystonewarps.application.services.scheduling.SchedulerService
import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.domain.playerstate.PlayerStateRepository
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import dev.mizarc.waystonewarps.domain.whitelist.WhitelistRepository
import dev.mizarc.waystonewarps.domain.world.WorldService
import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.java.JavaPlugin
import dev.mizarc.waystonewarps.interaction.commands.WarpMenuCommand
import dev.mizarc.waystonewarps.infrastructure.persistence.discoveries.DiscoveryRepositorySQLite
import dev.mizarc.waystonewarps.infrastructure.persistence.playerstate.PlayerStateRepositoryMemory
import dev.mizarc.waystonewarps.infrastructure.persistence.migrations.Migration0_CreateInitialTables
import dev.mizarc.waystonewarps.infrastructure.persistence.migrations.Migration1_AddWarpIconMeta
import dev.mizarc.waystonewarps.infrastructure.persistence.migrations.SchemaMigrator
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.SQLiteStorage
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.Storage
import dev.mizarc.waystonewarps.infrastructure.persistence.warps.WarpRepositorySQLite
import dev.mizarc.waystonewarps.infrastructure.persistence.whitelist.WhitelistRepositorySQLite
import dev.mizarc.waystonewarps.infrastructure.services.*
import dev.mizarc.waystonewarps.infrastructure.services.teleportation.TeleportationServiceBukkit
import dev.mizarc.waystonewarps.infrastructure.services.scheduling.SchedulerServiceBukkit
import dev.mizarc.waystonewarps.interaction.commands.InvalidsCommand
import dev.mizarc.waystonewarps.interaction.listeners.*
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

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
    private lateinit var whitelistRepository: WhitelistRepository

    // Services
    private lateinit var movementMonitorService: MovementMonitorService
    private lateinit var playerAttributeService: PlayerAttributeService
    private lateinit var structureBuilderService: StructureBuilderService
    private lateinit var teleportationService: TeleportationService
    private lateinit var structureParticleService: StructureParticleService
    private lateinit var playerParticleService: PlayerParticleService
    private lateinit var worldService: WorldService
    private lateinit var hologramService: HologramService
    private lateinit var configService: ConfigService
    private lateinit var scheduler: SchedulerService

    override fun onEnable() {
        // Create plugin folder
        if (!dataFolder.exists()) dataFolder.mkdir()

        // Get storage type
        storage = SQLiteStorage(this.dataFolder)

        try {
            SchemaMigrator(
                db = storage.connection,
                migrations = listOf(
                    Migration0_CreateInitialTables(),
                    Migration1_AddWarpIconMeta(),
                ),
            ).migrateToLatest()
        } catch (ex: Exception) {
            logger.severe("Failed to migrate database: ${ex.message}")
            ex.printStackTrace()
            server.pluginManager.disablePlugin(this)
            return
        }

        // Get command manager
        commandManager = PaperCommandManager(this)

        // Initialise everything else
        initialiseConfig()
        initialiseVaultDependency()
        initialiseRepositories()
        initialiseServices()
        registerDependencies()
        registerCommands()
        registerEvents()
        AddAllDisplays(warpRepository, structureBuilderService, hologramService).execute()

        for (warp in warpRepository.getAll()) {
            structureParticleService.spawnParticles(warp)
        }

        logger.info("WaystoneWarps has been Enabled")
    }

    override fun onDisable() {
        RemoveAllDisplays(warpRepository, structureBuilderService, hologramService).execute()
        logger.info("WaystoneWarps has been Disabled")
    }

    private fun initialiseVaultDependency() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            server.servicesManager.getRegistration(Chat::class.java)?.let { metadata = it.provider }
            server.servicesManager.getRegistration(Economy::class.java)?.let {economy = it.provider}
            logger.info(Chat::class.java.toString())
        }
    }

    private fun initialiseConfig() {
        saveDefaultConfig()
        reloadConfig()
        getResource("config.yml")?.use { defaultConfigStream ->
            val sampleConfigFile = File(dataFolder, "sample-config.yml")
            try {
                Files.copy(defaultConfigStream, sampleConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: IOException) {
                logger.severe("Failed to copy config: ${e.message}")
            }
        } ?: logger.warning("Default config file not found in the plugin resources")
    }

    private fun initialiseRepositories() {
        warpRepository = WarpRepositorySQLite(storage)
        discoveryRepository = DiscoveryRepositorySQLite(storage)
        playerStateRepository = PlayerStateRepositoryMemory()
        whitelistRepository = WhitelistRepositorySQLite(storage)
    }

    private fun initialiseServices() {
        movementMonitorService = MovementMonitorServiceBukkit()
        configService = ConfigServiceBukkit(this.config)
        playerAttributeService = if (::metadata.isInitialized) {
            PlayerAttributeServiceVault(configService, metadata)
        } else {
            PlayerAttributeServiceSimple(configService)
        }
        structureBuilderService = StructureBuilderServiceBukkit(this, configService)
        scheduler = SchedulerServiceBukkit(this)
        teleportationService = TeleportationServiceBukkit(playerAttributeService, configService,
            movementMonitorService, whitelistRepository, scheduler, economy)
        structureParticleService = StructureParticleServiceBukkit(this, discoveryRepository, whitelistRepository)
        playerParticleService = PlayerParticleServiceBukkit(this, playerAttributeService)
        hologramService = HologramServiceBukkit(configService)
        worldService = WorldServiceBukkit()
    }

    private fun registerDependencies() {
        val repositories = module {
            single<WarpRepository> { warpRepository }
            single<DiscoveryRepository> { discoveryRepository }
            single<PlayerStateRepository> { playerStateRepository }
            single<WhitelistRepository> { whitelistRepository }
        }

        val actions = module {
            single { CreateWarp(warpRepository, playerAttributeService, structureBuilderService,
                discoveryRepository, structureParticleService, hologramService) }
            single { GetWarpPlayerAccess(discoveryRepository) }
            single { GetPlayerWarpAccess(discoveryRepository, warpRepository) }
            single { UpdateWarpIcon(warpRepository) }
            single { UpdateWarpName(warpRepository, hologramService) }
            single { GetWarpAtPosition(warpRepository) }
            single { BreakWarpBlock(warpRepository, structureBuilderService,
                discoveryRepository, structureParticleService, hologramService) }
            single { TeleportPlayer(teleportationService, playerAttributeService, playerParticleService,
                discoveryRepository)}
            single { LogPlayerMovement(movementMonitorService) }
            single { DiscoverWarp(discoveryRepository) }
            single { MoveWarp(warpRepository, structureBuilderService, structureParticleService, hologramService) }
            single { ToggleLock(warpRepository) }
            single { GetWhitelistedPlayers(whitelistRepository) }
            single { ToggleWhitelist(whitelistRepository, warpRepository) }
            single { RevokeDiscovery(discoveryRepository) }
            single { IsPositionInTeleportZone(warpRepository) }
            single { UpdateWarpSkin(warpRepository, structureBuilderService, configService) }
            single { IsValidWarpBase(configService) }
            single { GetAllWarpSkins(configService) }
            single { IsPlayerFavouriteWarp(discoveryRepository) }
            single { ToggleFavouriteDiscovery(discoveryRepository) }
            single { GetFavouritedWarpAccess(discoveryRepository, warpRepository) }
            single { GetOwnedWarps(warpRepository) }
            single { ListInvalidWarps(warpRepository, worldService) }
            single { RemoveAllInvalidWarps(warpRepository, worldService, discoveryRepository, whitelistRepository) }
            single { RemoveInvalidWarpsForWorld(warpRepository, worldService, discoveryRepository, whitelistRepository) }
        }

        startKoin { modules(repositories, actions) }
    }

    private fun registerCommands() {
        commandManager.registerCommand(WarpMenuCommand())
        commandManager.registerCommand(InvalidsCommand())
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(WaystoneInteractListener(configService), this)
        server.pluginManager.registerEvents(WaystoneDestructionListener(), this)
        server.pluginManager.registerEvents(PlayerMovementListener(), this)
        server.pluginManager.registerEvents(MoveToolListener(), this)
        server.pluginManager.registerEvents(ToolRemovalListener(), this)
        server.pluginManager.registerEvents(TeleportZoneProtectionListener(), this)
        server.pluginManager.registerEvents(WarpItemListener(configService), this)
        server.pluginManager.registerEvents(WaystoneBaseInteractListener(), this)
    }
}