package dev.mizarc.waystonewarps.interaction.menus.management

import IconMeta
import com.destroystokyo.paper.profile.ProfileProperty
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.FurnaceGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.management.GetPlayerWarpIcon
import dev.mizarc.waystonewarps.application.actions.management.RemovePlayerWarpIcon
import dev.mizarc.waystonewarps.application.actions.management.SetPlayerWarpIcon
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.utils.applyIconMeta
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ArmorMeta
import org.bukkit.inventory.meta.BannerMeta
import org.bukkit.inventory.meta.FireworkEffectMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.SkullMeta
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.concurrent.thread

class PlayerWarpIconMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val warp: Warp
) : Menu, KoinComponent {
    private val getPlayerWarpIcon: GetPlayerWarpIcon by inject()
    private val setPlayerWarpIcon: SetPlayerWarpIcon by inject()
    private val removePlayerWarpIcon: RemovePlayerWarpIcon by inject()
    private val localizationProvider: LocalizationProvider by inject()

    @Suppress("UnstableApiUsage")
    override fun open() {
        val title = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_PLAYER_WARP_ICON_TITLE)
        val gui = FurnaceGui(title)
        gui.setOnTopClick { it.isCancelled = true }

        val existingOverride = getPlayerWarpIcon.execute(player.uniqueId, warp.id)

        // Fuel slot: Reset button (if override exists) or info paper
        val fuelPane = StaticPane(0, 0, 1, 1)
        if (existingOverride != null) {
            val resetItem = ItemStack(Material.valueOf(existingOverride.icon))
                .applyIconMeta(existingOverride.iconMeta)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_PLAYER_WARP_ICON_RESET_NAME), PrimaryColourPalette.CANCELLED.color!!)
                .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_PLAYER_WARP_ICON_RESET_LORE))
            fuelPane.addItem(GuiItem(resetItem) { guiEvent ->
                guiEvent.isCancelled = true
                removePlayerWarpIcon.execute(player.uniqueId, warp.id)
                menuNavigator.goBack()
            }, 0, 0)
        } else {
            val paperItem = ItemStack(Material.PAPER)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_PLAYER_WARP_ICON_INFO_NAME), PrimaryColourPalette.INFO.color!!)
                .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_PLAYER_WARP_ICON_INFO_LORE))
            fuelPane.addItem(GuiItem(paperItem) { it.isCancelled = true }, 0, 0)
        }
        gui.fuelComponent.addPane(fuelPane)

        // Input slot: player places their chosen item here
        val inputPane = StaticPane(0, 0, 1, 1)
        inputPane.setOnClick { guiEvent ->
            guiEvent.isCancelled = true
            val itemOnCursor = guiEvent.cursor
            if (itemOnCursor.type == Material.AIR) {
                inputPane.removeItem(0, 0)
                gui.update()
                return@setOnClick
            }
            inputPane.addItem(GuiItem(ItemStack(itemOnCursor.clone())), 0, 0)
            gui.update()
            thread(start = true) {
                Thread.sleep(1)
                player.setItemOnCursor(itemOnCursor)
            }
        }
        gui.ingredientComponent.addPane(inputPane)

        // Output slot: confirm
        val outputPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_CONFIRM_NAME), PrimaryColourPalette.SUCCESS.color!!)
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            guiEvent.isCancelled = true
            val newIcon = gui.ingredientComponent.getItem(0, 0)

            if (newIcon != null) {
                val registryAccess = RegistryAccess.registryAccess()

                val potionTypeKey = (newIcon.itemMeta as? PotionMeta)?.basePotionType?.key?.toString()
                val leatherColorRgb = (newIcon.itemMeta as? LeatherArmorMeta)?.color?.asRGB()
                val armorTrim = (newIcon.itemMeta as? ArmorMeta)?.trim
                val patternRegistry = registryAccess.getRegistry(RegistryKey.TRIM_PATTERN)
                val materialRegistry = registryAccess.getRegistry(RegistryKey.TRIM_MATERIAL)
                val trimPatternKey = armorTrim?.let { patternRegistry.getKey(it.pattern).toString() }
                val trimMaterialKey = armorTrim?.let { materialRegistry.getKey(it.material).toString() }
                val bannerPatternRegistry = registryAccess.getRegistry(RegistryKey.BANNER_PATTERN)
                val bannerMeta = newIcon.itemMeta as? BannerMeta
                val bannerPatterns = bannerMeta?.patterns?.mapNotNull { p ->
                    val patternKey = bannerPatternRegistry.getKey(p.pattern)
                    if (patternKey != null) "${patternKey}|${p.color.name}" else null
                } ?: emptyList()
                val skullMeta = newIcon.itemMeta as? SkullMeta
                val textures: ProfileProperty? = skullMeta?.playerProfile?.properties
                    ?.firstOrNull { it.name.equals("textures", ignoreCase = true) }
                val skullTextureValue = textures?.value
                val skullTextureSignature = textures?.signature
                val fireworkStarColorRgb = (newIcon.itemMeta as? FireworkEffectMeta)?.effect?.colors?.firstOrNull()?.asRGB()
                val paperCmd = newIcon.getData(DataComponentTypes.CUSTOM_MODEL_DATA)
                val iconMeta = if (paperCmd != null) {
                    IconMeta(
                        strings = paperCmd.strings(),
                        floats = paperCmd.floats(),
                        flags = paperCmd.flags(),
                        colorsArgb = paperCmd.colors().map { it.asARGB() },
                        potionTypeKey = potionTypeKey,
                        leatherColorRgb = leatherColorRgb,
                        trimPatternKey = trimPatternKey,
                        trimMaterialKey = trimMaterialKey,
                        bannerPatterns = bannerPatterns,
                        skullTextureValue = skullTextureValue,
                        skullTextureSignature = skullTextureSignature,
                        fireworkStarColorRgb = fireworkStarColorRgb
                    )
                } else {
                    IconMeta(
                        potionTypeKey = potionTypeKey,
                        leatherColorRgb = leatherColorRgb,
                        trimPatternKey = trimPatternKey,
                        trimMaterialKey = trimMaterialKey,
                        bannerPatterns = bannerPatterns,
                        skullTextureValue = skullTextureValue,
                        skullTextureSignature = skullTextureSignature,
                        fireworkStarColorRgb = fireworkStarColorRgb
                    )
                }
                setPlayerWarpIcon.execute(player.uniqueId, warp.id, newIcon.type.name, iconMeta)
            }

            menuNavigator.goBack()
        }
        outputPane.addItem(confirmGuiItem, 0, 0)
        gui.outputComponent.addPane(outputPane)
        gui.show(player)
    }
}
