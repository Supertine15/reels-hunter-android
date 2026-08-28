package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RadarGreen
import com.example.util.AppConstants
import com.example.util.AppUpdateManager
import com.example.util.UpdateCheckResult

@Composable
fun UpdateDialog(
    result: UpdateCheckResult,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    if (result is UpdateCheckResult.Idle) return

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (result) {
                    is UpdateCheckResult.Checking -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = RadarGreen,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Checking Updates...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    is UpdateCheckResult.UpdateAvailable -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(RadarGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = RadarGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Update Available",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    is UpdateCheckResult.LatestVersion -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(RadarGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = RadarGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "You're Up to Date",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    is UpdateCheckResult.Error -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AmberWarning.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "GitHub & Updates",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    else -> {}
                }
            }
        },
        text = {
            when (result) {
                is UpdateCheckResult.Checking -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Connecting to GitHub repository...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = AppConstants.GITHUB_LATEST_RELEASE_API,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                is UpdateCheckResult.UpdateAvailable -> {
                    val update = result.updateInfo
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(RadarGreen.copy(alpha = 0.1f))
                                .border(1.dp, RadarGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "New: ${update.tagName.ifEmpty { update.versionName }}",
                                fontWeight = FontWeight.Bold,
                                color = RadarGreen,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Easy Scroll",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (update.releaseTitle.isNotBlank()) {
                            Text(
                                text = update.releaseTitle,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = update.releaseNotes,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
                is UpdateCheckResult.LatestVersion -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Easy Scroll is currently running the latest version (v${result.currentVersion}).",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "GitHub Repository: ${AppConstants.GITHUB_REPO_URL}",
                            fontSize = 11.sp,
                            color = PurpleLight
                        )
                    }
                }
                is UpdateCheckResult.Error -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Could not fetch release data automatically (${result.message}).",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You can visit the GitHub repository directly to check for manual releases and APK downloads.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                else -> {}
            }
        },
        confirmButton = {
            when (result) {
                is UpdateCheckResult.UpdateAvailable -> {
                    Button(
                        onClick = {
                            val downloadUrl = result.updateInfo.apkDownloadUrl ?: result.updateInfo.htmlUrl
                            AppUpdateManager.openUrl(context, downloadUrl)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RadarGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color(0xFF003816), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Get APK Update", color = Color(0xFF003816), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                is UpdateCheckResult.LatestVersion -> {
                    Button(
                        onClick = {
                            AppUpdateManager.openUrl(context, AppConstants.GITHUB_REPO_URL)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Visit GitHub", fontSize = 13.sp)
                    }
                }
                is UpdateCheckResult.Error -> {
                    Button(
                        onClick = {
                            AppUpdateManager.openUrl(context, AppConstants.GITHUB_REPO_URL)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Repository", fontSize = 13.sp)
                    }
                }
                else -> {}
            }
        },
        dismissButton = {
            when (result) {
                is UpdateCheckResult.UpdateAvailable -> {
                    OutlinedButton(
                        onClick = {
                            AppUpdateManager.openUrl(context, AppConstants.GITHUB_REPO_URL)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("View on GitHub", fontSize = 12.sp)
                    }
                }
                is UpdateCheckResult.Checking -> {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
                else -> {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    )
}
