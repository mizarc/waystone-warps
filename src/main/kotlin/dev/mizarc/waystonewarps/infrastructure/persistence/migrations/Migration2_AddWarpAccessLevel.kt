package dev.mizarc.waystonewarps.infrastructure.persistence.migrations

import co.aikar.idb.Database

class Migration2_AddWarpAccessLevel : Migration {
    override val fromVersion: Int = 1
    override val toVersion: Int = 2

    override fun migrate(db: Database) {
        runCatching {
            db.executeUpdate("ALTER TABLE warps ADD COLUMN accessLevel TEXT NOT NULL DEFAULT 'PUBLIC';")
        }
        db.executeUpdate("UPDATE warps SET accessLevel = 'PRIVATE' WHERE isLocked = 1;")
        db.executeUpdate("UPDATE warps SET accessLevel = 'PUBLIC' WHERE isLocked = 0;")
    }
}
