package dev.mizarc.waystonewarps

import co.aikar.commands.PaperCommandManager
import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import dev.mizarc.waystonewarps.commands.WarpMenuCommand
import dev.mizarc.waystonewarps.domain.players.PlayerStateRepository
import dev.mizarc.waystonewarps.domain.waystones.WaystoneRepositorySQLite
import dev.mizarc.waystonewarps.listeners.*

class WaystoneWarps: JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager
    private lateinit var metadata: Chat
    private val config = Config(this)
    private val storage = DatabaseStorage(this)
    val players = PlayerStateRepository()
    val waystoneRepositorySQLite = WaystoneRepositorySQLite(storage.connection)
    val teleporter = Teleporter(this, config, players)

    override fun onEnable() {
        logger.info(Chat::class.java.toString())
        val serviceProvider: RegisteredServiceProvider<Chat> = server.servicesManager.getRegistration(Chat::class.java)!!
        metadata = serviceProvider.provider
        commandManager = PaperCommandManager(this)
        registerDependencies()
        registerCommands()
        registerEvents()
        logger.info("WaystoneWarps has been Enabled")
    }

    override fun onDisable() {
        logger.info("WaystoneWarps has been Disabled")
    }

    private fun registerDependencies() {
        commandManager.registerDependency(Config::class.java, config)
        commandManager.registerDependency(DatabaseStorage::class.java, storage)
        commandManager.registerDependency(PlayerStateRepository::class.java, players)
        commandManager.registerDependency(Teleporter::class.java, teleporter)
        commandManager.registerDependency(WaystoneRepositorySQLite::class.java, waystoneRepositorySQLite)
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
        server.pluginManager.registerEvents(WarpInteractListener(waystoneRepositorySQLite, warpAccessRepository), this)
        server.pluginManager.registerEvents(WarpDestructionListener(waystoneRepositorySQLite, warpAccessRepository), this)
        server.pluginManager.registerEvents(WarpMoveToolListener(waystoneRepositorySQLite), this)
        server.pluginManager.registerEvents(WarpMoveToolRemovalListener(), this)
    }
}