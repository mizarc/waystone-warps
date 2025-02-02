package dev.mizarc.waystonewarps.application.services

import java.util.*

interface MessagingService {
    fun sendChatMessage(playerId: UUID, message: String): Result<Unit>
    fun sendActionMessage(playerId: UUID, message: String): Result<Unit>
    fun sendTitleMessage(playerId: UUID, titleText: String, subtitleText: String): Result<Unit>
}