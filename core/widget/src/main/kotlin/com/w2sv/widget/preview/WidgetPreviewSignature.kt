package com.w2sv.widget.preview

import android.content.Context
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.widget.ui.theme.resolve

/**
 * Identifies the currently generated launcher preview.
 *
 * Persisting the last successfully published signature avoids redundant publication requests
 * across app restarts, which is important because Android rate-limits preview updates.
 * A new publication is needed when the app version, default colors, or locale changes.
 * See [Android's generated preview documentation](https://developer.android.com/develop/ui/compose/glance/generated-previews).
 */
internal data class WidgetPreviewSignature(val packageLastUpdateTime: Long, val palette: String, val locales: String) {
    companion object {
        fun current(context: Context): WidgetPreviewSignature {
            val packageLastUpdateTime = context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
            val colors = WidgetConfig.default.appearance.coloring.resolve(context)

            return WidgetPreviewSignature(
                packageLastUpdateTime = packageLastUpdateTime,
                palette = "${colors.background}:${colors.primary}:${colors.secondary}",
                locales = context.resources.configuration.locales.toLanguageTags()
            )
        }
    }
}
