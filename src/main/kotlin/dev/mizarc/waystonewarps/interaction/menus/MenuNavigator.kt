package dev.mizarc.waystonewarps.interaction.menus

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
     * @param menu The menu to open.
     */
   fun openMenu(menu: Menu) {
        menuStack.addFirst(menu)
        menu.open()
    }

    /**
     * Opens the previous menu in the navigation stack.
     *
     * This closes the currently displayed menu and moves the menu display back
     * a step in the stack when prompted for it.
     */
    fun goBack() {
        navigateBack()
    }

    /**
     * Clears the entire menu stack.
     *
     * This can be used to ensure that going back will instead close out of the
     * menu.
     */
    fun clearMenuStack() {
        menuStack.clear()
    }

    private fun navigateBack(data: Any? = null) {
        if (menuStack.isNotEmpty()) {
            menuStack.removeFirst()
            if (menuStack.isNotEmpty()) {
                menuStack.first().passData(data)
                menuStack.first().open()
            }
        }
    }
}
