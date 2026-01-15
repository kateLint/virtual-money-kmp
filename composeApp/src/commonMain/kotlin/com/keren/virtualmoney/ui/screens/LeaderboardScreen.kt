package com.keren.virtualmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
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
import com.keren.virtualmoney.backend.ServiceLocator

/**
 * Leaderboard screen showing top players.
 */
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Daily", "Weekly", "All Time")

    // Placeholder leaderboard data
    val leaderboardEntries = remember {
        listOf(
            LeaderboardEntryData("GoldHunter99", 15420, 45),
            LeaderboardEntryData("CoinMaster", 14890, 42),
            LeaderboardEntryData("ARChampion", 13750, 38),
            LeaderboardEntryData("SpeedRunner", 12340, 35),
            LeaderboardEntryData("ComboKing", 11890, 33),
            LeaderboardEntryData("ProCollector", 10540, 30),
            LeaderboardEntryData("NightOwl", 9870, 28),
            LeaderboardEntryData("DayPlayer", 8920, 25),
            LeaderboardEntryData("CasualGamer", 7650, 22),
            LeaderboardEntryData("NewPlayer", 5430, 18)
        )
    }

    val profile by ServiceLocator.progressionManager.profile.collectAsState()
    val stats by ServiceLocator.progressionManager.stats.collectAsState()

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
                    text = "LEADERBOARD",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tab selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(4.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedTab == index) Color(0xFFFFD700)
                                else Color.Transparent
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (selectedTab == index) Color.Black else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Top 3 podium
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd place
                if (leaderboardEntries.size > 1) {
                    PodiumPlace(
                        rank = 2,
                        name = leaderboardEntries[1].name,
                        score = leaderboardEntries[1].score,
                        level = leaderboardEntries[1].level,
                        height = 80.dp
                    )
                }

                // 1st place
                if (leaderboardEntries.isNotEmpty()) {
                    PodiumPlace(
                        rank = 1,
                        name = leaderboardEntries[0].name,
                        score = leaderboardEntries[0].score,
                        level = leaderboardEntries[0].level,
                        height = 100.dp
                    )
                }

                // 3rd place
                if (leaderboardEntries.size > 2) {
                    PodiumPlace(
                        rank = 3,
                        name = leaderboardEntries[2].name,
                        score = leaderboardEntries[2].score,
                        level = leaderboardEntries[2].level,
                        height = 60.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Rest of leaderboard
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(leaderboardEntries.drop(3)) { index, entry ->
                    LeaderboardRow(
                        rank = index + 4,
                        name = entry.name,
                        score = entry.score,
                        level = entry.level,
                        isCurrentUser = entry.name == profile.displayName
                    )
                }

                // Current user's position if not in top 10
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Position",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LeaderboardRow(
                        rank = 156,
                        name = profile.displayName,
                        score = stats.highScore,
                        level = profile.level,
                        isCurrentUser = true
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumPlace(
    rank: Int,
    name: String,
    score: Int,
    level: Int,
    height: androidx.compose.ui.unit.Dp
) {
    val color = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color.Gray
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name.take(8) + if (name.length > 8) "..." else "",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "$score",
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Podium block
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    name: String,
    score: Int,
    level: Int,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCurrentUser) Color(0xFFFFD700).copy(alpha = 0.1f)
                else Color.White.copy(alpha = 0.05f)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        Text(
            text = "#$rank",
            color = if (isCurrentUser) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )

        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name and level
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Level $level",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }

        // Score
        Text(
            text = "$score",
            color = if (isCurrentUser) Color(0xFFFFD700) else Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class LeaderboardEntryData(
    val name: String,
    val score: Int,
    val level: Int
)
