package dev.mizarc.waystonewarps

import co.aikar.commands.PaperCommandManager
import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import dev.mizarc.waystonewarps.commands.HomeCommand
import dev.mizarc.waystonewarps.commands.SetspawnCommand
import dev.mizarc.waystonewarps.commands.SpawnCommand
import dev.mizarc.waystonewarps.commands.WarpMenuCommand
import dev.mizarc.waystonewarps.domain.HomeRepository
import dev.mizarc.waystonewarps.domain.PlayerRepository
import dev.mizarc.waystonewarps.domain.WarpAccessRepository
import dev.mizarc.waystonewarps.domain.WarpRepository
import dev.mizarc.waystonewarps.listeners.*

class WorldWideWarps: JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager
    private lateinit var metadata: Chat
    private val config = Config(this)
    private val storage = DatabaseStorage(this)
    val players = PlayerRepository()
    val homeRepository = HomeRepository(storage.connection)
    val warpRepository = WarpRepository(storage.connection)
    val warpAccessRepository = WarpAccessRepository(storage.connection, warpRepository)
    val teleporter = Teleporter(this, config, players)

    override fun onEnable() {
        logger.info(Chat::class.java.toString())
        val serviceProvider: RegisteredServiceProvider<Chat> = server.servicesManager.getRegistration(Chat::class.java)!!
        metadata = serviceProvider.provider
        commandManager = PaperCommandManager(this)
        dataFolder.mkdir()
        warpAccessRepository.init()
        registerDependencies()
        registerCommands()
        registerEvents()
        logger.info("WorldWideWarps has been Enabled")
    }

    override fun onDisable() {
        logger.info("WorldWideWarps has been Disabled")
    }

    private fun registerDependencies() {
        commandManager.registerDependency(Config::class.java, config)
        commandManager.registerDependency(DatabaseStorage::class.java, storage)
        commandManager.registerDependency(PlayerRepository::class.java, players)
        commandManager.registerDependency(Teleporter::class.java, teleporter)
        commandManager.registerDependency(WarpRepository::class.java, warpRepository)
        commandManager.registerDependency(WarpAccessRepository::class.java, warpAccessRepository)
    }

    private fun registerCommands() {
        commandManager.registerCommand(HomeCommand())
        commandManager.registerCommand(SpawnCommand())
        commandManager.registerCommand(SetspawnCommand())
        commandManager.registerCommand(WarpMenuCommand())
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(PlayerRegistrationListener(homeRepository, players, config, metadata), this)
        server.pluginManager.registerEvents(TeleportCancelListener(players), this)
        server.pluginManager.registerEvents(BedInteractListener(homeRepository, players), this)
        server.pluginManager.registerEvents(BedDestructionListener(homeRepository), this)
        server.pluginManager.registerEvents(WarpInteractListener(warpRepository, warpAccessRepository), this)
        server.pluginManager.registerEvents(WarpDestructionListener(warpRepository, warpAccessRepository), this)
        server.pluginManager.registerEvents(WarpMoveToolListener(warpRepository), this)
        server.pluginManager.registerEvents(WarpMoveToolRemovalListener(), this)
    }
}