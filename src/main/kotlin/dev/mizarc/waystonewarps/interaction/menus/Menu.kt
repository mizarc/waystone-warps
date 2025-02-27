package dev.mizarc.waystonewarps.interaction.menus

import org.bukkit.entity.Player

interface Menu {
    fun open()
    fun passData(data: Any?) {
        // Default implementation does nothing
    }
}