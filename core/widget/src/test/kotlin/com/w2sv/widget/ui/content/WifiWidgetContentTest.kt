package com.w2sv.widget.ui.content

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.assertHasClickAction
import androidx.glance.testing.unit.hasContentDescriptionEqualTo
import androidx.glance.testing.unit.hasTextEqualTo
import androidx.test.core.app.ApplicationProvider
import com.w2sv.core.common.R
import com.w2sv.widget.ui.preview.connectedWifiWidgetPreviewState
import com.w2sv.widget.ui.preview.disabledWifiWidgetPreviewState
import com.w2sv.widget.ui.preview.notConnectedWifiWidgetPreviewState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiWidgetContentTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun smallWidget_displaysConnectedContentAndUtilities() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(DpSize(160.dp, 100.dp))

            provideComposable {
                WifiWidgetContent(connectedWifiWidgetPreviewState())
            }

            onNode(hasTextEqualTo("Coffee Shop WiFi")).assertExists()
            onNode(hasContentDescriptionEqualTo(context.getString(R.string.refresh_data)))
                .assertExists()
                .assertHasClickAction()
        }

    @Test
    fun largeWidget_displaysAllPreviewProperties() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(DpSize(320.dp, 220.dp))

            provideComposable {
                WifiWidgetContent(connectedWifiWidgetPreviewState())
            }

            onNode(hasTextEqualTo("Coffee Shop WiFi")).assertExists()
            onNode(hasTextEqualTo("203.0.113.24")).assertExists()
            onNode(hasTextEqualTo("192.168.1.1")).assertExists()
        }

    @Test
    fun disabledWifiWidget_displaysDisabledStatus() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(DpSize(250.dp, 140.dp))

            provideComposable {
                WifiWidgetContent(disabledWifiWidgetPreviewState())
            }

            onNode(hasTextEqualTo(context.getString(R.string.wifi_disabled))).assertExists()
        }

    @Test
    fun notConnectedWifiWidget_displaysNoConnectionStatus() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(DpSize(250.dp, 140.dp))

            provideComposable {
                WifiWidgetContent(notConnectedWifiWidgetPreviewState())
            }

            onNode(hasTextEqualTo(context.getString(R.string.no_wifi_connection))).assertExists()
        }
}
