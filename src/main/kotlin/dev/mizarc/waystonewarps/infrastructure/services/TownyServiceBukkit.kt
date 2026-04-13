package dev.mizarc.waystonewarps.infrastructure.services

import com.palmergames.bukkit.towny.TownyAPI
import dev.mizarc.waystonewarps.application.services.TownyService
import org.bukkit.Location

class TownyServiceBukkit : TownyService {
    override fun hasFreeTravel(locationA: Location, locationB: Location): Boolean {
        val townA = TownyAPI.getInstance().getTown(locationA) ?: return false
        val townB = TownyAPI.getInstance().getTown(locationB) ?: return false
        if (townA == townB) return true
        if (!townA.hasNation() || !townB.hasNation()) return false
        return townA.nation == townB.nation
    }
}
