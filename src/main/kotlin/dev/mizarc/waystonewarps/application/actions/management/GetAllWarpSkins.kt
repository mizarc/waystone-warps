package dev.mizarc.waystonewarps.application.actions.management

import dev.mizarc.waystonewarps.application.services.ConfigService

class GetAllWarpSkins(private val configService: ConfigService) {

    fun execute(): List<String> {
        return configService.getAllSkinTypes()
    }
}