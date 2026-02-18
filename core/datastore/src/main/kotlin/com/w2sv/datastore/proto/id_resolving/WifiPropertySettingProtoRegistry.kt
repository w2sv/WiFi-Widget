package com.w2sv.datastore.proto.id_resolving

import com.w2sv.domain.model.wifiproperty.settings.IpSetting
import com.w2sv.domain.model.wifiproperty.settings.LocationParameter
import com.w2sv.domain.model.wifiproperty.settings.WifiPropertySetting

/**
 * Central lookup for resolving [WifiPropertySetting] instances from their proto IDs.
 */
internal object WifiPropertySettingProtoRegistry : ProtoRegistry<WifiPropertySetting>(IpSetting.entries + LocationParameter.entries)
