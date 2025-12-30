package dev.mizarc.waystonewarps.interaction.utils

import IconMeta
import com.destroystokyo.paper.profile.ProfileProperty
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.material.MaterialData
import org.bukkit.persistence.PersistentDataType
import java.util.*
import java.util.function.Consumer
import org.bukkit.Color
import org.bukkit.block.banner.Pattern
import org.bukkit.inventory.meta.ArmorMeta
import org.bukkit.inventory.meta.BannerMeta
import org.bukkit.inventory.meta.FireworkEffectMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.inventory.meta.trim.ArmorTrim


fun ItemStack.amount(amount: Int): ItemStack {
    setAmount(amount)
    return this
}

fun ItemStack.name(name: String): ItemStack {
    val meta = itemMeta
    meta.itemName(Component.text(name))
    itemMeta = meta
    return this
}

fun ItemStack.lore(text: String): ItemStack {
    val meta = itemMeta
    var lore: MutableList<String>? = meta!!.lore
    if (lore == null) {
        lore = ArrayList()
    }
    lore.add(text)
    meta.lore = lore.c()
    itemMeta = meta
    return this
}

fun ItemStack.lore(vararg text: String): ItemStack {
    Arrays.stream(text).forEach { this.lore(it) }
    return this
}

fun ItemStack.lore(text: List<String>): ItemStack {
    this.clearLore()
    text.forEach { this.lore(it) }
    return this
}

fun ItemStack.durability(durability: Int): ItemStack {
    setDurability(durability.toShort())
    return this
}

fun ItemStack.data(data: Int): ItemStack {
    setData(MaterialData(type, data.toByte()))
    return this
}

fun ItemStack.enchantment(enchantment: Enchantment, level: Int): ItemStack {
    addUnsafeEnchantment(enchantment, level)
    return this
}

fun ItemStack.enchantment(enchantment: Enchantment): ItemStack {
    addUnsafeEnchantment(enchantment, 1)
    return this
}

fun ItemStack.type(material: Material): ItemStack {
    type = material
    return this
}

fun ItemStack.clearLore(): ItemStack {
    val meta = itemMeta
    meta!!.lore = ArrayList()
    itemMeta = meta
    return this
}

fun ItemStack.clearEnchantments(): ItemStack {
    enchantments.keys.forEach(Consumer<Enchantment> { this.removeEnchantment(it) })
    return this
}

fun ItemStack.color(color: Color): ItemStack {
    if (type == Material.LEATHER_BOOTS
        || type == Material.LEATHER_CHESTPLATE
        || type == Material.LEATHER_HELMET
        || type == Material.LEATHER_LEGGINGS) {

        val meta = itemMeta as LeatherArmorMeta
        meta.setColor(color)
        itemMeta = meta
        return this
    } else {
        throw IllegalArgumentException("Colors only applicable for leather armor!")
    }
}

fun ItemStack.flag(vararg flag: ItemFlag): ItemStack {
    val meta = itemMeta
    meta!!.addItemFlags(*flag)
    itemMeta = meta
    return this
}

fun ItemStack.getBooleanMeta(key: String): String? {
    val meta = itemMeta ?: return null
    return meta.persistentDataContainer.get(
        NamespacedKey("waystonewarps",key), PersistentDataType.STRING)
}

fun ItemStack.setBooleanMeta(key: String, value: Boolean): ItemStack {
    val meta = itemMeta
    meta.persistentDataContainer.set(
        NamespacedKey("waystonewarps",key), PersistentDataType.BOOLEAN, value)
    itemMeta = meta
    return this
}

fun ItemStack.getStringMeta(key: String): String? {
    val meta = itemMeta ?: return null
    return meta.persistentDataContainer.get(
        NamespacedKey("waystonewarps",key), PersistentDataType.STRING)
}

fun ItemStack.setStringMeta(key: String, value: String): ItemStack {
    val meta = itemMeta
    meta.persistentDataContainer.set(
        NamespacedKey("waystonewarps",key), PersistentDataType.STRING, value)
    itemMeta = meta
    return this
}

@Suppress("UnstableApiUsage")
fun ItemStack.applyIconMeta(meta: IconMeta): ItemStack {

    // Custom model data
    val builder = CustomModelData.customModelData()
    meta.strings.forEach(builder::addString)
    meta.floats.forEach(builder::addFloat)
    meta.flags.forEach(builder::addFlag)
    meta.colorsArgb.forEach { argb -> builder.addColor(Color.fromARGB(argb)) }

    // Potion base type
    if (meta.potionTypeKey != null) {
        val potionKey = NamespacedKey.fromString(meta.potionTypeKey)
        if (potionKey != null) {
            val potionType = Registry.POTION.get(potionKey)
            if (potionType != null) {
                val im = this.itemMeta
                if (im is PotionMeta) {
                    im.basePotionType = potionType
                    this.itemMeta = im
                }
            }
        }
    }

    // Leather armor dye color
    if (meta.leatherColorRgb != null) {
        val im = this.itemMeta
        if (im is LeatherArmorMeta) {
            im.setColor(Color.fromRGB(meta.leatherColorRgb))
            this.itemMeta = im
        }
    }

    // Armor trim
    val registryAccess = RegistryAccess.registryAccess()
    val trimPatternRegistry = registryAccess.getRegistry(RegistryKey.TRIM_PATTERN)
    val trimMaterialRegistry = registryAccess.getRegistry(RegistryKey.TRIM_MATERIAL)
    if (meta.trimPatternKey != null && meta.trimMaterialKey != null) {
        val patternKey = NamespacedKey.fromString(meta.trimPatternKey)
        val materialKey = NamespacedKey.fromString(meta.trimMaterialKey)
        if (patternKey != null && materialKey != null) {
            val pattern = trimPatternRegistry.get(patternKey)
            val material = trimMaterialRegistry.get(materialKey)
            if (pattern != null && material != null) {
                (this.itemMeta as? ArmorMeta)?.let { im ->
                    im.trim = ArmorTrim(material, pattern)
                    this.itemMeta = im
                }
            }
        }
    }

    // Banner base color + patterns (stored as "<patternKey>|<DyeColorName>")
    val bannerRegistry = registryAccess.getRegistry(RegistryKey.BANNER_PATTERN)
    if (meta.bannerBaseColor != null || meta.bannerPatterns.isNotEmpty()) {
        (this.itemMeta as? BannerMeta)?.let { im ->
            if (meta.bannerPatterns.isNotEmpty()) {
                val patterns = meta.bannerPatterns.mapNotNull { raw ->
                    val parts = raw.split("|", limit = 2)
                    if (parts.size != 2) return@mapNotNull null

                    val pKey = NamespacedKey.fromString(parts[0]) ?: return@mapNotNull null
                    val pType = bannerRegistry.get(pKey) ?: return@mapNotNull null
                    val dye = runCatching { DyeColor.valueOf(parts[1]) }.getOrNull() ?: return@mapNotNull null

                    Pattern(dye, pType)
                }
                im.patterns = patterns
            }

            this.itemMeta = im
        }
    }

    // Skull texture (freeze at time of set): use textures property if present
    if (meta.skullTextureValue != null) {
        (this.itemMeta as? SkullMeta)?.let { im ->
            val profile = Bukkit.createProfile(UUID.randomUUID(), null)
            profile.setProperty(ProfileProperty("textures", meta.skullTextureValue, meta.skullTextureSignature))
            im.playerProfile = profile
            this.itemMeta = im
        }
    }

    // Firework star color (primary)
    meta.fireworkStarColorRgb?.let { rgb ->
        (this.itemMeta as? FireworkEffectMeta)?.let { im ->
            val existing = im.effect
            val b = FireworkEffect.builder()

            if (existing != null) {
                b.with(existing.type)
                if (existing.hasFlicker()) b.flicker(true)
                if (existing.hasTrail()) b.trail(true)
                b.withFade(existing.fadeColors)
            }

            b.withColor(Color.fromRGB(rgb))
            im.effect = b.build()
            this.itemMeta = im
        }
    }

    this.setData(DataComponentTypes.CUSTOM_MODEL_DATA, builder.build())
    return this
}

private fun String.c(): String {
    return ChatColor.translateAlternateColorCodes('&', this)
}

private fun List<String>.c(): List<String> {
    val tempStringList = ArrayList<String>()
    for (text in this) {
        tempStringList.add(text.c())
    }
    return tempStringList
}