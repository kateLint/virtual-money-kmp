package com.keren.virtualmoney.theme

/** Available game themes (backgrounds). */
enum class ThemeId(val displayName: String, val description: String, val unlockLevel: Int) {
    CAMERA("Camera", "Real AR camera view", 0),
    FOREST("Forest", "Peaceful forest scene", 0),
    GALAXY("Galaxy", "Deep space adventure", 0),
    OCEAN("Ocean", "Underwater treasure hunt", 0),
    NEON_CITY("Neon City", "Cyberpunk cityscape", 0)
}

/** Types of background rendering. */
enum class BackgroundType {
    CAMERA, // Live AR camera feed
    ANIMATED, // Animated scene (particles, etc.)
    STATIC // Static image background
}

/** Particle effect types for themes. */
enum class ParticleType {
    NONE,
    LEAVES, // Forest theme
    STARS, // Galaxy theme
    BUBBLES, // Ocean theme
    NEON_SPARKS // Neon theme
}

/** Complete theme configuration. */
data class GameTheme(
        val id: ThemeId,
        val backgroundType: BackgroundType,
        val backgroundResource: String?,
        val particleType: ParticleType,
        val ambientColor: Long, // ARGB color for tinting
        val coinGlow: Boolean
) {
    companion object {
        val CAMERA =
                GameTheme(
                        id = ThemeId.CAMERA,
                        backgroundType = BackgroundType.CAMERA,
                        backgroundResource = null,
                        particleType = ParticleType.NONE,
                        ambientColor = 0x00000000,
                        coinGlow = false
                )

        val FOREST =
                GameTheme(
                        id = ThemeId.FOREST,
                        backgroundType = BackgroundType.ANIMATED,
                        backgroundResource = "bg_forest",
                        particleType = ParticleType.LEAVES,
                        ambientColor = 0x2000FF00, // Light green tint
                        coinGlow = true
                )

        val GALAXY =
                GameTheme(
                        id = ThemeId.GALAXY,
                        backgroundType = BackgroundType.ANIMATED,
                        backgroundResource = "bg_galaxy",
                        particleType = ParticleType.STARS,
                        ambientColor = 0x200000FF, // Light blue tint
                        coinGlow = true
                )

        val OCEAN =
                GameTheme(
                        id = ThemeId.OCEAN,
                        backgroundType = BackgroundType.ANIMATED,
                        backgroundResource = "bg_ocean",
                        particleType = ParticleType.BUBBLES,
                        ambientColor = 0x2000FFFF, // Cyan tint
                        coinGlow = true
                )

        val NEON_CITY =
                GameTheme(
                        id = ThemeId.NEON_CITY,
                        backgroundType = BackgroundType.ANIMATED,
                        backgroundResource = "bg_neon",
                        particleType = ParticleType.NEON_SPARKS,
                        ambientColor = 0x20FF00FF, // Magenta tint
                        coinGlow = true
                )

        fun fromId(id: ThemeId): GameTheme =
                when (id) {
                    ThemeId.CAMERA -> CAMERA
                    ThemeId.FOREST -> FOREST
                    ThemeId.GALAXY -> GALAXY
                    ThemeId.OCEAN -> OCEAN
                    ThemeId.NEON_CITY -> NEON_CITY
                }

        fun all(): List<GameTheme> = listOf(CAMERA, FOREST, GALAXY, OCEAN, NEON_CITY)
    }
}
