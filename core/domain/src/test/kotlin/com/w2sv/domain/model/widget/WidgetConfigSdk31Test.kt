package com.w2sv.domain.model.widget

import com.w2sv.domain.model.wifiproperty.WifiProperty
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class WidgetConfigSdk31Test {

    @Test
    fun `supportedProperties does not include unsupported properties`() {
        assertEquals(
            WifiProperty.entries,
            WidgetConfig.default.supportedProperties
        )
    }
}
