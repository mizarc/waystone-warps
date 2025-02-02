package dev.mizarc.waystonewarps.application.services

import java.util.*

interface MessagingService {
    fun sendChatMessage(playerId: UUID, message: String)
    fun sendActionMessage(playerId: UUID, message: String)
    fun sendAlertMessage(playerId: UUID, message: String)
}