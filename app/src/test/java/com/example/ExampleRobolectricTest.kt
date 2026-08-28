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
  fun `test regex version extraction`() {
    assertEquals("13.1", AppUpdateManager.extractNumericVersion("easy-scroll-v13.1"))
    assertEquals("13", AppUpdateManager.extractNumericVersion("easy-scroll-v13"))
    assertEquals("12.1", AppUpdateManager.extractNumericVersion("easy-scroll-v12.1"))
    assertEquals("1.0.1", AppUpdateManager.extractNumericVersion("v1.0.1"))
    assertEquals("2.3.4", AppUpdateManager.extractNumericVersion("Release-v2.3.4"))
  }

  @Test
  fun `test semantic version comparison`() {
    // Equal versions should NOT trigger update
    assertFalse(AppUpdateManager.isRemoteVersionNewer("easy-scroll-v13.1", "13.1"))
    assertFalse(AppUpdateManager.isRemoteVersionNewer("13.1", "13.1"))
    assertFalse(AppUpdateManager.isRemoteVersionNewer("13", "13.1"))
    assertFalse(AppUpdateManager.isRemoteVersionNewer("13.0", "13.1"))

    // Older remote version should NOT trigger update
    assertFalse(AppUpdateManager.isRemoteVersionNewer("easy-scroll-v12", "13.1"))
    assertFalse(AppUpdateManager.isRemoteVersionNewer("1.0.0", "13.1"))

    // Newer remote version SHOULD trigger update
    assertTrue(AppUpdateManager.isRemoteVersionNewer("easy-scroll-v13.2", "13.1"))
    assertTrue(AppUpdateManager.isRemoteVersionNewer("easy-scroll-v14", "13.1"))
    assertTrue(AppUpdateManager.isRemoteVersionNewer("14.0", "13.1"))
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
