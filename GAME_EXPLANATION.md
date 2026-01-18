# 🎮 Virtual Money: Coin Hunter AR

## 🌟 What is this game?
**Virtual Money** is an **Augmented Reality (AR) Scavenger Hunt** game. It turns your real-world surroundings into a playground filled with virtual coins that you must find and collect using your phone.

## 🕹️ How to Play

1. **Start a Game:** Choose "Single Player" or "Multiplayer" from the main menu.
2. **Look Around:** Hold your phone up in front of you. The screen shows your real camera view.
3. **Find the Money:** Virtual coins (Shekels ₪) are floating **all around you in 360°**.
   - You must physically **turn your body** and **move your phone** left, right, up (ceiling), and down (floor) to search for them.
   - They are "world-locked", meaning they stay in fixed positions in your room as you move.
4. **Collect:** Tap the coins on your screen to collect them and earn points.
   - 🪙 **Gold Coins (Hapoalim):** Collect these for points!
   - 🚫 **Penalty Coins:** Avoid these! They decrease your score.
5. **Score Big:** Try to collect as many as possible before the time runs out!

## 🕶️ AR Technology
The game uses your phone's sensors (Gyroscope, Accelerometer) and Camera to track your movement. 
- When you turn left, the camera view turns left, revealing coins that were "behind" you.
- It creates a deeply immersive experience where you are "inside" the game world.

## 🧩 Features
- **Global Multiplayer:** Play with friends and race to collect the same coins in real-time.
- **Power-Ups:** Find shields, magnets, and time-freezers.
- **Themes:** Unlock different backgrounds (Forest, Space, Ocean) if you prefer 2D mode, or stick to "Camera" for the full AR experience.

Ready to become the ultimate Coin Hunter? Press **START** and get moving! 🏃💨

---

## 🪙 How Coins Work (Technical)

The coin system is the core of the experience. Here's what's happening under the hood:

### 1. Spawning & Positioning
- **3D World Space:** Coins are spawned in a **spherical coordinate system** around you (distance: 3-5 meters).
- **Random Distribution:** They appear at varying heights (floor, eye level, ceiling) and random angles (360°), forcing you to physically look around.
- **Anchoring:** Each coin is "anchored" to a specific coordinate in the real world using **ARCore (Android)** or **ARKit (iOS)**. This ensures that even if you walk away and come back, the coin stays exactly where it was.

### 2. Drift Correction
Phone sensors aren't perfect. As you move, the digital world might "drift" from the real world.
- The app actively monitors the **native AR anchors** from the device's AR system.
- **Real-time updates:** The position of every coin is corrected 60 times a second to match the anchor's latest tracked position, eliminating "floating" or sliding coins.

### 3. Coin Types
- **Hapoalim Coin (Bank Hapoalim):** The primary target. Adds points and increases your combo multiplier.
- **Penalty Coins (Leumi, Mizrahi, Discount):** "Bad" coins. Collecting them reduces your score, breaks your combo, and in Survival Mode, costs you a life.
- **Spawn Logic:** The game intelligently manages density. If you clear an area, new coins might respawn behind you to keep the game flowing.

### 4. Architecture (KMP)
The game is built with **Kotlin Multiplatform (KMP)**.
- **Common Logic:** The "Brain" (CoinManager) lives in shared code, deciding *when* and *where* to spawn coins.
- **Native Power:** The "Body" uses platform-specific code (Android/iOS) to talk directly to the hardware for high-performance tracking.
