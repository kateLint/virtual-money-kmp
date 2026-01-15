# Firebase Database Schema

## Firestore Collections

### users/{userId}
```javascript
{
  displayName: "CoinHunter99",
  avatarUrl: "https://...",
  level: 27,
  xp: 8450,
  prestige: 0,
  selectedTheme: "camera",
  selectedSkin: "golden",
  createdAt: Timestamp,
  lastLogin: Timestamp
}
```

### users/{userId}/stats/global
```javascript
{
  gamesPlayed: 847,
  totalCoins: 12450,
  highScore: 523,
  bestCombo: 25,
  perfectRuns: 12,
  totalPlayTime: 3600000, // ms
  multiplayerWins: 43,
  multiplayerTop10: 156,
  battleRoyaleWins: 8
}
```

### users/{userId}/unlocks/items
```javascript
{
  themes: ["camera", "forest", "galaxy"],
  skins: ["classic", "golden", "diamond"]
}
```

### users/{userId}/achievements/earned
```javascript
{
  first_game: Timestamp,
  coin_hunter: Timestamp,
  combo_master: Timestamp
  // ... more achievement IDs with unlock timestamps
}
```

### leaderboards/daily/{date}/{rank}
```javascript
{
  odId: "abc123",
  displayName: "CoinHunter99",
  avatarUrl: "https://...",
  score: 523,
  level: 27,
  timestamp: Timestamp
}
```

### leaderboards/weekly/{weekId}/{rank}
Same structure as daily.

### leaderboards/allTime/{rank}
Same structure as daily.

### leaderboards/multiplayer/{seasonId}/{rank}
```javascript
{
  odId: "abc123",
  displayName: "CoinHunter99",
  avatarUrl: "https://...",
  wins: 43,
  gamesPlayed: 156,
  winRate: 0.276,
  level: 27,
  timestamp: Timestamp
}
```

### challenges/daily/{date}
```javascript
{
  id: "perfect_run",
  type: "PERFECT_RUN",
  description: "Complete a game without hitting penalty coins",
  target: 1,
  reward: {
    xp: 100,
    skin: null
  },
  completedBy: ["userId1", "userId2"]
}
```

### challenges/weekly/{weekId}
```javascript
{
  id: "marathon",
  type: "GAMES_PLAYED",
  description: "Play 50 games this week",
  target: 50,
  reward: {
    xp: 500,
    theme: "forest"
  },
  progress: {
    "userId1": 32,
    "userId2": 50
  },
  completedBy: ["userId2"]
}
```

### gameConfig/current
```javascript
{
  version: "2.0",
  minAppVersion: "2.0.0",
  maintenanceMode: false,
  seasonId: "season_1",
  seasonEndDate: Timestamp,
  powerUpSpawnRates: {
    magnet: 0.15,
    multiplier: 0.20,
    shield: 0.20,
    freeze: 0.10,
    invisibility: 0.10
  },
  coinValues: {
    hapoalim: 10,
    penalty: -15
  }
}
```

---

## Realtime Database Structure

### lobbies/{lobbyId}
```javascript
{
  gameMode: "battle_royale",
  maxPlayers: 100,
  status: "waiting", // waiting | countdown | playing | finished
  hostId: "userId",
  createdAt: ServerTimestamp,
  startTime: null, // set when game starts

  players: {
    "userId1": {
      displayName: "CoinHunter99",
      avatarUrl: "https://...",
      level: 27,
      ready: true,
      teamId: null,
      joinedAt: ServerTimestamp
    },
    "userId2": {
      // ...
    }
  }
}
```

### games/{gameId}
```javascript
{
  lobbyId: "lobby123",
  gameMode: "battle_royale",
  phase: "playing", // countdown | playing | elimination | finished
  startTime: ServerTimestamp,
  timeRemaining: 145,

  eliminationCountdown: null, // seconds until next elimination
  nextEliminationCount: 10, // how many will be eliminated

  players: {
    "userId1": {
      displayName: "CoinHunter99",
      score: 145,
      rank: 3,
      isEliminated: false,
      position: { x: 1.2, y: 0.5, z: -0.8 },
      lastUpdate: ServerTimestamp
    }
  },

  coins: {
    "coinId1": {
      type: "hapoalim",
      position: { x: 2.0, y: 1.0, z: 1.5 },
      spawnTime: ServerTimestamp,
      collectedBy: null
    }
  },

  powerUps: {
    "powerUpId1": {
      type: "magnet",
      position: { x: -1.0, y: 0.5, z: 2.0 },
      spawnTime: ServerTimestamp,
      collectedBy: null
    }
  },

  events: {
    "eventId1": {
      type: "elimination",
      data: { eliminatedPlayers: ["userId5", "userId8"] },
      timestamp: ServerTimestamp
    }
  }
}
```

### matchmaking/queues/{gameMode}/{odId}
```javascript
{
  odId: "userId1",
  displayName: "CoinHunter99",
  skill: 1200, // ELO rating
  level: 27,
  region: "eu",
  joinedAt: ServerTimestamp
}
```

### presence/{userId}
```javascript
{
  online: true,
  lastSeen: ServerTimestamp,
  inGame: "gameId123", // or null
  inLobby: null
}
```

---

## Security Rules (Firestore)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users can read/write their own profile
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;

      match /stats/{statId} {
        allow read: if request.auth != null;
        allow write: if request.auth.uid == userId;
      }

      match /unlocks/{unlockId} {
        allow read: if request.auth != null;
        allow write: if request.auth.uid == userId;
      }

      match /achievements/{achievementId} {
        allow read: if request.auth != null;
        allow write: if request.auth.uid == userId;
      }
    }

    // Leaderboards - anyone can read, only functions can write
    match /leaderboards/{type}/{id}/{rank} {
      allow read: if request.auth != null;
      allow write: if false; // Only Cloud Functions
    }

    // Challenges - anyone can read, only functions can write
    match /challenges/{type}/{id} {
      allow read: if request.auth != null;
      allow write: if false; // Only Cloud Functions
    }

    // Game config - read only
    match /gameConfig/{docId} {
      allow read: if request.auth != null;
      allow write: if false;
    }
  }
}
```

## Security Rules (Realtime Database)

```javascript
{
  "rules": {
    "lobbies": {
      "$lobbyId": {
        ".read": "auth != null",
        ".write": "auth != null",
        "players": {
          "$odId": {
            ".write": "auth.uid == $odId || data.child('hostId').val() == auth.uid"
          }
        }
      }
    },

    "games": {
      "$gameId": {
        ".read": "auth != null",
        "players": {
          "$odId": {
            ".write": "auth.uid == $odId"
          }
        },
        "coins": {
          ".write": false // Only Cloud Functions
        },
        "powerUps": {
          ".write": false // Only Cloud Functions
        }
      }
    },

    "matchmaking": {
      "queues": {
        "$gameMode": {
          "$odId": {
            ".read": "auth != null",
            ".write": "auth.uid == $odId"
          }
        }
      }
    },

    "presence": {
      "$odId": {
        ".read": "auth != null",
        ".write": "auth.uid == $odId"
      }
    }
  }
}
```

---

## Cloud Functions (Required)

### Matchmaking
- `onMatchmakingQueueWrite` - Processes queue, creates lobbies when enough players

### Game Management
- `onLobbyStatusChange` - Starts game when countdown ends
- `onGameTick` - Updates time, triggers eliminations
- `spawnCoins` - Server-authoritative coin spawning
- `spawnPowerUps` - Server-authoritative power-up spawning

### Score Validation
- `onCoinCollected` - Validates and awards points
- `onGameEnd` - Calculates final scores, updates leaderboards

### Challenges
- `dailyChallengeReset` - Scheduled function, resets daily challenges
- `weeklyChallengeReset` - Scheduled function, resets weekly challenges

### Leaderboards
- `updateLeaderboards` - Updates daily/weekly/all-time rankings
