package com.w2sv.widget.preview

import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.w2sv.common.di.qualifier.AppDefaultScope
import com.w2sv.widget.WifiWidgetProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import slimber.log.w

/** Publishes launcher picker previews after app updates or changes to the default appearance. */
@Singleton
class WidgetPreviewPublisher @Inject internal constructor(
    @ApplicationContext private val context: Context,
    @AppDefaultScope private val scope: CoroutineScope,
    private val publicationStore: WidgetPreviewPublicationStore
) {
    // Startup and configuration changes can launch overlapping publication coroutines.
    // Serialize the freshness check and publication to prevent duplicate requests.
    private val publicationMutex = Mutex()

    fun publishIfOutdated() {
        if (!isSetWidgetPreviewSupported) return

        scope.launch {
            publicationMutex.withLock {
                withContext(Dispatchers.IO) {
                    attemptPublishIfOutdated()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private suspend fun attemptPublishIfOutdated() {
        try {
            val signature = WidgetPreviewSignature.current(context)
            if (publicationStore.signatureWasPublished(signature)) return

            val result = GlanceAppWidgetManager(context).setWidgetPreviews(WifiWidgetProvider::class)

            if (result == GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_SUCCESS) {
                publicationStore.recordPublished(signature)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            w { "Failed publishing launcher widget previews: error=$exception" }
        }
    }
}

@get:ChecksSdkIntAtLeast(Build.VERSION_CODES.VANILLA_ICE_CREAM)
private val isSetWidgetPreviewSupported
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
