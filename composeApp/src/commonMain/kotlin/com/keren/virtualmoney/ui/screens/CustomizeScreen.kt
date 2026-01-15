package com.keren.virtualmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keren.virtualmoney.theme.*

/**
 * Screen for customizing themes and skins.
 */
@Composable
fun CustomizeScreen(
    themes: List<ThemeWithStatus>,
    skins: List<SkinWithStatus>,
    playerLevel: Int,
    onThemeSelected: (ThemeId) -> Unit,
    onSkinSelected: (CoinSkinId) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBack() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "CUSTOMIZE",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Themes section
            Text(
                text = "THEMES",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose your game background",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(themes) { themeStatus ->
                    ThemeCard(
                        themeStatus = themeStatus,
                        playerLevel = playerLevel,
                        onClick = {
                            if (themeStatus.isUnlocked) {
                                onThemeSelected(themeStatus.theme.id)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Skins section
            Text(
                text = "COIN SKINS",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Customize how your coins look",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(skins) { skinStatus ->
                    SkinCard(
                        skinStatus = skinStatus,
                        onClick = {
                            if (skinStatus.isUnlocked) {
                                onSkinSelected(skinStatus.skin.id)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Preview section
            Text(
                text = "PREVIEW",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preview box
            val selectedTheme = themes.find { it.isSelected }?.theme
            val selectedSkin = skins.find { it.isSelected }?.skin

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(getThemePreviewColor(selectedTheme?.id)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Simulated coin with skin
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(40.dp))
                            .background(getSkinPreviewColor(selectedSkin?.id))
                            .then(
                                if (selectedSkin?.glowColor != null) {
                                    Modifier.border(
                                        width = 3.dp,
                                        color = Color(selectedSkin.glowColor),
                                        shape = RoundedCornerShape(40.dp)
                                    )
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏛️",
                            fontSize = 40.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${selectedTheme?.id?.displayName ?: "Camera"} + ${selectedSkin?.id?.displayName ?: "Classic"}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    themeStatus: ThemeWithStatus,
    playerLevel: Int,
    onClick: () -> Unit
) {
    val theme = themeStatus.theme
    val isUnlocked = themeStatus.isUnlocked
    val isSelected = themeStatus.isSelected

    Box(
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(getThemePreviewColor(theme.id))
            .then(
                if (isSelected) Modifier.border(
                    width = 3.dp,
                    color = Color(0xFFFFD700),
                    shape = RoundedCornerShape(12.dp)
                ) else Modifier
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Theme icon/preview
            Text(
                text = getThemeEmoji(theme.id),
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = theme.id.displayName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (!isUnlocked) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Lvl ${theme.id.unlockLevel}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Lock overlay
        if (!isUnlocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
private fun SkinCard(
    skinStatus: SkinWithStatus,
    onClick: () -> Unit
) {
    val skin = skinStatus.skin
    val isUnlocked = skinStatus.isUnlocked
    val isSelected = skinStatus.isSelected

    Box(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .then(
                if (isSelected) Modifier.border(
                    width = 3.dp,
                    color = Color(0xFFFFD700),
                    shape = RoundedCornerShape(12.dp)
                ) else Modifier
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Skin preview
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(getSkinPreviewColor(skin.id))
                    .then(
                        if (skin.glowColor != null) {
                            Modifier.border(
                                width = 2.dp,
                                color = Color(skin.glowColor),
                                shape = RoundedCornerShape(25.dp)
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🪙", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = skin.id.displayName,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            if (!isUnlocked) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
            } else if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        if (!isUnlocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }
    }
}

private fun getThemeEmoji(themeId: ThemeId): String = when (themeId) {
    ThemeId.CAMERA -> "📷"
    ThemeId.FOREST -> "🌲"
    ThemeId.GALAXY -> "🌌"
    ThemeId.OCEAN -> "🌊"
    ThemeId.NEON_CITY -> "🌃"
}

private fun getThemePreviewColor(themeId: ThemeId?): Color = when (themeId) {
    ThemeId.CAMERA -> Color(0xFF333333)
    ThemeId.FOREST -> Color(0xFF2E7D32)
    ThemeId.GALAXY -> Color(0xFF1A237E)
    ThemeId.OCEAN -> Color(0xFF0277BD)
    ThemeId.NEON_CITY -> Color(0xFF880E4F)
    null -> Color(0xFF333333)
}

private fun getSkinPreviewColor(skinId: CoinSkinId?): Color = when (skinId) {
    CoinSkinId.CLASSIC -> Color(0xFFB8860B)
    CoinSkinId.GOLDEN -> Color(0xFFFFD700)
    CoinSkinId.DIAMOND -> Color(0xFFB9F2FF)
    CoinSkinId.NEON -> Color(0xFF00FF00)
    CoinSkinId.FIRE -> Color(0xFFFF4500)
    CoinSkinId.ICE -> Color(0xFF87CEEB)
    CoinSkinId.HOLOGRAPHIC -> Color(0xFFE040FB)
    CoinSkinId.RAINBOW -> Color(0xFFFF6B6B)
    CoinSkinId.LEGENDARY -> Color(0xFFFFD700)
    null -> Color(0xFFB8860B)
}
