package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.model.PlatformPreset
import com.example.model.SwipeDirection
import com.example.service.CoinHunterAccessibilityService
import com.example.service.FloatingOverlayService
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.RadarGreenBright

@Composable
fun PlatformsScreen(
    preferences: AppPreferences,
    onApplyAndLaunchApp: (PlatformPreset) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val presets = listOf(
        PlatformPreset(
            id = "instagram_reels",
            name = "Instagram Reels",
            packageName = "com.instagram.android",
            defaultIntervalSeconds = 8,
            swipeDurationMs = 350L,
            distancePercent = 65,
            iconDescription = "📸",
            badge = "Popular"
        ),
        PlatformPreset(
            id = "tiktok",
            name = "TikTok",
            packageName = "com.zhiliaoapp.musically",
            defaultIntervalSeconds = 7,
            swipeDurationMs = 300L,
            distancePercent = 70,
            iconDescription = "🎵",
            badge = "Optimized"
        ),
        PlatformPreset(
            id = "youtube_shorts",
            name = "YouTube Shorts",
            packageName = "com.google.android.youtube",
            defaultIntervalSeconds = 10,
            swipeDurationMs = 380L,
            distancePercent = 60,
            iconDescription = "▶️",
            badge = "HD Feeds"
        ),
        PlatformPreset(
            id = "facebook_reels",
            name = "Facebook Reels",
            packageName = "com.facebook.katana",
            defaultIntervalSeconds = 9,
            swipeDurationMs = 350L,
            distancePercent = 65,
            iconDescription = "👤",
            badge = "Meta"
        ),
        PlatformPreset(
            id = "snapchat_spotlight",
            name = "Snapchat Spotlight",
            packageName = "com.snapchat.android",
            defaultIntervalSeconds = 6,
            swipeDurationMs = 280L,
            distancePercent = 75,
            iconDescription = "👻",
            badge = "Fast"
        ),
        PlatformPreset(
            id = "douyin",
            name = "Douyin (抖音)",
            packageName = "com.ss.android.ugc.aweme",
            defaultIntervalSeconds = 7,
            swipeDurationMs = 300L,
            distancePercent = 70,
            iconDescription = "🇨🇳",
            badge = "Special"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(RadarGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = RadarGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "One-Tap Platform Presets",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Instant algorithmic calibration and auto-launch for short-form apps",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Presets List
        presets.forEach { preset ->
            PresetCard(
                preset = preset,
                isCurrentInterval = preferences.intervalSeconds == preset.defaultIntervalSeconds,
                onApply = {
                    preferences.intervalSeconds = preset.defaultIntervalSeconds
                    preferences.swipeDurationMs = preset.swipeDurationMs
                    preferences.swipeDistancePercent = preset.distancePercent
                    preferences.keepAsDefault = false
                    Toast.makeText(context, "${preset.name} preset applied (${preset.defaultIntervalSeconds}s interval)", Toast.LENGTH_SHORT).show()
                },
                onApplyAndLaunch = {
                    preferences.intervalSeconds = preset.defaultIntervalSeconds
                    preferences.swipeDurationMs = preset.swipeDurationMs
                    preferences.swipeDistancePercent = preset.distancePercent
                    preferences.keepAsDefault = false
                    onApplyAndLaunchApp(preset)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PresetCard(
    preset: PlatformPreset,
    isCurrentInterval: Boolean,
    onApply: () -> Unit,
    onApplyAndLaunch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrentInterval) 1.5.dp else 1.dp,
                color = if (isCurrentInterval) RadarGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = preset.iconDescription, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = preset.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${preset.defaultIntervalSeconds}s Interval • ${preset.swipeDurationMs}ms Speed",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PurpleLight.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = preset.badge,
                        color = PurpleLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onApply,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isCurrentInterval) "Applied ✓" else "Apply Preset",
                        fontSize = 12.sp,
                        color = if (isCurrentInterval) RadarGreen else MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = onApplyAndLaunch,
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(containerColor = RadarGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Apply & Launch",
                            color = Color(0xFF003816),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = null,
                            tint = Color(0xFF003816),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
