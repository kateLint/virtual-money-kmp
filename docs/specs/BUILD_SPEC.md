# Coin Hunter - Build Specification

**Version:** 2.0
**Date:** 2025-01-15
**Platform:** Kotlin Multiplatform (Android + iOS)

---

## Phase Overview

| Phase | Focus | Deliverables |
|-------|-------|--------------|
| **1** | Core Enhancements | Power-ups, Combos, Audio/Haptics |
| **2** | Themes & Skins | Theme system, Coin skins, UI |
| **3** | Progression | XP, Levels, Achievements, Challenges |
| **4** | Firebase Setup | Auth, Firestore, Realtime DB |
| **5** | Single Player Polish | All modes, Leaderboards, Profile |
| **6** | Multiplayer | Lobbies, Quick Match, Battle Royale |

---

## Phase 1: Core Enhancements

### 1.1 Power-Up System

**Files to create:**
```
commonMain/kotlin/com/keren/virtualmoney/
├── game/
│   ├── PowerUp.kt
│   ├── PowerUpManager.kt
│   └── ActivePowerUp.kt
└── ui/components/
    └── PowerUpHUD.kt
```

**PowerUp.kt:**
```kotlin
enum class PowerUpType(
    val displayName: String,
    val icon: String,
    val duration: Long,
    val spawnWeight: Float,
    val multiplayerOnly: Boolean
) {
    MAGNET("Magnet", "magnet", 5000L, 0.15f, false),
    MULTIPLIER("2x Points", "multiplier", 10000L, 0.20f, false),
    SHIELD("Shield", "shield", 8000L, 0.20f, false),
    FREEZE("Freeze", "freeze", 3000L, 0.10f, true),
    INVISIBILITY("Invisibility", "ghost", 5000L, 0.10f, true)
}

data class PowerUp(
    val id: String,
    val type: PowerUpType,
    val position3D: Vector3D,
    val spawnTime: Long,
    val expiresAt: Long
)

data class ActivePowerUp(
    val type: PowerUpType,
    val startTime: Long,
    val endTime: Long
) {
    fun remainingTime(): Long = maxOf(0, endTime - currentTimeMillis())
    fun isExpired(): Boolean = currentTimeMillis() >= endTime
}
```

### 1.2 Combo System

**Files to create:**
```
commonMain/kotlin/com/keren/virtualmoney/
├── game/
│   └── ComboTracker.kt
└── ui/components/
    └── ComboDisplay.kt
```

**ComboTracker.kt:**
```kotlin
data class ComboState(
    val count: Int = 0,
    val lastCollectionTime: Long = 0,
    val comboWindow: Long = 2000L
) {
    fun isActive(): Boolean =
        count > 0 && (currentTimeMillis() - lastCollectionTime) < comboWindow

    fun multiplier(): Float = when {
        count < 3 -> 1.0f
        count < 5 -> 1.2f
        count < 10 -> 1.5f
        count < 20 -> 2.0f
        else -> 2.5f
    }

    fun timeRemaining(): Long =
        maxOf(0, comboWindow - (currentTimeMillis() - lastCollectionTime))
}

class ComboTracker {
    private val _state = MutableStateFlow(ComboState())
    val state: StateFlow<ComboState> = _state.asStateFlow()

    fun onCoinCollected() {
        val now = currentTimeMillis()
        val current = _state.value

        _state.value = if (current.isActive()) {
            current.copy(count = current.count + 1, lastCollectionTime = now)
        } else {
            ComboState(count = 1, lastCollectionTime = now)
        }
    }

    fun reset() {
        _state.value = ComboState()
    }
}
```

### 1.3 Audio System

**Files to create:**
```
commonMain/kotlin/com/keren/virtualmoney/
└── audio/
    ├── SoundManager.kt (expect)
    ├── GameSound.kt
    └── HapticManager.kt (expect)

androidMain/kotlin/com/keren/virtualmoney/
└── audio/
    ├── SoundManager.android.kt (actual)
    └── HapticManager.android.kt (actual)

iosMain/kotlin/com/keren/virtualmoney/
└── audio/
    ├── SoundManager.ios.kt (actual)
    └── HapticManager.ios.kt (actual)
```

**GameSound.kt:**
```kotlin
enum class GameSound(val fileName: String) {
    // Collection
    COIN_COLLECT("coin_pop"),
    PENALTY_HIT("negative_buzz"),
    POWERUP_COLLECT("powerup_chime"),

    // Combos
    COMBO_MILESTONE("combo_ding"),
    COMBO_BREAK("combo_break"),

    // Power-ups
    MAGNET_ACTIVE("magnet_hum"),
    SHIELD_BLOCK("shield_deflect"),
    FREEZE_CAST("ice_crack"),

    // Game events
    COUNTDOWN_TICK("tick"),
    GAME_START("game_start"),
    GAME_END("game_end"),
    NEW_HIGH_SCORE("fanfare"),

    // Multiplayer
    ELIMINATION_WARNING("alarm"),
    PLAYER_ELIMINATED("elimination"),
    VICTORY("victory_fanfare")
}

enum class HapticType {
    LIGHT,
    MEDIUM,
    HEAVY,
    SUCCESS,
    WARNING,
    ERROR
}
```

---

## Phase 2: Themes & Skins

### 2.1 Theme System

**Files to create:**
```
commonMain/kotlin/com/keren/virtualmoney/
└── theme/
    ├── GameTheme.kt
    ├── CoinSkin.kt
    ├── ThemeManager.kt
    └── ParticleEffect.kt
```

**GameTheme.kt:**
```kotlin
enum class ThemeId(
    val displayName: String,
    val unlockLevel: Int
) {
    CAMERA("Camera", 0),
    FOREST("Forest", 10),
    GALAXY("Galaxy", 20),
    OCEAN("Ocean", 30),
    NEON_CITY("Neon City", 40)
}

data class GameTheme(
    val id: ThemeId,
    val backgroundType: BackgroundType,
    val particleEffects: List<ParticleType>,
    val ambientSound: GameSound?
)

enum class BackgroundType {
    CAMERA,
    ANIMATED_FOREST,
    ANIMATED_GALAXY,
    ANIMATED_OCEAN,
    ANIMATED_NEON
}
```

**CoinSkin.kt:**
```kotlin
enum class CoinSkinId(
    val displayName: String,
    val unlockRequirement: UnlockRequirement
) {
    CLASSIC("Classic", UnlockRequirement.Default),
    GOLDEN("Golden", UnlockRequirement.Level(5)),
    DIAMOND("Diamond", UnlockRequirement.Level(15)),
    NEON("Neon", UnlockRequirement.Level(25)),
    FIRE("Fire", UnlockRequirement.Achievement("perfect_5")),
    ICE("Ice", UnlockRequirement.Achievement("freeze_50")),
    HOLOGRAPHIC("Holographic", UnlockRequirement.Achievement("mp_wins_10")),
    RAINBOW("Rainbow", UnlockRequirement.Achievement("coins_1000")),
    LEGENDARY("Legendary", UnlockRequirement.Achievement("leaderboard_1"))
}

sealed class UnlockRequirement {
    object Default : UnlockRequirement()
    data class Level(val minLevel: Int) : UnlockRequirement()
    data class Achievement(val achievementId: String) : UnlockRequirement()
}
```

---

## Phase 3: Progression System

### 3.1 Player Profile & XP

**Files to create:**
```
commonMain/kotlin/com/keren/virtualmoney/
└── progression/
    ├── PlayerProfile.kt
    ├── LevelSystem.kt
    ├── Achievement.kt
    ├── Challenge.kt
    └── ProgressionManager.kt
```

**PlayerProfile.kt:**
```kotlin
data class PlayerProfile(
    val odId: String = "",
    val displayName: String = "Player",
    val avatarUrl: String? = null,
    val level: Int = 1,
    val xp: Int = 0,
    val prestige: Int = 0,
    val createdAt: Long = currentTimeMillis(),
    val lastLogin: Long = currentTimeMillis()
)

data class PlayerStats(
    val gamesPlayed: Int = 0,
    val totalCoins: Int = 0,
    val highScore: Int = 0,
    val bestCombo: Int = 0,
    val perfectRuns: Int = 0,
    val multiplayerWins: Int = 0,
    val multiplayerTop10: Int = 0,
    val totalPlayTime: Long = 0
)
```

**LevelSystem.kt:**
```kotlin
object LevelSystem {
    fun xpForLevel(level: Int): Int = when {
        level <= 1 -> 0
        level <= 10 -> 100 * (level - 1) + 50 * (level - 2)
        level <= 30 -> xpForLevel(10) + 300 * (level - 10)
        else -> xpForLevel(30) + 500 * (level - 30)
    }

    fun levelForXp(xp: Int): Int {
        var level = 1
        while (xpForLevel(level + 1) <= xp) level++
        return level
    }

    fun xpToNextLevel(currentXp: Int): Int {
        val level = levelForXp(currentXp)
        return xpForLevel(level + 1) - currentXp
    }

    // XP rewards
    const val XP_GAME_COMPLETE = 10
    const val XP_PER_COIN = 1
    const val XP_HIGH_SCORE = 50
    const val XP_DAILY_CHALLENGE = 75
    const val XP_WEEKLY_CHALLENGE = 300
    const val XP_MULTIPLAYER_WIN = 100
    const val XP_MULTIPLAYER_TOP10 = 50
}
```

### 3.2 Achievements

**Achievement.kt:**
```kotlin
enum class AchievementId(
    val displayName: String,
    val description: String,
    val xpReward: Int,
    val requirement: AchievementRequirement
) {
    // Beginner
    FIRST_GAME("First Steps", "Complete your first game", 25,
        AchievementRequirement.GamesPlayed(1)),
    SCORE_100("Getting Started", "Score 100 points", 25,
        AchievementRequirement.ScoreInGame(100)),

    // Collector
    COINS_100("Coin Collector", "Collect 100 coins total", 50,
        AchievementRequirement.TotalCoins(100)),
    COINS_500("Hoarder", "Collect 500 coins total", 100,
        AchievementRequirement.TotalCoins(500)),
    COINS_1000("Treasure Hunter", "Collect 1000 coins total", 200,
        AchievementRequirement.TotalCoins(1000)),

    // Skill
    COMBO_5("Combo Starter", "Get a 5x combo", 50,
        AchievementRequirement.ComboReached(5)),
    COMBO_10("Combo Master", "Get a 10x combo", 100,
        AchievementRequirement.ComboReached(10)),
    COMBO_20("Combo Legend", "Get a 20x combo", 200,
        AchievementRequirement.ComboReached(20)),

    // Perfect
    PERFECT_1("Clean Run", "Complete without penalties", 75,
        AchievementRequirement.PerfectRuns(1)),
    PERFECT_5("Perfectionist", "5 perfect runs", 150,
        AchievementRequirement.PerfectRuns(5)),
    PERFECT_10("Untouchable", "10 perfect runs", 300,
        AchievementRequirement.PerfectRuns(10)),

    // High Score
    SCORE_300("Rising Star", "Score 300+ in one game", 100,
        AchievementRequirement.ScoreInGame(300)),
    SCORE_500("Elite Hunter", "Score 500+ in one game", 200,
        AchievementRequirement.ScoreInGame(500)),

    // Multiplayer
    MP_WIN_1("First Victory", "Win a multiplayer game", 100,
        AchievementRequirement.MultiplayerWins(1)),
    MP_WIN_10("Champion", "Win 10 multiplayer games", 300,
        AchievementRequirement.MultiplayerWins(10)),
    MP_TOP10_50("Consistent", "Finish top 10 fifty times", 250,
        AchievementRequirement.MultiplayerTop10(50))
}

sealed class AchievementRequirement {
    data class GamesPlayed(val count: Int) : AchievementRequirement()
    data class TotalCoins(val count: Int) : AchievementRequirement()
    data class ScoreInGame(val score: Int) : AchievementRequirement()
    data class ComboReached(val combo: Int) : AchievementRequirement()
    data class PerfectRuns(val count: Int) : AchievementRequirement()
    data class MultiplayerWins(val count: Int) : AchievementRequirement()
    data class MultiplayerTop10(val count: Int) : AchievementRequirement()
}
```

---

## Phase 4: Firebase Setup

### 4.1 Dependencies

**build.gradle.kts additions:**
```kotlin
// In commonMain
commonMain.dependencies {
    // Firebase KMP
    implementation("dev.gitlive:firebase-auth:1.11.1")
    implementation("dev.gitlive:firebase-firestore:1.11.1")
    implementation("dev.gitlive:firebase-database:1.11.1")

    // Ktor for additional networking
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // DateTime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // Settings
    implementation("com.russhwolf:multiplatform-settings:1.1.1")
}

androidMain.dependencies {
    implementation("io.ktor:ktor-client-okhttp:2.3.7")
}

iosMain.dependencies {
    implementation("io.ktor:ktor-client-darwin:2.3.7")
}
```

### 4.2 Firebase Service

**Files to create:**
```
commonMain/kotlin/com/keren/virtualmoney/
└── backend/
    ├── FirebaseService.kt
    ├── AuthManager.kt
    ├── ProfileRepository.kt
    ├── LeaderboardRepository.kt
    └── GameRepository.kt
```

See `FIREBASE_SCHEMA.md` for full database structure.

---

## Phase 5: Single Player Complete

### 5.1 Game Modes

**Files to modify/create:**
```
commonMain/kotlin/com/keren/virtualmoney/
├── game/
│   ├── GameMode.kt (new)
│   ├── GameEngine.kt (modify)
│   └── GameConfig.kt (new)
└── ui/screens/
    ├── MainMenuScreen.kt (new)
    ├── SinglePlayerMenu.kt (new)
    ├── CustomizeScreen.kt (new)
    └── ProfileScreen.kt (new)
```

**GameMode.kt:**
```kotlin
enum class GameMode(
    val displayName: String,
    val description: String,
    val duration: Int?, // null = endless
    val isMultiplayer: Boolean
) {
    // Single Player
    CLASSIC("Classic", "60 second coin hunt", 60, false),
    BLITZ("Blitz", "Fast 30 second round", 30, false),
    SURVIVAL("Survival", "3 lives, endless", null, false),

    // Multiplayer
    QUICK_MATCH("Quick Match", "60s, 2-10 players", 60, true),
    BATTLE_ROYALE("Battle Royale", "Last one standing", 180, true),
    TEAM_BATTLE("Team Battle", "Teams compete", 180, true),
    KING_OF_HILL("King of Hill", "Hold #1 to win", 120, true)
}
```

---

## Phase 6: Multiplayer

### 6.1 Multiplayer Architecture

**Files to create:**
```
commonMain/kotlin/com/keren/virtualmoney/
└── multiplayer/
    ├── MultiplayerManager.kt
    ├── LobbyManager.kt
    ├── MatchmakingService.kt
    ├── GameSyncManager.kt
    ├── RemotePlayer.kt
    └── MultiplayerGameState.kt
```

### 6.2 Data Models

**RemotePlayer.kt:**
```kotlin
data class RemotePlayer(
    val odId: String,
    val displayName: String,
    val avatarUrl: String?,
    val level: Int,
    val position: Vector3D,
    val score: Int,
    val rank: Int,
    val isEliminated: Boolean,
    val teamId: Int? = null
)

data class Lobby(
    val id: String,
    val gameMode: GameMode,
    val hostId: String,
    val maxPlayers: Int,
    val players: List<LobbyPlayer>,
    val status: LobbyStatus,
    val createdAt: Long
)

enum class LobbyStatus {
    WAITING,
    COUNTDOWN,
    STARTING,
    IN_GAME,
    FINISHED
}
```

**MultiplayerGameState.kt:**
```kotlin
data class MultiplayerGameState(
    val gameId: String,
    val mode: GameMode,
    val phase: GamePhase,
    val timeRemaining: Int,
    val players: List<RemotePlayer>,
    val coins: List<Coin>,
    val powerUps: List<PowerUp>,
    val eliminationCountdown: Int?,
    val nextEliminationCount: Int?
)

enum class GamePhase {
    COUNTDOWN,
    PLAYING,
    ELIMINATION_WARNING,
    ELIMINATION,
    FINAL_SHOWDOWN,
    FINISHED
}
```

---

## File Structure Summary

```
composeApp/src/
├── commonMain/kotlin/com/keren/virtualmoney/
│   ├── App.kt
│   │
│   ├── game/
│   │   ├── Coin.kt (existing, modify)
│   │   ├── GameEngine.kt (existing, modify)
│   │   ├── GameState.kt (existing, modify)
│   │   ├── GameMode.kt [NEW]
│   │   ├── GameConfig.kt [NEW]
│   │   ├── PowerUp.kt [NEW]
│   │   ├── PowerUpManager.kt [NEW]
│   │   └── ComboTracker.kt [NEW]
│   │
│   ├── ar/ (existing)
│   │   ├── math/
│   │   ├── data/
│   │   ├── projection/
│   │   └── camera/
│   │
│   ├── audio/ [NEW]
│   │   ├── GameSound.kt
│   │   ├── SoundManager.kt (expect)
│   │   └── HapticManager.kt (expect)
│   │
│   ├── theme/ [NEW]
│   │   ├── GameTheme.kt
│   │   ├── CoinSkin.kt
│   │   ├── ThemeManager.kt
│   │   └── ParticleEffect.kt
│   │
│   ├── progression/ [NEW]
│   │   ├── PlayerProfile.kt
│   │   ├── PlayerStats.kt
│   │   ├── LevelSystem.kt
│   │   ├── Achievement.kt
│   │   ├── Challenge.kt
│   │   └── ProgressionManager.kt
│   │
│   ├── backend/ [NEW]
│   │   ├── FirebaseService.kt
│   │   ├── AuthManager.kt
│   │   ├── ProfileRepository.kt
│   │   ├── LeaderboardRepository.kt
│   │   └── GameRepository.kt
│   │
│   ├── multiplayer/ [NEW]
│   │   ├── MultiplayerManager.kt
│   │   ├── LobbyManager.kt
│   │   ├── MatchmakingService.kt
│   │   ├── GameSyncManager.kt
│   │   ├── RemotePlayer.kt
│   │   └── MultiplayerGameState.kt
│   │
│   ├── ui/
│   │   ├── screens/
│   │   │   ├── GameScreen.kt (existing)
│   │   │   ├── ARGameScreen.kt (existing)
│   │   │   ├── MainMenuScreen.kt [NEW]
│   │   │   ├── SinglePlayerMenu.kt [NEW]
│   │   │   ├── MultiplayerLobbyScreen.kt [NEW]
│   │   │   ├── CustomizeScreen.kt [NEW]
│   │   │   ├── ProfileScreen.kt [NEW]
│   │   │   ├── LeaderboardScreen.kt [NEW]
│   │   │   ├── ChallengesScreen.kt [NEW]
│   │   │   └── SettingsScreen.kt [NEW]
│   │   │
│   │   ├── components/
│   │   │   ├── CoinOverlay.kt (existing, modify)
│   │   │   ├── AnimatedCoin.kt (existing, modify)
│   │   │   ├── PowerUpHUD.kt [NEW]
│   │   │   ├── ComboDisplay.kt [NEW]
│   │   │   ├── MiniMap.kt [NEW]
│   │   │   ├── LiveLeaderboard.kt [NEW]
│   │   │   ├── PlayerCard.kt [NEW]
│   │   │   └── AchievementPopup.kt [NEW]
│   │   │
│   │   └── effects/ [NEW]
│   │       ├── ParticleCanvas.kt
│   │       ├── ScreenShake.kt
│   │       ├── Confetti.kt
│   │       └── GlowEffect.kt
│   │
│   └── platform/ (existing)
│
├── androidMain/kotlin/com/keren/virtualmoney/
│   ├── audio/ [NEW]
│   │   ├── SoundManager.android.kt
│   │   └── HapticManager.android.kt
│   └── ... (existing)
│
└── iosMain/kotlin/com/keren/virtualmoney/
    ├── audio/ [NEW]
    │   ├── SoundManager.ios.kt
    │   └── HapticManager.ios.kt
    └── ... (existing)
```

---

## Implementation Order

### Step 1: Power-Ups & Combos
1. Create `PowerUp.kt`, `PowerUpManager.kt`
2. Create `ComboTracker.kt`
3. Modify `GameEngine.kt` to integrate both
4. Create `PowerUpHUD.kt`, `ComboDisplay.kt`
5. Test in single player

### Step 2: Audio & Haptics
1. Create `GameSound.kt`
2. Create `SoundManager` (expect/actual)
3. Create `HapticManager` (expect/actual)
4. Add sound effects to game events
5. Add haptic feedback

### Step 3: Themes & Skins
1. Create `GameTheme.kt`, `CoinSkin.kt`
2. Create `ThemeManager.kt`
3. Modify `CoinOverlay.kt` for skins
4. Create `CustomizeScreen.kt`
5. Add background rendering per theme

### Step 4: Progression
1. Create `PlayerProfile.kt`, `LevelSystem.kt`
2. Create `Achievement.kt`, `Challenge.kt`
3. Create `ProgressionManager.kt`
4. Create `ProfileScreen.kt`
5. Create `ChallengesScreen.kt`

### Step 5: Firebase
1. Add Firebase dependencies
2. Create `FirebaseService.kt`
3. Create `AuthManager.kt`
4. Create repositories
5. Connect to UI

### Step 6: Single Player Polish
1. Create `MainMenuScreen.kt`
2. Create `SinglePlayerMenu.kt`
3. Add all game modes to engine
4. Create `LeaderboardScreen.kt`
5. Full single player testing

### Step 7: Multiplayer
1. Create multiplayer data models
2. Create `LobbyManager.kt`
3. Create `MatchmakingService.kt`
4. Create `GameSyncManager.kt`
5. Create `MultiplayerLobbyScreen.kt`
6. Create `MiniMap.kt`, `LiveLeaderboard.kt`
7. Full multiplayer testing

---

**Ready to start Phase 1?**
