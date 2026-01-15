package com.keren.virtualmoney

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.keren.virtualmoney.ar.camera.CameraProviderFactory
import com.keren.virtualmoney.backend.ServiceLocator
import com.keren.virtualmoney.game.*
import com.keren.virtualmoney.platform.CameraView
import com.keren.virtualmoney.ui.screens.*

/**
 * Navigation destinations in the app.
 */
sealed class Screen {
    data object MainMenu : Screen()
    data object SinglePlayerMenu : Screen()
    data object Multiplayer : Screen()
    data object Customize : Screen()
    data object Challenges : Screen()
    data object Leaderboard : Screen()
    data object Profile : Screen()
    data object Settings : Screen()
    data class Game(val mode: GameMode) : Screen()
    data class GameOver(val result: GameState.Finished) : Screen()
}

@Composable
fun App() {
    // Initialize services once
    LaunchedEffect(Unit) {
        ServiceLocator.initialize()
    }

    MaterialTheme {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.MainMenu) }

        // Collect player profile from progression manager
        val profile by ServiceLocator.progressionManager.profile.collectAsState()
        val stats by ServiceLocator.progressionManager.stats.collectAsState()

        // Theme manager states
        val themes by ServiceLocator.themeManager.themesWithStatus.collectAsState()
        val skins by ServiceLocator.themeManager.skinsWithStatus.collectAsState()

        // Camera provider for AR
        val cameraProvider = remember { CameraProviderFactory.create() }

        // Clean up on disposal
        DisposableEffect(Unit) {
            onDispose {
                cameraProvider.stopSession()
                ServiceLocator.release()
            }
        }

        when (val screen = currentScreen) {
            is Screen.MainMenu -> {
                MainMenuScreen(
                    playerName = profile.displayName,
                    playerLevel = profile.level,
                    onSinglePlayer = { currentScreen = Screen.SinglePlayerMenu },
                    onMultiplayer = { currentScreen = Screen.Multiplayer },
                    onCustomize = { currentScreen = Screen.Customize },
                    onChallenges = { currentScreen = Screen.Challenges },
                    onProfile = { currentScreen = Screen.Profile },
                    onSettings = { currentScreen = Screen.Settings },
                    onLeaderboard = { currentScreen = Screen.Leaderboard }
                )
            }

            is Screen.SinglePlayerMenu -> {
                SinglePlayerMenuScreen(
                    highScores = mapOf(
                        GameMode.CLASSIC to stats.highScore,
                        GameMode.BLITZ to stats.highScore,
                        GameMode.SURVIVAL to stats.highScore
                    ),
                    onModeSelected = { mode -> currentScreen = Screen.Game(mode) },
                    onBack = { currentScreen = Screen.MainMenu }
                )
            }

            is Screen.Multiplayer -> {
                MultiplayerMenuScreen(
                    onModeSelected = { mode -> currentScreen = Screen.Game(mode) },
                    onBack = { currentScreen = Screen.MainMenu }
                )
            }

            is Screen.Customize -> {
                CustomizeScreen(
                    themes = themes,
                    skins = skins,
                    playerLevel = profile.level,
                    onThemeSelected = { themeId -> ServiceLocator.themeManager.selectTheme(themeId) },
                    onSkinSelected = { skinId -> ServiceLocator.themeManager.selectSkin(skinId) },
                    onBack = { currentScreen = Screen.MainMenu }
                )
            }

            is Screen.Challenges -> {
                ChallengesScreen(
                    onBack = { currentScreen = Screen.MainMenu }
                )
            }

            is Screen.Leaderboard -> {
                LeaderboardScreen(
                    onBack = { currentScreen = Screen.MainMenu }
                )
            }

            is Screen.Profile -> {
                ProfileScreen(
                    onBack = { currentScreen = Screen.MainMenu }
                )
            }

            is Screen.Settings -> {
                SettingsScreen(
                    onBack = { currentScreen = Screen.MainMenu }
                )
            }

            is Screen.Game -> {
                GameplayScreen(
                    mode = screen.mode,
                    cameraProvider = cameraProvider,
                    cameraBackground = { CameraView() },
                    onGameOver = { result -> currentScreen = Screen.GameOver(result) },
                    onExit = { currentScreen = Screen.MainMenu }
                )
            }

            is Screen.GameOver -> {
                GameOverScreen(
                    result = screen.result,
                    onPlayAgain = { currentScreen = Screen.Game(screen.result.gameMode) },
                    onMainMenu = { currentScreen = Screen.MainMenu }
                )
            }
        }
    }
}
