package com.w2sv.domain.model.wifiproperty

import com.w2sv.domain.model.wifiproperty.settings.WifiPropertySetting

data class WifiPropertyConfig<S : WifiPropertySetting>(val isEnabled: Boolean, val settings: Map<S, Boolean> = emptyMap())
