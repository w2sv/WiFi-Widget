package com.w2sv.common.enums

import android.graphics.Color
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.common.R
import com.w2sv.common.datastore.DataStoreVariable

enum class WidgetColorSection(@StringRes val labelRes: Int, override val defaultValue: Int) :
    DataStoreVariable<Int> {

    Background(R.string.background, Color.GRAY),
    Labels(R.string.labels, Color.RED),
    Other(R.string.other, Color.WHITE);

    override val preferencesKey = intPreferencesKey(name)
}