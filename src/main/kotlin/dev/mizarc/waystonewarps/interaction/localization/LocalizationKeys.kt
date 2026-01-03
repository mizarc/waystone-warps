package dev.mizarc.waystonewarps.interaction.localization

object LocalizationKeys {
    // -------------------------------------
    // General Messages
    // -------------------------------------
    const val GENERAL_ERROR = "general.error"
    const val GENERAL_NAME_ERROR = "general.name_error"
    const val GENERAL_LIST_SEPARATOR = "general.list_separator"


    // -------------------------------------
    // Action Feedback
    // -------------------------------------

    // Move Tool
    const val FEEDBACK_MOVE_TOOL_WARP_NOT_FOUND = "feedback.move_tool.warp_not_found"
    const val FEEDBACK_MOVE_TOOL_NO_PERMISSION = "feedback.move_tool.no_permission"
    const val FEEDBACK_MOVE_TOOL_NO_SPACE = "feedback.move_tool.no_space"
    const val FEEDBACK_MOVE_TOOL_SUCCESS = "feedback.move_tool.success"
    const val FEEDBACK_MOVE_TOOL_NOT_OWNER = "feedback.move_tool.not_owner"

    // Waystone Base Interact
    const val FEEDBACK_WAYSTONE_SKIN_UPDATED = "feedback.waystone.skin_updated"
    const val FEEDBACK_WAYSTONE_INVALID = "feedback.waystone.invalid"

    // Waystone Destruction
    const val FEEDBACK_WAYSTONE_BREAK_PROGRESS = "feedback.waystone.break_progress"
    const val FEEDBACK_WAYSTONE_DESTROYED = "feedback.waystone.destroyed"

    // Waystone Interact
    const val FEEDBACK_WAYSTONE_PRIVATE = "feedback.waystone.private"

    // Teleport Action Bar Messages
    const val FEEDBACK_TELEPORT_PENDING = "feedback.teleport.pending"
    const val FEEDBACK_TELEPORT_SUCCESS = "feedback.teleport.success"
    const val FEEDBACK_TELEPORT_FAILED = "feedback.teleport.failed"
    const val FEEDBACK_TELEPORT_INSUFFICIENT_FUNDS = "feedback.teleport.insufficient_funds"
    const val FEEDBACK_TELEPORT_WORLD_NOT_FOUND = "feedback.teleport.world_not_found"
    const val FEEDBACK_TELEPORT_LOCKED = "feedback.teleport.locked"
    const val FEEDBACK_TELEPORT_CANCELLED = "feedback.teleport.cancelled"
    const val FEEDBACK_TELEPORT_NO_PERMISSION = "feedback.teleport.no_permission"
    const val FEEDBACK_TELEPORT_NO_INTERWORLD_PERMISSION = "feedback.teleport.no_interworld_permission"


    // -------------------------------------
    // Conditions
    // -------------------------------------

    // Warp Naming Conditions
    const val CONDITION_NAMING_EXISTING = "condition.naming.existing"
    const val CONDITION_NAMING_BLANK = "condition.naming.blank"
    const val CONDITION_NAMING_LIMIT = "condition.naming.limit"
    const val CONDITION_NAMING_NO_PERMISSION = "condition.naming.no_permission"
    const val CONDITION_NAMING_NOT_FOUND = "condition.naming.not_found"


    // -------------------------------------
    // Menu Elements
    // -------------------------------------

    // Common Menu Items
    const val MENU_COMMON_ITEM_BACK_NAME = "menu.common.item.back.name"
    const val MENU_COMMON_ITEM_CLOSE_NAME = "menu.common.item.close.name"
    const val MENU_COMMON_ITEM_CONFIRM_NAME = "menu.common.item.confirm.name"
    const val MENU_COMMON_ITEM_DESELECT_ALL_NAME = "menu.common.item.deselect_all.name"
    const val MENU_COMMON_ITEM_ERROR_NAME = "menu.common.item.error.name"
    const val MENU_COMMON_ITEM_ERROR_LORE = "menu.common.item.error.lore"
    const val MENU_COMMON_ITEM_NEXT_NAME = "menu.common.item.next.name"
    const val MENU_COMMON_ITEM_PAGE_NAME = "menu.common.item.page.name"
    const val MENU_COMMON_ITEM_PREV_NAME = "menu.common.item.prev.name"
    const val MENU_COMMON_ITEM_SELECT_ALL_NAME = "menu.common.item.select_all.name"
    
    // Player Search Menu
    const val MENU_PLAYER_SEARCH_TITLE = "menu.player_search.title"
    const val MENU_PLAYER_SEARCH_CONFIRM_ITEM_NAME = "menu.player_search.item.confirm.name"
    
    // Warp Icon Menu
    const val MENU_WARP_ICON_TITLE = "menu.warp_icon.title"
    const val MENU_WARP_ICON_INFO_ITEM_NAME = "menu.warp_icon.info_item.name"
    const val MENU_WARP_ICON_INFO_ITEM_LORE = "menu.warp_icon.info_item.lore"
    const val MENU_WARP_ICON_CONFIRM_ITEM_NAME = "menu.warp_icon.confirm_item.name"
    
    // Warp Management Menu
    const val MENU_WARP_MANAGEMENT_TITLE = "menu.warp_management.title"
    const val MENU_WARP_MANAGEMENT_ACCESS_PRIVATE = "menu.warp_management.access.private"
    const val MENU_WARP_MANAGEMENT_ACCESS_PUBLIC = "menu.warp_management.access.public"
    const val MENU_WARP_MANAGEMENT_ACCESS_LORE = "menu.warp_management.access.lore"
    const val MENU_WARP_MANAGEMENT_ACCESS_LORE_NO_PERM = "menu.warp_management.access.lore_no_perm"
    const val MENU_WARP_MANAGEMENT_PLAYERS = "menu.warp_management.players"
    const val MENU_WARP_MANAGEMENT_PLAYERS_LORE = "menu.warp_management.players.lore"
    const val MENU_WARP_MANAGEMENT_RENAME = "menu.warp_management.rename"
    const val MENU_WARP_MANAGEMENT_RENAME_LORE = "menu.warp_management.rename.lore"
    const val MENU_WARP_MANAGEMENT_ICON = "menu.warp_management.icon"
    const val MENU_WARP_MANAGEMENT_ICON_LORE = "menu.warp_management.icon.lore"
    const val MENU_WARP_MANAGEMENT_SKINS = "menu.warp_management.skins"
    const val MENU_WARP_MANAGEMENT_SKINS_LORE = "menu.warp_management.skins.lore"
    const val MENU_WARP_MANAGEMENT_MOVE = "menu.warp_management.move"
    const val MENU_WARP_MANAGEMENT_MOVE_LORE = "menu.warp_management.move.lore"
    const val MENU_WARP_MANAGEMENT_COMMON_NO_PERMISSION = "menu.warp_management.common.no_permission"

    // Warp Naming Menu
    const val MENU_WARP_NAMING_TITLE = "menu.warp_naming.title"
    const val MENU_WARP_NAMING_ITEM_WARP_LORE = "menu.warp_naming.item.warp.lore"
    
    // Warp Player Menu
    const val MENU_WARP_PLAYER_TITLE = "menu.warp_player.title"
    const val MENU_WARP_PLAYER_ITEM_DISCOVERED = "menu.warp_player.item.discovered.name"
    const val MENU_WARP_PLAYER_VIEW_MODE_DISCOVERED_LORE = "menu.warp_player.item.view_mode.discovered.lore"
    const val MENU_WARP_PLAYER_ITEM_WHITELISTED = "menu.warp_player.item.whitelisted.name"
    const val MENU_WARP_PLAYER_VIEW_MODE_WHITELISTED_LORE = "menu.warp_player.item.view_mode.whitelisted.lore"
    const val MENU_WARP_PLAYER_ITEM_ONLINE = "menu.warp_player.item.online.name"
    const val MENU_WARP_PLAYER_VIEW_MODE_ONLINE_LORE = "menu.warp_player.item.online.lore"
    const val MENU_WARP_PLAYER_ITEM_SEARCH = "menu.warp_player.item.search.name"
    const val MENU_WARP_PLAYER_ITEM_CLEAR_SEARCH = "menu.warp_player.item.clear_search.name"
    const val MENU_WARP_PLAYER_ITEM_PLAYER_LORE_WHITELISTED = "menu.warp_player.item.player.lore.whitelisted"
    const val MENU_WARP_PLAYER_ITEM_PLAYER_LORE_DISCOVERED = "menu.warp_player.item.player.lore.discovered"
    const val MENU_WARP_PLAYER_ITEM_PLAYER_LORE_NO_PERMISSION = "menu.warp_player.item.player.lore.no_permission"
    const val MENU_WARP_PLAYER_ITEM_PLAYER_LORE_TOGGLE_WHITELIST = "menu.warp_player.item.player.lore.toggle_whitelist"
    const val MENU_WARP_PLAYER_ITEM_PLAYER_LORE_REVOKE_ACCESS = "menu.warp_player.item.player.lore.revoke_access"
    
    // Warp Renaming Menu
    const val MENU_WARP_RENAMING_TITLE = "menu.warp_renaming.title"
    
    // Warp Options Menu
    const val MENU_WARP_OPTIONS_TITLE = "menu.warp_options.title"
    const val MENU_WARP_OPTIONS_ITEM_LOCATE_NAME = "menu.warp_options.item.locate.name"
    const val MENU_WARP_OPTIONS_ITEM_LOCATE_LORE = "menu.warp_options.item.locate.lore"
    const val MENU_WARP_OPTIONS_ITEM_NO_COMPASS_NAME = "menu.warp_options.item.no_compass.name"
    const val MENU_WARP_OPTIONS_ITEM_NO_COMPASS_LORE = "menu.warp_options.item.no_compass.lore"
    const val MENU_WARP_OPTIONS_ITEM_FAVOURITE_NAME = "menu.warp_options.item.favourite.name"
    const val MENU_WARP_OPTIONS_ITEM_FAVOURITE_LORE = "menu.warp_options.item.favourite.lore"
    const val MENU_WARP_OPTIONS_ITEM_UNFAVOURITE_NAME = "menu.warp_options.item.unfavourite.name"
    const val MENU_WARP_OPTIONS_ITEM_UNFAVOURITE_LORE = "menu.warp_options.item.unfavourite.lore"
    const val MENU_WARP_OPTIONS_ITEM_DELETE_NAME = "menu.warp_options.item.delete.name"
    const val MENU_WARP_OPTIONS_ITEM_DELETE_LORE = "menu.warp_options.item.delete.lore"
    const val MENU_WARP_OPTIONS_ITEM_CANNOT_DELETE_NAME = "menu.warp_options.item.cannot_delete.name"
    const val MENU_WARP_OPTIONS_ITEM_CANNOT_DELETE_LORE = "menu.warp_options.item.cannot_delete.lore"
    const val MENU_WARP_OPTIONS_CONFIRM_DELETE = "menu.warp_options.confirm.delete"
    
    // Warp Search Menu
    const val MENU_WARP_SEARCH_TITLE = "menu.warp_search.title"
    
    // Warp Menu
    const val MENU_WARP_TITLE = "menu.warp.title"
    const val MENU_WARP_ITEM_VIEW_MODE_DISCOVERED_NAME = "menu.warp.item.view_mode.discovered.name"
    const val MENU_WARP_ITEM_VIEW_MODE_DISCOVERED_LORE = "menu.warp.item.view_mode.discovered.lore"
    const val MENU_WARP_ITEM_VIEW_MODE_FAVOURITES_NAME = "menu.warp.item.view_mode.favourites.name"
    const val MENU_WARP_ITEM_VIEW_MODE_FAVOURITES_LORE = "menu.warp.item.view_mode.favourites.lore"
    const val MENU_WARP_ITEM_VIEW_MODE_OWNED_NAME = "menu.warp.item.view_mode.owned.name"
    const val MENU_WARP_ITEM_VIEW_MODE_OWNED_LORE = "menu.warp.item.view_mode.owned.lore"
    const val MENU_WARP_ITEM_SEARCH_NAME = "menu.warp.item.search.name"
    const val MENU_WARP_ITEM_CLEAR_SEARCH_NAME = "menu.warp.item.clear_search.name"
    const val MENU_WARP_ITEM_WARP_LORE_RIGHT_CLICK = "menu.warp.item.warp.lore.right_click"
    const val MENU_WARP_ITEM_WARP_LORE_LEFT_CLICK = "menu.warp.item.warp.lore.left_click"
    const val MENU_WARP_ITEM_WARP_LORE_LOCKED = "menu.warp.item.warp.lore.locked"
    const val MENU_WARP_ITEM_WARP_LORE_NO_TELEPORT_PERMISSION = "menu.warp.item.warp.lore.no_teleport_permission"
    const val MENU_WARP_ITEM_WARP_LORE_NO_INTERWORLD_PERMISSION = "menu.warp.item.warp.lore.no_interworld_permission"
    const val MENU_WARP_ITEM_PAGE_NAME = "menu.warp.item.page.name"
    
    // Warp Skins Menu
    const val MENU_WARP_SKINS_TITLE = "menu.warp_skins.title"
    const val MENU_WARP_SKINS_ITEM_TOOLTIP_NAME = "menu.warp_skins.item.tooltip.name"
    const val MENU_WARP_SKINS_ITEM_TOOLTIP_LINE_1 = "menu.warp_skins.item.tooltip.line1"
    const val MENU_WARP_SKINS_ITEM_TOOLTIP_LINE_2 = "menu.warp_skins.item.tooltip.line2"
    
    // Confirmation Menu
    const val MENU_CONFIRMATION_ITEM_YES_NAME = "menu.confirmation.item.yes.name"
    const val MENU_CONFIRMATION_ITEM_YES_LORE = "menu.confirmation.item.yes.lore"
    const val MENU_CONFIRMATION_ITEM_NO_NAME = "menu.confirmation.item.no.name"
    const val MENU_CONFIRMATION_ITEM_NO_LORE = "menu.confirmation.item.no.lore"


    // -------------------------------------
    // Commands
    // -------------------------------------
    const val COMMAND_INVALIDS_LIST_HEADER = "command.invalids.list.header"
    const val COMMAND_INVALIDS_LIST_WORLD_ENTRY = "command.invalids.list.world_entry"
    const val COMMAND_INVALIDS_LIST_CLIPBOARD_HOVER = "command.invalids.list.clipboard_hover"
    const val COMMAND_INVALIDS_REMOVE_SUCCESS = "command.invalids.remove.success"
    const val COMMAND_INVALIDS_REMOVE_ERROR = "command.invalids.remove.error"
    const val COMMAND_INVALIDS_REMOVE_INVALID_WORLD = "command.invalids.remove.invalid_world"
    const val COMMAND_INVALIDS_NO_INVALID_WARPS = "command.invalids.no_invalid_warps"


    // -------------------------------------
    // Items
    // -------------------------------------

    // Move Tool
    const val ITEM_MOVE_TOOL_NAME = "item.move_tool.name"
    const val ITEM_MOVE_TOOL_LORE = "item.move_tool.lore"
}