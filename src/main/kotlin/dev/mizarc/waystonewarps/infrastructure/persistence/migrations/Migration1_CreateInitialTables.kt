package dev.mizarc.waystonewarps.infrastructure.persistence.migrations

import co.aikar.idb.Database

class Migration1_CreateInitialTables : Migration {
    override val fromVersion: Int = 0
    override val toVersion: Int = 1

    override fun migrate(db: Database) {
        db.executeUpdate(
            "CREATE TABLE IF NOT EXISTS warps (id TEXT NOT NULL, " +
                "playerId TEXT NOT NULL, creationTime TEXT NOT NULL, name TEXT, worldId TEXT NOT NULL, " +
                "positionX INTEGER NOT NULL, positionY INTEGER NOT NULL, positionZ INTEGER NOT NULL, icon TEXT, iconMeta TEXT," +
                "block TEXT, isLocked INTEGER);"
        )

        db.executeUpdate(
            "CREATE TABLE IF NOT EXISTS discoveries (warpId TEXT, " +
                "playerId TEXT, discoveredTime TEXT, lastVisitedTime TEXT, isFavourite INTEGER," +
                "PRIMARY KEY(warpId, playerId));"
        )

        db.executeUpdate(
            "CREATE TABLE IF NOT EXISTS whitelist (" +
                "warpId TEXT NOT NULL," +
                "playerId TEXT NOT NULL," +
                "creationTime TEXT NOT NULL," +
                "PRIMARY KEY (warpId, playerId)" +
                ");"
        )
    }
}
