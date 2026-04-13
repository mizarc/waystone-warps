package dev.mizarc.waystonewarps.infrastructure.persistence.migrations

import co.aikar.idb.Database

class Migration3_AddWarpGroups : Migration {
    override val fromVersion: Int = 2
    override val toVersion: Int = 3

    override fun migrate(db: Database) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS warp_groups (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                createdBy TEXT NOT NULL
            );
        """.trimIndent())
        runCatching {
            db.executeUpdate("ALTER TABLE warps ADD COLUMN groupId TEXT DEFAULT NULL;")
        }
    }
}
