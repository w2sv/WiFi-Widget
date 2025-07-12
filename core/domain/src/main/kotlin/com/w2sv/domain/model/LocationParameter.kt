package com.w2sv.domain.model

import androidx.annotation.StringRes
import com.w2sv.core.common.R

enum class LocationParameter(@param:StringRes override val labelRes: Int) : WidgetProperty {
    ZipCode(R.string.zip_code),
    District(R.string.district),
    City(R.string.city),
    Region(R.string.region),
    Country(R.string.country),
    Continent(R.string.continent)
}
