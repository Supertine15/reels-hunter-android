package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppTab
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.RadarGreenBright
import com.example.ui.theme.RoseAlert
import com.example.util.ChineseRomHelper

@Composable
fun CyberRadarIcon(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    color: Color = RadarGreen
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0xFF0F172A))
            .border(
                width = 1.5.dp,
                color = if (isActive) color else color.copy(alpha = 0.5f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_app_logo),
            contentDescription = "Easy Scroll Logo",
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    isServiceActive: Boolean,
    isAutoScrolling: Boolean,
    allPermissionsGranted: Boolean = true,
    onOpenPermissions: () -> Unit = {},
    onCheckUpdates: () -> Unit = {}
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    if (!allPermissionsGranted) onOpenPermissions()
                }
            ) {
                CyberRadarIcon(
                    isActive = isAutoScrolling || isServiceActive,
                    color = if (isAutoScrolling) RadarGreen else if (allPermissionsGranted) RadarGreen else AmberWarning
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Easy Scroll",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Live status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isAutoScrolling) RadarGreen.copy(alpha = 0.2f)
                            else if (allPermissionsGranted && isServiceActive) RadarGreen.copy(alpha = 0.15f)
                            else if (allPermissionsGranted) RadarGreenBright.copy(alpha = 0.15f)
                            else AmberWarning.copy(alpha = 0.2f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isAutoScrolling) RadarGreen
                            else if (allPermissionsGranted) RadarGreen
                            else AmberWarning,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isAutoScrolling) RadarGreen
                                    else if (allPermissionsGranted) RadarGreen
                                    else AmberWarning
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAutoScrolling) "RUNNING"
                            else if (allPermissionsGranted) "READY"
                            else "SETUP",
                            color = if (isAutoScrolling) RadarGreen
                            else if (allPermissionsGranted) RadarGreen
                            else AmberWarning,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        },
        actions = {
            Box {
                IconButton(onClick = { menuExpanded = !menuExpanded }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    // 1. Theme Toggle
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isDarkTheme) "Light Mode (Day)" else "Dark Mode (Cyber Night)",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Theme Toggle",
                                tint = PurpleLight
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleTheme()
                        }
                    )

                    // 2. Permission Setup
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Permission Setup Guide",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Permissions",
                                tint = RadarGreen
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenPermissions()
                        }
                    )

                    // 3. OEM / Autostart Settings
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "OEM Autostart & Battery",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = "OEM Guide",
                                tint = CyanAccent
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            ChineseRomHelper.openAutostartSettings(context)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // 4. Community / GitHub
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "GitHub & Updates",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "GitHub",
                                tint = PurpleLight
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onCheckUpdates()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun AppBottomBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // 1. Dashboard Tab
        NavigationBarItem(
            selected = currentTab == AppTab.DASHBOARD,
            onClick = { onTabSelected(AppTab.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = if (currentTab == AppTab.DASHBOARD) Icons.Filled.TrackChanges else Icons.Default.TrackChanges,
                    contentDescription = "Dashboard"
                )
            },
            label = { Text("Dashboard") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF003816),
                selectedTextColor = RadarGreen,
                indicatorColor = RadarGreen,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // 2. Settings Tab
        NavigationBarItem(
            selected = currentTab == AppTab.SETTINGS,
            onClick = { onTabSelected(AppTab.SETTINGS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == AppTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PurpleLight,
                indicatorColor = PurplePrimary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // 3. Presets Tab
        NavigationBarItem(
            selected = currentTab == AppTab.PLATFORMS,
            onClick = { onTabSelected(AppTab.PLATFORMS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == AppTab.PLATFORMS) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Presets"
                )
            },
            label = { Text("Presets") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PurpleLight,
                indicatorColor = PurplePrimary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // 4. About / Guide Tab
        NavigationBarItem(
            selected = currentTab == AppTab.ABOUT,
            onClick = { onTabSelected(AppTab.ABOUT) },
            icon = {
                Icon(
                    imageVector = if (currentTab == AppTab.ABOUT) Icons.Filled.Info else Icons.Outlined.Info,
                    contentDescription = "Guide"
                )
            },
            label = { Text("Guide") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PurpleLight,
                indicatorColor = PurplePrimary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

