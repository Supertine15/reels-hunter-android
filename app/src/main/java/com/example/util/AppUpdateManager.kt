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
    suspend fun checkForUpdates(
        installedVersion: String = BuildConfig.VERSION_NAME
    ): UpdateCheckResult {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(AppConstants.GITHUB_LATEST_RELEASE_API)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
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
                    val rawReleaseNotes = json.optString("body", "")
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

                    // Extract strictly numeric version from tag or title
                    val remoteVersionStr = extractNumericVersion(tagName.ifEmpty { releaseTitle })
                    val isNewer = isRemoteVersionNewer(
                        remoteVersionStr = remoteVersionStr,
                        installedVersionStr = installedVersion
                    )

                    val cleanedNotes = cleanReleaseNotes(rawReleaseNotes)

                    val updateInfo = UpdateInfo(
                        tagName = tagName,
                        versionName = remoteVersionStr.ifEmpty { tagName },
                        releaseTitle = releaseTitle,
                        releaseNotes = cleanedNotes,
                        apkDownloadUrl = apkDownloadUrl ?: htmlUrl,
                        htmlUrl = htmlUrl,
                        isNewer = isNewer
                    )

                    if (isNewer) {
                        UpdateCheckResult.UpdateAvailable(updateInfo)
                    } else {
                        UpdateCheckResult.LatestVersion(installedVersion, updateInfo)
                    }
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    // No releases published yet on GitHub repo
                    UpdateCheckResult.LatestVersion(
                        currentVersion = installedVersion,
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
     * Uses Regex `\d+(\.\d+)*` to extract ONLY the numeric version string.
     * Examples:
     * - "easy-scroll-v12" -> "12"
     * - "easy-scroll-v12.1" -> "12.1"
     * - "v1.0.1" -> "1.0.1"
     * - "Release 2.3.4" -> "2.3.4"
     */
    fun extractNumericVersion(raw: String): String {
        if (raw.isBlank()) return ""
        val regex = Regex("""\d+(\.\d+)*""")
        return regex.find(raw)?.value ?: ""
    }

    /**
     * Parses numeric version string into integer parts list.
     * Examples:
     * - "12" -> [12]
     * - "12.1" -> [12, 1]
     * - "1.0.1" -> [1, 0, 1]
     */
    fun parseVersionParts(versionStr: String): List<Int> {
        val clean = extractNumericVersion(versionStr)
        if (clean.isBlank()) return emptyList()
        return clean.split('.').mapNotNull { it.toIntOrNull() }
    }

    /**
     * Strict Semantic Version comparison.
     * Returns true ONLY if remoteVersion > installedVersion.
     * If remoteVersion <= installedVersion, returns false.
     */
    fun isRemoteVersionNewer(remoteVersionStr: String, installedVersionStr: String): Boolean {
        val remoteParts = parseVersionParts(remoteVersionStr)
        val installedParts = parseVersionParts(installedVersionStr)

        if (remoteParts.isEmpty() || installedParts.isEmpty()) {
            return false
        }

        val maxLength = maxOf(remoteParts.size, installedParts.size)
        for (i in 0 until maxLength) {
            val r = remoteParts.getOrElse(i) { 0 }
            val inst = installedParts.getOrElse(i) { 0 }
            if (r > inst) return true
            if (r < inst) return false
        }

        // Exact equal version means it is NOT newer
        return false
    }

    /**
     * Cleans up GitHub Actions technical metadata from release notes to provide
     * user-friendly changelog text without Markdown artifacts.
     */
    fun cleanReleaseNotes(rawBody: String): String {
        if (rawBody.isBlank()) {
            return "• Performance improvements and smoother auto-scrolling\n• Stability optimizations and bug fixes"
        }

        val ignoredKeywords = listOf(
            "app name",
            "automatically generated",
            "run number",
            "source commit",
            "commit sha",
            "artifact",
            "release asset",
            "release tag",
            "build & release report",
            "github_step_summary",
            "github action",
            "workflow"
        )

        val cleanedLines = rawBody.lines().filter { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@filter false
            if (trimmed.startsWith("---") || trimmed.startsWith("===") || trimmed.startsWith("```")) return@filter false
            val lower = trimmed.lowercase()
            ignoredKeywords.none { lower.contains(it) }
        }.map { line ->
            line.trim()
                .removePrefix("## ")
                .removePrefix("# ")
                .removePrefix("### ")
                .removePrefix("#### ")
                .replace(Regex("""\*\*"""), "")
                .replace(Regex("""`"""), "")
        }

        return if (cleanedLines.isEmpty()) {
            "• Performance improvements and smoother auto-scrolling\n• Stability optimizations and bug fixes"
        } else {
            cleanedLines.joinToString("\n")
        }
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
