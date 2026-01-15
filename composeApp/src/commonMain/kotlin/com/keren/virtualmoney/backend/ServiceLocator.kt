package com.keren.virtualmoney.backend

import com.keren.virtualmoney.audio.HapticManager
import com.keren.virtualmoney.audio.SoundManager
import com.keren.virtualmoney.progression.ProgressionManager
import com.keren.virtualmoney.theme.ThemeManager

/**
 * Simple service locator for dependency management.
 * Provides singleton access to all backend services.
 */
object ServiceLocator {
    // Lazy-initialized services
    private var _localStorage: LocalStorage? = null
    private var _authManager: AuthManager? = null
    private var _profileRepository: ProfileRepository? = null
    private var _leaderboardRepository: LeaderboardRepository? = null
    private var _gameRepository: GameRepository? = null
    private var _soundManager: SoundManager? = null
    private var _hapticManager: HapticManager? = null
    private var _progressionManager: ProgressionManager? = null
    private var _themeManager: ThemeManager? = null

    /**
     * Initialize all services.
     * Call this once at app startup.
     */
    fun initialize() {
        // Services are lazily created on first access
    }

    val localStorage: LocalStorage
        get() {
            if (_localStorage == null) {
                _localStorage = LocalStorage()
            }
            return _localStorage!!
        }

    val authManager: AuthManager
        get() {
            if (_authManager == null) {
                _authManager = AuthManager(localStorage)
            }
            return _authManager!!
        }

    val profileRepository: ProfileRepository
        get() {
            if (_profileRepository == null) {
                _profileRepository = ProfileRepository(authManager, localStorage)
            }
            return _profileRepository!!
        }

    val leaderboardRepository: LeaderboardRepository
        get() {
            if (_leaderboardRepository == null) {
                _leaderboardRepository = LeaderboardRepository(authManager)
            }
            return _leaderboardRepository!!
        }

    val gameRepository: GameRepository
        get() {
            if (_gameRepository == null) {
                _gameRepository = GameRepository(authManager)
            }
            return _gameRepository!!
        }

    val soundManager: SoundManager
        get() {
            if (_soundManager == null) {
                _soundManager = SoundManager()
                _soundManager!!.initialize()
            }
            return _soundManager!!
        }

    val hapticManager: HapticManager
        get() {
            if (_hapticManager == null) {
                _hapticManager = HapticManager()
                _hapticManager!!.initialize()
            }
            return _hapticManager!!
        }

    val progressionManager: ProgressionManager
        get() {
            if (_progressionManager == null) {
                _progressionManager = ProgressionManager(
                    saveProfile = { profile ->
                        // Fire and forget - actual save happens in coroutine
                        localStorage.saveProfile(profile)
                    },
                    loadProfile = { localStorage.loadProfile() },
                    saveStats = { stats -> localStorage.saveStats(stats) },
                    loadStats = { localStorage.loadStats() },
                    saveAchievements = { achievements -> localStorage.saveAchievements(achievements) },
                    loadAchievements = { localStorage.loadAchievements() },
                    saveChallengeProgress = { progress -> localStorage.saveChallengeProgress(progress) },
                    loadChallengeProgress = { localStorage.loadChallengeProgress() }
                )
                _progressionManager!!.initialize()
            }
            return _progressionManager!!
        }

    val themeManager: ThemeManager
        get() {
            if (_themeManager == null) {
                _themeManager = ThemeManager(
                    getPlayerLevel = { progressionManager.profile.value.level },
                    getUnlockedAchievements = { localStorage.loadAchievements() },
                    saveSelectedTheme = { themeId -> localStorage.saveSelectedTheme(themeId) },
                    saveSelectedSkin = { skinId -> localStorage.saveSelectedSkin(skinId) },
                    loadSelectedTheme = { localStorage.loadSelectedTheme() },
                    loadSelectedSkin = { localStorage.loadSelectedSkin() }
                )
                _themeManager!!.initialize()
            }
            return _themeManager!!
        }

    /**
     * Release all services.
     * Call this when app is being destroyed.
     */
    fun release() {
        _soundManager?.release()
        _hapticManager?.release()

        _localStorage = null
        _authManager = null
        _profileRepository = null
        _leaderboardRepository = null
        _gameRepository = null
        _soundManager = null
        _hapticManager = null
        _progressionManager = null
        _themeManager = null
    }
}
