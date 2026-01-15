package com.keren.virtualmoney.theme

/**
 * Available coin skins.
 */
enum class CoinSkinId(
    val displayName: String,
    val description: String
) {
    CLASSIC("Classic", "Original bank logos"),
    GOLDEN("Golden", "Shiny gold coins"),
    DIAMOND("Diamond", "Crystal clear sparkle"),
    NEON("Neon", "Glowing neon outline"),
    FIRE("Fire", "Flames around coin"),
    ICE("Ice", "Frozen crystal effect"),
    HOLOGRAPHIC("Holographic", "Rainbow shift effect"),
    RAINBOW("Rainbow", "Color cycling"),
    LEGENDARY("Legendary", "Ultimate animated skin")
}

/**
 * Unlock requirements for skins.
 */
sealed class SkinUnlockRequirement {
    /** Unlocked by default */
    data object Default : SkinUnlockRequirement()

    /** Unlocked at a certain level */
    data class Level(val minLevel: Int) : SkinUnlockRequirement()

    /** Unlocked by completing an achievement */
    data class Achievement(val achievementId: String, val description: String) : SkinUnlockRequirement()

    /** Unlocked by completing a challenge */
    data class Challenge(val challengeId: String) : SkinUnlockRequirement()
}

/**
 * Visual effects for coin skins.
 */
enum class CoinEffect {
    NONE,
    GLOW,
    SPARKLE,
    PULSE,
    RAINBOW_SHIFT,
    FLAME,
    FROST,
    HOLOGRAM
}

/**
 * Complete coin skin configuration.
 */
data class CoinSkin(
    val id: CoinSkinId,
    val unlockRequirement: SkinUnlockRequirement,
    val overlayResource: String?, // Overlay image to apply on top of coin
    val glowColor: Long?, // ARGB glow color
    val effect: CoinEffect,
    val animationSpeed: Float // Animation speed multiplier
) {
    companion object {
        val CLASSIC = CoinSkin(
            id = CoinSkinId.CLASSIC,
            unlockRequirement = SkinUnlockRequirement.Default,
            overlayResource = null,
            glowColor = null,
            effect = CoinEffect.NONE,
            animationSpeed = 1.0f
        )

        val GOLDEN = CoinSkin(
            id = CoinSkinId.GOLDEN,
            unlockRequirement = SkinUnlockRequirement.Level(5),
            overlayResource = "skin_golden_overlay",
            glowColor = 0xFFFFD700, // Gold
            effect = CoinEffect.GLOW,
            animationSpeed = 1.0f
        )

        val DIAMOND = CoinSkin(
            id = CoinSkinId.DIAMOND,
            unlockRequirement = SkinUnlockRequirement.Level(15),
            overlayResource = "skin_diamond_overlay",
            glowColor = 0xFFB9F2FF, // Light cyan
            effect = CoinEffect.SPARKLE,
            animationSpeed = 1.2f
        )

        val NEON = CoinSkin(
            id = CoinSkinId.NEON,
            unlockRequirement = SkinUnlockRequirement.Level(25),
            overlayResource = "skin_neon_overlay",
            glowColor = 0xFF00FF00, // Bright green
            effect = CoinEffect.PULSE,
            animationSpeed = 1.5f
        )

        val FIRE = CoinSkin(
            id = CoinSkinId.FIRE,
            unlockRequirement = SkinUnlockRequirement.Achievement(
                "perfect_5",
                "Complete 5 perfect runs"
            ),
            overlayResource = "skin_fire_overlay",
            glowColor = 0xFFFF4500, // Orange red
            effect = CoinEffect.FLAME,
            animationSpeed = 2.0f
        )

        val ICE = CoinSkin(
            id = CoinSkinId.ICE,
            unlockRequirement = SkinUnlockRequirement.Achievement(
                "freeze_50",
                "Use Freeze power-up 50 times"
            ),
            overlayResource = "skin_ice_overlay",
            glowColor = 0xFF87CEEB, // Sky blue
            effect = CoinEffect.FROST,
            animationSpeed = 0.8f
        )

        val HOLOGRAPHIC = CoinSkin(
            id = CoinSkinId.HOLOGRAPHIC,
            unlockRequirement = SkinUnlockRequirement.Achievement(
                "mp_wins_10",
                "Win 10 multiplayer games"
            ),
            overlayResource = "skin_holo_overlay",
            glowColor = null,
            effect = CoinEffect.HOLOGRAM,
            animationSpeed = 1.0f
        )

        val RAINBOW = CoinSkin(
            id = CoinSkinId.RAINBOW,
            unlockRequirement = SkinUnlockRequirement.Achievement(
                "coins_1000",
                "Collect 1000 coins total"
            ),
            overlayResource = "skin_rainbow_overlay",
            glowColor = null,
            effect = CoinEffect.RAINBOW_SHIFT,
            animationSpeed = 1.0f
        )

        val LEGENDARY = CoinSkin(
            id = CoinSkinId.LEGENDARY,
            unlockRequirement = SkinUnlockRequirement.Achievement(
                "leaderboard_1",
                "Reach #1 on any leaderboard"
            ),
            overlayResource = "skin_legendary_overlay",
            glowColor = 0xFFFFD700, // Gold
            effect = CoinEffect.HOLOGRAM,
            animationSpeed = 1.5f
        )

        fun fromId(id: CoinSkinId): CoinSkin = when (id) {
            CoinSkinId.CLASSIC -> CLASSIC
            CoinSkinId.GOLDEN -> GOLDEN
            CoinSkinId.DIAMOND -> DIAMOND
            CoinSkinId.NEON -> NEON
            CoinSkinId.FIRE -> FIRE
            CoinSkinId.ICE -> ICE
            CoinSkinId.HOLOGRAPHIC -> HOLOGRAPHIC
            CoinSkinId.RAINBOW -> RAINBOW
            CoinSkinId.LEGENDARY -> LEGENDARY
        }

        fun all(): List<CoinSkin> = listOf(
            CLASSIC, GOLDEN, DIAMOND, NEON, FIRE, ICE, HOLOGRAPHIC, RAINBOW, LEGENDARY
        )
    }
}
