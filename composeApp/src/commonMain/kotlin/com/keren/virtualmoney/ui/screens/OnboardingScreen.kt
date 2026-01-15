package com.keren.virtualmoney.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class OnboardingPage(
    val title: String,
    val description: String,
    val illustration: @Composable () -> Unit
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = remember {
        listOf(
            OnboardingPage(
                title = "Welcome to VirtualMoney",
                description = "Collect coins in augmented reality! Point your camera around and tap coins floating in the air.",
                illustration = { WelcomeIllustration() }
            ),
            OnboardingPage(
                title = "Collect Good Coins",
                description = "Tap the red Bank Hapoalim coins to earn points. Avoid other bank coins - they're penalties!",
                illustration = { CoinsIllustration() }
            ),
            OnboardingPage(
                title = "Build Combos",
                description = "Collect coins quickly to build combos. Higher combos mean bigger score multipliers!",
                illustration = { ComboIllustration() }
            ),
            OnboardingPage(
                title = "Grab Power-ups",
                description = "Collect power-ups for special abilities: Magnet attracts coins, Shield protects you, and more!",
                illustration = { PowerUpsIllustration() }
            ),
            OnboardingPage(
                title = "Compete Globally",
                description = "Challenge players worldwide in multiplayer modes. Climb the leaderboards and unlock rewards!",
                illustration = { MultiplayerIllustration() }
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

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
            modifier = Modifier.fillMaxSize()
        ) {
            // Skip button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (pagerState.currentPage < pages.lastIndex) {
                    Text(
                        text = "Skip",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 16.sp,
                        modifier = Modifier.clickable { onComplete() }
                    )
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            // Bottom section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(pages.size) { index ->
                        PageIndicator(
                            isSelected = pagerState.currentPage == index
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Next/Complete button
                val isLastPage = pagerState.currentPage == pages.lastIndex

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            if (isLastPage) {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF2196F3), Color(0xFF1976D2))
                                )
                            }
                        )
                        .clickable {
                            if (isLastPage) {
                                onComplete()
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isLastPage) "Get Started" else "Next",
                            color = if (isLastPage) Color.Black else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (isLastPage) Icons.Default.Check else Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = if (isLastPage) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration
        Box(
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            page.illustration()
        }

        // Title
        Text(
            text = page.title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun PageIndicator(isSelected: Boolean) {
    val width by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 8.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = Modifier
            .height(8.dp)
            .width(width)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isSelected) Color(0xFFFFD700)
                else Color.White.copy(alpha = 0.3f)
            )
    )
}

// Illustrations
@Composable
private fun WelcomeIllustration() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 3

        // Phone outline
        drawRoundRect(
            color = Color.White.copy(alpha = 0.2f),
            topLeft = Offset(center.x - 60f, center.y - 100f),
            size = androidx.compose.ui.geometry.Size(120f, 200f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
        )

        // Floating coins around
        for (i in 0 until 5) {
            val angle = (rotation + i * 72) * PI / 180
            val coinX = center.x + cos(angle).toFloat() * radius
            val coinY = center.y + sin(angle).toFloat() * radius * 0.6f

            drawCircle(
                color = Color(0xFFFFD700),
                radius = 20f,
                center = Offset(coinX, coinY)
            )
            drawCircle(
                color = Color(0xFFB8860B),
                radius = 15f,
                center = Offset(coinX, coinY)
            )
        }
    }
}

@Composable
private fun CoinsIllustration() {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)

        // Good coin (center, larger, pulsing)
        drawCircle(
            color = Color(0xFFE53935),
            radius = 50f * pulse,
            center = center
        )
        drawCircle(
            color = Color(0xFFB71C1C),
            radius = 40f * pulse,
            center = center
        )

        // Checkmark on good coin
        val checkPath = Path().apply {
            moveTo(center.x - 15f, center.y)
            lineTo(center.x - 5f, center.y + 12f)
            lineTo(center.x + 18f, center.y - 12f)
        }
        drawPath(checkPath, Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))

        // Bad coins (smaller, around)
        val badCoins = listOf(
            Offset(center.x - 90f, center.y - 40f) to Color(0xFF1565C0),
            Offset(center.x + 90f, center.y - 30f) to Color(0xFF2E7D32),
            Offset(center.x + 70f, center.y + 70f) to Color(0xFFFF6F00)
        )

        badCoins.forEach { (offset, color) ->
            drawCircle(color = color, radius = 30f, center = offset)
            // X mark
            drawLine(Color.White, Offset(offset.x - 10f, offset.y - 10f), Offset(offset.x + 10f, offset.y + 10f), strokeWidth = 3f)
            drawLine(Color.White, Offset(offset.x + 10f, offset.y - 10f), Offset(offset.x - 10f, offset.y + 10f), strokeWidth = 3f)
        }
    }
}

@Composable
private fun ComboIllustration() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "10x",
            color = Color(0xFFFFD700),
            fontSize = (48 * scale).sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "COMBO!",
            color = Color(0xFFFF8C00),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "2.0x Multiplier",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 18.sp
        )
    }
}

@Composable
private fun PowerUpsIllustration() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 3

        val powerUps = listOf(
            Color(0xFF2196F3) to "M",  // Magnet
            Color(0xFFFFD700) to "2x", // Multiplier
            Color(0xFF4CAF50) to "S",  // Shield
            Color(0xFF00BCD4) to "F",  // Freeze
            Color(0xFF9C27B0) to "I"   // Invisibility
        )

        powerUps.forEachIndexed { index, (color, _) ->
            val angle = (rotation + index * 72) * PI / 180
            val x = center.x + cos(angle).toFloat() * radius
            val y = center.y + sin(angle).toFloat() * radius * 0.7f

            // Glow
            drawCircle(
                brush = Brush.radialGradient(
                    0f to color.copy(alpha = 0.5f),
                    1f to Color.Transparent,
                    center = Offset(x, y)
                ),
                radius = 45f,
                center = Offset(x, y)
            )

            // Orb
            drawCircle(
                brush = Brush.radialGradient(
                    0f to color.copy(alpha = 1f),
                    1f to color.copy(alpha = 0.6f),
                    center = Offset(x - 5f, y - 5f)
                ),
                radius = 25f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun MultiplayerIllustration() {
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)

        // Globe
        drawCircle(
            brush = Brush.radialGradient(
                0f to Color(0xFF2196F3),
                1f to Color(0xFF0D47A1),
                center = center
            ),
            radius = 80f,
            center = center
        )

        // Globe lines
        drawCircle(
            color = Color.White.copy(alpha = 0.2f),
            radius = 80f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
        )

        // Player icons around
        val players = listOf(
            Offset(center.x - 100f, center.y - 60f + offset),
            Offset(center.x + 100f, center.y - 40f - offset),
            Offset(center.x - 80f, center.y + 80f - offset),
            Offset(center.x + 90f, center.y + 70f + offset)
        )

        players.forEachIndexed { index, pos ->
            val colors = listOf(Color(0xFFFFD700), Color(0xFFC0C0C0), Color(0xFFCD7F32), Color(0xFF607D8B))
            drawCircle(colors[index], 20f, pos)
            // Connection line to globe
            drawLine(
                color = colors[index].copy(alpha = 0.3f),
                start = pos,
                end = center,
                strokeWidth = 1f
            )
        }

        // Trophy in center
        drawCircle(Color(0xFFFFD700), 25f, center)
    }
}
