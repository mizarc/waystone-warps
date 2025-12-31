package dev.mizarc.waystonewarps.infrastructure.persistence.migrations

import co.aikar.idb.Database

class SchemaMigrator(
    private val db: Database,
    private val migrations: List<Migration>,
) {
    fun migrateToLatest() {
        ensureSchemaVersionTable()

        val currentVersion = getCurrentVersion()
        val latestVersion = migrations.maxOfOrNull { it.toVersion } ?: 0

        if (currentVersion > latestVersion) {
            throw IllegalStateException("Database schema version $currentVersion is newer than supported version $latestVersion")
        }

        val migrationByFromVersion = migrations.associateBy { it.fromVersion }
        val toVersionDuplicates = migrations.groupBy { it.toVersion }.filterValues { it.size > 1 }
        if (toVersionDuplicates.isNotEmpty()) {
            throw IllegalStateException("Duplicate migration toVersion values: ${toVersionDuplicates.keys.sorted()}")
        }

        var version = currentVersion
        while (version < latestVersion) {
            val migration = migrationByFromVersion[version]
                ?: throw IllegalStateException("Missing migration fromVersion=$version")

            if (migration.toVersion != version + 1) {
                throw IllegalStateException("Invalid migration chain: ${migration.fromVersion} -> ${migration.toVersion}")
            }

            runInTransaction {
                migration.migrate(db)
                setCurrentVersion(migration.toVersion)
            }

            version = migration.toVersion
        }
    }

    private fun ensureSchemaVersionTable() {
        db.executeUpdate(
            "CREATE TABLE IF NOT EXISTS schema_version (version INTEGER NOT NULL);"
        )

        val row = db.getResults("SELECT version FROM schema_version LIMIT 1;").firstOrNull()
        if (row == null) {
            db.executeUpdate("INSERT INTO schema_version (version) VALUES (0);")
        }
    }

    private fun getCurrentVersion(): Int {
        val row = db.getResults("SELECT version FROM schema_version LIMIT 1;").firstOrNull()
        return row?.getInt("version") ?: 0
    }

    private fun setCurrentVersion(version: Int) {
        db.executeUpdate("UPDATE schema_version SET version=?;", version)
    }

    private inline fun runInTransaction(block: () -> Unit) {
        db.executeUpdate("BEGIN IMMEDIATE;")
        try {
            block()
            db.executeUpdate("COMMIT;")
        } catch (ex: Exception) {
            runCatching { db.executeUpdate("ROLLBACK;") }
            throw ex
        }
    }
}
