package com.keren.virtualmoney.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keren.virtualmoney.theme.ThemeManager
import com.keren.virtualmoney.theme.ThemeWithStatus

/** Theme selection screen for changing game appearance. */
@Composable
fun ThemeSelectionScreen(
        themeManager: ThemeManager,
        onBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    val themesWithStatus by themeManager.themesWithStatus.collectAsState()
    val currentTheme by themeManager.currentTheme.collectAsState()

    Box(
            modifier =
                    modifier.fillMaxSize()
                            .background(
                                    Brush.verticalGradient(
                                            colors =
                                                    listOf(
                                                            Color(0xFF1A1A2E),
                                                            Color(0xFF16213E),
                                                            Color(0xFF0F3460)
                                                    )
                                    )
                            )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            // Header
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp).clickable { onBack() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                        text = "CHOOSE THEME",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info text
            Text(
                    text = "Unlock new themes by leveling up!",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Theme Grid
            LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(themesWithStatus) { themeStatus ->
                    ThemeCard(
                            themeStatus = themeStatus,
                            onClick = { themeManager.selectTheme(themeStatus.theme.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(themeStatus: ThemeWithStatus, onClick: () -> Unit) {
    val isSelected = themeStatus.isSelected
    val isUnlocked = themeStatus.isUnlocked

    // Pulsing animation for selected theme
    val infiniteTransition = rememberInfiniteTransition()
    val borderAlpha by
            infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                            )
            )

    Card(
            modifier = Modifier.aspectRatio(1f).clickable(enabled = isUnlocked) { onClick() },
            shape = RoundedCornerShape(16.dp),
            elevation = if (isSelected) 12.dp else 4.dp,
            border =
                    if (isSelected) {
                        BorderStroke(3.dp, Color(0xFF4CAF50).copy(alpha = borderAlpha))
                    } else null
    ) {
        Box {
            // Theme preview background
            Box(
                    modifier =
                            Modifier.fillMaxSize()
                                    .background(
                                            Brush.verticalGradient(
                                                    colors =
                                                            listOf(
                                                                    Color(
                                                                            themeStatus
                                                                                    .theme
                                                                                    .colors
                                                                                    .primary
                                                                    ),
                                                                    Color(
                                                                            themeStatus
                                                                                    .theme
                                                                                    .colors
                                                                                    .secondary
                                                                    )
                                                            )
                                            )
                                    )
            )

            // Pattern overlay (optional decorative element)
            Box(
                    modifier =
                            Modifier.fillMaxSize()
                                    .background(
                                            Brush.radialGradient(
                                                    colors =
                                                            listOf(
                                                                    Color.White.copy(alpha = 0.1f),
                                                                    Color.Transparent
                                                            )
                                            )
                                    )
            )

            // Lock overlay if not unlocked
            AnimatedVisibility(visible = !isUnlocked) {
                Box(
                        modifier =
                                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                ) {
                    Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                text = "Level ${themeStatus.theme.id.unlockLevel}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                                progress = themeStatus.unlockProgress,
                                modifier =
                                        Modifier.width(80.dp)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                color = Color(0xFF4CAF50),
                                backgroundColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // Selected checkmark
            AnimatedVisibility(visible = isSelected, modifier = Modifier.align(Alignment.TopEnd)) {
                Box(
                        modifier =
                                Modifier.padding(8.dp)
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color(0xFF4CAF50)),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Theme name
            Box(
                    modifier =
                            Modifier.align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(
                                            Brush.verticalGradient(
                                                    colors =
                                                            listOf(
                                                                    Color.Transparent,
                                                                    Color.Black.copy(alpha = 0.8f)
                                                            )
                                            )
                                    )
                                    .padding(12.dp),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = themeStatus.theme.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
