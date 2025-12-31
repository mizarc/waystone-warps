package dev.mizarc.waystonewarps.infrastructure.persistence.migrations

import co.aikar.idb.Database

interface Migration {
    val fromVersion: Int
    val toVersion: Int

    fun migrate(db: Database)
}
