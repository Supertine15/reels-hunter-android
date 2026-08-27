package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.model.SwipeDirection
import com.example.service.CoinHunterAccessibilityService
import com.example.service.FloatingOverlayService
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.RadarGreenBright
import com.example.ui.theme.RadarGreenDark
import com.example.ui.theme.RoseAlert
import com.example.util.ChineseRomHelper
import com.example.util.PermissionHelper
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DashboardScreen(
    preferences: AppPreferences,
    onStartAutoScrollAndMinimize: () -> Unit,
    onLaunchFloatingBarOnly: () -> Unit,
    showPermissionDialogInitially: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()

    val isStandby by preferences.standbyFlow.collectAsState()
    val interval by preferences.intervalFlow.collectAsState()
    val direction by preferences.directionFlow.collectAsState()
    val autoStopMinutes by preferences.autoStopFlow.collectAsState()

    val isAutoScrolling by CoinHunterAccessibilityService.isAutoScrollingFlow.collectAsState()
    val countdown by CoinHunterAccessibilityService.countdownFlow.collectAsState()
    val autoStopRemainingSeconds by CoinHunterAccessibilityService.autoStopRemainingSecondsFlow.collectAsState()
    val totalSwipes by CoinHunterAccessibilityService.totalSwipesCompletedFlow.collectAsState()
    val isServiceConnected by CoinHunterAccessibilityService.serviceConnectedFlow.collectAsState()

    var showPermissionDialog by remember { mutableStateOf(false) }

    // Live permission check
    var isAccessibilityGranted by remember { mutableStateOf(PermissionHelper.isAccessibilityGranted(context)) }
    var isOverlayGranted by remember { mutableStateOf(PermissionHelper.isOverlayGranted(context)) }
    var isBatteryGranted by remember { mutableStateOf(PermissionHelper.isBatteryOptimizationIgnored(context)) }

    fun refreshPermissions() {
        isAccessibilityGranted = PermissionHelper.isAccessibilityGranted(context)
        isOverlayGranted = PermissionHelper.isOverlayGranted(context)
        isBatteryGranted = PermissionHelper.isBatteryOptimizationIgnored(context)
    }

    val areAllGranted = isAccessibilityGranted && isOverlayGranted && isBatteryGranted

    // Activity Result Launchers for instant callback on return
    val accessibilityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissions()
    }

    val overlayLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissions()
    }

    val batteryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissions()
    }

    // Auto-refresh instantly when returning from system settings (onResume)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Automatically prompt permission modal once if not all granted on entry
    LaunchedEffect(Unit) {
        refreshPermissions()
        if (!areAllGranted && !preferences.permissionsCompleted) {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(areAllGranted) {
        if (areAllGranted) {
            preferences.permissionsCompleted = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // -------------------------------------------------------------
        // 1. Permission Status Banner / Action Bar
        // -------------------------------------------------------------
        if (!areAllGranted) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPermissionDialog = true }
                    .border(
                        1.dp,
                        AmberWarning.copy(alpha = 0.6f),
                        RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = AmberWarning.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AmberWarning.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Setup Required",
                                tint = AmberWarning,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Setup Required for Auto-Scroll",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap to grant Accessibility & Overlay (${listOf(isAccessibilityGranted, isOverlayGranted, isBatteryGranted).count { it }}/3 ready)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { showPermissionDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "Enable",
                            color = Color(0xFF1E1400),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // All permissions granted banner with green badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = RadarGreen.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(RadarGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Ready",
                            tint = RadarGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "All Permissions Active • Ready to Auto-Scroll",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = RadarGreen
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 2. Hero Circular Master Start Dial (Static & Battery-Optimized)
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .testTag("hero_start_dial")
                .clickable {
                    if (!areAllGranted) {
                        showPermissionDialog = true
                    } else {
                        if (isAutoScrolling) {
                            CoinHunterAccessibilityService.instance?.stopAutoScroll()
                        } else {
                            if (!isStandby) {
                                preferences.isStandbyEnabled = true
                            }
                            onStartAutoScrollAndMinimize()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Static High-Precision Dial Canvas (Zero Battery Drain)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f

                // Outer Background Circle
                drawCircle(
                    color = Color(0xFF131B2A),
                    radius = radius
                )

                // Concentric Calibration Rings
                drawCircle(
                    color = Color(0xFF1F2D44),
                    radius = radius * 0.92f,
                    style = Stroke(width = 1.5f)
                )
                drawCircle(
                    color = Color(0xFF1F2D44),
                    radius = radius * 0.65f,
                    style = Stroke(width = 1.2f)
                )
                drawCircle(
                    color = Color(0xFF1F2D44),
                    radius = radius * 0.38f,
                    style = Stroke(width = 1f)
                )

                // Crosshairs
                drawLine(
                    color = Color(0xFF24334D),
                    start = Offset(center.x, 0f),
                    end = Offset(center.x, size.height),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFF24334D),
                    start = Offset(0f, center.y),
                    end = Offset(size.width, center.y),
                    strokeWidth = 1f
                )

                // Outer Accent Ring
                drawCircle(
                    color = if (isAutoScrolling) RadarGreen else if (areAllGranted) RadarGreen.copy(alpha = 0.7f) else AmberWarning.copy(alpha = 0.6f),
                    radius = radius - 4f,
                    style = Stroke(width = 2.5f)
                )

                // Perimeter precision tick marks
                for (i in 0 until 36) {
                    val angleDeg = i * 10
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val isMajor = i % 9 == 0
                    val tickLen = if (isMajor) 10f else 5f
                    val startR = radius - 6f - tickLen
                    val endR = radius - 6f
                    val sX = (center.x + startR * cos(rad)).toFloat()
                    val sY = (center.y + startR * sin(rad)).toFloat()
                    val eX = (center.x + endR * cos(rad)).toFloat()
                    val eY = (center.y + endR * sin(rad)).toFloat()
                    drawLine(
                        color = if (isMajor) (if (areAllGranted) RadarGreen else AmberWarning) else Color(0xFF334A6E),
                        start = Offset(sX, sY),
                        end = Offset(eX, eY),
                        strokeWidth = if (isMajor) 2f else 1f
                    )
                }
            }

            // Center Content: Pure clean normal text "START" inside circle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = if (isAutoScrolling) "STOP"
                    else if (!areAllGranted) "SETUP"
                    else "START",
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    letterSpacing = 2.sp,
                    color = if (isAutoScrolling) RoseAlert
                    else if (!areAllGranted) AmberWarning
                    else RadarGreen
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isAutoScrolling) "RUNNING (${countdown}s)"
                    else if (!areAllGranted) "tap to enable access"
                    else "tap anywhere to start & minimize",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isAutoScrolling) RadarGreen else Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }
        }

        // -------------------------------------------------------------
        // 3. Auto-Off Timer (Sleep Timer) Card
        // -------------------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    color = if (isAutoScrolling && autoStopRemainingSeconds > 0) CyanAccent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = "Auto-Off Timer",
                            tint = CyanAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Auto-Off Timer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isAutoScrolling && autoStopRemainingSeconds > 0) {
                                val hrs = autoStopRemainingSeconds / 3600
                                val mins = (autoStopRemainingSeconds % 3600) / 60
                                val secs = autoStopRemainingSeconds % 60
                                if (hrs > 0) {
                                    String.format("Active: %d:%02d:%02d remaining", hrs, mins, secs)
                                } else {
                                    String.format("Active: %02d:%02d remaining", mins, secs)
                                }
                            } else {
                                "Stops scroll & closes bar when timer hits 0:00"
                            },
                            fontSize = 12.sp,
                            color = if (isAutoScrolling && autoStopRemainingSeconds > 0) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Timer Stepper: [-] 1 hr [+] (15m to 600m)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (autoStopMinutes > 15) {
                                val newDuration = autoStopMinutes - 15
                                preferences.autoStopMinutes = newDuration
                            }
                        },
                        enabled = autoStopMinutes > 15,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease Timer",
                            tint = if (autoStopMinutes > 15) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    val hours = autoStopMinutes / 60
                    val mins = autoStopMinutes % 60
                    val durationText = when {
                        hours == 0 -> "$mins min"
                        mins == 0 -> if (hours == 1) "1 hr" else "$hours hrs"
                        else -> "${hours}h ${mins}m"
                    }

                    Text(
                        text = durationText,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = CyanAccent,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = {
                            if (autoStopMinutes < 600) {
                                val newDuration = autoStopMinutes + 15
                                preferences.autoStopMinutes = newDuration
                            }
                        },
                        enabled = autoStopMinutes < 600,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase Timer",
                            tint = if (autoStopMinutes < 600) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 4. Interactive Control Box (INTERVAL & SWIPE DIRECTION)
        // -------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Section Title: INTERVAL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "INTERVAL",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "SWIPE DIRECTION",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Interval Stepper: [-] 8s [+]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (interval > 1) {
                                    preferences.intervalSeconds = interval - 1
                                    if (preferences.keepAsDefault) {
                                        preferences.keepAsDefault = false
                                    }
                                }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "${interval}s",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = RadarGreen,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )

                        IconButton(
                            onClick = {
                                if (interval < 60) {
                                    preferences.intervalSeconds = interval + 1
                                    if (preferences.keepAsDefault) {
                                        preferences.keepAsDefault = false
                                    }
                                }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Direction Selector Pills: Up, Down, Left, Right
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DirectionIconButton(
                            icon = Icons.Default.ArrowUpward,
                            label = "Up",
                            isSelected = direction == SwipeDirection.UP,
                            onClick = {
                                preferences.swipeDirection = SwipeDirection.UP
                            }
                        )
                        DirectionIconButton(
                            icon = Icons.Default.ArrowDownward,
                            label = "Down",
                            isSelected = direction == SwipeDirection.DOWN,
                            onClick = {
                                preferences.swipeDirection = SwipeDirection.DOWN
                            }
                        )
                        DirectionIconButton(
                            icon = Icons.Default.ArrowForward,
                            label = "Left",
                            isSelected = direction == SwipeDirection.LEFT,
                            onClick = {
                                preferences.swipeDirection = SwipeDirection.LEFT
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row: Floating Bar + Test Swipe
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Floating Bar Button
                    Button(
                        onClick = {
                            if (!areAllGranted) {
                                showPermissionDialog = true
                            } else {
                                onLaunchFloatingBarOnly()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = PurpleLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Floating Bar",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Test Swipe x1 Button
                    Button(
                        onClick = {
                            if (!areAllGranted) {
                                showPermissionDialog = true
                            } else {
                                val acc = CoinHunterAccessibilityService.instance
                                if (acc != null) {
                                    acc.triggerSingleSwipe()
                                    Toast.makeText(context, "Executing 1x Test Gesture...", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Accessibility service not running", Toast.LENGTH_SHORT).show()
                                    showPermissionDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = RadarGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Test Swipe ×1",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Info note
                Text(
                    text = "ⓘ Starting collapses ReelsHunter into the floating overlay controller",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // -------------------------------------------------------------
        // 5. Live Stats Row (matching Screenshot 4)
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "VIDEOS SCROLLED",
                value = "$totalSwipes",
                highlightColor = RadarGreen,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "TARGET APP",
                value = "Shorts / Reels",
                highlightColor = PurpleLight,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "SWIPE ENGINE",
                value = "${preferences.swipeDurationMs}ms / ${preferences.swipeDistancePercent}%",
                highlightColor = CyanAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    // -------------------------------------------------------------
    // Permission Setup Modal Dialog
    // -------------------------------------------------------------
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RadarGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = RadarGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Permissions Setup",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Grant the following permissions to enable automated gestures and floating overlay on video apps:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 1. Accessibility
                    PermissionStepCard(
                        title = "1. Accessibility Service",
                        desc = "Required to perform swipe gestures automatically",
                        isGranted = isAccessibilityGranted,
                        onGrant = {
                            try {
                                accessibilityLauncher.launch(PermissionHelper.getAccessibilityIntent())
                            } catch (e: Exception) {
                                PermissionHelper.openAccessibilitySettings(context)
                            }
                        }
                    )

                    // 2. Display Overlay
                    PermissionStepCard(
                        title = "2. Display Over Other Apps",
                        desc = "Required for the floating control bar",
                        isGranted = isOverlayGranted,
                        onGrant = {
                            try {
                                overlayLauncher.launch(PermissionHelper.getOverlayIntent(context))
                            } catch (e: Exception) {
                                PermissionHelper.openOverlaySettings(context)
                            }
                        }
                    )

                    // 3. Battery Optimization
                    PermissionStepCard(
                        title = "3. Battery Optimization",
                        desc = "Allows continuous background operation",
                        isGranted = isBatteryGranted,
                        onGrant = {
                            try {
                                batteryLauncher.launch(PermissionHelper.getBatteryOptimizationIntent(context))
                            } catch (e: Exception) {
                                PermissionHelper.openBatteryOptimizationSettings(context)
                            }
                        }
                    )

                    // OEM / Autostart Extra Helper
                    OutlinedButton(
                        onClick = { ChineseRomHelper.openAutostartSettings(context) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "MIUI / EMUI / Vivo Autostart Guide",
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        refreshPermissions()
                        showPermissionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RadarGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (areAllGranted) "Done" else "Close",
                        color = Color(0xFF003816),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Skip for Now")
                }
            }
        )
    }
}

@Composable
fun DirectionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) RadarGreen.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = 1.5.dp,
                color = if (isSelected) RadarGreen else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) RadarGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    highlightColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = highlightColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun PermissionStepCard(
    title: String,
    desc: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isGranted) RadarGreen.copy(alpha = 0.4f) else AmberWarning.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) RadarGreen.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) RadarGreen else Color(0xFF64748B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isGranted) Color(0xFF003816) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = desc,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isGranted) {
                Button(
                    onClick = onGrant,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = "Grant",
                        color = Color(0xFF1E1400),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Active",
                    color = RadarGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}
