package com.example.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.example.service.CoinHunterAccessibilityService

object PermissionHelper {

    enum class PermissionType {
        ACCESSIBILITY,
        OVERLAY,
        BATTERY_OPTIMIZATION
    }

    fun isAccessibilityGranted(context: Context): Boolean {
        // First check our service active singleton instance
        if (CoinHunterAccessibilityService.isServiceRunning) {
            return true
        }
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedServiceId = "${context.packageName}/${CoinHunterAccessibilityService::class.java.name}"
        val expectedServiceShortId = "${context.packageName}/.service.CoinHunterAccessibilityService"

        for (service in enabledServices) {
            val id = service.id
            if (id.equals(expectedServiceId, ignoreCase = true) ||
                id.equals(expectedServiceShortId, ignoreCase = true) ||
                id.contains("CoinHunterAccessibilityService", ignoreCase = true) ||
                id.contains("ReelsHunter", ignoreCase = true)
            ) {
                return true
            }
        }

        // Fallback check from Settings.Secure
        val settingValue = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        return settingValue.contains("CoinHunterAccessibilityService") || settingValue.contains("ReelsHunter")
    }

    fun isOverlayGranted(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun areAllPermissionsGranted(context: Context): Boolean {
        return isAccessibilityGranted(context) &&
                isOverlayGranted(context) &&
                isBatteryOptimizationIgnored(context)
    }

    fun getAccessibilityIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }

    fun getOverlayIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
    }

    fun getBatteryOptimizationIntent(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = getAccessibilityIntent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun openOverlaySettings(context: Context) {
        val intent = getOverlayIntent(context).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallback)
            } catch (_: Exception) {}
        }
    }

    fun openBatteryOptimizationSettings(context: Context) {
        requestIgnoreBatteryOptimization(context)
    }

    fun requestIgnoreBatteryOptimization(context: Context) {
        try {
            val intent = getBatteryOptimizationIntent(context).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallback)
            } catch (_: Exception) {
            }
        }
    }
}
