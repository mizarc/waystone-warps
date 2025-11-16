package dev.mizarc.waystonewarps.application.results

enum class UpdateWarpNameResult {
    SUCCESS,
    WARP_NOT_FOUND,
    NAME_ALREADY_TAKEN,
    NAME_BLANK,
    NOT_AUTHORIZED
}