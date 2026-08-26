package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberRadarIcon
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.RadarGreenBright
import com.example.util.ChineseRomHelper
import com.example.util.PermissionHelper

enum class PermissionStep(
    val title: String,
    val shortDesc: String,
    val icon: ImageVector,
    val instructions: List<String>
) {
    ACCESSIBILITY(
        title = "Accessibility Service",
        shortDesc = "Required to execute auto-swipe gestures smoothly.",
        icon = Icons.Default.Accessibility,
        instructions = listOf(
            "Tap 'Open Accessibility Settings' below.",
            "Find 'ReelsHunter' in Installed / Downloaded Services.",
            "Turn ON 'Use ReelsHunter' switch and allow permission."
        )
    ),
    OVERLAY(
        title = "Display Over Other Apps",
        shortDesc = "Shows the floating control bar on video apps.",
        icon = Icons.Default.Layers,
        instructions = listOf(
            "Tap 'Open Overlay Settings' below.",
            "Locate 'ReelsHunter' in the app list.",
            "Toggle 'Allow display over other apps' to ON."
        )
    ),
    BATTERY_OPTIMIZATION(
        title = "Background / Battery",
        shortDesc = "Keeps the scroller running continuously without being killed.",
        icon = Icons.Default.BatteryChargingFull,
        instructions = listOf(
            "Tap 'Allow Unrestricted' below.",
            "Select 'Allow' on system battery prompt.",
            "For Xiaomi/Huawei/Vivo: Enable Autostart in system settings."
        )
    )
}

@Composable
fun OnboardingScreen(
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()

    var isAccessibilityOk by remember { mutableStateOf(PermissionHelper.isAccessibilityGranted(context)) }
    var isOverlayOk by remember { mutableStateOf(PermissionHelper.isOverlayGranted(context)) }
    var isBatteryOk by remember { mutableStateOf(PermissionHelper.isBatteryOptimizationIgnored(context)) }

    var activePopupPermission by remember { mutableStateOf<PermissionStep?>(null) }
    var showOemAutostartDialog by remember { mutableStateOf(false) }

    fun refreshAll() {
        isAccessibilityOk = PermissionHelper.isAccessibilityGranted(context)
        isOverlayOk = PermissionHelper.isOverlayGranted(context)
        isBatteryOk = PermissionHelper.isBatteryOptimizationIgnored(context)
    }

    val accessibilityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        refreshAll()
    }

    val overlayLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        refreshAll()
    }

    val batteryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        refreshAll()
    }

    // Auto-refresh when user returns from settings (onResume)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshAll()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val grantedCount = listOf(isAccessibilityOk, isOverlayOk, isBatteryOk).count { it }
    val allGranted = isAccessibilityOk && isOverlayOk && isBatteryOk

    LaunchedEffect(allGranted) {
        if (allGranted) {
            onAllPermissionsGranted()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showOemAutostartDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "OEM Guide",
                        tint = PurpleLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "MIUI / Vivo Guide",
                        fontSize = 12.sp,
                        color = PurpleLight,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { refreshAll() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Status",
                        tint = RadarGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ReelsHunter Logo & Friendly Header
            CyberRadarIcon(
                isActive = true,
                color = RadarGreen,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Welcome to ReelsHunter",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Smart auto-scroller for short videos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Progress Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Setup Progress",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$grantedCount of 3 ready",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (allGranted) RadarGreen else PurpleLight
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { grantedCount / 3f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (allGranted) RadarGreen else PurplePrimary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3 Clean, Interactive Permission Cards
            PermissionItemCard(
                step = PermissionStep.ACCESSIBILITY,
                isGranted = isAccessibilityOk,
                onCardClick = {
                    activePopupPermission = PermissionStep.ACCESSIBILITY
                },
                onGrantClick = {
                    activePopupPermission = PermissionStep.ACCESSIBILITY
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionItemCard(
                step = PermissionStep.OVERLAY,
                isGranted = isOverlayOk,
                onCardClick = {
                    activePopupPermission = PermissionStep.OVERLAY
                },
                onGrantClick = {
                    activePopupPermission = PermissionStep.OVERLAY
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionItemCard(
                step = PermissionStep.BATTERY_OPTIMIZATION,
                isGranted = isBatteryOk,
                onCardClick = {
                    activePopupPermission = PermissionStep.BATTERY_OPTIMIZATION
                },
                onGrantClick = {
                    activePopupPermission = PermissionStep.BATTERY_OPTIMIZATION
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Primary Action
            if (allGranted) {
                Button(
                    onClick = onAllPermissionsGranted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("enter_dashboard_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RadarGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Enter Dashboard",
                            color = Color(0xFF003816),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF003816),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onAllPermissionsGranted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Skip to Dashboard",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Pop-up Modal
        activePopupPermission?.let { step ->
            PermissionDetailPopup(
                step = step,
                isGranted = when (step) {
                    PermissionStep.ACCESSIBILITY -> isAccessibilityOk
                    PermissionStep.OVERLAY -> isOverlayOk
                    PermissionStep.BATTERY_OPTIMIZATION -> isBatteryOk
                },
                onDismiss = {
                    activePopupPermission = null
                    refreshAll()
                },
                onProceedToSettings = {
                    when (step) {
                        PermissionStep.ACCESSIBILITY -> {
                            try {
                                accessibilityLauncher.launch(PermissionHelper.getAccessibilityIntent())
                            } catch (_: Exception) {
                                PermissionHelper.openAccessibilitySettings(context)
                            }
                        }
                        PermissionStep.OVERLAY -> {
                            try {
                                overlayLauncher.launch(PermissionHelper.getOverlayIntent(context))
                            } catch (_: Exception) {
                                PermissionHelper.openOverlaySettings(context)
                            }
                        }
                        PermissionStep.BATTERY_OPTIMIZATION -> {
                            try {
                                batteryLauncher.launch(PermissionHelper.getBatteryOptimizationIntent(context))
                            } catch (_: Exception) {
                                PermissionHelper.requestIgnoreBatteryOptimization(context)
                            }
                        }
                    }
                    activePopupPermission = null
                }
            )
        }

        // OEM Autostart Dialog
        if (showOemAutostartDialog) {
            AlertDialog(
                onDismissRequest = { showOemAutostartDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = PurpleLight
                    )
                },
                title = {
                    Text(
                        text = "OEM Background Guide",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Certain device brands (Xiaomi / MIUI, Huawei / EMUI, Vivo, Oppo) aggressively close background services after a few minutes.\n\n" +
                                    "To ensure uninterrupted auto-scrolling:\n" +
                                    "1. Enable 'Autostart' for ReelsHunter.\n" +
                                    "2. Set Battery Saver to 'No Restrictions'.\n" +
                                    "3. Lock ReelsHunter in the recent apps tray.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showOemAutostartDialog = false
                            ChineseRomHelper.openAutostartSettings(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Text("Open Autostart Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOemAutostartDialog = false }) {
                        Text("Got it")
                    }
                }
            )
        }
    }
}

@Composable
fun PermissionItemCard(
    step: PermissionStep,
    isGranted: Boolean,
    onCardClick: () -> Unit,
    onGrantClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .border(
                width = 1.dp,
                color = if (isGranted) RadarGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) RadarGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isGranted) RadarGreen.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.Check else step.icon,
                        contentDescription = step.title,
                        tint = if (isGranted) RadarGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = step.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = step.shortDesc,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isGranted) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RadarGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Active ✓",
                        color = RadarGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            } else {
                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RadarGreen),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Grant",
                        color = Color(0xFF003816),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionDetailPopup(
    step: PermissionStep,
    isGranted: Boolean,
    onDismiss: () -> Unit,
    onProceedToSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(RadarGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = RadarGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = step.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = step.shortDesc,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Quick Instructions:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                step.instructions.forEachIndexed { index, instruction ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${index + 1}.",
                            fontWeight = FontWeight.Bold,
                            color = RadarGreen,
                            fontSize = 12.sp,
                            modifier = Modifier.width(18.dp)
                        )
                        Text(
                            text = instruction,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onProceedToSettings,
                colors = ButtonDefaults.buttonColors(containerColor = RadarGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isGranted) "Re-check Settings" else "Open Settings",
                        color = Color(0xFF003816),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = Color(0xFF003816),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
