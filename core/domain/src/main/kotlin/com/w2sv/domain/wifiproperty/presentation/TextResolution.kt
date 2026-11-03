package com.w2sv.domain.wifiproperty.presentation

import android.content.res.Resources
import com.w2sv.domain.wifiproperty.Text

fun Text.resolve(resources: Resources): String =
    when (this) {
        is Text.Raw -> value
        is Text.Resource -> resources.getString(value)
    }
