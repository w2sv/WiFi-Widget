package com.w2sv.widget.ui

import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_5
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import com.w2sv.core.widget.R
import org.junit.Rule
import org.junit.Test

class WifiGlancePaparazziPreviewTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar",
        renderingMode = RenderingMode.SHRINK,
        appCompatEnabled = false
    )

    @Test
    fun connectedSmall() =
        snapshot(R.layout.glance_widget_preview_connected_small, widthDp = 160, heightDp = 100)

    @Test
    fun connectedMedium() =
        snapshot(R.layout.glance_widget_preview_connected_medium, widthDp = 250, heightDp = 140)

    @Test
    fun connectedLarge() =
        snapshot(R.layout.glance_widget_preview_connected_large, widthDp = 320, heightDp = 220)

    @Test
    fun disabled() =
        snapshot(R.layout.glance_widget_preview_disabled, widthDp = 250, heightDp = 140)

    @Test
    fun notConnected() =
        snapshot(R.layout.glance_widget_preview_not_connected, widthDp = 250, heightDp = 140)

    private fun snapshot(
        layoutId: Int,
        widthDp: Int,
        heightDp: Int
    ) {
        val view = paparazzi.inflate<View>(layoutId)
        val density = paparazzi.context.resources.displayMetrics.density
        val width = (widthDp * density).toInt()
        val height = (heightDp * density).toInt()
        paparazzi.snapshot(
            FrameLayout(paparazzi.context).apply {
                layoutParams = LayoutParams(width, height)
                addView(view, LayoutParams(width, height))
            }
        )
    }
}
