package com.w2sv.common

import android.graphics.Color
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.common.datastore.DataStoreProperty

enum class WidgetColorSection(@StringRes val labelRes: Int, override val defaultValue: Int) :
    DataStoreProperty<Int> {
    Background(R.string.background, Color.GRAY),
    Labels(R.string.labels, Color.RED),
    Values(R.string.values, Color.WHITE);

    override val preferencesKey = intPreferencesKey(name)
}