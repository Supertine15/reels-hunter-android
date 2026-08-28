package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.data.AppPreferences
import com.example.model.AppTab
import com.example.model.PlatformPreset
import com.example.service.CoinHunterAccessibilityService
import com.example.service.FloatingOverlayService
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.UpdateDialog
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PlatformsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppUpdateManager
import com.example.util.PermissionHelper
import com.example.util.UpdateCheckResult
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferences: AppPreferences
    private var allPermissionsGrantedState = mutableStateOf(false)
    private var triggerPermissionDialogState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = AppPreferences(this)
        allPermissionsGrantedState.value = PermissionHelper.areAllPermissionsGranted(this)

        setContent {
            var isDarkTheme by remember { mutableStateOf(preferences.isDarkTheme) }
            var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
            var allPermissionsGranted by remember { allPermissionsGrantedState }

            val isServiceActive by CoinHunterAccessibilityService.serviceConnectedFlow.collectAsState()
            val isAutoScrolling by CoinHunterAccessibilityService.isAutoScrollingFlow.collectAsState()

            val scope = rememberCoroutineScope()
            var updateCheckResult by remember { mutableStateOf<UpdateCheckResult>(UpdateCheckResult.Idle) }
            var showUpdateDialog by remember { mutableStateOf(false) }

            // Automatic startup check to notify the user if a new APK is available for Easy Scroll
            LaunchedEffect(Unit) {
                try {
                    val result = AppUpdateManager.checkForUpdates()
                    if (result is UpdateCheckResult.UpdateAvailable) {
                        updateCheckResult = result
                        showUpdateDialog = true
                    }
                } catch (_: Exception) {}
            }

            fun performManualUpdateCheck() {
                showUpdateDialog = true
                updateCheckResult = UpdateCheckResult.Checking
                scope.launch {
                    val result = AppUpdateManager.checkForUpdates()
                    updateCheckResult = result
                }
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AppTopBar(
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = {
                                isDarkTheme = !isDarkTheme
                                preferences.isDarkTheme = isDarkTheme
                            },
                            isServiceActive = isServiceActive,
                            isAutoScrolling = isAutoScrolling,
                            allPermissionsGranted = allPermissionsGranted,
                            onOpenPermissions = {
                                currentTab = AppTab.DASHBOARD
                                triggerPermissionDialogState.value = true
                            },
                            onCheckUpdates = {
                                performManualUpdateCheck()
                            }
                        )
                    },
                    bottomBar = {
                        AppBottomBar(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            AppTab.DASHBOARD -> {
                                DashboardScreen(
                                    preferences = preferences,
                                    onStartAutoScrollAndMinimize = {
                                        startAutoScrollAndMinimize()
                                    },
                                    onLaunchFloatingBarOnly = {
                                        launchFloatingBar()
                                    }
                                )
                            }
                            AppTab.SETTINGS -> {
                                SettingsScreen(
                                    preferences = preferences,
                                    onSettingsChanged = {
                                        // Update state if needed
                                    }
                                )
                            }
                            AppTab.PLATFORMS -> {
                                PlatformsScreen(
                                    preferences = preferences,
                                    onApplyAndLaunchApp = { preset ->
                                        applyPresetAndLaunchApp(preset)
                                    }
                                )
                            }
                            AppTab.ABOUT -> {
                                AboutScreen(
                                    onCheckUpdates = {
                                        performManualUpdateCheck()
                                    }
                                )
                            }
                        }

                        // In-App GitHub Update & Release Dialog
                        if (showUpdateDialog) {
                            UpdateDialog(
                                result = updateCheckResult,
                                onDismiss = {
                                    showUpdateDialog = false
                                    updateCheckResult = UpdateCheckResult.Idle
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissionsOnResume()
    }

    private fun checkPermissionsOnResume() {
        val allGranted = PermissionHelper.areAllPermissionsGranted(this)
        allPermissionsGrantedState.value = allGranted
    }

    private fun startAutoScrollAndMinimize() {
        if (!PermissionHelper.isAccessibilityGranted(this)) {
            Toast.makeText(this, "Please enable Easy Scroll Accessibility Service.", Toast.LENGTH_SHORT).show()
            PermissionHelper.openAccessibilitySettings(this)
            return
        }

        if (!PermissionHelper.isOverlayGranted(this)) {
            Toast.makeText(this, "Please grant Overlay permission for the floating bar.", Toast.LENGTH_SHORT).show()
            PermissionHelper.openOverlaySettings(this)
            return
        }

        // Launch Floating Bar Service
        FloatingOverlayService.startService(this)

        // Start Auto-Scroll Engine
        CoinHunterAccessibilityService.instance?.startAutoScroll()

        // Minimize app so user sees their short video feed
        moveTaskToBack(true)
    }

    private fun launchFloatingBar() {
        if (!PermissionHelper.isOverlayGranted(this)) {
            Toast.makeText(this, "Please grant Overlay permission to display the floating bar.", Toast.LENGTH_SHORT).show()
            PermissionHelper.openOverlaySettings(this)
            return
        }
        FloatingOverlayService.startService(this)
        Toast.makeText(this, "Floating bar active on screen", Toast.LENGTH_SHORT).show()
    }

    private fun applyPresetAndLaunchApp(preset: PlatformPreset) {
        if (!PermissionHelper.isOverlayGranted(this)) {
            PermissionHelper.openOverlaySettings(this)
            return
        }

        // Launch overlay
        FloatingOverlayService.startService(this)

        // Start scrolling
        CoinHunterAccessibilityService.instance?.startAutoScroll()

        if (preset.packageName.isNotEmpty()) {
            val launchIntent = packageManager.getLaunchIntentForPackage(preset.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
            } else {
                Toast.makeText(this, "${preset.name} opened. Minimizing to background.", Toast.LENGTH_SHORT).show()
                moveTaskToBack(true)
            }
        } else {
            moveTaskToBack(true)
        }
    }
}
