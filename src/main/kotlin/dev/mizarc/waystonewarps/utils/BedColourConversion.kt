package dev.mizarc.waystonewarps.utils

import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.block.data.type.Bed

fun DyeColor.toBed(): Material {
    return when (this) {
        DyeColor.BLACK -> Material.BLACK_BED
        DyeColor.WHITE -> Material.WHITE_BED
        DyeColor.ORANGE -> Material.ORANGE_BED
        DyeColor.MAGENTA -> Material.MAGENTA_BED
        DyeColor.LIGHT_BLUE -> Material.LIGHT_BLUE_BED
        DyeColor.YELLOW -> Material.YELLOW_BED
        DyeColor.LIME -> Material.LIME_BED
        DyeColor.PINK -> Material.PINK_BED
        DyeColor.GRAY -> Material.GRAY_BED
        DyeColor.LIGHT_GRAY -> Material.LIGHT_GRAY_BED
        DyeColor.CYAN -> Material.CYAN_BED
        DyeColor.PURPLE -> Material.PURPLE_BED
        DyeColor.BLUE -> Material.BLUE_BED
        DyeColor.BROWN -> Material.BROWN_BED
        DyeColor.GREEN -> Material.GREEN_BED
        DyeColor.RED -> Material.RED_BED
    }
}

fun Bed.getColour(): DyeColor {
    return when (this.material) {
        Material.BLACK_BED -> DyeColor.BLACK
        Material.WHITE_BED -> DyeColor.WHITE
        Material.ORANGE_BED -> DyeColor.ORANGE
        Material.MAGENTA_BED -> DyeColor.MAGENTA
        Material.LIGHT_BLUE_BED -> DyeColor.LIGHT_BLUE
        Material.YELLOW_BED -> DyeColor.YELLOW
        Material.LIME_BED -> DyeColor.LIME
        Material.PINK_BED -> DyeColor.PINK
        Material.GRAY_BED -> DyeColor.GRAY
        Material.LIGHT_GRAY_BED -> DyeColor.LIGHT_GRAY
        Material.CYAN_BED -> DyeColor.CYAN
        Material.PURPLE_BED -> DyeColor.PURPLE
        Material.BLUE_BED -> DyeColor.BLUE
        Material.BROWN_BED -> DyeColor.BROWN
        Material.GREEN_BED -> DyeColor.GREEN
        Material.RED_BED -> DyeColor.RED
        else -> DyeColor.WHITE
    }
}