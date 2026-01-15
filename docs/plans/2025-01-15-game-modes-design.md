# Coin Hunter - Complete Game Design Document

**Date:** 2025-01-15
**Version:** 2.0
**Platform:** Kotlin Multiplatform (Android + iOS)

---

## Table of Contents

1. [Game Overview](#1-game-overview)
2. [Single Player Mode](#2-single-player-mode)
3. [Multiplayer Mode (2-100 Players)](#3-multiplayer-mode-2-100-players)
4. [Power-Ups System](#4-power-ups-system)
5. [Themes & Skins](#5-themes--skins)
6. [Visual & Audio Polish](#6-visual--audio-polish)
7. [Progression System](#7-progression-system)
8. [Technical Architecture (KMP)](#8-technical-architecture-kmp)

---

## 1. Game Overview

### Core Concept
An AR coin hunting battle game where players compete to collect coins in augmented reality. Simple to learn, hard to master, endlessly competitive.

### Key Pillars
| Pillar | How We Achieve It |
|--------|-------------------|
| **Addictive** | Short rounds, instant replay, progression rewards |
| **Fun** | Power-ups, combos, satisfying feedback |
| **Simple** | One action: collect coins. Easy to understand |
| **Competitive** | Live leaderboards, eliminations, coin stealing |
| **Appealing** | Beautiful themes, smooth animations, juicy effects |

---

## 2. Single Player Mode

### 2.1 Game Modes

#### Classic Mode (60 seconds)
- Collect as many Hapoalim coins as possible
- Avoid penalty coins (-15 points each)
- Beat your high score

#### Blitz Mode (30 seconds)
- Fast, intense gameplay
- More coins spawn at once
- Perfect for quick sessions

#### Survival Mode (Endless)
- Start with 3 lives (hearts)
- Collecting penalty coin = lose 1 life
- Coins spawn faster over time
- How long can you survive?

#### Challenge Mode (Daily/Weekly)
- New challenge every day
- Special rules and modifiers
- Compete on global leaderboard

### 2.2 Single Player Challenges

#### Daily Challenges (Reset every 24h)
| Challenge | Description | Reward |
|-----------|-------------|--------|
| **Coin Collector** | Collect 100 coins total | 50 XP |
| **Perfect Run** | Complete a game without hitting penalty coins | 100 XP + Coin skin |
| **Speed Demon** | Collect 30 coins in Blitz mode | 75 XP |
| **Survivor** | Last 2 minutes in Survival mode | 100 XP |
| **Combo Master** | Get a 10x combo | 80 XP |

#### Weekly Challenges (Reset every Monday)
| Challenge | Description | Reward |
|-----------|-------------|--------|
| **Marathon** | Play 50 games | 500 XP + Theme unlock |
| **High Roller** | Score 1000+ in a single game | 300 XP + Rare skin |
| **Perfectionist** | Complete 10 perfect runs | 400 XP + Title |
| **Explorer** | Use 3 different themes | 200 XP |
| **Power Player** | Collect 20 power-ups | 250 XP |

#### Achievement System
| Achievement | Requirement | Reward |
|-------------|-------------|--------|
| **First Steps** | Complete first game | Bronze badge |
| **Getting Started** | Score 100 points | 25 XP |
| **Coin Hunter** | Collect 500 coins total | Silver badge |
| **Veteran** | Play 100 games | Gold badge |
| **Master Hunter** | Score 500+ in one game | Platinum badge |
| **Untouchable** | 5 perfect runs in a row | Diamond badge + Title |

### 2.3 Single Player UI Flow

```
┌─────────────────────────────────────────────────────────────┐
│                      MAIN MENU                               │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│                    COIN HUNTER                               │
│                    [Animated Logo]                           │
│                                                              │
│     ┌─────────────┐     ┌─────────────┐                     │
│     │ SINGLE      │     │ MULTIPLAYER │                     │
│     │ PLAYER      │     │ BATTLE      │                     │
│     └─────────────┘     └─────────────┘                     │
│                                                              │
│     ┌─────────────┐     ┌─────────────┐                     │
│     │ CUSTOMIZE   │     │ CHALLENGES  │                     │
│     │ (Themes)    │     │ (Daily)     │                     │
│     └─────────────┘     └─────────────┘                     │
│                                                              │
│     [Settings]  [Leaderboard]  [Profile]                    │
│                                                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   SINGLE PLAYER MENU                         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│     ┌───────────────────────────────────────────┐           │
│     │  CLASSIC          Best: 450    [60s]      │           │
│     │  Standard coin hunting                    │           │
│     └───────────────────────────────────────────┘           │
│                                                              │
│     ┌───────────────────────────────────────────┐           │
│     │  BLITZ            Best: 180    [30s]      │           │
│     │  Fast and intense                         │           │
│     └───────────────────────────────────────────┘           │
│                                                              │
│     ┌───────────────────────────────────────────┐           │
│     │  SURVIVAL         Best: 3:45   [Endless]  │           │
│     │  How long can you last?                   │           │
│     └───────────────────────────────────────────┘           │
│                                                              │
│     ┌───────────────────────────────────────────┐           │
│     │  CHALLENGE        Daily Reset  [Special]  │           │
│     │  Today: "No Penalties Allowed"            │           │
│     └───────────────────────────────────────────┘           │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Multiplayer Mode (2-100 Players)

### 3.1 Multiplayer Concepts

#### Player Presence
Players see each other as:
- **Radar dots** on mini-map (shows direction & distance)
- **Glow effect** when nearby (within 5 meters)
- **Name tags** floating above their position

#### Coin Stealing Mechanic
```
When Player A is within 3 meters of Player B:
  - Player A can "intercept" coins Player B is approaching
  - Visual: Coins have a "contested" glow when both players are near
  - The faster player gets the coin
  - Intercepted coins give +5 bonus points
```

### 3.2 Multiplayer Game Modes

#### Quick Match (2-10 Players)
| Setting | Value |
|---------|-------|
| Duration | 60 seconds |
| Players | 2-10 |
| Matchmaking | Skill-based (ELO) |
| Win Condition | Highest score |

#### Battle Royale (10-100 Players)
| Setting | Value |
|---------|-------|
| Duration | 3-5 minutes |
| Players | 10-100 |
| Elimination | Bottom 20% every 30s |
| Win Condition | Last player standing |

**Battle Royale Flow:**
```
Start: 100 Players
  ↓ (30 seconds)
80 Players remain (20 eliminated)
  ↓ (30 seconds)
64 Players remain
  ↓ (30 seconds)
51 Players remain
  ↓ (continues...)
  ↓
Final 2 Players: 60-second showdown
  ↓
Winner!
```

#### Team Battle (4-50 Players)
| Setting | Value |
|---------|-------|
| Duration | 3 minutes |
| Teams | 2-10 teams of 2-5 players |
| Scoring | Team total score |
| Win Condition | Highest team score |

#### King of the Hill (2-20 Players)
| Setting | Value |
|---------|-------|
| Duration | 2 minutes |
| Win Condition | Hold #1 position for 30 seconds total |
| Twist | Only #1 player's timer counts |

### 3.3 Multiplayer UI

#### Lobby Screen
```
┌─────────────────────────────────────────────────────────────┐
│  BATTLE ROYALE LOBBY                    [Leave]             │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│     Players: 47/100                     Starting in: 15s     │
│     ████████████████████░░░░░░░░░░░░░░                      │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  PLAYERS                                             │    │
│  │  1. CoinMaster99      [Level 45] [Crown]            │    │
│  │  2. HunterPro         [Level 32]                    │    │
│  │  3. You               [Level 18]  ←                 │    │
│  │  4. GoldRush2024      [Level 27]                    │    │
│  │  5. SneakyCollector   [Level 41]                    │    │
│  │  ... +42 more                                        │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│     [Chat]              [Invite Friends]                    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

#### In-Game HUD (Multiplayer)
```
┌─────────────────────────────────────────────────────────────┐
│  #3/47    Score: 145    ⏱ 1:45    [Pause]                   │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│     [AR Camera View with Coins]                             │
│                                                              │
│                    ┌───────────┐                            │
│                    │  MINI-MAP │                            │
│                    │    ·  ·   │  ← Other players           │
│                    │  · YOU ·  │                            │
│                    │    ·      │                            │
│                    └───────────┘                            │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ LIVE LEADERBOARD                                     │    │
│  │ #1 CoinMaster99   210                               │    │
│  │ #2 HunterPro      178                               │    │
│  │ #3 You            145  ←                            │    │
│  │ #4 GoldRush2024   139                               │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│     47 Players Remaining    Next elimination: 0:15          │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

#### Elimination Alert
```
┌─────────────────────────────────────────────────────────────┐
│                                                              │
│               ⚠️ ELIMINATION INCOMING ⚠️                     │
│                                                              │
│              Bottom 10 players will be eliminated            │
│              in 10... 9... 8...                              │
│                                                              │
│              Your rank: #38 / 47                             │
│              SAFE (Top 37 survive)                           │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 3.4 Multiplayer Networking Architecture (KMP)

#### Technology Stack
```kotlin
// commonMain - Shared networking interface
expect class MultiplayerClient {
    suspend fun connect(serverUrl: String)
    suspend fun joinLobby(gameMode: GameMode): Lobby
    suspend fun sendPosition(pose: Pose)
    suspend fun sendCoinCollected(coinId: String)
    fun observeGameState(): Flow<MultiplayerGameState>
    fun observePlayers(): Flow<List<RemotePlayer>>
    suspend fun disconnect()
}

// Data classes
data class RemotePlayer(
    val id: String,
    val name: String,
    val position: Vector3D,
    val score: Int,
    val isEliminated: Boolean
)

data class MultiplayerGameState(
    val phase: GamePhase,  // Lobby, Playing, Elimination, Finished
    val players: List<RemotePlayer>,
    val coins: List<Coin>,
    val timeRemaining: Int,
    val eliminationCountdown: Int?
)
```

#### Server Communication
```
Client ←→ WebSocket ←→ Game Server

Messages:
  → POSITION_UPDATE { pose }
  → COIN_COLLECTED { coinId }
  → USE_POWERUP { powerupId }

  ← GAME_STATE { players, coins, time }
  ← PLAYER_JOINED { player }
  ← PLAYER_ELIMINATED { playerId }
  ← COIN_SPAWNED { coin }
  ← COIN_REMOVED { coinId, collectedBy }
  ← ELIMINATION_WARNING { countdown }
```

---

## 4. Power-Ups System

### 4.1 Power-Up Types

#### Magnet (5 seconds)
```
Effect: Auto-collect coins within 2 meters
Visual: Blue magnetic field around player
Sound: Humming electric sound
Icon: 🧲
Spawn Rate: 15%
```

#### 2x Multiplier (10 seconds)
```
Effect: Double points for all coins
Visual: Golden glow on coins, "2X" badge on HUD
Sound: Cha-ching on collection
Icon: ✨
Spawn Rate: 20%
```

#### Shield (8 seconds)
```
Effect: Penalty coins don't affect score
Visual: Translucent blue bubble around player
Sound: Deflection sound when hitting penalty
Icon: 🛡️
Spawn Rate: 20%
```

#### Freeze (Multiplayer Only, 3 seconds)
```
Effect: Nearby opponents (within 5m) can't move
Visual: Ice crystals on frozen players' screens
Sound: Freezing crack sound
Icon: ❄️
Spawn Rate: 10% (multiplayer only)
```

#### Invisibility (Multiplayer Only, 5 seconds)
```
Effect: Hidden from other players' radar
Visual: Player fades to transparent
Sound: Whisper/wind sound
Icon: 👻
Spawn Rate: 10% (multiplayer only)
```

### 4.2 Power-Up Spawning

```kotlin
data class PowerUp(
    val id: String,
    val type: PowerUpType,
    val position3D: Vector3D,
    val spawnTime: Long,
    val expiresAt: Long  // Despawn after 10 seconds if not collected
)

enum class PowerUpType(
    val duration: Long,
    val spawnWeight: Float,
    val multiplayerOnly: Boolean
) {
    MAGNET(5000, 0.15f, false),
    MULTIPLIER(10000, 0.20f, false),
    SHIELD(8000, 0.20f, false),
    FREEZE(3000, 0.10f, true),
    INVISIBILITY(5000, 0.10f, true)
}
```

### 4.3 Power-Up UI

```
Active Power-Up Display (top-right):
┌──────────────┐
│ 🛡️ SHIELD   │
│ ████░░ 5s   │
└──────────────┘

Power-Up Collection Animation:
  [Power-up icon zooms to HUD]
  [Flash effect on screen]
  [Sound effect plays]
```

---

## 5. Themes & Skins

### 5.1 Background Themes

#### Camera Theme (Default)
```
Background: Live AR camera feed
Coins: Standard bank logos
Atmosphere: Real world
```

#### Forest Theme
```
Background: Animated forest scene with trees, leaves
Coins: Leaf-wrapped, nature colors
Atmosphere: Birds chirping, wind sounds
Particles: Floating leaves, butterflies
```

#### Galaxy Theme
```
Background: Deep space with stars, nebulas
Coins: Glowing cosmic orbs
Atmosphere: Space ambient sounds
Particles: Stardust, shooting stars
```

#### Ocean Theme
```
Background: Underwater scene with fish
Coins: Treasure chest coins, bubbles
Atmosphere: Water sounds, whale calls
Particles: Bubbles, small fish
```

#### Neon City Theme
```
Background: Cyberpunk cityscape at night
Coins: Neon-lit, glowing edges
Atmosphere: Synthwave music, electronic sounds
Particles: Neon sparks, holographic effects
```

### 5.2 Coin Skins

| Skin Name | Description | How to Unlock |
|-----------|-------------|---------------|
| **Classic** | Original bank logos | Default |
| **Golden** | Shiny gold coins | Level 5 |
| **Diamond** | Crystal clear with sparkle | Level 15 |
| **Neon** | Glowing neon outline | Level 25 |
| **Pixel** | 8-bit retro style | Complete "Retro" challenge |
| **Holographic** | Rainbow shift effect | Win 10 multiplayer games |
| **Fire** | Flames around coin | 5 perfect runs |
| **Ice** | Frozen crystal effect | Use Freeze power-up 50x |
| **Rainbow** | Color cycling | Collect 1000 coins total |
| **Legendary** | Ultimate animated skin | Reach #1 on leaderboard |

### 5.3 Theme Selection UI

```
┌─────────────────────────────────────────────────────────────┐
│  CUSTOMIZE                                    [Back]         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  THEMES                                                      │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐           │
│  │ 📷      │ │ 🌲      │ │ 🌌      │ │ 🌊      │           │
│  │ Camera  │ │ Forest  │ │ Galaxy  │ │ Ocean   │           │
│  │ [✓]     │ │ [🔒 L10]│ │ [🔒 L20]│ │ [🔒 L30]│           │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘           │
│                                                              │
│  COIN SKINS                                                  │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐           │
│  │ 🪙      │ │ ✨      │ │ 💎      │ │ 🔥      │           │
│  │ Classic │ │ Golden  │ │ Diamond │ │ Fire    │           │
│  │ [✓]     │ │ [owned] │ │ [🔒 L15]│ │ [🔒]    │           │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘           │
│                                                              │
│  PREVIEW                                                     │
│  ┌───────────────────────────────────────────────────┐      │
│  │                                                    │      │
│  │     [Animated preview of selected theme + skin]   │      │
│  │                                                    │      │
│  └───────────────────────────────────────────────────┘      │
│                                                              │
│                    [Apply]                                   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 5.4 Theme Implementation (KMP)

```kotlin
// commonMain
data class GameTheme(
    val id: String,
    val name: String,
    val backgroundType: BackgroundType,
    val ambientSoundId: String?,
    val particleEffects: List<ParticleEffect>,
    val unlockRequirement: UnlockRequirement
)

enum class BackgroundType {
    CAMERA,           // AR camera feed
    STATIC_IMAGE,     // Static background
    ANIMATED,         // Animated scene (Lottie/custom)
    VIDEO_LOOP        // Looping video
}

data class CoinSkin(
    val id: String,
    val name: String,
    val baseTextureId: String,
    val glowColor: Color?,
    val particleEffect: ParticleEffect?,
    val animationType: CoinAnimationType,
    val unlockRequirement: UnlockRequirement
)

sealed class UnlockRequirement {
    object Default : UnlockRequirement()
    data class Level(val minLevel: Int) : UnlockRequirement()
    data class Achievement(val achievementId: String) : UnlockRequirement()
    data class Challenge(val challengeId: String) : UnlockRequirement()
    data class Purchase(val productId: String) : UnlockRequirement()
}
```

---

## 6. Visual & Audio Polish

### 6.1 Coin Collection Effects

#### Pop Effect
```
On coin tap:
1. Play "pop" sound (satisfying bubble pop)
2. Scale coin to 1.5x over 100ms
3. Fade to transparent over 200ms
4. Spawn 8-12 particles (coin color) expanding outward
5. Add +10 floating text that rises and fades
```

#### Combo System
```kotlin
data class ComboState(
    val count: Int = 0,
    val lastCollectionTime: Long = 0,
    val comboWindow: Long = 2000  // 2 seconds
)

// Combo multiplier
fun getComboMultiplier(comboCount: Int): Float = when {
    comboCount < 3 -> 1.0f
    comboCount < 5 -> 1.2f   // 3-4 combo
    comboCount < 10 -> 1.5f  // 5-9 combo
    comboCount < 20 -> 2.0f  // 10-19 combo
    else -> 2.5f             // 20+ combo
}
```

#### Combo UI
```
┌─────────────────────────┐
│   COMBO x7!             │
│   ███████░░░            │  ← Timer bar (resets with each coin)
│   +1.5x MULTIPLIER      │
└─────────────────────────┘

Visual:
- Text scales up with combo count
- Screen edge glows gold at high combos
- Particles increase with combo
```

### 6.2 Near-Miss Warning

```
When penalty coin is within 1 meter:
1. Screen edges pulse red
2. Warning vibration (short pulse)
3. Danger sound (subtle alarm)
4. Penalty coin glows brighter red

When penalty coin is within 0.5 meters:
1. Screen shake (subtle)
2. Stronger vibration
3. Louder warning sound
```

### 6.3 Victory Screen

```
┌─────────────────────────────────────────────────────────────┐
│                                                              │
│                      🎉 GAME OVER 🎉                         │
│                                                              │
│                    ┌─────────────┐                          │
│                    │    450      │                          │
│                    │   POINTS    │                          │
│                    └─────────────┘                          │
│                                                              │
│                   ⭐ NEW HIGH SCORE! ⭐                       │
│                   Previous: 380                              │
│                                                              │
│     Stats:                                                   │
│     • Coins collected: 52                                    │
│     • Best combo: 12x                                        │
│     • Power-ups used: 3                                      │
│     • Perfect accuracy: 94%                                  │
│                                                              │
│     Rewards:                                                 │
│     +150 XP    +1 Level Up!    🏆 Achievement unlocked       │
│                                                              │
│     ┌─────────────┐     ┌─────────────┐                     │
│     │ PLAY AGAIN  │     │    MENU     │                     │
│     └─────────────┘     └─────────────┘                     │
│                                                              │
│     [Share]        [Leaderboard]                            │
│                                                              │
└─────────────────────────────────────────────────────────────┘

Animations:
- Confetti falls from top
- Score counts up from 0
- Stars burst when showing new high score
- Level up has special glow effect
```

### 6.4 Haptic Feedback

| Event | Haptic Type | Intensity |
|-------|-------------|-----------|
| Coin collected | Success | Medium |
| Combo milestone (5, 10, 20) | Success | Strong |
| Penalty coin hit | Error | Strong |
| Power-up collected | Impact | Medium |
| Near-miss warning | Warning | Light pulse |
| Game start countdown | Tick | Light |
| Elimination warning | Alarm | Pulsing |
| Victory | Celebration | Pattern |

### 6.5 Sound Design

```kotlin
enum class GameSound(val resourceId: String) {
    // Collection
    COIN_COLLECT("coin_pop"),
    PENALTY_HIT("negative_buzz"),
    POWERUP_COLLECT("powerup_chime"),

    // Combos
    COMBO_START("combo_start"),
    COMBO_BUILD("combo_whoosh"),
    COMBO_BREAK("combo_break"),

    // Power-ups
    MAGNET_ACTIVE("magnet_hum"),
    SHIELD_ACTIVE("shield_bubble"),
    SHIELD_BLOCK("shield_deflect"),
    FREEZE_CAST("ice_crack"),
    INVISIBILITY_ON("whisper_fade"),

    // Game events
    COUNTDOWN_TICK("tick"),
    GAME_START("game_start"),
    GAME_END("game_end"),
    NEW_HIGH_SCORE("fanfare"),
    LEVEL_UP("level_up"),

    // Multiplayer
    PLAYER_ELIMINATED("elimination"),
    PLAYER_NEARBY("radar_ping"),
    COIN_STOLEN("steal_whoosh"),
    ELIMINATION_WARNING("alarm"),
    VICTORY("victory_fanfare")
}
```

---

## 7. Progression System

### 7.1 Experience & Levels

```
XP Sources:
- Complete game: 10 XP
- Per coin collected: 1 XP
- High score beaten: 50 XP
- Daily challenge: 50-100 XP
- Weekly challenge: 200-500 XP
- Multiplayer win: 100 XP
- Multiplayer top 10: 50 XP

Level Requirements:
Level 1:   0 XP
Level 2:   100 XP
Level 3:   250 XP
Level 4:   450 XP
Level 5:   700 XP (Unlock: Golden skin)
...
Level 10:  2000 XP (Unlock: Forest theme)
Level 15:  4000 XP (Unlock: Diamond skin)
Level 20:  7000 XP (Unlock: Galaxy theme)
Level 25:  11000 XP (Unlock: Neon skin)
Level 30:  16000 XP (Unlock: Ocean theme)
...
Level 50:  50000 XP (Prestige available)
```

### 7.2 Prestige System

```
At Level 50, players can "Prestige":
- Reset to Level 1
- Keep all unlocked cosmetics
- Gain Prestige badge (shown to other players)
- Unlock exclusive Prestige rewards
- 10% permanent XP bonus per prestige

Prestige Levels: Bronze → Silver → Gold → Platinum → Diamond
```

### 7.3 Player Profile

```
┌─────────────────────────────────────────────────────────────┐
│  PROFILE                                      [Edit]         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│     ┌─────────┐                                             │
│     │  [Avatar │   CoinHunter99                             │
│     │   Image] │   Level 27 ⭐⭐⭐                            │
│     └─────────┘   Prestige: Silver                          │
│                                                              │
│     XP: 8,450 / 11,000                                      │
│     █████████████████░░░░░░░░░                              │
│                                                              │
│     STATS                                                    │
│     ┌──────────────┬──────────────┬──────────────┐          │
│     │ Games: 847   │ Coins: 12,450│ High: 523    │          │
│     └──────────────┴──────────────┴──────────────┘          │
│     ┌──────────────┬──────────────┬──────────────┐          │
│     │ MP Wins: 43  │ Top 10: 156  │ Best Combo:25│          │
│     └──────────────┴──────────────┴──────────────┘          │
│                                                              │
│     ACHIEVEMENTS  [12/50]                                    │
│     🏆🏆🏆🏆🏆🏆🏆🏆🏆🏆🏆🏆⬜⬜⬜...                            │
│                                                              │
│     TITLES                                                   │
│     [Coin Master] [Survivor] [Speed Demon]                  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 8. Technical Architecture (KMP)

### 8.1 Module Structure

```
composeApp/src/
├── commonMain/kotlin/com/keren/virtualmoney/
│   ├── game/
│   │   ├── GameEngine.kt          (Core game logic)
│   │   ├── GameState.kt           (State management)
│   │   ├── Coin.kt                (Coin data & spawning)
│   │   ├── PowerUp.kt             (Power-up system) [NEW]
│   │   └── ComboTracker.kt        (Combo logic) [NEW]
│   │
│   ├── multiplayer/               [NEW MODULE]
│   │   ├── MultiplayerClient.kt   (Network interface)
│   │   ├── MultiplayerState.kt    (MP game state)
│   │   ├── Lobby.kt               (Lobby management)
│   │   ├── RemotePlayer.kt        (Other players)
│   │   └── SyncManager.kt         (State synchronization)
│   │
│   ├── progression/               [NEW MODULE]
│   │   ├── PlayerProfile.kt       (Profile data)
│   │   ├── LevelSystem.kt         (XP & levels)
│   │   ├── Achievements.kt        (Achievement tracking)
│   │   ├── Challenges.kt          (Daily/weekly)
│   │   └── Unlockables.kt         (Themes, skins)
│   │
│   ├── theme/                     [NEW MODULE]
│   │   ├── GameTheme.kt           (Theme data)
│   │   ├── CoinSkin.kt            (Skin data)
│   │   ├── ThemeManager.kt        (Theme loading)
│   │   └── ParticleEffect.kt      (Visual effects)
│   │
│   ├── ar/                        (Existing AR code)
│   │   ├── math/
│   │   ├── projection/
│   │   └── camera/
│   │
│   ├── audio/                     [NEW MODULE]
│   │   ├── SoundManager.kt        (Sound playback)
│   │   └── HapticManager.kt       (Vibration)
│   │
│   └── ui/
│       ├── screens/
│       │   ├── MainMenuScreen.kt   [NEW]
│       │   ├── SinglePlayerMenu.kt [NEW]
│       │   ├── MultiplayerLobby.kt [NEW]
│       │   ├── CustomizeScreen.kt  [NEW]
│       │   ├── ProfileScreen.kt    [NEW]
│       │   └── ...existing screens
│       │
│       ├── components/
│       │   ├── CoinOverlay.kt      (Updated with skins)
│       │   ├── PowerUpHUD.kt       [NEW]
│       │   ├── ComboDisplay.kt     [NEW]
│       │   ├── MiniMap.kt          [NEW]
│       │   ├── Leaderboard.kt      [NEW]
│       │   └── ParticleCanvas.kt   [NEW]
│       │
│       └── effects/
│           ├── ScreenShake.kt      [NEW]
│           ├── Confetti.kt         [NEW]
│           └── GlowEffect.kt       [NEW]
│
├── androidMain/kotlin/
│   ├── multiplayer/
│   │   └── MultiplayerClient.android.kt (Ktor WebSocket)
│   ├── audio/
│   │   ├── SoundManager.android.kt (SoundPool)
│   │   └── HapticManager.android.kt (Vibrator)
│   └── ... existing platform code
│
└── iosMain/kotlin/
    ├── multiplayer/
    │   └── MultiplayerClient.ios.kt (URLSession WebSocket)
    ├── audio/
    │   ├── SoundManager.ios.kt (AVAudioPlayer)
    │   └── HapticManager.ios.kt (UIImpactFeedbackGenerator)
    └── ... existing platform code
```

### 8.2 Dependencies to Add

```kotlin
// build.gradle.kts (commonMain)
commonMain.dependencies {
    // Networking for multiplayer
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-websockets:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // Date/time for challenges
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // Settings storage
    implementation("com.russhwolf:multiplatform-settings:1.1.1")
}

// Android
androidMain.dependencies {
    implementation("io.ktor:ktor-client-okhttp:2.3.7")
}

// iOS
iosMain.dependencies {
    implementation("io.ktor:ktor-client-darwin:2.3.7")
}
```

### 8.3 Data Persistence

```kotlin
// Local storage for offline data
interface GameStorage {
    // Profile
    suspend fun saveProfile(profile: PlayerProfile)
    suspend fun loadProfile(): PlayerProfile?

    // Progress
    suspend fun saveProgress(progress: PlayerProgress)
    suspend fun loadProgress(): PlayerProgress

    // Settings
    suspend fun saveSettings(settings: GameSettings)
    suspend fun loadSettings(): GameSettings

    // Unlocks
    suspend fun saveUnlocks(unlocks: Set<String>)
    suspend fun loadUnlocks(): Set<String>
}

// Server sync for multiplayer
interface GameServer {
    suspend fun authenticate(token: String): Player
    suspend fun syncProfile(profile: PlayerProfile)
    suspend fun getLeaderboard(type: LeaderboardType): List<LeaderboardEntry>
    suspend fun submitScore(score: GameScore)
}
```

---

## Implementation Priority

### Phase 1: Core Enhancements
1. Power-ups system (single player)
2. Combo system & visual feedback
3. Sound effects & haptics
4. Basic themes (Camera + 1 animated)

### Phase 2: Progression
1. XP & leveling system
2. Achievements
3. Daily challenges
4. Unlockable skins & themes

### Phase 3: Multiplayer Foundation
1. Network layer (WebSocket)
2. Quick Match (2-4 players)
3. Live leaderboard
4. Basic matchmaking

### Phase 4: Multiplayer Advanced
1. Battle Royale mode
2. Team battles
3. Coin stealing mechanic
4. Elimination system

### Phase 5: Polish
1. All themes completed
2. All skins implemented
3. Advanced particle effects
4. Performance optimization

---

**End of Game Design Document**

Ready for implementation!
