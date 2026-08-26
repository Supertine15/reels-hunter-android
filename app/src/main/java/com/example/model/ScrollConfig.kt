package com.example.model

enum class SwipeDirection(val label: String, val description: String) {
    UP(label = "Up (Next Video)", description = "Bottom-to-Top swipe to load next video"),
    DOWN(label = "Down (Prev Video)", description = "Top-to-Bottom swipe to load previous video"),
    LEFT(label = "Left", description = "Right-to-Left horizontal swipe"),
    RIGHT(label = "Right", description = "Left-to-Right horizontal swipe")
}

data class ScrollSettings(
    val isStandbyEnabled: Boolean = true,
    val keepAsDefault: Boolean = true,
    val intervalSeconds: Int = 8, // 0 to 60s
    val swipeDurationMs: Long = 320L, // 150ms to 800ms
    val swipeDistancePercent: Int = 65, // 30% to 90%
    val antiBotEnabled: Boolean = true,
    val swipeDirection: SwipeDirection = SwipeDirection.UP,
    val autoStopMinutes: Int = 0 // 0 = Off, 15, 30, 60, 120, 240, 480, 720
) {
    // Universal optimized defaults for Shorts/Reels/TikTok
    companion object {
        const val DEFAULT_INTERVAL = 8
        const val DEFAULT_DURATION_MS = 320L
        const val DEFAULT_DISTANCE_PERCENT = 65

        fun getUniversalDefaults(): ScrollSettings {
            return ScrollSettings(
                isStandbyEnabled = true,
                keepAsDefault = true,
                intervalSeconds = DEFAULT_INTERVAL,
                swipeDurationMs = DEFAULT_DURATION_MS,
                swipeDistancePercent = DEFAULT_DISTANCE_PERCENT,
                antiBotEnabled = true,
                swipeDirection = SwipeDirection.UP,
                autoStopMinutes = 0
            )
        }
    }
}

data class PlatformPreset(
    val id: String,
    val name: String,
    val packageName: String,
    val defaultIntervalSeconds: Int,
    val swipeDurationMs: Long,
    val distancePercent: Int,
    val iconDescription: String,
    val badge: String
)

enum class AppTab(val title: String) {
    DASHBOARD("Dashboard"),
    SETTINGS("Settings"),
    PLATFORMS("Platforms"),
    ABOUT("About Us")
}
