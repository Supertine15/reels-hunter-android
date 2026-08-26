package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.model.SwipeDirection
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.RadarGreenBright
import com.example.ui.theme.RoseAlert
import com.example.util.ChineseRomHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: AppPreferences,
    onSettingsChanged: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var keepDefault by remember { mutableStateOf(preferences.keepAsDefault) }
    var interval by remember { mutableIntStateOf(preferences.intervalSeconds) }
    var durationMs by remember { mutableLongStateOf(preferences.swipeDurationMs) }
    var distancePercent by remember { mutableIntStateOf(preferences.swipeDistancePercent) }
    var antiBot by remember { mutableStateOf(preferences.antiBotEnabled) }
    var direction by remember { mutableStateOf(preferences.swipeDirection) }
    var autoStopMinutes by remember { mutableIntStateOf(preferences.autoStopMinutes) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner (matching Screenshot 3 & 5)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(22.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(PurplePrimary, Color(0xFF651FFF))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "RH",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ReelsHunter",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gesture & Engine Optimization",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = {
                        ChineseRomHelper.openAutostartSettings(context)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Help Guide",
                        tint = PurpleLight
                    )
                }
            }
        }

        // Section 1: Direction Selection Cards (matching Screenshot 3)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = RadarGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Gesture Direction",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Direction used for short-form video auto-swiping",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Up
                    DirectionCard(
                        title = "Up (Next)",
                        subtitle = "Bottom → Top",
                        iconText = "⬆️",
                        isSelected = direction == SwipeDirection.UP,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            direction = SwipeDirection.UP
                            preferences.swipeDirection = SwipeDirection.UP
                            onSettingsChanged()
                        }
                    )

                    // Down
                    DirectionCard(
                        title = "Down (Prev)",
                        subtitle = "Top → Bottom",
                        iconText = "⬇️",
                        isSelected = direction == SwipeDirection.DOWN,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            direction = SwipeDirection.DOWN
                            preferences.swipeDirection = SwipeDirection.DOWN
                            onSettingsChanged()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left
                    DirectionCard(
                        title = "Left",
                        subtitle = "Right → Left",
                        iconText = "⬅️",
                        isSelected = direction == SwipeDirection.LEFT,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            direction = SwipeDirection.LEFT
                            preferences.swipeDirection = SwipeDirection.LEFT
                            onSettingsChanged()
                        }
                    )

                    // Right
                    DirectionCard(
                        title = "Right",
                        subtitle = "Left → Right",
                        iconText = "➡️",
                        isSelected = direction == SwipeDirection.RIGHT,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            direction = SwipeDirection.RIGHT
                            preferences.swipeDirection = SwipeDirection.RIGHT
                            onSettingsChanged()
                        }
                    )
                }
            }
        }

        // Section 2: Universal Keep Default Setting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    color = if (keepDefault) RadarGreen.copy(alpha = 0.6f) else PurpleLight.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(20.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (keepDefault) RadarGreen.copy(alpha = 0.2f) else PurpleLight.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (keepDefault) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (keepDefault) RadarGreen else PurpleLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Keep as Default Settings",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (keepDefault) "Settings persist across sessions" else "Temporary session values",
                                fontSize = 11.sp,
                                color = if (keepDefault) RadarGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = keepDefault,
                        onCheckedChange = {
                            keepDefault = it
                            preferences.keepAsDefault = it
                            onSettingsChanged()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF003816),
                            checkedTrackColor = RadarGreen
                        )
                    )
                }
            }
        }

        // Section 3: Time Interval & Swipe Speed Calibration
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Slider 1: Scroll Interval
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = RadarGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scroll Interval",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "$interval seconds",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = RadarGreen
                    )
                }

                Slider(
                    value = interval.toFloat(),
                    onValueChange = {
                        interval = it.toInt()
                        preferences.intervalSeconds = it.toInt()
                        onSettingsChanged()
                    },
                    valueRange = 1f..60f,
                    steps = 58,
                    colors = SliderDefaults.colors(
                        thumbColor = RadarGreen,
                        activeTrackColor = RadarGreen,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Slider 2: Swipe Duration (Speed)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = PurpleLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Swipe Duration (Speed)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "$durationMs ms",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = PurpleLight
                    )
                }

                Slider(
                    value = durationMs.toFloat(),
                    onValueChange = {
                        durationMs = it.toLong()
                        preferences.swipeDurationMs = it.toLong()
                        onSettingsChanged()
                    },
                    valueRange = 150f..800f,
                    steps = 12,
                    colors = SliderDefaults.colors(
                        thumbColor = PurplePrimary,
                        activeTrackColor = PurplePrimary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Slider 3: Swipe Distance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Swipe Distance",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "$distancePercent% of Screen",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = CyanAccent
                    )
                }

                Slider(
                    value = distancePercent.toFloat(),
                    onValueChange = {
                        distancePercent = it.toInt()
                        preferences.swipeDistancePercent = it.toInt()
                        onSettingsChanged()
                    },
                    valueRange = 30f..90f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = CyanAccent,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // Section 4: Anti-Bot & Natural Trajectory Randomization
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(RadarGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = RadarGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Anti-Bot Trajectory Variation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Adds natural micro-jitter (±15%) to avoid detection",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = antiBot,
                        onCheckedChange = {
                            antiBot = it
                            preferences.antiBotEnabled = it
                            onSettingsChanged()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF003816),
                            checkedTrackColor = RadarGreen
                        )
                    )
                }
            }
        }

        // Section 5: OEM & Battery Optimization Guide Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    ChineseRomHelper.openAutostartSettings(context)
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PurpleLight.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = PurpleLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "MIUI, EMUI & Vivo Autostart",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Prevent background service kills by OS",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun DirectionCard(
    title: String,
    subtitle: String,
    iconText: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) RadarGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) RadarGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = iconText,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSelected) RadarGreen else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
