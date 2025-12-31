package dev.mizarc.waystonewarps.infrastructure.persistence.migrations

import co.aikar.idb.Database

class Migration1_AddWarpIconMeta : Migration {
    override val fromVersion: Int = 0
    override val toVersion: Int = 1

    override fun migrate(db: Database) {
        runCatching {
            db.executeUpdate("ALTER TABLE warps ADD COLUMN iconMeta TEXT;")
        }
    }
}
