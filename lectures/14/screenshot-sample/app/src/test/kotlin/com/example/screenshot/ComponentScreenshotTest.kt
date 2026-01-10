package com.example.screenshot

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

/**
 * Screenshot tests using Paparazzi.
 *
 * Paparazzi renders Compose/View UI to images WITHOUT an emulator or device.
 * This enables:
 * - Visual regression testing in CI
 * - Faster test execution
 * - Consistent rendering across environments
 *
 * Usage:
 * - ./gradlew recordPaparazziDebug  - Record golden images
 * - ./gradlew verifyPaparazziDebug  - Compare against golden images
 *
 * Golden images are stored in: app/src/test/snapshots/
 */
class ComponentScreenshotTest {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_5,
    showSystemUi = false
  )

  // ============================================
  // ProfileCard Screenshots
  // ============================================

  @Test
  fun profileCard_default() {
    paparazzi.snapshot {
      MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
          ProfileCard(
            name = "John Doe",
            email = "john@example.com"
          )
        }
      }
    }
  }

  @Test
  fun profileCard_longName() {
    paparazzi.snapshot {
      MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
          ProfileCard(
            name = "Alexander Christopher Johnson III",
            email = "alexander.christopher.johnson@verylongdomain.com"
          )
        }
      }
    }
  }

  @Test
  fun profileCard_customColor() {
    paparazzi.snapshot {
      MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
          ProfileCard(
            name = "Custom",
            email = "custom@test.com",
            avatarColor = Color(0xFFE91E63)
          )
        }
      }
    }
  }

  // ============================================
  // StatCard Screenshots
  // ============================================

  @Test
  fun statCard_green() {
    paparazzi.snapshot {
      MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
          StatCard(
            title = "COMPLETED",
            value = "42",
            subtitle = "tasks done",
            color = Color(0xFF4CAF50)
          )
        }
      }
    }
  }

  @Test
  fun statCard_orange() {
    paparazzi.snapshot {
      MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
          StatCard(
            title = "PENDING",
            value = "7",
            subtitle = "in progress",
            color = Color(0xFFFF9800)
          )
        }
      }
    }
  }

  @Test
  fun statCard_largeNumber() {
    paparazzi.snapshot {
      MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
          StatCard(
            title = "TOTAL",
            value = "1,234",
            subtitle = "all time"
          )
        }
      }
    }
  }

  // ============================================
  // ActionButton Screenshots
  // ============================================

  @Test
  fun actionButton_enabled() {
    paparazzi.snapshot {
      MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
          ActionButton(
            text = "Submit",
            onClick = { }
          )
        }
      }
    }
  }

  @Test
  fun actionButton_disabled() {
    paparazzi.snapshot {
      MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
          ActionButton(
            text = "Submit",
            onClick = { },
            enabled = false
          )
        }
      }
    }
  }

  @Test
  fun actionButton_loading() {
    paparazzi.snapshot {
      MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
          ActionButton(
            text = "Processing...",
            onClick = { },
            isLoading = true
          )
        }
      }
    }
  }

  // ============================================
  // EmptyState Screenshots
  // ============================================

  @Test
  fun emptyState_default() {
    paparazzi.snapshot {
      MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
          EmptyState(
            title = "No items yet",
            description = "Add your first item to get started"
          )
        }
      }
    }
  }

  // ============================================
  // Full Screen Composition
  // ============================================

  @Test
  fun demoScreen_full() {
    paparazzi.snapshot {
      MaterialTheme {
        DemoScreen()
      }
    }
  }
}

/**
 * Test different device configurations.
 */
class DeviceConfigScreenshotTest {

  @get:Rule
  val paparazziPhone = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_5
  )

  @Test
  fun profileCard_phone() {
    paparazziPhone.snapshot {
      MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
          ProfileCard(
            name = "Phone Test",
            email = "phone@test.com"
          )
        }
      }
    }
  }
}
