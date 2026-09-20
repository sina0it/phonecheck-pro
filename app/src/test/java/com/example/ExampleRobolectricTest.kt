package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.DeviceDiagnosticsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PhoneCheck Pro", appName)
  }

  @Test
  fun `repository safe extraction does not crash on robolectric environment`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = DeviceDiagnosticsRepository(context)

    val identity = repo.getDeviceIdentity()
    assertNotNull(identity)
    assertNotNull(identity.brand)

    val battery = repo.getBatteryInfo()
    assertNotNull(battery)
    assertTrue(battery.levelPercent in 0..100)

    val storage = repo.getStorageInfo()
    assertNotNull(storage)

    val memory = repo.getMemoryInfo()
    assertNotNull(memory)

    val connectivity = repo.getConnectivitySpecs()
    assertNotNull(connectivity)

    val complete = repo.getCompleteDeviceInfo()
    assertNotNull(complete)
  }
}
