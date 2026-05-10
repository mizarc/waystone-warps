package dev.mizarc.waystonewarps.application.services

import org.bukkit.Location

interface TownyService {
    fun hasFreeTravel(locationA: Location, locationB: Location): Boolean
}
