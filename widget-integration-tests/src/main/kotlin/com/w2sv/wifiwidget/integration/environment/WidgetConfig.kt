package com.w2sv.wifiwidget.integration.environment

import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.wifiproperty.WifiProperty

fun widgetConfig(vararg enabled: WifiProperty): WidgetConfig =
    WidgetConfig.default.run {
        copy(
            propertyConfig = propertyConfig.mapValues { (property, config) ->
                config.copy(isEnabled = property in enabled)
            },
            refreshing = refreshing.copy(refreshPeriodically = false),
            utilities = utilities.mapValues { false }
        )
    }
