package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ScrollSettings
import com.example.model.SwipeDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

        // Global StateFlows for real-time inter-process and inter-component state synchronization
        private val _intervalFlow = MutableStateFlow(ScrollSettings.DEFAULT_INTERVAL)
        val intervalFlow: StateFlow<Int> = _intervalFlow.asStateFlow()

        private val _standbyFlow = MutableStateFlow(true)
        val standbyFlow: StateFlow<Boolean> = _standbyFlow.asStateFlow()

        private val _directionFlow = MutableStateFlow(SwipeDirection.UP)
        val directionFlow: StateFlow<SwipeDirection> = _directionFlow.asStateFlow()

        private val _keepDefaultFlow = MutableStateFlow(true)
        val keepDefaultFlow: StateFlow<Boolean> = _keepDefaultFlow.asStateFlow()

        private val _autoStopFlow = MutableStateFlow(ScrollSettings.DEFAULT_AUTO_STOP_MINUTES)
        val autoStopFlow: StateFlow<Int> = _autoStopFlow.asStateFlow()
    }

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            KEY_INTERVAL -> _intervalFlow.value = intervalSeconds
            KEY_STANDBY -> _standbyFlow.value = isStandbyEnabled
            KEY_DIRECTION -> _directionFlow.value = swipeDirection
            KEY_KEEP_DEFAULT -> _keepDefaultFlow.value = keepAsDefault
            KEY_AUTO_STOP -> _autoStopFlow.value = autoStopMinutes
        }
    }

    init {
        _intervalFlow.value = intervalSeconds
        _standbyFlow.value = isStandbyEnabled
        _directionFlow.value = swipeDirection
        _keepDefaultFlow.value = keepAsDefault
        _autoStopFlow.value = autoStopMinutes
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    val intervalFlow: StateFlow<Int> get() = Companion.intervalFlow
    val standbyFlow: StateFlow<Boolean> get() = Companion.standbyFlow
    val directionFlow: StateFlow<SwipeDirection> get() = Companion.directionFlow
    val keepDefaultFlow: StateFlow<Boolean> get() = Companion.keepDefaultFlow
    val autoStopFlow: StateFlow<Int> get() = Companion.autoStopFlow

    var isStandbyEnabled: Boolean
        get() = prefs.getBoolean(KEY_STANDBY, true)
        set(value) {
            prefs.edit().putBoolean(KEY_STANDBY, value).apply()
            _standbyFlow.value = value
        }

    var keepAsDefault: Boolean
        get() = prefs.getBoolean(KEY_KEEP_DEFAULT, true)
        set(value) {
            prefs.edit().putBoolean(KEY_KEEP_DEFAULT, value).apply()
            _keepDefaultFlow.value = value
        }

    var intervalSeconds: Int
        get() = prefs.getInt(KEY_INTERVAL, ScrollSettings.DEFAULT_INTERVAL)
        set(value) {
            prefs.edit().putInt(KEY_INTERVAL, value).apply()
            _intervalFlow.value = value
        }

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
        get() = prefs.getInt(KEY_AUTO_STOP, ScrollSettings.DEFAULT_AUTO_STOP_MINUTES)
        set(value) {
            prefs.edit().putInt(KEY_AUTO_STOP, value).apply()
            _autoStopFlow.value = value
        }

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
