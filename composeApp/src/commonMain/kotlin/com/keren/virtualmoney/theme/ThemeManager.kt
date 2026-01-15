package com.keren.virtualmoney.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages game theme and skin selection.
 * Handles unlocking and persistence.
 */
class ThemeManager(
    private val getPlayerLevel: () -> Int,
    private val getUnlockedAchievements: () -> Set<String>,
    private val saveSelectedTheme: (ThemeId) -> Unit,
    private val saveSelectedSkin: (CoinSkinId) -> Unit,
    private val loadSelectedTheme: () -> ThemeId,
    private val loadSelectedSkin: () -> CoinSkinId
) {
    private val _currentTheme = MutableStateFlow(GameTheme.CAMERA)
    val currentTheme: StateFlow<GameTheme> = _currentTheme.asStateFlow()

    private val _currentSkin = MutableStateFlow(CoinSkin.CLASSIC)
    val currentSkin: StateFlow<CoinSkin> = _currentSkin.asStateFlow()

    private val _themesWithStatus = MutableStateFlow<List<ThemeWithStatus>>(emptyList())
    val themesWithStatus: StateFlow<List<ThemeWithStatus>> = _themesWithStatus.asStateFlow()

    private val _skinsWithStatus = MutableStateFlow<List<SkinWithStatus>>(emptyList())
    val skinsWithStatus: StateFlow<List<SkinWithStatus>> = _skinsWithStatus.asStateFlow()

    /**
     * Initialize manager and load saved selections.
     */
    fun initialize() {
        val savedThemeId = loadSelectedTheme()
        val savedSkinId = loadSelectedSkin()

        // Only apply if unlocked
        if (isThemeUnlocked(savedThemeId)) {
            _currentTheme.value = GameTheme.fromId(savedThemeId)
        }

        if (isSkinUnlocked(savedSkinId)) {
            _currentSkin.value = CoinSkin.fromId(savedSkinId)
        }

        // Update status lists
        updateStatusLists()
    }

    private fun updateStatusLists() {
        _themesWithStatus.value = getAllThemesWithStatus()
        _skinsWithStatus.value = getAllSkinsWithStatus()
    }

    /**
     * Select a theme if unlocked.
     * @return true if theme was selected, false if locked
     */
    fun selectTheme(themeId: ThemeId): Boolean {
        if (!isThemeUnlocked(themeId)) return false

        val theme = GameTheme.fromId(themeId)
        _currentTheme.value = theme
        saveSelectedTheme(themeId)
        updateStatusLists()
        return true
    }

    /**
     * Select a skin if unlocked.
     * @return true if skin was selected, false if locked
     */
    fun selectSkin(skinId: CoinSkinId): Boolean {
        if (!isSkinUnlocked(skinId)) return false

        val skin = CoinSkin.fromId(skinId)
        _currentSkin.value = skin
        saveSelectedSkin(skinId)
        updateStatusLists()
        return true
    }

    /**
     * Check if a theme is unlocked.
     */
    fun isThemeUnlocked(themeId: ThemeId): Boolean {
        val playerLevel = getPlayerLevel()
        return playerLevel >= themeId.unlockLevel
    }

    /**
     * Check if a skin is unlocked.
     */
    fun isSkinUnlocked(skinId: CoinSkinId): Boolean {
        val skin = CoinSkin.fromId(skinId)
        return when (val req = skin.unlockRequirement) {
            is SkinUnlockRequirement.Default -> true
            is SkinUnlockRequirement.Level -> getPlayerLevel() >= req.minLevel
            is SkinUnlockRequirement.Achievement -> {
                getUnlockedAchievements().contains(req.achievementId)
            }
            is SkinUnlockRequirement.Challenge -> {
                // For now, treat challenges like achievements
                getUnlockedAchievements().contains(req.challengeId)
            }
        }
    }

    /**
     * Get all themes with their unlock status.
     */
    fun getAllThemesWithStatus(): List<ThemeWithStatus> {
        val playerLevel = getPlayerLevel()
        return GameTheme.all().map { theme ->
            ThemeWithStatus(
                theme = theme,
                isUnlocked = playerLevel >= theme.id.unlockLevel,
                isSelected = _currentTheme.value.id == theme.id,
                unlockProgress = if (theme.id.unlockLevel > 0) {
                    (playerLevel.toFloat() / theme.id.unlockLevel).coerceIn(0f, 1f)
                } else 1f
            )
        }
    }

    /**
     * Get all skins with their unlock status.
     */
    fun getAllSkinsWithStatus(): List<SkinWithStatus> {
        return CoinSkin.all().map { skin ->
            SkinWithStatus(
                skin = skin,
                isUnlocked = isSkinUnlocked(skin.id),
                isSelected = _currentSkin.value.id == skin.id
            )
        }
    }
}

/**
 * Theme with unlock status for UI display.
 */
data class ThemeWithStatus(
    val theme: GameTheme,
    val isUnlocked: Boolean,
    val isSelected: Boolean,
    val unlockProgress: Float // 0.0 to 1.0
)

/**
 * Skin with unlock status for UI display.
 */
data class SkinWithStatus(
    val skin: CoinSkin,
    val isUnlocked: Boolean,
    val isSelected: Boolean
)
