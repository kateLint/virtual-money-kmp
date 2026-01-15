package com.keren.virtualmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.keren.virtualmoney.backend.ServiceLocator
import com.keren.virtualmoney.progression.Challenge
import com.keren.virtualmoney.progression.ChallengeType

/**
 * Screen showing daily and weekly challenges.
 */
@Composable
fun ChallengesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeChallenges by ServiceLocator.progressionManager.activeChallenges.collectAsState()
    val challengeProgress by ServiceLocator.progressionManager.challengeProgress.collectAsState()

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
                    text = "CHALLENGES",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily challenges section
            Text(
                text = "DAILY CHALLENGES",
                color = Color(0xFFFFD700),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Resets in 8h 32m",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            val dailyChallenges = activeChallenges.filter { it.isDaily }
            if (dailyChallenges.isEmpty()) {
                // Show placeholder daily challenges
                ChallengeCard(
                    title = "Coin Collector",
                    description = "Collect 100 coins",
                    progress = 45,
                    target = 100,
                    reward = "50 XP",
                    isCompleted = false
                )
                Spacer(modifier = Modifier.height(12.dp))
                ChallengeCard(
                    title = "Combo Master",
                    description = "Reach a 10x combo",
                    progress = 7,
                    target = 10,
                    reward = "75 XP",
                    isCompleted = false
                )
                Spacer(modifier = Modifier.height(12.dp))
                ChallengeCard(
                    title = "Perfect Run",
                    description = "Complete a game without hitting penalties",
                    progress = 0,
                    target = 1,
                    reward = "100 XP",
                    isCompleted = false
                )
            } else {
                dailyChallenges.forEach { challenge ->
                    val progress = challengeProgress[challenge.id] ?: 0
                    ChallengeCard(
                        title = challenge.title,
                        description = challenge.description,
                        progress = progress,
                        target = challenge.target,
                        reward = "${challenge.xpReward} XP",
                        isCompleted = progress >= challenge.target
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Weekly challenges section
            Text(
                text = "WEEKLY CHALLENGES",
                color = Color(0xFF9C27B0),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Resets in 4d 12h",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            val weeklyChallenges = activeChallenges.filter { !it.isDaily }
            if (weeklyChallenges.isEmpty()) {
                // Show placeholder weekly challenges
                ChallengeCard(
                    title = "Dedicated Player",
                    description = "Play 20 games this week",
                    progress = 8,
                    target = 20,
                    reward = "200 XP",
                    isCompleted = false,
                    accentColor = Color(0xFF9C27B0)
                )
                Spacer(modifier = Modifier.height(12.dp))
                ChallengeCard(
                    title = "High Scorer",
                    description = "Score 5000 total points",
                    progress = 2340,
                    target = 5000,
                    reward = "300 XP",
                    isCompleted = false,
                    accentColor = Color(0xFF9C27B0)
                )
            } else {
                weeklyChallenges.forEach { challenge ->
                    val progress = challengeProgress[challenge.id] ?: 0
                    ChallengeCard(
                        title = challenge.title,
                        description = challenge.description,
                        progress = progress,
                        target = challenge.target,
                        reward = "${challenge.xpReward} XP",
                        isCompleted = progress >= challenge.target,
                        accentColor = Color(0xFF9C27B0)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    title: String,
    description: String,
    progress: Int,
    target: Int,
    reward: String,
    isCompleted: Boolean,
    accentColor: Color = Color(0xFFFFD700)
) {
    val progressPercent = (progress.toFloat() / target).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCompleted) Color(0xFF4CAF50).copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.1f)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }

                // Reward badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = reward,
                        color = accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPercent)
                        .fillMaxHeight()
                        .background(
                            if (isCompleted) Color(0xFF4CAF50)
                            else accentColor
                        )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$progress / $target",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                if (isCompleted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Completed!",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
