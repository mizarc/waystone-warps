package dev.mizarc.waystonewarps.application.services

import java.util.*

interface PlayerLocaleService {
    fun getLocale(playerId: UUID): String
}