package com.w2sv.widget

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WidgetPreviewPublisher @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun publishIfOutdated() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return

        val packageLastUpdateTime = context.packageManager
            .getPackageInfo(context.packageName, 0)
            .lastUpdateTime
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (preferences.getLong(LAST_PUBLISHED_UPDATE_TIME, -1L) == packageLastUpdateTime) return

        val result = runCatching {
            GlanceAppWidgetManager(context).setWidgetPreviews(WifiWidgetProvider::class)
        }.getOrNull()
        if (result == GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_SUCCESS) {
            preferences.edit {
                putLong(LAST_PUBLISHED_UPDATE_TIME, packageLastUpdateTime)
            }
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "widget_previews"
        const val LAST_PUBLISHED_UPDATE_TIME = "last_published_update_time"
    }
}
