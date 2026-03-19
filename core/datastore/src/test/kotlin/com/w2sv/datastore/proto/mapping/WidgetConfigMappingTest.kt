package com.w2sv.datastore.proto.mapping

import com.w2sv.domain.model.widget.FontSize
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.widget.WidgetUtility
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.IpSetting
import com.w2sv.kotlinutils.copy
import kotlin.time.Duration.Companion.minutes
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class WidgetConfigMappingTest {

    @Test
    fun `default instance mapping`() {
        val config = WidgetConfig.default
        assertEquals(config, config.backAndForthMapped())
    }

    @Test
    fun `non default instance mapping`() {
        val config = WidgetConfig.default.run {
            this
                // change order
                .withUpdatedPropertyPosition(0, 5)
                .withUpdatedPropertyPosition(4, 2)
                .withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
                .withEnabledLocationAccessRequiringProperties()
                .withUpdatedPropertyConfig(WifiProperty.LoopbackIp) {
                    it.withUpdatedSetting(IpSetting.V4Enabled, false)
                }
                .copy(
                    utilities = utilities.copy { put(WidgetUtility.RefreshButton, false) },
                    appearance = appearance.copy(
                        backgroundOpacity = 0.42f,
                        fontSize = FontSize.Small,
                        coloring = appearance.coloring.copy(useCustom = true)
                    ),
                    refreshing = refreshing.copy(refreshPeriodically = false, interval = 20.minutes)
                )
        }
        assertEquals(config, config.backAndForthMapped())
    }
}

private fun WidgetConfig.backAndForthMapped(): WidgetConfig =
    toProto().toExternal()
