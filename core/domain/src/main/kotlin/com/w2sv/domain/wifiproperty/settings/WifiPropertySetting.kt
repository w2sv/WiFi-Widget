package com.w2sv.domain.wifiproperty.settings

import com.w2sv.domain.WithLabel
import com.w2sv.domain.wifiproperty.WithProtoId

sealed interface WifiPropertySetting :
    WithLabel,
    WithProtoId
