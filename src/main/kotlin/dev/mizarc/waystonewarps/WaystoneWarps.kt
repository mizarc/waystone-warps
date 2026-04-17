package dev.mizarc.waystonewarps

import co.aikar.commands.PaperCommandManager
import co.aikar.idb.Database
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import dev.mizarc.waystonewarps.api.WaystoneWarpsAPI
import dev.mizarc.waystonewarps.application.actions.administration.ListInvalidWarps
import dev.mizarc.waystonewarps.application.actions.administration.RemoveAllInvalidWarps
import dev.mizarc.waystonewarps.application.actions.administration.RemoveInvalidWarpsForWorld
import dev.mizarc.waystonewarps.application.actions.discovery.DiscoverWarp
import dev.mizarc.waystonewarps.application.actions.discovery.GetFavouritedWarpAccess
import dev.mizarc.waystonewarps.application.actions.teleport.LogPlayerMovement
import dev.mizarc.waystonewarps.application.actions.teleport.TeleportPlayer
import dev.mizarc.waystonewarps.application.actions.teleport.TeleportPlayerImmediately
import dev.mizarc.waystonewarps.application.actions.world.BreakWarpBlock
import dev.mizarc.waystonewarps.application.actions.world.CreateWarp
import dev.mizarc.waystonewarps.application.actions.discovery.GetPlayerWarpAccess
import dev.mizarc.waystonewarps.application.actions.world.GetWarpAtPosition
import dev.mizarc.waystonewarps.application.actions.discovery.GetWarpPlayerAccess
import dev.mizarc.waystonewarps.application.actions.discovery.IsPlayerFavouriteWarp
import dev.mizarc.waystonewarps.application.actions.discovery.RevokeDiscovery
import dev.mizarc.waystonewarps.application.actions.discovery.ToggleFavouriteDiscovery
import dev.mizarc.waystonewarps.application.actions.groups.CreateWarpGroup
import dev.mizarc.waystonewarps.application.actions.groups.DeleteWarpGroup
import dev.mizarc.waystonewarps.application.actions.groups.GetAllWarpGroups
import dev.mizarc.waystonewarps.application.actions.groups.RenameWarpGroup
import dev.mizarc.waystonewarps.application.actions.management.AssignWarpGroup
import dev.mizarc.waystonewarps.application.actions.management.GetAllWarpSkins
import dev.mizarc.waystonewarps.application.actions.management.GetOwnedWarps
import dev.mizarc.waystonewarps.application.actions.management.GetPlayerWarpIcon
import dev.mizarc.waystonewarps.application.actions.management.RemovePlayerWarpIcon
import dev.mizarc.waystonewarps.application.actions.management.SetPlayerWarpIcon
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
import dev.mizarc.waystonewarps.application.services.TeleportationService
import dev.mizarc.waystonewarps.application.services.TownyService
import dev.mizarc.waystonewarps.application.services.scheduling.SchedulerService
import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.domain.playerstate.PlayerStateRepository
import dev.mizarc.waystonewarps.domain.warps.PlayerWarpIconRepository
import dev.mizarc.waystonewarps.domain.warps.WarpGroupRepository
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import dev.mizarc.waystonewarps.domain.whitelist.WhitelistRepository
import dev.mizarc.waystonewarps.domain.world.WorldService
import dev.mizarc.waystonewarps.infrastructure.localization.PropertiesLocalizationProvider
import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.java.JavaPlugin
import dev.mizarc.waystonewarps.interaction.commands.WarpGroupsCommand
import dev.mizarc.waystonewarps.interaction.commands.WarpMenuCommand
import dev.mizarc.waystonewarps.infrastructure.persistence.discoveries.DiscoveryRepositorySQLite
import dev.mizarc.waystonewarps.infrastructure.persistence.playerstate.PlayerStateRepositoryMemory
import dev.mizarc.waystonewarps.infrastructure.persistence.groups.WarpGroupRepositorySQLite
import dev.mizarc.waystonewarps.infrastructure.persistence.migrations.Migration0_CreateInitialTables
import dev.mizarc.waystonewarps.infrastructure.persistence.migrations.Migration1_AddWarpIconMeta
import dev.mizarc.waystonewarps.infrastructure.persistence.migrations.Migration2_AddWarpAccessLevel
import dev.mizarc.waystonewarps.infrastructure.persistence.migrations.Migration3_AddWarpGroups
import dev.mizarc.waystonewarps.infrastructure.persistence.migrations.Migration4_AddPlayerWarpIcons
import dev.mizarc.waystonewarps.infrastructure.persistence.migrations.SchemaMigrator
import dev.mizarc.waystonewarps.infrastructure.persistence.playericons.PlayerWarpIconRepositorySQLite
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.SQLiteStorage
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.Storage
import dev.mizarc.waystonewarps.infrastructure.persistence.warps.WarpRepositorySQLite
import dev.mizarc.waystonewarps.infrastructure.persistence.whitelist.WhitelistRepositorySQLite
import dev.mizarc.waystonewarps.infrastructure.services.*
import dev.mizarc.waystonewarps.infrastructure.services.teleportation.TeleportationServiceBukkit
import dev.mizarc.waystonewarps.infrastructure.services.TownyServiceBukkit
import dev.mizarc.waystonewarps.infrastructure.services.scheduling.SchedulerServiceBukkit
import dev.mizarc.waystonewarps.interaction.commands.GiveWarpstoneCommand
import dev.mizarc.waystonewarps.interaction.commands.InvalidsCommand
import dev.mizarc.waystonewarps.interaction.commands.WarpCreateCommand
import dev.mizarc.waystonewarps.interaction.listeners.*
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.ServicePriority
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class WaystoneWarps: JavaPlugin() {
    companion object {
        lateinit var api: WaystoneWarpsAPI
            private set

        @JvmStatic
        fun getAPI(): WaystoneWarpsAPI = api
    }

    private lateinit var commandManager: PaperCommandManager
    private lateinit var metadata: Chat
    private var economy: Economy? = null
    private var townyService: TownyService? = null
    private var worldGroupService: WorldGroupService? = null

    // Storage
    private lateinit var storage: Storage<Database>

    // Repositories
    internal lateinit var warpRepository: WarpRepository
    private lateinit var discoveryRepository: DiscoveryRepository
    private lateinit var playerStateRepository: PlayerStateRepository
    private lateinit var whitelistRepository: WhitelistRepository
    private lateinit var warpGroupRepository: WarpGroupRepository
    private lateinit var playerWarpIconRepository: PlayerWarpIconRepository

    // Services
    private lateinit var movementMonitorService: MovementMonitorService
    private lateinit var playerAttributeService: PlayerAttributeService
    private lateinit var structureBuilderService: StructureBuilderService
    private lateinit var teleportationService: TeleportationService
    private lateinit var structureParticleService: StructureParticleService
    private lateinit var playerParticleService: PlayerParticleService
    private lateinit var playerCountdownService: PlayerCountdownService
    private lateinit var worldService: WorldService
    private lateinit var hologramService: HologramService
    private lateinit var configService: ConfigService
    private lateinit var scheduler: SchedulerService
    private lateinit var warpEventPublisher: WarpEventPublisher
    private lateinit var playerLocaleService: PlayerLocaleService
    private lateinit var localizationProvider: LocalizationProvider

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
                    Migration2_AddWarpAccessLevel(),
                    Migration3_AddWarpGroups(),
                    Migration4_AddPlayerWarpIcons(),
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
        initialiseTownyDependency()
        initialiseMultiverseInventoriesDependency()
        initialiseRepositories()
        initialiseServices()
        initialiseLang()
        registerDependencies()
        registerCommands()
        registerEvents()
        registerRecipes()
        AddAllDisplays(warpRepository, structureBuilderService, hologramService).execute()

        // Initialise API
        api = WaystoneWarpsAPI(warpRepository)
        server.servicesManager.register( WaystoneWarpsAPI::class.java, api, this, ServicePriority.Normal )

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

    private fun initialiseTownyDependency() {
        if (Bukkit.getPluginManager().getPlugin("Towny") != null) {
            townyService = TownyServiceBukkit()
            logger.info("Towny detected; same-town warp travel will be free.")
        }
    }

    private fun initialiseMultiverseInventoriesDependency(){
        if (Bukkit.getPluginManager().getPlugin("Multiverse-Inventories") != null) {
            worldGroupService = WorldGroupBukkitMultiverse(this)
            logger.info("Multiverse-Inventories detected; inter-world-group warps can be enabled")
        }
    }

    private fun initialiseConfig() {
        saveDefaultConfig()
        reloadConfig()
        mergeConfig()
        getResource("config.yml")?.use { defaultConfigStream ->
            val sampleConfigFile = File(dataFolder, "sample-config.yml")
            try {
                Files.copy(defaultConfigStream, sampleConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: IOException) {
                logger.severe("Failed to copy config: ${e.message}")
            }
        } ?: logger.warning("Default config file not found in the plugin resources")
    }

    private fun mergeConfig() {
        val defaults = getResource("config.yml")?.use { stream ->
            YamlConfiguration.loadConfiguration(InputStreamReader(stream))
        } ?: return
        var changed = false
        for (key in defaults.getKeys(true)) {
            if (!config.isSet(key)) {
                config.set(key, defaults.get(key))
                changed = true
            }
        }
        if (changed) {
            saveConfig()
            reloadConfig()
        }
    }

    private fun initialiseRepositories() {
        warpRepository = WarpRepositorySQLite(storage)
        discoveryRepository = DiscoveryRepositorySQLite(storage)
        playerStateRepository = PlayerStateRepositoryMemory()
        whitelistRepository = WhitelistRepositorySQLite(storage)
        warpGroupRepository = WarpGroupRepositorySQLite(storage)
        playerWarpIconRepository = PlayerWarpIconRepositorySQLite(storage)
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
        teleportationService = TeleportationServiceBukkit(
            playerAttributeService, configService, movementMonitorService, whitelistRepository,
            playerStateRepository, scheduler, economy, townyService, worldGroupService
        )
        structureParticleService = StructureParticleServiceBukkit(this, discoveryRepository, whitelistRepository)
        playerParticleService = PlayerParticleServiceBukkit(this, playerAttributeService)
        hologramService = HologramServiceBukkit(configService)
        worldService = WorldServiceBukkit()
        warpEventPublisher = WarpEventPublisherBukkit()
        playerLocaleService = PlayerLocaleServicePaper()
        localizationProvider = PropertiesLocalizationProvider(configService, dataFolder, PlayerLocaleServicePaper())
        playerCountdownService = PlayerCountdownServiceBukkit(this, localizationProvider, playerAttributeService)
    }

    fun initialiseLang() {
        val defaultLanguageFilenames = listOf(
            "en.properties"
        )

        // Move languages to the required folder and add readme for override instructions
        defaultLanguageFilenames.forEach { filename ->
            val resourcePathInJar = "lang/defaults/$filename"
            saveResource(resourcePathInJar, true)
        }
        saveResource("lang/overrides/README.txt", true)
    }

    private fun registerDependencies() {
        val repositories = module {
            single<WarpRepository> { warpRepository }
            single<DiscoveryRepository> { discoveryRepository }
            single<PlayerStateRepository> { playerStateRepository }
            single<WhitelistRepository> { whitelistRepository }
            single<WarpGroupRepository> { warpGroupRepository }
            single<PlayerWarpIconRepository> { playerWarpIconRepository }
            single<ConfigService> { configService }
            single<TeleportationService> { teleportationService }
            single<WarpEventPublisher> { warpEventPublisher }
            single<WorldGroupService?> { worldGroupService }
        }

        val actions = module {
            single { CreateWarp(warpRepository, playerAttributeService, structureBuilderService,
                discoveryRepository, structureParticleService, hologramService, warpEventPublisher) }
            single { GetWarpPlayerAccess(discoveryRepository) }
            single { GetPlayerWarpAccess(discoveryRepository, warpRepository) }
            single { UpdateWarpIcon(warpRepository, warpEventPublisher) }
            single { UpdateWarpName(warpRepository, hologramService, warpEventPublisher) }
            single { GetWarpAtPosition(warpRepository) }
            single { BreakWarpBlock(warpRepository, structureBuilderService,
                discoveryRepository, whitelistRepository, structureParticleService, hologramService, warpEventPublisher) }
            single { TeleportPlayerImmediately(teleportationService) }
            single { TeleportPlayer(teleportationService, playerAttributeService, playerParticleService, playerCountdownService,
                discoveryRepository, warpEventPublisher, get())}
            single { LogPlayerMovement(movementMonitorService) }
            single { DiscoverWarp(discoveryRepository, warpEventPublisher) }
            single { MoveWarp(warpRepository, structureBuilderService, structureParticleService, hologramService, warpEventPublisher) }
            single { ToggleLock(warpRepository, warpEventPublisher) }
            single { GetWhitelistedPlayers(whitelistRepository) }
            single { ToggleWhitelist(whitelistRepository, warpRepository) }
            single { RevokeDiscovery(discoveryRepository) }
            single { IsPositionInTeleportZone(warpRepository) }
            single { UpdateWarpSkin(warpRepository, structureBuilderService, configService, warpEventPublisher) }
            single { IsValidWarpBase(configService) }
            single { GetAllWarpSkins(configService) }
            single { IsPlayerFavouriteWarp(discoveryRepository) }
            single { ToggleFavouriteDiscovery(discoveryRepository) }
            single { GetFavouritedWarpAccess(discoveryRepository, warpRepository) }
            single { GetOwnedWarps(warpRepository) }
            single { ListInvalidWarps(warpRepository, worldService) }
            single { RemoveAllInvalidWarps(warpRepository, worldService, discoveryRepository, whitelistRepository, warpEventPublisher) }
            single { RemoveInvalidWarpsForWorld(warpRepository, worldService, discoveryRepository, whitelistRepository, warpEventPublisher) }

            // Group actions
            single { CreateWarpGroup(warpGroupRepository) }
            single { DeleteWarpGroup(warpGroupRepository, warpRepository) }
            single { RenameWarpGroup(warpGroupRepository) }
            single { GetAllWarpGroups(warpGroupRepository) }
            single { AssignWarpGroup(warpRepository, warpGroupRepository) }

            // Personal icon actions
            single { SetPlayerWarpIcon(playerWarpIconRepository) }
            single { RemovePlayerWarpIcon(playerWarpIconRepository) }
            single { GetPlayerWarpIcon(playerWarpIconRepository) }

            single<LocalizationProvider> { PropertiesLocalizationProvider(configService, dataFolder, playerLocaleService) }
        }

        startKoin { modules(repositories, actions) }
    }

    private fun registerCommands() {
        commandManager.registerCommand(WarpMenuCommand())
        commandManager.registerCommand(InvalidsCommand())
        commandManager.registerCommand(WarpCreateCommand())
        commandManager.registerCommand(GiveWarpstoneCommand())
        commandManager.registerCommand(WarpGroupsCommand())
    }

    private fun registerRecipes() {
        val key = NamespacedKey(this, "warpstone")
        server.removeRecipe(key)
        val result = ItemStack(Material.ECHO_SHARD, 4)
        result.setData(DataComponentTypes.ITEM_MODEL, Key.key("minecraft:warpstone"))
        result.setData(DataComponentTypes.CUSTOM_NAME,
            Component.text("Warp Stone")
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false))
        result.setData(DataComponentTypes.LORE, ItemLore.lore()
            .addLine(Component.text("A compact stone attuned to long-distance travel.")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, true))
            .build())
        val recipe = ShapedRecipe(key, result)
        recipe.shape("QGQ", "GEG", "QGQ")
        recipe.setIngredient('Q', Material.QUARTZ)
        recipe.setIngredient('G', Material.GOLD_INGOT)
        recipe.setIngredient('E', Material.ENDER_PEARL)
        server.addRecipe(recipe)
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
