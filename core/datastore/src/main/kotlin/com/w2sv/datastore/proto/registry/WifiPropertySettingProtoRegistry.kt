package com.w2sv.datastore.proto.registry

import com.w2sv.domain.wifiproperty.settings.IpSetting
import com.w2sv.domain.wifiproperty.settings.LocationParameter
import com.w2sv.domain.wifiproperty.settings.WifiPropertySetting

/**
 * Central lookup for resolving [WifiPropertySetting] instances from their proto IDs.
 */
internal object WifiPropertySettingProtoRegistry : ProtoRegistry<WifiPropertySetting>(IpSetting.entries + LocationParameter.entries)
