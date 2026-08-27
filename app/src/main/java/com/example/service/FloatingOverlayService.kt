package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.AppPreferences
import com.example.model.SwipeDirection
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RoseAlert

class FloatingOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    private var windowManager: WindowManager? = null
    private var floatingView: ComposeView? = null
    private lateinit var windowParams: WindowManager.LayoutParams
    private lateinit var appPreferences: AppPreferences

    companion object {
        private const val CHANNEL_ID = "coinhunter_overlay_channel"
        private const val NOTIFICATION_ID = 2001
        var isOverlayShowing: Boolean = false
            private set

        fun startService(context: Context) {
            val intent = Intent(context, FloatingOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, FloatingOverlayService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(Bundle())
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        appPreferences = AppPreferences(this)
        startForegroundNotification()
        initOverlayWindow()
        isOverlayShowing = true
    }

    private fun startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "CoinHunter Floating Bar",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active overlay control bar for CoinHunter auto-scroller"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CoinHunter Floating Controller")
            .setContentText("Auto-scroller floating bar is active on screen")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOverlayWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 200
        }

        floatingView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingOverlayService)
            setViewTreeSavedStateRegistryOwner(this@FloatingOverlayService)
            setViewTreeViewModelStoreOwner(this@FloatingOverlayService)

            setContent {
                MyApplicationTheme(darkTheme = true) {
                    FloatingBarRoot(
                        onDragDelta = { dx, dy ->
                            windowParams.x += dx.toInt()
                            windowParams.y += dy.toInt()
                            windowManager?.updateViewLayout(this@apply, windowParams)
                        },
                        onCloseRequested = {
                            CoinHunterAccessibilityService.instance?.stopAutoScroll()
                            stopSelf()
                        },
                        onOpenAppRequested = {
                            val launchIntent = Intent(this@FloatingOverlayService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            startActivity(launchIntent)
                        },
                        preferences = appPreferences
                    )
                }
            }
        }

        try {
            windowManager?.addView(floatingView, windowParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        isOverlayShowing = false
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()

        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        floatingView = null
        super.onDestroy()
    }
}

@Composable
fun FloatingBarRoot(
    onDragDelta: (Float, Float) -> Unit,
    onCloseRequested: () -> Unit,
    onOpenAppRequested: () -> Unit,
    preferences: AppPreferences
) {
    var isMinimized by remember { mutableStateOf(false) }

    val isAutoScrolling by CoinHunterAccessibilityService.isAutoScrollingFlow.collectAsState()
    val countdown by CoinHunterAccessibilityService.countdownFlow.collectAsState()
    val serviceConnected by CoinHunterAccessibilityService.serviceConnectedFlow.collectAsState()

    val currentInterval by preferences.intervalFlow.collectAsState()
    val currentDirection by preferences.directionFlow.collectAsState()

    if (isMinimized) {
        // Minimized Bubble Pill
        MinimizedBubble(
            isAutoScrolling = isAutoScrolling,
            countdown = countdown,
            onExpand = { isMinimized = false },
            onDragDelta = onDragDelta,
            onToggleScroll = {
                CoinHunterAccessibilityService.instance?.toggleAutoScroll()
            }
        )
    } else {
        // Full Floating Bar matching design model
        ExpandedFloatingBar(
            isAutoScrolling = isAutoScrolling,
            countdown = countdown,
            serviceConnected = serviceConnected,
            interval = currentInterval,
            direction = currentDirection,
            onToggleScroll = {
                val acc = CoinHunterAccessibilityService.instance
                if (acc != null) {
                    acc.toggleAutoScroll()
                } else {
                    onOpenAppRequested()
                }
            },
            onToggleDirection = {
                preferences.swipeDirection = currentDirection.next()
            },
            onDecreaseInterval = {
                if (currentInterval > 1) {
                    val newInterval = currentInterval - 1
                    preferences.intervalSeconds = newInterval
                    if (preferences.keepAsDefault) {
                        preferences.keepAsDefault = false
                    }
                }
            },
            onIncreaseInterval = {
                if (currentInterval < 60) {
                    val newInterval = currentInterval + 1
                    preferences.intervalSeconds = newInterval
                    if (preferences.keepAsDefault) {
                        preferences.keepAsDefault = false
                    }
                }
            },
            onNextSwipe = {
                CoinHunterAccessibilityService.instance?.triggerSingleSwipe()
            },
            onMinimize = { isMinimized = true },
            onClose = onCloseRequested,
            onOpenApp = onOpenAppRequested,
            onDragDelta = onDragDelta
        )
    }
}

@Composable
fun MinimizedBubble(
    isAutoScrolling: Boolean,
    countdown: Int,
    onExpand: () -> Unit,
    onDragDelta: (Float, Float) -> Unit,
    onToggleScroll: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .shadow(16.dp, CircleShape)
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isAutoScrolling) listOf(Color(0xFF7C4DFF), Color(0xFF651FFF))
                    else listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                )
            )
            .border(
                width = 2.dp,
                color = if (isAutoScrolling) Color(0xFF00E676) else Color(0xFF334155),
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragDelta(dragAmount.x, dragAmount.y)
                }
            }
            .clickable { onExpand() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isAutoScrolling) {
                Text(
                    text = "${countdown}s",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676))
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Resume",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun ExpandedFloatingBar(
    isAutoScrolling: Boolean,
    countdown: Int,
    serviceConnected: Boolean,
    interval: Int,
    direction: SwipeDirection,
    onToggleScroll: () -> Unit,
    onToggleDirection: () -> Unit,
    onDecreaseInterval: () -> Unit,
    onIncreaseInterval: () -> Unit,
    onNextSwipe: () -> Unit,
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    onOpenApp: () -> Unit,
    onDragDelta: (Float, Float) -> Unit
) {
    Surface(
        modifier = Modifier
            .width(310.dp)
            .shadow(20.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .border(1.5.dp, Color(0xFF7C4DFF).copy(alpha = 0.4f), RoundedCornerShape(28.dp)),
        color = Color(0xFF181D2E),
        tonalElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Drag Handle, Title "ReelsHunter", Minimize, Close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDragDelta(dragAmount.x, dragAmount.y)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Drag handle Pill
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF64748B))
                )

                // Title Clickable to open app
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenApp() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .border(1.dp, if (isAutoScrolling) Color(0xFF00E676) else Color(0xFF7C4DFF), CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_logo),
                            contentDescription = "ReelsHunter Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ReelsHunter",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                // Window action icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Minimize
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.UnfoldLess,
                            contentDescription = "Minimize",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // Close
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Controls Row: [Play/Pause] [Direction Selector] [- 8s +] [Instant Next]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Big Circular Play/Pause button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                if (isAutoScrolling) listOf(Color(0xFF7C4DFF), Color(0xFF651FFF))
                                else listOf(Color(0xFF00E676), Color(0xFF00C853))
                            )
                        )
                        .clickable { onToggleScroll() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isAutoScrolling) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${countdown}s",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color(0xFF003816),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // 2. Circular Direction / Gesture Selector Button (Cycles Down -> Up -> Right -> Left)
                val (directionIcon, directionLabel) = when (direction) {
                    SwipeDirection.DOWN -> Icons.Default.ArrowDownward to "Down (↓)"
                    SwipeDirection.UP -> Icons.Default.ArrowUpward to "Up (↑)"
                    SwipeDirection.RIGHT -> Icons.Default.ArrowForward to "Right (→)"
                    SwipeDirection.LEFT -> Icons.AutoMirrored.Filled.ArrowBack to "Left (←)"
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF263045))
                        .border(1.2.dp, CyanAccent.copy(alpha = 0.55f), CircleShape)
                        .clickable { onToggleDirection() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = directionIcon,
                        contentDescription = "Direction: $directionLabel",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 3. Interval Adjustment Pill [ - 4s + ]
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF263045))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDecreaseInterval,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = "${interval}s",
                        color = Color(0xFF69F0AE),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                    IconButton(
                        onClick = onIncreaseInterval,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // 4. Instant Next / 1x Swipe Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C4DFF).copy(alpha = 0.25f))
                        .border(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.6f), CircleShape)
                        .clickable { onNextSwipe() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Reel",
                        tint = Color(0xFFB388FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
