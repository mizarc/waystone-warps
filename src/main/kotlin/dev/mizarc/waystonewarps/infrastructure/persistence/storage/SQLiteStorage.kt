package dev.mizarc.waystonewarps.infrastructure.persistence.storage

import co.aikar.idb.Database
import co.aikar.idb.DatabaseOptions
import co.aikar.idb.PooledDatabaseOptions
import java.io.File

class SQLiteStorage(dataFolder: File): Storage<Database> {
    override val connection: Database

    init {
        val options = DatabaseOptions.builder().sqlite("$dataFolder/storage.sqlite").build()
        connection = PooledDatabaseOptions.builder().options(options).createHikariDatabase()
    }
}