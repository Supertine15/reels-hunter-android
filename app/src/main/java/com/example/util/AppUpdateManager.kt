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
    val versionCode: Int,
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
        currentVersionCode: Int = BuildConfig.VERSION_CODE,
        currentVersionName: String = BuildConfig.VERSION_NAME
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
                    var firstApkAssetName = ""
                    val assetsArray = json.optJSONArray("assets")
                    if (assetsArray != null) {
                        for (i in 0 until assetsArray.length()) {
                            val asset = assetsArray.getJSONObject(i)
                            val assetName = asset.optString("name", "")
                            if (assetName.endsWith(".apk", ignoreCase = true)) {
                                if (apkDownloadUrl == null) {
                                    apkDownloadUrl = asset.optString("browser_download_url", null)
                                    firstApkAssetName = assetName
                                }
                            }
                        }
                    }

                    val cleanReleaseVersion = extractCleanVersionName(tagName.ifEmpty { releaseTitle })
                    val remoteVersionCode = extractVersionCode(
                        tagName = tagName,
                        releaseTitle = releaseTitle,
                        releaseNotes = rawReleaseNotes,
                        assetName = firstApkAssetName,
                        versionName = cleanReleaseVersion
                    )

                    val isNewer = isRemoteVersionNewer(
                        remoteVersionName = cleanReleaseVersion,
                        remoteVersionCode = remoteVersionCode,
                        currentVersionName = currentVersionName,
                        currentVersionCode = currentVersionCode
                    )

                    val cleanedNotes = cleanReleaseNotes(rawReleaseNotes)

                    val updateInfo = UpdateInfo(
                        tagName = tagName,
                        versionName = cleanReleaseVersion.ifEmpty { "1.0.2" },
                        versionCode = remoteVersionCode,
                        releaseTitle = releaseTitle,
                        releaseNotes = cleanedNotes,
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
     * Strips prefixes and extracts clean semver strings:
     * "easy-scroll-v1.0.2" -> "1.0.2"
     * "v1.0.1" -> "1.0.1"
     * "Release 1.0.3" -> "1.0.3"
     */
    fun extractCleanVersionName(raw: String): String {
        if (raw.isBlank()) return ""
        val withoutPrefix = raw
            .replace(Regex("""(?i)^easy[-_]?scroll[-_]?v?"""), "")
            .replace(Regex("""(?i)^reels[-_]?hunter[-_]?v?"""), "")
            .replace(Regex("""(?i)^release[-_]?v?"""), "")
            .replace(Regex("""(?i)^version\s*"""), "")
            .trim()
            .removePrefix("v")
            .removePrefix("V")

        val semverRegex = Regex("""(\d+(?:\.\d+)+)""")
        val semverMatch = semverRegex.find(withoutPrefix)
        if (semverMatch != null) {
            return semverMatch.value
        }

        val singleNumberRegex = Regex("""\d+""")
        return singleNumberRegex.find(withoutPrefix)?.value ?: withoutPrefix
    }

    /**
     * Extracts integer versionCode from release metadata, tags, or version parts.
     */
    fun extractVersionCode(
        tagName: String,
        releaseTitle: String,
        releaseNotes: String,
        assetName: String = "",
        versionName: String = ""
    ): Int {
        // 1. Check for explicit versionCode in release notes or title: e.g. "versionCode: 3"
        val explicitCodeRegex = Regex("""(?i)(?:versioncode|version_code|build|code)\s*[:=]\s*(\d+)""")
        explicitCodeRegex.find(releaseNotes)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        explicitCodeRegex.find(releaseTitle)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }

        // 2. Check for explicit versionCode in asset name: e.g. "easy-scroll-vc3.apk", "app-v2.apk"
        if (assetName.isNotBlank()) {
            val assetCodeRegex = Regex("""(?i)[-_](?:vc|c|build|v)?(\d+)\.apk""")
            assetCodeRegex.find(assetName)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        }

        // 3. Check if tagName is a single integer like "v3" or "3"
        val cleanTag = tagName.trim().removePrefix("v").removePrefix("V")
        if (cleanTag.isNotEmpty() && cleanTag.all { it.isDigit() }) {
            cleanTag.toIntOrNull()?.let { return it }
        }

        // 4. Calculate composite integer from semver parts (e.g. 1.0.1 -> 10001, 1.0.2 -> 10002)
        val cleanName = if (versionName.isNotBlank()) versionName else extractCleanVersionName(tagName)
        val parts = cleanName.split('.').mapNotNull { it.toIntOrNull() }
        if (parts.isNotEmpty()) {
            val major = parts.getOrElse(0) { 0 }
            val minor = parts.getOrElse(1) { 0 }
            val patch = parts.getOrElse(2) { 0 }
            return major * 10000 + minor * 100 + patch
        }

        return 0
    }

    /**
     * Strictly compares version codes and numeric semver components.
     * Returns true ONLY if the remote version is strictly newer.
     */
    fun isRemoteVersionNewer(
        remoteVersionName: String,
        remoteVersionCode: Int,
        currentVersionName: String,
        currentVersionCode: Int
    ): Boolean {
        if (remoteVersionName.isBlank() && remoteVersionCode <= 0) return false

        val cleanRemote = extractCleanVersionName(remoteVersionName)
        val cleanCurrent = extractCleanVersionName(currentVersionName)

        // Equal version names means it is identical
        if (cleanRemote.equals(cleanCurrent, ignoreCase = true) && cleanRemote.isNotBlank()) {
            return false
        }

        val remoteParts = cleanRemote.split('.').mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split('.').mapNotNull { it.toIntOrNull() }

        if (remoteParts.isNotEmpty() && currentParts.isNotEmpty()) {
            val maxLen = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            return false
        }

        // Fallback to integer versionCode comparison if semver not present
        if (remoteVersionCode > 0 && currentVersionCode > 0) {
            return remoteVersionCode > currentVersionCode
        }

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
