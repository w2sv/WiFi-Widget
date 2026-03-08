package com.w2sv.domain.model.wifiproperty.settings

import com.w2sv.domain.model.Labelled
import com.w2sv.domain.model.wifiproperty.WithProtoId

sealed interface WifiPropertySetting :
    Labelled,
    WithProtoId
