package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("नर्मदा जन्मोत्सव", appName)
  }

  @Test
  fun `verify secure sha256 token generation`() {
    val sampleInput = "NBF27-000001|PAS27-000001|9826012345"
    val bytes = MessageDigest.getInstance("SHA-256").digest(sampleInput.toByteArray(Charsets.UTF_8))
    val token = bytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
    assertEquals(16, token.length)
    assertTrue(token.matches(Regex("[0-9A-F]{16}")))
  }
}
