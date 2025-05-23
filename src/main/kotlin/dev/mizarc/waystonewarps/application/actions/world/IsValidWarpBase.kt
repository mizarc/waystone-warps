package dev.mizarc.waystonewarps.application.actions.world

import dev.mizarc.waystonewarps.application.services.ConfigService

class IsValidWarpBase(private val configService: ConfigService) {
    fun execute(blockName: String): Boolean {
        return configService.getStructureBlocks(blockName).isNotEmpty()
    }
}