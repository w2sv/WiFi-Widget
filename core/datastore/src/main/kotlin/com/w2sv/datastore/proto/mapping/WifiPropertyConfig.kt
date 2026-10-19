package com.w2sv.datastore.proto.mapping

import com.w2sv.datastore.WifiPropertyConfigProto
import com.w2sv.datastore.proto.registry.WifiPropertySettingProtoRegistry
import com.w2sv.domain.wifiproperty.WifiPropertyConfig
import com.w2sv.domain.wifiproperty.settings.WifiPropertySetting

internal fun WifiPropertyConfig<*>.toProto(): WifiPropertyConfigProto =
    WifiPropertyConfigProto.newBuilder()
        .setIsEnabled(isEnabled)
        .putAllSettings(settings.mapKeys { it.key.protoId })
        .build()

internal fun WifiPropertyConfigProto.toExternal(): WifiPropertyConfig<WifiPropertySetting> =
    WifiPropertyConfig(
        isEnabled = isEnabled,
        settings = settingsMap.mapKeys { (id, _) -> WifiPropertySettingProtoRegistry(id) }
    )
