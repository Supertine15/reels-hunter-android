package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.data.AppPreferences
import com.example.model.ScrollSettings
import com.example.model.SwipeDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class CoinHunterAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var autoScrollJob: Job? = null
    private var autoStopJob: Job? = null

    private lateinit var appPreferences: AppPreferences

    companion object {
        private const val TAG = "CoinHunterService"

        var isServiceRunning: Boolean = false
            private set

        var instance: CoinHunterAccessibilityService? = null
            private set

        private val _serviceConnectedFlow = MutableStateFlow(false)
        val serviceConnectedFlow: StateFlow<Boolean> = _serviceConnectedFlow.asStateFlow()

        private val _isAutoScrollingFlow = MutableStateFlow(false)
        val isAutoScrollingFlow: StateFlow<Boolean> = _isAutoScrollingFlow.asStateFlow()

        private val _countdownFlow = MutableStateFlow(0)
        val countdownFlow: StateFlow<Int> = _countdownFlow.asStateFlow()

        private val _autoStopRemainingSecondsFlow = MutableStateFlow(0L)
        val autoStopRemainingSecondsFlow: StateFlow<Long> = _autoStopRemainingSecondsFlow.asStateFlow()

        private val _totalSwipesCompletedFlow = MutableStateFlow(0)
        val totalSwipesCompletedFlow: StateFlow<Int> = _totalSwipesCompletedFlow.asStateFlow()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isServiceRunning = true
        _serviceConnectedFlow.value = true
        appPreferences = AppPreferences(this)
        Log.d(TAG, "CoinHunter Accessibility Service connected successfully")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Accessibility events monitoring if needed for app detection
    }

    override fun onInterrupt() {
        Log.w(TAG, "CoinHunter Accessibility Service interrupted")
        stopAutoScroll()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAutoScroll()
        serviceScope.cancel()
        instance = null
        isServiceRunning = false
        _serviceConnectedFlow.value = false
        Log.d(TAG, "CoinHunter Accessibility Service destroyed")
    }

    fun startAutoScroll() {
        if (!appPreferences.isStandbyEnabled) {
            Log.d(TAG, "Standby is disabled, cannot start auto-scroll")
            return
        }

        stopAutoScroll()
        _isAutoScrollingFlow.value = true

        val settings = appPreferences.getSettings()

        // Handle Auto-Stop Timer
        if (settings.autoStopMinutes > 0) {
            val totalSeconds = settings.autoStopMinutes * 60L
            _autoStopRemainingSecondsFlow.value = totalSeconds
            autoStopJob = serviceScope.launch {
                var remaining = totalSeconds
                while (isActive && remaining > 0) {
                    delay(1000)
                    remaining--
                    _autoStopRemainingSecondsFlow.value = remaining
                }
                if (isActive) {
                    stopAutoScroll()
                }
            }
        } else {
            _autoStopRemainingSecondsFlow.value = 0L
        }

        // Main Auto-Scroll Loop
        autoScrollJob = serviceScope.launch {
            while (isActive && _isAutoScrollingFlow.value) {
                val currentSettings = appPreferences.getSettings()
                val interval = max(1, currentSettings.intervalSeconds)

                // Countdown ticker
                for (sec in interval downTo 1) {
                    _countdownFlow.value = sec
                    delay(1000)
                    if (!isActive || !_isAutoScrollingFlow.value) break
                }

                if (!isActive || !_isAutoScrollingFlow.value) break

                // Trigger swipe
                _countdownFlow.value = 0
                performSwipe(
                    direction = currentSettings.swipeDirection,
                    durationMs = currentSettings.swipeDurationMs,
                    distancePercent = currentSettings.swipeDistancePercent,
                    antiBot = currentSettings.antiBotEnabled
                )

                // Brief post-swipe settling delay (250ms - 400ms)
                delay(350)
            }
        }
    }

    fun stopAutoScroll() {
        _isAutoScrollingFlow.value = false
        _countdownFlow.value = 0
        autoScrollJob?.cancel()
        autoScrollJob = null
        autoStopJob?.cancel()
        autoStopJob = null
        _autoStopRemainingSecondsFlow.value = 0L
    }

    fun toggleAutoScroll(): Boolean {
        return if (_isAutoScrollingFlow.value) {
            stopAutoScroll()
            false
        } else {
            startAutoScroll()
            true
        }
    }

    fun triggerSingleSwipe(
        customDirection: SwipeDirection? = null,
        callback: ((Boolean) -> Unit)? = null
    ) {
        val settings = appPreferences.getSettings()
        performSwipe(
            direction = customDirection ?: settings.swipeDirection,
            durationMs = settings.swipeDurationMs,
            distancePercent = settings.swipeDistancePercent,
            antiBot = settings.antiBotEnabled,
            callback = callback
        )
    }

    fun performSwipe(
        direction: SwipeDirection = SwipeDirection.UP,
        durationMs: Long = 320L,
        distancePercent: Int = 65,
        antiBot: Boolean = true,
        callback: ((Boolean) -> Unit)? = null
    ) {
        val metrics: DisplayMetrics = resources.displayMetrics
        val width = metrics.widthPixels.toFloat()
        val height = metrics.heightPixels.toFloat()

        if (width <= 0 || height <= 0) {
            callback?.invoke(false)
            return
        }

        val distanceFraction = min(max(distancePercent / 100f, 0.2f), 0.95f)

        // Calculate base start and end points
        var startX: Float
        var startY: Float
        var endX: Float
        var endY: Float

        val centerX = width / 2f
        val centerY = height / 2f

        when (direction) {
            SwipeDirection.UP -> {
                // To scroll UP in feed (showing next item), finger swipes bottom to top
                val totalDistance = height * distanceFraction
                startY = centerY + (totalDistance / 2f)
                endY = centerY - (totalDistance / 2f)
                startX = centerX
                endX = centerX
            }
            SwipeDirection.DOWN -> {
                // To scroll DOWN in feed (showing previous item), finger swipes top to bottom
                val totalDistance = height * distanceFraction
                startY = centerY - (totalDistance / 2f)
                endY = centerY + (totalDistance / 2f)
                startX = centerX
                endX = centerX
            }
            SwipeDirection.LEFT -> {
                val totalDistance = width * distanceFraction
                startX = centerX + (totalDistance / 2f)
                endX = centerX - (totalDistance / 2f)
                startY = centerY
                endY = centerY
            }
            SwipeDirection.RIGHT -> {
                val totalDistance = width * distanceFraction
                startX = centerX - (totalDistance / 2f)
                endX = centerX + (totalDistance / 2f)
                startY = centerY
                endY = centerY
            }
        }

        var finalDuration = durationMs

        // Anti-Bot Natural Verification: inject micro-variations, curved trajectory & speed jitter
        val path = Path()

        if (antiBot) {
            // 1. Randomized start point jitter (-18px to +18px)
            val jitterStartX = (Random.nextFloat() - 0.5f) * (width * 0.08f)
            val jitterStartY = (Random.nextFloat() - 0.5f) * (height * 0.04f)
            startX = min(max(startX + jitterStartX, 20f), width - 20f)
            startY = min(max(startY + jitterStartY, 40f), height - 40f)

            // 2. Randomized end point jitter
            val jitterEndX = (Random.nextFloat() - 0.5f) * (width * 0.12f)
            val jitterEndY = (Random.nextFloat() - 0.5f) * (height * 0.05f)
            endX = min(max(endX + jitterEndX, 20f), width - 20f)
            endY = min(max(endY + jitterEndY, 40f), height - 40f)

            // 3. Natural curved Bézier trajectory (human finger natural arc)
            val controlPointVariance = (Random.nextFloat() - 0.5f) * (width * 0.15f)
            val controlX = (startX + endX) / 2f + controlPointVariance
            val controlY = (startY + endY) / 2f + (Random.nextFloat() - 0.5f) * (height * 0.05f)

            path.moveTo(startX, startY)
            path.quadTo(controlX, controlY, endX, endY)

            // 4. Velocity micro-variation (±15% duration variance)
            val velocityFactor = 0.85f + (Random.nextFloat() * 0.30f)
            finalDuration = (durationMs * velocityFactor).toLong().coerceIn(120L, 1000L)
        } else {
            // Linear standard path
            path.moveTo(startX, startY)
            path.lineTo(endX, endY)
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, finalDuration)
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(stroke)
        val gesture = gestureBuilder.build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                _totalSwipesCompletedFlow.value += 1
                callback?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.w(TAG, "Gesture swipe was cancelled")
                callback?.invoke(false)
            }
        }, null)
    }
}
