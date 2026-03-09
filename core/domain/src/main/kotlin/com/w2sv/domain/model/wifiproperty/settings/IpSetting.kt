package com.w2sv.domain.model.wifiproperty.settings

import androidx.annotation.StringRes
import com.w2sv.core.common.R
import com.w2sv.domain.model.networking.IpAddress

enum class IpSetting(override val protoId: Int, @StringRes override val labelRes: Int) : WifiPropertySetting {
    ShowPrefixLength(1, R.string.show_prefix_length),
    ShowSubnetMask(2, R.string.show_ipv4_subnet_mask),
    V4Enabled(3, R.string.ipv4),
    V6Enabled(4, R.string.ipv6);

    internal companion object {
        val forV6Only: List<IpSetting> = listOf(ShowPrefixLength)
        fun forV64(includePrefixLength: Boolean): List<IpSetting> =
            buildList {
                add(V4Enabled)
                add(V6Enabled)
                if (includePrefixLength) {
                    add(ShowPrefixLength)
                    add(ShowSubnetMask)
                }
            }
    }
}

fun List<IpSetting>.enabledVersions(): List<IpAddress.Version> =
    mapNotNull { setting ->
        when (setting) {
            IpSetting.V4Enabled -> IpAddress.Version.V4
            IpSetting.V6Enabled -> IpAddress.Version.V6
            else -> null
        }
    }
