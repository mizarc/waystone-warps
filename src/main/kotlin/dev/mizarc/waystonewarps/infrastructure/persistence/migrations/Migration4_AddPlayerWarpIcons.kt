package dev.mizarc.waystonewarps.infrastructure.persistence.migrations

import co.aikar.idb.Database

class Migration4_AddPlayerWarpIcons : Migration {
    override val fromVersion: Int = 3
    override val toVersion: Int = 4

    override fun migrate(db: Database) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS player_warp_icons (
                playerId TEXT NOT NULL,
                warpId TEXT NOT NULL,
                icon TEXT NOT NULL,
                iconMeta TEXT NOT NULL DEFAULT '{}',
                PRIMARY KEY (playerId, warpId)
            );
        """.trimIndent())
    }
}
