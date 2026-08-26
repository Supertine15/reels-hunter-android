package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ScrollSettings
import com.example.model.SwipeDirection

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("coinhunter_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_STANDBY = "is_standby_enabled"
        private const val KEY_KEEP_DEFAULT = "keep_as_default"
        private const val KEY_INTERVAL = "interval_seconds"
        private const val KEY_DURATION = "swipe_duration_ms"
        private const val KEY_DISTANCE = "swipe_distance_percent"
        private const val KEY_ANTI_BOT = "anti_bot_enabled"
        private const val KEY_DIRECTION = "swipe_direction"
        private const val KEY_AUTO_STOP = "auto_stop_minutes"
        private const val KEY_PERMISSIONS_COMPLETED = "permissions_completed"
        private const val KEY_DARK_THEME = "is_dark_theme"
    }

    var isStandbyEnabled: Boolean
        get() = prefs.getBoolean(KEY_STANDBY, true)
        set(value) = prefs.edit().putBoolean(KEY_STANDBY, value).apply()

    var keepAsDefault: Boolean
        get() = prefs.getBoolean(KEY_KEEP_DEFAULT, true)
        set(value) = prefs.edit().putBoolean(KEY_KEEP_DEFAULT, value).apply()

    var intervalSeconds: Int
        get() = prefs.getInt(KEY_INTERVAL, ScrollSettings.DEFAULT_INTERVAL)
        set(value) = prefs.edit().putInt(KEY_INTERVAL, value).apply()

    var swipeDurationMs: Long
        get() = prefs.getLong(KEY_DURATION, ScrollSettings.DEFAULT_DURATION_MS)
        set(value) = prefs.edit().putLong(KEY_DURATION, value).apply()

    var swipeDistancePercent: Int
        get() = prefs.getInt(KEY_DISTANCE, ScrollSettings.DEFAULT_DISTANCE_PERCENT)
        set(value) = prefs.edit().putInt(KEY_DISTANCE, value).apply()

    var antiBotEnabled: Boolean
        get() = prefs.getBoolean(KEY_ANTI_BOT, true)
        set(value) = prefs.edit().putBoolean(KEY_ANTI_BOT, value).apply()

    var swipeDirection: SwipeDirection
        get() {
            val name = prefs.getString(KEY_DIRECTION, SwipeDirection.UP.name)
            return try {
                SwipeDirection.valueOf(name ?: SwipeDirection.UP.name)
            } catch (e: Exception) {
                SwipeDirection.UP
            }
        }
        set(value) = prefs.edit().putString(KEY_DIRECTION, value.name).apply()

    var autoStopMinutes: Int
        get() = prefs.getInt(KEY_AUTO_STOP, 0)
        set(value) = prefs.edit().putInt(KEY_AUTO_STOP, value).apply()

    var permissionsCompleted: Boolean
        get() = prefs.getBoolean(KEY_PERMISSIONS_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_PERMISSIONS_COMPLETED, value).apply()

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    fun getSettings(): ScrollSettings {
        return if (keepAsDefault) {
            ScrollSettings.getUniversalDefaults().copy(
                isStandbyEnabled = isStandbyEnabled,
                keepAsDefault = true,
                antiBotEnabled = antiBotEnabled,
                swipeDirection = swipeDirection,
                autoStopMinutes = autoStopMinutes
            )
        } else {
            ScrollSettings(
                isStandbyEnabled = isStandbyEnabled,
                keepAsDefault = false,
                intervalSeconds = intervalSeconds,
                swipeDurationMs = swipeDurationMs,
                swipeDistancePercent = swipeDistancePercent,
                antiBotEnabled = antiBotEnabled,
                swipeDirection = swipeDirection,
                autoStopMinutes = autoStopMinutes
            )
        }
    }

    fun saveSettings(settings: ScrollSettings) {
        isStandbyEnabled = settings.isStandbyEnabled
        keepAsDefault = settings.keepAsDefault
        intervalSeconds = settings.intervalSeconds
        swipeDurationMs = settings.swipeDurationMs
        swipeDistancePercent = settings.swipeDistancePercent
        antiBotEnabled = settings.antiBotEnabled
        swipeDirection = settings.swipeDirection
        autoStopMinutes = settings.autoStopMinutes
    }
}
