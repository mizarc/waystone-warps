package dev.mizarc.waystonewarps.application.results

import dev.mizarc.waystonewarps.domain.warps.Warp

/**
 * Represents the possible outcomes of the CreateWarp action.
*/
sealed class CreateWarpResult {
    data class Success(val warp: Warp) : CreateWarpResult()
    data object NameAlreadyExists : CreateWarpResult()
    data object NameCannotBeBlank : CreateWarpResult()
    data object LimitExceeded : CreateWarpResult()
}