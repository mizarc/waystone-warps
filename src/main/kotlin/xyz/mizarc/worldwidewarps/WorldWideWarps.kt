package xyz.mizarc.worldwidewarps

import co.aikar.commands.PaperCommandManager
import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import xyz.mizarc.worldwidewarps.commands.HomeCommand
import xyz.mizarc.worldwidewarps.commands.SetspawnCommand
import xyz.mizarc.worldwidewarps.commands.SpawnCommand
import xyz.mizarc.worldwidewarps.events.*

class WorldWideWarps: JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager
    private lateinit var metadata: Chat
    private val config = Config(this)
    private val storage = DatabaseStorage(this)
    val players = PlayerRepository()
    val homeRepository = HomeRepository(storage.connection, players)
    val warpRepository = WarpRepository(storage.connection)
    val warpAccessRepository = WarpAccessRepository(storage.connection, warpRepository)
    val teleporter = Teleporter(this, config, players)

    override fun onEnable() {
        logger.info(Chat::class.java.toString())
        val serviceProvider: RegisteredServiceProvider<Chat> = server.servicesManager.getRegistration(Chat::class.java)!!
        metadata = serviceProvider.provider
        commandManager = PaperCommandManager(this)
        dataFolder.mkdir()
        homeRepository.init()
        warpRepository.init()
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
    }

    private fun registerCommands() {
        commandManager.registerCommand(HomeCommand())
        commandManager.registerCommand(SpawnCommand())
        commandManager.registerCommand(SetspawnCommand())
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(PlayerRegistrationListener(homeRepository, players, config, metadata), this)
        server.pluginManager.registerEvents(TeleportCancelListener(players), this)
        server.pluginManager.registerEvents(BedInteractListener(homeRepository, players), this)
        server.pluginManager.registerEvents(BedDestructionListener(homeRepository), this)
        server.pluginManager.registerEvents(WarpInteractListener(warpRepository), this)
        server.pluginManager.registerEvents(WarpDestructionListener(warpRepository), this)
    }
}