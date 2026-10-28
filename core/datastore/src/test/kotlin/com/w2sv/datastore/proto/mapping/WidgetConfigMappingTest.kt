package com.w2sv.datastore.proto.mapping

import com.w2sv.datastore.WidgetRefreshingProto
import com.w2sv.domain.widget.FontSize
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetUtility
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.settings.IpSetting
import com.w2sv.kotlinutils.copy
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WidgetConfigMappingTest {

    @Test
    fun `default instance mapping`() {
        assertEqualityAfterBackAndForthMapping(WidgetConfig.default)
    }

    @Test
    fun `refreshing mapping preserves enabled state and interval`() {
        val refreshing = WidgetConfig.default.refreshing.copy(refreshPeriodically = false, interval = 30.minutes)

        assertEquals(refreshing, refreshing.toProto().toExternal())
    }

    @Test
    fun `legacy battery field is ignored while other refreshing settings survive`() {
        val configured = WidgetConfig.default.refreshing.copy(refreshPeriodically = false, interval = 30.minutes)
        // Field 2 was the old boolean setting; protobuf keeps it as an unknown field.
        val persisted = configured.toProto().toByteArray() + byteArrayOf(0x10, 0x01)

        val restored = WidgetRefreshingProto.parseFrom(persisted).toExternal()

        assertEquals(configured, restored)
        assertEquals(configured.toProto(), restored.toProto())
    }

    @Test
    fun `retired widget settings utility ID is ignored without shifting existing IDs`() {
        val proto = WidgetConfig.default.toProto().toBuilder()
            .putUtilities(1, false)
            .putUtilities(3, true)
            .build()

        val restored = proto.toExternal()
        assertEquals(false, restored.utilities.getValue(WidgetUtility.RefreshButton))
        assertFalse(WidgetUtility.entries.any { it.protoId == 3 })
        assertFalse(restored.toProto().utilitiesMap.containsKey(3))
    }

    @Test
    fun `existing proto ID maps to IP coordinates`() {
        val configured = WidgetConfig.default.withUpdatedPropertyEnablement(WifiProperty.IpCoordinates, false)
        val proto = configured.toProto()

        assertEquals(24, WifiProperty.IpCoordinates.protoId)
        assertFalse(proto.toExternal().isEnabled(WifiProperty.IpCoordinates))
        assertEquals(configured, proto.toExternal())
    }

    @Test
    fun `non default instance mapping`() {
        assertEqualityAfterBackAndForthMapping(
            WidgetConfig.default.run {
                this
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
        )
    }
}

private fun assertEqualityAfterBackAndForthMapping(config: WidgetConfig) {
    assertEquals(config, config.backAndForthMapped())
}

private fun WidgetConfig.backAndForthMapped(): WidgetConfig =
    toProto().toExternal()
