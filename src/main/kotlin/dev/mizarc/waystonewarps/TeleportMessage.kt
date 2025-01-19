package dev.mizarc.waystonewarps

enum class TeleportMessage(val messageString: String) {
    NONE(""),
    HOME("§aWelcome home."),
    HOMESWAP("§aYou have shifted home"),
    SPAWN("§aWelcome to spawn."),
    WARP("§aYou have warped."),
    INVITEACCEPT("§aYou have accepted the invite to §6{name}'s §ahome"),
}