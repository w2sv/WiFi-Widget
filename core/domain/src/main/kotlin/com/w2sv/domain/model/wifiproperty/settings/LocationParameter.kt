package com.w2sv.domain.model.wifiproperty.settings

import androidx.annotation.StringRes
import com.w2sv.core.domain.R

enum class LocationParameter(override val protoId: Int, @StringRes override val labelRes: Int) : WifiPropertySetting {
    ZipCode(101, R.string.zip_code),
    District(102, R.string.district),
    City(103, R.string.city),
    Region(104, R.string.region),
    Country(105, R.string.country),
    Continent(106, R.string.continent)
}
