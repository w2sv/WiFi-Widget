package com.w2sv.widget

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsNull
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class WidgetTest {
    private lateinit var device: UiDevice

    @Before
    fun goToHomeScreen() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            .apply {
                pressHome()

                // Wait for launcher
                MatcherAssert.assertThat(launcherPackageName, IsNull.notNullValue())
                wait(
                    Until.hasObject(By.pkg(launcherPackageName).depth(0)),
                    LAUNCH_TIMEOUT
                )
            }
    }

    @Test
    fun refreshButton() {
        with(device.findObject(UiSelector().description("Refresh data"))) {
            Assert.assertTrue(isClickable)
            click()
        }
    }

    @Test
    fun configureWidgetButton() {
        with(device.findObject(UiSelector().description("Configure Widget"))) {
            Assert.assertTrue(isClickable)

            // check that app opens on click
            click()
            device.wait(
                Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)),
                LAUNCH_TIMEOUT
            )
        }
    }

    companion object {
        private const val LAUNCH_TIMEOUT = 5000L
        private const val PACKAGE_NAME = "com.w2sv.wifiwidget.debug"
    }
}