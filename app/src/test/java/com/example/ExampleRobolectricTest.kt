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
    assertEquals("1.0.1", AppUpdateManager.extractVersionString("easy-scroll-v1.0.1"))
    assertEquals("1.0.2", AppUpdateManager.extractVersionString("v1.0.2"))
    assertEquals("2.0.0", AppUpdateManager.extractVersionString("Release 2.0.0"))

    // Equal versions should not trigger update
    assertFalse(AppUpdateManager.isRemoteVersionNewer("1.0.1", "1.0.1"))
    assertFalse(AppUpdateManager.isRemoteVersionNewer("1.0.0", "1.0.1"))
    assertFalse(AppUpdateManager.isRemoteVersionNewer("1.0", "1.0.1"))

    // Newer version should trigger update
    assertTrue(AppUpdateManager.isRemoteVersionNewer("1.0.2", "1.0.1"))
    assertTrue(AppUpdateManager.isRemoteVersionNewer("2.0.0", "1.0.1"))
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
