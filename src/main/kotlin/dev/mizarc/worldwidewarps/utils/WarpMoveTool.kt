package dev.mizarc.worldwidewarps.utils

import dev.mizarc.worldwidewarps.domain.Warp
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun getWarpMoveTool(warp: Warp): ItemStack {
    return ItemStack(Material.LODESTONE)
        .name("§bMove Warp '${warp.name}'")
        .lore("Place this where you want the new location to be.")
        .setStringMeta("warp", warp.id.toString())
}