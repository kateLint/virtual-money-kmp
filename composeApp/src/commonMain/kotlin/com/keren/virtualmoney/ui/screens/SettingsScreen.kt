package com.keren.virtualmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Settings screen for game options. */
@Composable
fun SettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    var soundEnabled by remember { mutableStateOf(true) }
    var hapticEnabled by remember { mutableStateOf(true) }
    var showFPS by remember { mutableStateOf(false) }
    var highQualityEffects by remember { mutableStateOf(true) }

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
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState()) // FIXED: Added scrolling
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
                        modifier = Modifier.size(32.dp).clickable { onBack() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                        text = "SETTINGS",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Audio section
            SettingsSection(title = "AUDIO & HAPTICS") {
                SettingsToggleRow(
                        icon = Icons.Default.VolumeUp,
                        title = "Sound Effects",
                        description = "Play sound effects during gameplay",
                        isEnabled = soundEnabled,
                        onToggle = { soundEnabled = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsToggleRow(
                        icon = Icons.Default.Vibration,
                        title = "Haptic Feedback",
                        description = "Vibrate on coin collection",
                        isEnabled = hapticEnabled,
                        onToggle = { hapticEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Graphics section
            SettingsSection(title = "GRAPHICS") {
                SettingsToggleRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "High Quality Effects",
                        description = "Enable particle effects and animations",
                        isEnabled = highQualityEffects,
                        onToggle = { highQualityEffects = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsToggleRow(
                        icon = Icons.Default.Speed,
                        title = "Show FPS",
                        description = "Display frames per second counter",
                        isEnabled = showFPS,
                        onToggle = { showFPS = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account section
            SettingsSection(title = "ACCOUNT") {
                SettingsActionRow(
                        icon = Icons.Default.Person,
                        title = "Change Username",
                        description = "Update your display name",
                        onClick = { /* Show dialog */}
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsActionRow(
                        icon = Icons.Default.CloudSync,
                        title = "Sync Data",
                        description = "Sync progress to cloud",
                        onClick = { /* Trigger sync */}
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsActionRow(
                        icon = Icons.Default.Login,
                        title = "Sign In with Google",
                        description = "Save progress across devices",
                        onClick = {
                            // ServiceLocator.authManager.signInWithGoogle()
                        }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About section
            SettingsSection(title = "ABOUT") {
                SettingsInfoRow(icon = Icons.Default.Info, title = "Version", value = "2.0.0")

                Spacer(modifier = Modifier.height(8.dp))

                SettingsActionRow(
                        icon = Icons.Default.Star,
                        title = "Rate App",
                        description = "Love the game? Leave a review!",
                        onClick = { /* Open store */}
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsActionRow(
                        icon = Icons.Default.Policy,
                        title = "Privacy Policy",
                        description = "View our privacy policy",
                        onClick = { /* Open browser */}
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Danger zone
            SettingsActionRow(
                    icon = Icons.Default.DeleteForever,
                    title = "Reset Progress",
                    description = "Delete all game data (cannot be undone)",
                    onClick = { /* Show confirmation */},
                    isDanger = true
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
                text = title,
                color = Color(0xFFFFD700),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(12.dp)
        ) { Column { content() } }
    }
}

@Composable
private fun SettingsToggleRow(
        icon: ImageVector,
        title: String,
        description: String,
        isEnabled: Boolean,
        onToggle: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
            )
            Text(text = description, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }

        Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors =
                        SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFD700),
                                checkedTrackColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                        )
        )
    }
}

@Composable
private fun SettingsActionRow(
        icon: ImageVector,
        title: String,
        description: String,
        onClick: () -> Unit,
        isDanger: Boolean = false
) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onClick() }
                            .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDanger) Color(0xFFE53935) else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = title,
                    color = if (isDanger) Color(0xFFE53935) else Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
            )
            Text(text = description, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }

        Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, title: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
        )

        Text(text = value, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
    }
}
