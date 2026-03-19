package com.w2sv.domain.model.wifiproperty

import com.w2sv.domain.model.wifiproperty.settings.WifiPropertySetting
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.trueKeys

data class WifiPropertyConfig<S : WifiPropertySetting>(val isEnabled: Boolean, val settings: Map<S, Boolean> = emptyMap()) {

    val enabledSettings: List<S>
        get() = settings.trueKeys()

    fun isSettingEnabled(setting: S): Boolean =
        settings.getValue(setting)

    fun withUpdatedSetting(setting: S, isEnabled: Boolean): WifiPropertyConfig<S> =
        copy(settings = settings.copy { put(setting, isEnabled) })
}
