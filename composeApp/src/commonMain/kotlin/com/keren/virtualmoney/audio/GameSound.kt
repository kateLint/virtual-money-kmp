package com.keren.virtualmoney.audio

/**
 * All sound effects used in the game.
 */
enum class GameSound(val fileName: String) {
    // Coin collection
    COIN_COLLECT("coin_pop"),
    PENALTY_HIT("negative_buzz"),

    // Power-ups
    POWERUP_COLLECT("powerup_chime"),
    MAGNET_ACTIVE("magnet_hum"),
    SHIELD_ACTIVE("shield_bubble"),
    SHIELD_BLOCK("shield_deflect"),
    FREEZE_CAST("ice_crack"),
    INVISIBILITY_ON("whisper_fade"),

    // Combos
    COMBO_MILESTONE("combo_ding"),
    COMBO_BREAK("combo_break"),

    // Game events
    COUNTDOWN_TICK("tick"),
    COUNTDOWN_GO("go"),
    GAME_START("game_start"),
    GAME_END("game_end"),
    NEW_HIGH_SCORE("fanfare"),
    LEVEL_UP("level_up"),

    // UI
    BUTTON_CLICK("button_click"),
    MENU_OPEN("menu_open"),
    ACHIEVEMENT_UNLOCK("achievement"),

    // Multiplayer
    ELIMINATION_WARNING("alarm"),
    PLAYER_ELIMINATED("elimination"),
    PLAYER_NEARBY("radar_ping"),
    COIN_STOLEN("steal_whoosh"),
    VICTORY("victory_fanfare"),
    DEFEAT("defeat")
}

/**
 * Haptic feedback types.
 */
enum class HapticType {
    /** Light tap - button presses, small feedback */
    LIGHT,

    /** Medium impact - coin collection */
    MEDIUM,

    /** Heavy impact - important events */
    HEAVY,

    /** Success pattern - achievements, wins */
    SUCCESS,

    /** Warning pattern - near miss, danger */
    WARNING,

    /** Error pattern - penalties, failures */
    ERROR,

    /** Selection tick - scrolling, selecting */
    SELECTION
}
