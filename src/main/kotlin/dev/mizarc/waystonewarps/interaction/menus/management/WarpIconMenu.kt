package dev.mizarc.waystonewarps.interaction.menus.management

import IconMeta
import com.destroystokyo.paper.profile.ProfileProperty
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.FurnaceGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpIcon
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.utils.PermissionHelper
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

class WarpIconMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator, 
    private val warp: Warp
): Menu, KoinComponent {
    private val updateWarpIcon: UpdateWarpIcon by inject()
    private val localizationProvider: LocalizationProvider by inject()

    @Suppress("UnstableApiUsage")
    override fun open() {
        // Check if the player has permission to change the icon
        val canChangeIcon = PermissionHelper.canChangeIcon(player, warp.playerId)
        if (!canChangeIcon) {
            menuNavigator.goBack()
            return
        }

        val title = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ICON_TITLE)
        val gui = FurnaceGui(title)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        val fuelPane = StaticPane(0, 0, 1, 1)

        // Add info paper menu item
        val paperItem = ItemStack(Material.PAPER)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ICON_INFO_ITEM_NAME), PrimaryColourPalette.INFO.color!!)
            .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ICON_INFO_ITEM_LORE))
        val guiIconEditorItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
        fuelPane.addItem(guiIconEditorItem, 0, 0)
        gui.fuelComponent.addPane(fuelPane)

        // Allow item to be placed in the slot
        val inputPane = StaticPane(0, 0, 1, 1)
        inputPane.setOnClick {guiEvent ->
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

        // Add confirm menu item
        val outputPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ICON_CONFIRM_ITEM_NAME),
                PrimaryColourPalette.SUCCESS.color!!)
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            guiEvent.isCancelled = true
            val newIcon = gui.ingredientComponent.getItem(0, 0)
            val registryAccess = RegistryAccess.registryAccess()

            // Set icon if the item in slot
            if (newIcon != null) {
                // Get potion type
                val potionTypeKey = (newIcon.itemMeta as? PotionMeta)
                    ?.basePotionType
                    ?.key
                    ?.toString()

                // Get leather armour colour
                val leatherColorRgb = (newIcon.itemMeta as? LeatherArmorMeta)
                    ?.color
                    ?.asRGB()

                // Get armour trim pattern and material
                val armorTrim = (newIcon.itemMeta as? ArmorMeta)?.trim
                val patternRegistry = registryAccess.getRegistry(RegistryKey.TRIM_PATTERN)
                val materialRegistry = registryAccess.getRegistry(RegistryKey.TRIM_MATERIAL)
                val trimPatternKey = armorTrim?.let { patternRegistry.getKey(it.pattern).toString() }
                val trimMaterialKey = armorTrim?.let { materialRegistry.getKey(it.material).toString() }

                // Get banner pattern and colour
                val bannerPatternRegistry = registryAccess.getRegistry(RegistryKey.BANNER_PATTERN)
                val bannerMeta = newIcon.itemMeta as? BannerMeta
                val bannerPatterns = bannerMeta?.patterns?.mapNotNull { p ->
                    val patternKey = bannerPatternRegistry.getKey(p.pattern)
                    if (patternKey != null) {
                        "${patternKey}|${p.color.name}"
                    } else {
                        null
                    }
                } ?: emptyList()

                // Get skull skin
                val skullMeta = newIcon.itemMeta as? SkullMeta
                val textures: ProfileProperty? = skullMeta
                    ?.playerProfile
                    ?.properties
                    ?.firstOrNull { it.name.equals("textures", ignoreCase = true) }
                val skullTextureValue = textures?.value
                val skullTextureSignature = textures?.signature

                // Get firework colour
                val fireworkStarColorRgb = (newIcon.itemMeta as? FireworkEffectMeta)
                    ?.effect
                    ?.colors
                    ?.firstOrNull()
                    ?.asRGB()

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

                val result = updateWarpIcon.execute(
                    editorPlayerId = player.uniqueId,
                    warpId = warp.id,
                    materialName = newIcon.type.name,
                    iconMeta = iconMeta,
                    bypassOwnership = player.hasPermission("waystonewarps.bypass.icon"),
                )
            }

            // Go back to edit menu
            menuNavigator.goBack()
        }
        outputPane.addItem(confirmGuiItem, 0, 0)
        gui.outputComponent.addPane(outputPane)
        gui.show(player)
    }
}