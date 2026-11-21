package com.w2sv.widget.ui.actions

import android.content.Context
import android.content.Intent
import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import com.w2sv.widget.di.glanceWidgetEntryPoint

internal fun WifiPropertyPresentation.clickIntent(context: Context): Intent? =
    when (resolutionError) {
        WifiPropertyResolutionError.NoLocationAccessPermission,
        WifiPropertyResolutionError.GpsDisabled -> locationAccessActivityIntent(context)
        else -> copyIntent(context)
    }

internal fun WifiPropertyPresentation.copyIntent(context: Context): Intent? {
    if (resolutionError != null || value.isBlank()) return null

    val propertyLabel = label.run { subscript?.let { "$text $it" } ?: text }

    return CopyPropertyToClipboardActivity.Property
        .intent(propertyLabel, value)
        .setClass(context, CopyPropertyToClipboardActivity::class.java)
}
