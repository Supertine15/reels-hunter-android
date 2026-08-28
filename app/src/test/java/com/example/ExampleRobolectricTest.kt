package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.AppUpdateManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Easy Scroll", appName)
  }

  @Test
  fun `test version extraction and comparison`() {
    assertEquals("1.0.1", AppUpdateManager.extractCleanVersionName("easy-scroll-v1.0.1"))
    assertEquals("1.0.2", AppUpdateManager.extractCleanVersionName("v1.0.2"))
    assertEquals("2.0.0", AppUpdateManager.extractCleanVersionName("Release 2.0.0"))

    // Equal versions should not trigger update
    assertFalse(AppUpdateManager.isRemoteVersionNewer("1.0.1", 2, "1.0.1", 2))
    assertFalse(AppUpdateManager.isRemoteVersionNewer("1.0.0", 1, "1.0.1", 2))
    assertFalse(AppUpdateManager.isRemoteVersionNewer("1.0", 1, "1.0.1", 2))

    // Newer version should trigger update
    assertTrue(AppUpdateManager.isRemoteVersionNewer("1.0.2", 3, "1.0.1", 2))
    assertTrue(AppUpdateManager.isRemoteVersionNewer("2.0.0", 10, "1.0.1", 2))
  }

  @Test
  fun `test versionCode extraction`() {
    val codeFromNotes = AppUpdateManager.extractVersionCode(
      tagName = "v1.0.2",
      releaseTitle = "Easy Scroll Update",
      releaseNotes = "versionCode: 3\nBug fixes and improvements",
      assetName = "easy-scroll-vc3.apk"
    )
    assertEquals(3, codeFromNotes)
  }

  @Test
  fun `test clean release notes`() {
    val rawGhActionMarkdown = """
      ## App Name: Easy Scroll
      Automatically generated build by GitHub Actions.
      * **Run number**: 15
      * **Source commit**: abc123
      * **Artifact**: `easy-scroll-debug-apk`
    """.trimIndent()

    val cleaned = AppUpdateManager.cleanReleaseNotes(rawGhActionMarkdown)
    assertTrue(cleaned.contains("Performance improvements"))
    assertFalse(cleaned.contains("Run number"))
    assertFalse(cleaned.contains("Source commit"))
  }
}
