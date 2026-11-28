package com.w2sv.widget.preview

import android.content.Context
import android.content.res.Configuration
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.test.core.app.ApplicationProvider
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.widget.ui.theme.resolve
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import java.util.Locale
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetPreviewPublisherTest {
    // The publisher switches to Dispatchers.IO, so virtual test time cannot control publication completion.
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun `concurrent requests publish once after rechecking stored state`() =
        runBlocking {
            context.getSharedPreferences("widget_previews", Context.MODE_PRIVATE).edit().clear().commit()
            val entered = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            mockkConstructor(GlanceAppWidgetManager::class)
            try {
                coEvery {
                    anyConstructed<GlanceAppWidgetManager>().setWidgetPreviews(WifiWidgetProvider::class)
                } coAnswers {
                    entered.complete(Unit)
                    release.await()
                    GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_SUCCESS
                }

                val publisher = WidgetPreviewPublisher(context, this, WidgetPreviewPublicationStore(context))
                publisher.publishIfOutdated()
                publisher.publishIfOutdated()
                withTimeout(5_000) { entered.await() }
                release.complete(Unit)
                awaitPublications()

                coVerify(exactly = 1) {
                    anyConstructed<GlanceAppWidgetManager>().setWidgetPreviews(WifiWidgetProvider::class)
                }
            } finally {
                unmockkConstructor(GlanceAppWidgetManager::class)
            }
        }

    @Test fun `rate-limited publication can be retried`() =
        runBlocking {
            context.getSharedPreferences("widget_previews", Context.MODE_PRIVATE).edit().clear().commit()
            mockkConstructor(GlanceAppWidgetManager::class)
            try {
                coEvery {
                    anyConstructed<GlanceAppWidgetManager>().setWidgetPreviews(WifiWidgetProvider::class)
                } returnsMany listOf(
                    GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_RATE_LIMITED,
                    GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_SUCCESS
                )

                val publisher = WidgetPreviewPublisher(context, this, WidgetPreviewPublicationStore(context))
                publisher.publishIfOutdated()
                awaitPublications()
                coVerify(exactly = 1) {
                    anyConstructed<GlanceAppWidgetManager>().setWidgetPreviews(WifiWidgetProvider::class)
                }
                publisher.publishIfOutdated()
                awaitPublications()
                publisher.publishIfOutdated()
                awaitPublications()

                coVerify(exactly = 2) {
                    anyConstructed<GlanceAppWidgetManager>().setWidgetPreviews(WifiWidgetProvider::class)
                }
            } finally {
                unmockkConstructor(GlanceAppWidgetManager::class)
            }
        }

    @Test fun `locale change republishes once and then remains current`() =
        runBlocking {
            context.getSharedPreferences("widget_previews", Context.MODE_PRIVATE).edit().clear().commit()
            val germanConfiguration = Configuration(context.resources.configuration).apply { setLocale(Locale.GERMAN) }
            val germanContext = context.createConfigurationContext(germanConfiguration)
            assertTrue(
                context.resources.configuration.locales.toLanguageTags() != germanContext.resources.configuration.locales.toLanguageTags()
            )
            assertEquals(
                WidgetConfig.default.appearance.coloring.resolve(context),
                WidgetConfig.default.appearance.coloring.resolve(germanContext)
            )
            mockkConstructor(GlanceAppWidgetManager::class)
            try {
                coEvery {
                    anyConstructed<GlanceAppWidgetManager>().setWidgetPreviews(WifiWidgetProvider::class)
                } returns GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_SUCCESS

                WidgetPreviewPublisher(context, this, WidgetPreviewPublicationStore(context)).publishIfOutdated()
                awaitPublications()
                val localizedPublisher = WidgetPreviewPublisher(germanContext, this, WidgetPreviewPublicationStore(germanContext))
                localizedPublisher.publishIfOutdated()
                awaitPublications()
                localizedPublisher.publishIfOutdated()
                awaitPublications()

                coVerify(exactly = 2) {
                    anyConstructed<GlanceAppWidgetManager>().setWidgetPreviews(WifiWidgetProvider::class)
                }
            } finally {
                unmockkConstructor(GlanceAppWidgetManager::class)
            }
        }

    private suspend fun CoroutineScope.awaitPublications() {
        val publications = coroutineContext[Job]!!.children.toList()
        withTimeout(5_000) { publications.joinAll() }
    }
}
