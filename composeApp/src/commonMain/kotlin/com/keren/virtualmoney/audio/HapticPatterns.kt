package com.keren.virtualmoney.audio

/**
 * Predefined haptic feedback patterns for game events.
 * These patterns define vibration sequences (duration in ms).
 */
object HapticPatterns {

    /**
     * Coin collected - quick satisfying tap.
     */
    val COIN_COLLECT = longArrayOf(0, 15)

    /**
     * Gold/bonus coin collected - double tap.
     */
    val BONUS_COIN_COLLECT = longArrayOf(0, 20, 30, 25)

    /**
     * Penalty coin hit - harsh buzz.
     */
    val PENALTY_HIT = longArrayOf(0, 100, 50, 100)

    /**
     * Power-up collected - ascending pattern.
     */
    val POWERUP_COLLECT = longArrayOf(0, 30, 50, 40, 50, 50)

    /**
     * Shield activated - protective pulse.
     */
    val SHIELD_ACTIVATE = longArrayOf(0, 50, 100, 50)

    /**
     * Shield blocked attack - satisfying deflect.
     */
    val SHIELD_BLOCK = longArrayOf(0, 30, 20, 30, 20, 30)

    /**
     * Magnet activated - pulling sensation.
     */
    val MAGNET_ACTIVATE = longArrayOf(0, 20, 30, 30, 30, 40, 30, 50)

    /**
     * Freeze power-up - cold burst.
     */
    val FREEZE_ACTIVATE = longArrayOf(0, 100, 50, 20, 50, 20)

    /**
     * Invisibility - fading pulse.
     */
    val INVISIBILITY_ACTIVATE = longArrayOf(0, 40, 100, 30, 100, 20, 100, 10)

    /**
     * Combo milestone reached (5x, 10x, etc.).
     */
    val COMBO_MILESTONE = longArrayOf(0, 20, 40, 30, 40, 40, 40, 50)

    /**
     * Combo broken - disappointing thud.
     */
    val COMBO_BREAK = longArrayOf(0, 150)

    /**
     * Countdown tick.
     */
    val COUNTDOWN_TICK = longArrayOf(0, 10)

    /**
     * Game start - energetic pulse.
     */
    val GAME_START = longArrayOf(0, 30, 50, 30, 50, 30, 50, 100)

    /**
     * Game end - final pulse.
     */
    val GAME_END = longArrayOf(0, 100, 100, 50, 100, 50)

    /**
     * New high score - celebration pattern!
     */
    val HIGH_SCORE = longArrayOf(0, 50, 50, 50, 50, 50, 50, 50, 50, 100, 100, 200)

    /**
     * Level up - ascending celebration.
     */
    val LEVEL_UP = longArrayOf(0, 30, 60, 40, 60, 50, 60, 60, 60, 80)

    /**
     * Achievement unlocked - special pattern.
     */
    val ACHIEVEMENT = longArrayOf(0, 50, 100, 50, 100, 100, 150, 200)

    /**
     * Button click - light feedback.
     */
    val BUTTON_CLICK = longArrayOf(0, 8)

    /**
     * Menu navigation - selection tick.
     */
    val MENU_SELECT = longArrayOf(0, 5)

    /**
     * Error/invalid action.
     */
    val ERROR = longArrayOf(0, 50, 30, 50, 30, 50)

    /**
     * Warning - attention needed.
     */
    val WARNING = longArrayOf(0, 100, 50, 100)

    /**
     * Elimination warning in multiplayer.
     */
    val ELIMINATION_WARNING = longArrayOf(0, 200, 100, 200, 100, 200)

    /**
     * Player eliminated - harsh feedback.
     */
    val PLAYER_ELIMINATED = longArrayOf(0, 300, 100, 150)

    /**
     * Victory in multiplayer.
     */
    val VICTORY = longArrayOf(0, 50, 100, 50, 100, 50, 100, 50, 200, 300)

    /**
     * Defeat in multiplayer.
     */
    val DEFEAT = longArrayOf(0, 200, 200, 100, 300)

    /**
     * Nearby player detected (radar).
     */
    val PLAYER_NEARBY = longArrayOf(0, 30, 100, 30)

    /**
     * Coin stolen by/from player.
     */
    val COIN_STOLEN = longArrayOf(0, 80, 40, 40)

    /**
     * Get pattern for haptic type.
     */
    fun getPattern(type: HapticType): LongArray = when (type) {
        HapticType.LIGHT -> BUTTON_CLICK
        HapticType.MEDIUM -> COIN_COLLECT
        HapticType.HEAVY -> GAME_END
        HapticType.SUCCESS -> LEVEL_UP
        HapticType.WARNING -> WARNING
        HapticType.ERROR -> ERROR
        HapticType.SELECTION -> MENU_SELECT
    }

    /**
     * Get pattern for game event.
     */
    fun forGameEvent(event: GameHapticEvent): LongArray = when (event) {
        GameHapticEvent.COIN_COLLECT -> COIN_COLLECT
        GameHapticEvent.BONUS_COIN -> BONUS_COIN_COLLECT
        GameHapticEvent.PENALTY_HIT -> PENALTY_HIT
        GameHapticEvent.POWERUP_COLLECT -> POWERUP_COLLECT
        GameHapticEvent.SHIELD_ACTIVATE -> SHIELD_ACTIVATE
        GameHapticEvent.SHIELD_BLOCK -> SHIELD_BLOCK
        GameHapticEvent.MAGNET_ACTIVATE -> MAGNET_ACTIVATE
        GameHapticEvent.FREEZE_ACTIVATE -> FREEZE_ACTIVATE
        GameHapticEvent.INVISIBILITY_ACTIVATE -> INVISIBILITY_ACTIVATE
        GameHapticEvent.COMBO_UP -> COMBO_MILESTONE
        GameHapticEvent.COMBO_BREAK -> COMBO_BREAK
        GameHapticEvent.COUNTDOWN -> COUNTDOWN_TICK
        GameHapticEvent.GAME_START -> GAME_START
        GameHapticEvent.GAME_END -> GAME_END
        GameHapticEvent.HIGH_SCORE -> HIGH_SCORE
        GameHapticEvent.LEVEL_UP -> LEVEL_UP
        GameHapticEvent.ACHIEVEMENT -> ACHIEVEMENT
        GameHapticEvent.ELIMINATION_WARNING -> ELIMINATION_WARNING
        GameHapticEvent.ELIMINATED -> PLAYER_ELIMINATED
        GameHapticEvent.VICTORY -> VICTORY
        GameHapticEvent.DEFEAT -> DEFEAT
        GameHapticEvent.PLAYER_NEARBY -> PLAYER_NEARBY
        GameHapticEvent.COIN_STOLEN -> COIN_STOLEN
    }
}

/**
 * Game events that trigger haptic feedback.
 */
enum class GameHapticEvent {
    COIN_COLLECT,
    BONUS_COIN,
    PENALTY_HIT,
    POWERUP_COLLECT,
    SHIELD_ACTIVATE,
    SHIELD_BLOCK,
    MAGNET_ACTIVATE,
    FREEZE_ACTIVATE,
    INVISIBILITY_ACTIVATE,
    COMBO_UP,
    COMBO_BREAK,
    COUNTDOWN,
    GAME_START,
    GAME_END,
    HIGH_SCORE,
    LEVEL_UP,
    ACHIEVEMENT,
    ELIMINATION_WARNING,
    ELIMINATED,
    VICTORY,
    DEFEAT,
    PLAYER_NEARBY,
    COIN_STOLEN
}
