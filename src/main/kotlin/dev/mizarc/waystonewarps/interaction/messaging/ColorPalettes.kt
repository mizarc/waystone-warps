package dev.mizarc.waystonewarps.interaction.messaging

import net.kyori.adventure.text.format.TextColor

enum class PrimaryColourPalette(val color: TextColor?) {
    PRIMARY(TextColor.fromHexString("#ffaa00")),
    INFO(TextColor.fromHexString("#589bbf")),
    SUCCESS(TextColor.fromHexString("#58bf78")),
    PENDING(TextColor.fromHexString("#e0d01e")),
    CANCELLED(TextColor.fromHexString("#d3270d")),
    FAILED(TextColor.fromHexString("#910f0f")),
    SPECIAL(TextColor.fromHexString("#980dd3")),
    UNAVAILABLE(TextColor.fromHexString("#808080"))
}

enum class AccentColourPalette(val color: TextColor?) {
    INFO(TextColor.fromHexString("#bf7c58")),
    SUCCESS(TextColor.fromHexString("#bf589e")),
    PENDING(TextColor.fromHexString("#1e2ee0")),
    CANCELLED(TextColor.fromHexString("#0db9d3")),
    FAILED(TextColor.fromHexString("#0f6462")),
    SPECIAL(TextColor.fromHexString("#d3980d"))
}