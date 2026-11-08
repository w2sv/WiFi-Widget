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
    @get:Rule val paparazzi = Paparazzi(
        deviceConfig = PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar",
        renderingMode = RenderingMode.SHRINK,
        appCompatEnabled = false
    )

    @Test fun `normal connected widget`() =
        snapshot(R.layout.glance_widget_preview_connected_medium, "ConnectedMediumPreview", 250, 140)

    @Test fun `left aligned connected widget`() =
        snapshot(R.layout.glance_widget_preview_connected_left_aligned, "ConnectedLeftAlignedPreview", 250, 140)

    @Test fun `normal disconnected widget`() =
        snapshot(R.layout.glance_widget_preview_disabled, "DisabledPreview", 250, 140)

    @Test fun `compact disconnected widget`() =
        snapshot(R.layout.glance_widget_preview_compact_disabled, "CompactDisabledPreview", 160, 100)

    @Test fun `minimum size disconnected widget`() =
        snapshot(R.layout.glance_widget_preview_tiny_disabled, "TinyDisabledPreview", 100, 40)

    @Test fun `minimum height disconnected widget`() =
        snapshot(R.layout.glance_widget_preview_short_disabled, "ShortDisabledPreview", 250, 40)

    private fun snapshot(
        layoutId: Int,
        previewName: String,
        widthDp: Int,
        heightDp: Int
    ) {
        val view = paparazzi.inflate<View>(layoutId)
        val density = paparazzi.context.resources.displayMetrics.density
        val width = (widthDp * density).toInt()
        val height = (heightDp * density).toInt()
        val root = FrameLayout(paparazzi.context).apply {
            layoutParams = LayoutParams(width, height)
            addView(view, LayoutParams(width, height))
        }
        paparazzi.snapshot(root, name = previewName)
    }
}
