package dev.mizarc.waystonewarps.interaction.menus

import org.bukkit.entity.Player
import kotlin.collections.ArrayDeque

/**
 * A menu hook to store navigated menus and allow for backwards travel.
 */
class MenuNavigator {
    private val menuStack = ArrayDeque<Menu>()

    /**
     * Opens the provided menu for the target player.
     *
     * Opening menus this way stores it to the navigation stack, which is
     * required for menus to understand how to go back when prompted for it.
     *
     * @param player The target player.
     * @param menu The menu to open.
     */
   fun openMenu(player: Player, menu: Menu) {
        menuStack.addFirst(menu)
        menu.open(player)
    }

    /**
     * Opens the previous menu in the navigation stack.
     *
     * This closes the currently displayed menu and moves the menu display back
     * a step in the stack when prompted for it.
     *
     * @param player The target player.
     */
    fun goBack(player: Player) {
        if (menuStack.isNotEmpty()) {
            menuStack.removeFirst()
            if (menuStack.isNotEmpty()) {
                menuStack.first().open(player)
            }
        }
    }

    /**
     * Clears the entire menu stack.
     *
     * @param player The target player.
     */
    fun clearMenuStack(player: Player) {
        menuStack.clear()
    }
}
