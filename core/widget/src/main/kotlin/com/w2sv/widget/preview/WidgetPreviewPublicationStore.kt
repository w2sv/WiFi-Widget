package com.w2sv.widget.preview

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class WidgetPreviewPublicationStore @Inject constructor(@ApplicationContext context: Context) {
    private val preferences =
        context.getSharedPreferences("widget_previews", Context.MODE_PRIVATE)

    fun signatureWasPublished(signature: WidgetPreviewSignature): Boolean {
        val persistedSignature = WidgetPreviewSignature(
            preferences.getLong(LAST_PUBLISHED_UPDATE_TIME, -1L),
            preferences.getString(LAST_PUBLISHED_PALETTE, null).orEmpty(),
            preferences.getString(LAST_PUBLISHED_LOCALES, null).orEmpty()
        )
        return persistedSignature == signature
    }

    fun recordPublished(signature: WidgetPreviewSignature) {
        preferences.edit {
            putLong(LAST_PUBLISHED_UPDATE_TIME, signature.packageLastUpdateTime)
            putString(LAST_PUBLISHED_PALETTE, signature.palette)
            putString(LAST_PUBLISHED_LOCALES, signature.locales)
        }
    }

    private companion object {
        const val LAST_PUBLISHED_UPDATE_TIME = "last_published_update_time"
        const val LAST_PUBLISHED_PALETTE = "last_published_palette"
        const val LAST_PUBLISHED_LOCALES = "last_published_locales"
    }
}
