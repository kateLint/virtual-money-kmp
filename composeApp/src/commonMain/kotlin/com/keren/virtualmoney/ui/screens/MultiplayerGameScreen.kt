package com.keren.virtualmoney.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keren.virtualmoney.backend.MultiplayerPhase
import com.keren.virtualmoney.multiplayer.MultiplayerEvent
import com.keren.virtualmoney.multiplayer.MultiplayerGameState
import com.keren.virtualmoney.multiplayer.MultiplayerPlayer

/**
 * HUD overlay for multiplayer games.
 */
@Composable
fun MultiplayerHUD(
    gameState: MultiplayerGameState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Top bar with rankings
        MultiplayerTopBar(
            timeRemaining = gameState.timeRemaining,
            currentRank = gameState.currentPlayerRank,
            totalPlayers = gameState.players.count { !it.isEliminated },
            phase = gameState.phase,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Leaderboard (side panel)
        CompactLeaderboard(
            players = gameState.players.take(5),
            currentPlayerId = gameState.players.find { it.isCurrentPlayer }?.odId ?: "",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 8.dp)
        )

        // Elimination warning
        AnimatedVisibility(
            visible = gameState.eliminationCountdown != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            EliminationWarning(
                countdown = gameState.eliminationCountdown ?: 0,
                eliminationCount = gameState.nextEliminationCount ?: 0
            )
        }

        // Nearby players radar
        if (gameState.nearbyPlayers.isNotEmpty()) {
            NearbyPlayersIndicator(
                count = gameState.nearbyPlayers.size,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            )
        }
    }
}

@Composable
private fun MultiplayerTopBar(
    timeRemaining: Int,
    currentRank: Int,
    totalPlayers: Int,
    phase: MultiplayerPhase,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank display
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(getRankColor(currentRank, totalPlayers))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#$currentRank",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "/ $totalPlayers",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }

        // Timer
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            val minutes = timeRemaining / 60
            val seconds = timeRemaining % 60
            Text(
                text = "%d:%02d".format(minutes, seconds),
                color = if (timeRemaining <= 10) Color.Red else Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Phase indicator
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(getPhaseColor(phase))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = getPhaseName(phase),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CompactLeaderboard(
    players: List<MultiplayerPlayer>,
    currentPlayerId: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(8.dp)
    ) {
        players.forEachIndexed { index, player ->
            LeaderboardRow(
                rank = index + 1,
                player = player,
                isCurrentPlayer = player.odId == currentPlayerId
            )
            if (index < players.lastIndex) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    player: MultiplayerPlayer,
    isCurrentPlayer: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCurrentPlayer) {
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFD700).copy(alpha = 0.3f))
                        .padding(4.dp)
                } else {
                    Modifier.padding(4.dp)
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank badge
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(getRankBadgeColor(rank)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Name
        Text(
            text = if (isCurrentPlayer) "You" else player.displayName.take(8),
            color = if (isCurrentPlayer) Color(0xFFFFD700) else Color.White,
            fontSize = 11.sp,
            fontWeight = if (isCurrentPlayer) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        // Score
        Text(
            text = "${player.score}",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun EliminationWarning(
    countdown: Int,
    eliminationCount: Int
) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .padding(bottom = 100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Red.copy(alpha = alpha * 0.9f))
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "ELIMINATION IN ${countdown}s",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bottom $eliminationCount players will be eliminated!",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun NearbyPlayersIndicator(
    count: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    0f to Color.Red.copy(alpha = 0.8f),
                    1f to Color.Red.copy(alpha = 0.2f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "$count nearby",
                color = Color.White,
                fontSize = 9.sp
            )
        }
    }
}

/**
 * Event toast for multiplayer events.
 */
@Composable
fun MultiplayerEventToast(
    event: MultiplayerEvent?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = event != null,
        enter = slideInVertically() + fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        event?.let { e ->
            val (text, color) = when (e) {
                is MultiplayerEvent.PlayerEliminated ->
                    "${e.player.displayName} eliminated!" to Color.Red
                is MultiplayerEvent.RankChanged -> {
                    if (e.newRank < e.oldRank) {
                        "Moved up to #${e.newRank}!" to Color.Green
                    } else {
                        "Dropped to #${e.newRank}" to Color(0xFFFF9800)
                    }
                }
                is MultiplayerEvent.GameEnded -> {
                    if (e.isWinner) "VICTORY!" to Color(0xFFFFD700)
                    else "Finished #${e.finalRank}" to Color.White
                }
                is MultiplayerEvent.PlayerNearby ->
                    "Players nearby!" to Color.Red
                else -> "" to Color.White
            }

            if (text.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = text,
                        color = color,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun getRankColor(rank: Int, total: Int): Color {
    val percentage = rank.toFloat() / total
    return when {
        rank == 1 -> Color(0xFFFFD700)  // Gold
        rank == 2 -> Color(0xFFC0C0C0)  // Silver
        rank == 3 -> Color(0xFFCD7F32)  // Bronze
        percentage <= 0.1f -> Color(0xFF4CAF50)  // Top 10% - Green
        percentage <= 0.25f -> Color(0xFF8BC34A) // Top 25% - Light green
        percentage <= 0.5f -> Color(0xFFFFEB3B)  // Top 50% - Yellow
        percentage <= 0.75f -> Color(0xFFFF9800) // Bottom half - Orange
        else -> Color(0xFFF44336)  // Bottom 25% - Red
    }
}

private fun getRankBadgeColor(rank: Int): Color = when (rank) {
    1 -> Color(0xFFFFD700)
    2 -> Color(0xFFC0C0C0)
    3 -> Color(0xFFCD7F32)
    else -> Color(0xFF607D8B)
}

private fun getPhaseColor(phase: MultiplayerPhase): Color = when (phase) {
    MultiplayerPhase.COUNTDOWN -> Color(0xFF2196F3)
    MultiplayerPhase.PLAYING -> Color(0xFF4CAF50)
    MultiplayerPhase.ELIMINATION_WARNING -> Color(0xFFFF9800)
    MultiplayerPhase.ELIMINATION -> Color(0xFFF44336)
    MultiplayerPhase.FINAL_SHOWDOWN -> Color(0xFF9C27B0)
    MultiplayerPhase.FINISHED -> Color(0xFF607D8B)
}

private fun getPhaseName(phase: MultiplayerPhase): String = when (phase) {
    MultiplayerPhase.COUNTDOWN -> "STARTING"
    MultiplayerPhase.PLAYING -> "PLAYING"
    MultiplayerPhase.ELIMINATION_WARNING -> "WARNING"
    MultiplayerPhase.ELIMINATION -> "ELIMINATION"
    MultiplayerPhase.FINAL_SHOWDOWN -> "FINAL"
    MultiplayerPhase.FINISHED -> "FINISHED"
}

private fun String.format(vararg args: Any): String {
    var result = this
    args.forEachIndexed { index, arg ->
        result = result.replaceFirst("%d", arg.toString())
            .replaceFirst("%02d", (arg as? Int)?.toString()?.padStart(2, '0') ?: arg.toString())
    }
    return result
}
