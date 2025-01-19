package dev.mizarc.waystonewarps

import co.aikar.idb.Database
import co.aikar.idb.DatabaseOptions
import co.aikar.idb.PooledDatabaseOptions
import org.bukkit.plugin.Plugin

class DatabaseStorage(plugin: Plugin) {
    val connection: Database

    init {
        val options = DatabaseOptions.builder().sqlite(plugin.dataFolder.toString() + "/warps.db").build()
        connection = PooledDatabaseOptions.builder().options(options).createHikariDatabase()
    }
}
