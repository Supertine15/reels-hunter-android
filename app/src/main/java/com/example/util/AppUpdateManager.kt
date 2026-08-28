package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val tagName: String,
    val versionName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val apkDownloadUrl: String?,
    val htmlUrl: String,
    val isNewer: Boolean
)

sealed class UpdateCheckResult {
    object Idle : UpdateCheckResult()
    object Checking : UpdateCheckResult()
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UpdateCheckResult()
    data class LatestVersion(val currentVersion: String, val updateInfo: UpdateInfo?) : UpdateCheckResult()
    data class Error(val message: String, val fallbackUrl: String) : UpdateCheckResult()
}

object AppUpdateManager {

    private const val TAG = "AppUpdateManager"

    /**
     * Queries the official GitHub latest release API endpoint:
     * https://api.github.com/repos/Supertine15/reels-hunter-android/releases/latest
     */
    suspend fun checkForUpdates(currentVersionName: String = BuildConfig.VERSION_NAME): UpdateCheckResult {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(AppConstants.GITHUB_LATEST_RELEASE_API)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "EasyScroll-Android-App")
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val json = JSONObject(response)
                    val tagName = json.optString("tag_name", "")
                    val releaseTitle = json.optString("name", "New Release")
                    val releaseNotes = json.optString("body", "No release notes provided.")
                    val htmlUrl = json.optString("html_url", AppConstants.GITHUB_REPO_URL)

                    // Find APK asset in assets list if available
                    var apkDownloadUrl: String? = null
                    val assetsArray = json.optJSONArray("assets")
                    if (assetsArray != null) {
                        for (i in 0 until assetsArray.length()) {
                            val asset = assetsArray.getJSONObject(i)
                            val assetName = asset.optString("name", "")
                            if (assetName.endsWith(".apk", ignoreCase = true)) {
                                apkDownloadUrl = asset.optString("browser_download_url", null)
                                break
                            }
                        }
                    }

                    val cleanReleaseVersion = extractVersionString(tagName.ifEmpty { releaseTitle })
                    val isNewer = isRemoteVersionNewer(cleanReleaseVersion, currentVersionName)

                    val updateInfo = UpdateInfo(
                        tagName = tagName,
                        versionName = cleanReleaseVersion.ifEmpty { tagName },
                        releaseTitle = releaseTitle,
                        releaseNotes = releaseNotes,
                        apkDownloadUrl = apkDownloadUrl ?: htmlUrl,
                        htmlUrl = htmlUrl,
                        isNewer = isNewer
                    )

                    if (isNewer) {
                        UpdateCheckResult.UpdateAvailable(updateInfo)
                    } else {
                        UpdateCheckResult.LatestVersion(currentVersionName, updateInfo)
                    }
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    // No releases published yet on GitHub repo
                    UpdateCheckResult.LatestVersion(
                        currentVersion = currentVersionName,
                        updateInfo = null
                    )
                } else {
                    UpdateCheckResult.Error(
                        message = "GitHub API returned status $responseCode",
                        fallbackUrl = AppConstants.GITHUB_REPO_URL
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check for updates from GitHub", e)
                UpdateCheckResult.Error(
                    message = e.localizedMessage ?: "Unable to connect to GitHub",
                    fallbackUrl = AppConstants.GITHUB_REPO_URL
                )
            } finally {
                connection?.disconnect()
            }
        }
    }

    /**
     * Extracts version numbers (e.g. "1.0.2" from "easy-scroll-v1.0.2" or "v1.0.2")
     */
    fun extractVersionString(raw: String): String {
        val regex = Regex("""\d+(\.\d+)+""")
        return regex.find(raw)?.value ?: raw.replace(Regex("""[^0-9.]"""), "").trim('.')
    }

    /**
     * Determines whether the remote version is strictly newer than current version.
     */
    fun isRemoteVersionNewer(remote: String, current: String): Boolean {
        if (remote.isBlank()) return false
        val remoteParts = remote.split('.').mapNotNull { it.toIntOrNull() }
        val currentParts = current.split('.').mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    /**
     * Opens the direct APK download link or GitHub repository release page in browser.
     */
    fun openUrl(context: Context, urlString: String) {
        val safeUrl = if (urlString.isNotBlank()) urlString else AppConstants.GITHUB_REPO_URL
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open URL: $safeUrl", e)
        }
    }
}
