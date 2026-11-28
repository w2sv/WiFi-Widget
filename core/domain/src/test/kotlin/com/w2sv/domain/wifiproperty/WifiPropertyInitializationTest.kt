package com.w2sv.domain.wifiproperty

import com.w2sv.domain.widget.WidgetConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// A distinct sandbox prevents other tests from initializing the property collection first.
@Config(sdk = [35], instrumentedPackages = ["com.w2sv.domain.wifiproperty"])
class WifiPropertyInitializationTest {
    @Test
    fun `accessing a property first preserves complete entries and default widget configuration`() {
        val ssid = WifiProperty.SSID

        assertSame(ssid, WifiProperty.entries.first())
        WifiProperty.entries.forEach { assertNotNull(it) }
        assertEquals(WifiProperty.entries, WidgetConfig.default.propertyOrder)
    }
}
