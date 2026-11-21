package com.w2sv.widget.ui.actions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError
import com.w2sv.domain.wifiproperty.presentation.SubscriptableText
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import com.w2sv.widget.di.GlanceWidgetEntryPoint
import com.w2sv.widget.di.glanceWidgetEntryPoint
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetPropertyActionTest {
    private val context = mockk<Context> {
        every { packageName } returns "test.app"
    }
    private val destination = ComponentName(context.packageName, "test.app.RecoveryActivity")
    private val provider = mockk<LocationAccessActivityIntentProvider> {
        every { this@mockk.invoke(any()) } answers { Intent().setComponent(destination) }
    }
    private val entryPoint = mockk<GlanceWidgetEntryPoint> {
        every { locationAccessActivityIntentProvider() } returns provider
    }
    private val locationErrors = listOf(WifiPropertyResolutionError.NoLocationAccessPermission, WifiPropertyResolutionError.GpsDisabled)

    @Before
    fun setUp() {
        mockkStatic(::glanceWidgetEntryPoint)
        every { glanceWidgetEntryPoint(any()) } returns entryPoint
    }

    @After
    fun tearDown() {
        unmockkStatic(::glanceWidgetEntryPoint)
    }

    @Test
    fun `location errors recover even for blank values with consistent explicit launch intents`() {
        val intents = locationErrors.flatMap { error ->
            listOf("Unavailable", "").map { value ->
                checkNotNull(presentation(error, value).clickIntent(context))
            }
        }
        intents.forEach { intent ->
            assertEquals(destination, intent.component)
            assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP, intent.flags)
            assertEquals("wifiwidget://${context.packageName}/location-access", intent.dataString)
        }
        intents.drop(1).forEach { intent ->
            assertNotSame(intents.first(), intent)
            assertTrue(intents.first().filterEquals(intent))
        }
        verify(exactly = 4) { provider(context) }
        verify(exactly = 4) { entryPoint.locationAccessActivityIntentProvider() }
        verify(exactly = 4) { glanceWidgetEntryPoint(context) }
    }

    @Test
    fun `resolved values copy displayed text and blanks remain inert without destination lookup`() {
        val intent = checkNotNull(presentation().clickIntent(context))
        assertEquals(CopyPropertyToClipboardActivity::class.java.name, intent.component?.className)
        assertEquals("displayed value", CopyPropertyToClipboardActivity.Property.fromIntent(intent)?.value)

        listOf("", " \t").forEach { value -> assertNull(presentation(value = value).clickIntent(context)) }
        verifyNoRecoveryLookup()
    }

    @Test
    fun `other resolution errors remain inert without looking up the destination`() {
        WifiPropertyResolutionError.entries.filterNot(locationErrors::contains).forEach { error ->
            assertNull(presentation(error).clickIntent(context))
        }
        verifyNoRecoveryLookup()
    }

    private fun verifyNoRecoveryLookup() {
        verify(exactly = 0) { provider(any()) }
        verify(exactly = 0) { entryPoint.locationAccessActivityIntentProvider() }
        verify(exactly = 0) { glanceWidgetEntryPoint(any()) }
    }

    private fun presentation(error: WifiPropertyResolutionError? = null, value: String = "displayed value") =
        WifiPropertyPresentation(SubscriptableText("label"), value, resolutionError = error)
}
