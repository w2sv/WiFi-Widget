package com.w2sv.domain.model

import com.w2sv.core.domain.R
import androidx.annotation.StringRes

enum class IpLocationParameter(@StringRes override val labelRes: Int): WidgetProperty {
    ZipCode(R.string.zip_code),
    District(R.string.district),
    City(R.string.city),
    Region(R.string.region),
    Country(R.string.country),
    Continent(R.string.continent)
}
