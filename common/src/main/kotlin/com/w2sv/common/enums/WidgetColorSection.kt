package com.w2sv.common.enums

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.common.R
import com.w2sv.common.datastore.DataStoreVariable

enum class WidgetColorSection(@StringRes val labelRes: Int, override val defaultValue: Int) :
    DataStoreVariable<Int> {

    Background(R.string.background, Color(112, 24, 136).toArgb()),
    Labels(R.string.labels, Color.Red.toArgb()),
    Other(R.string.other, Color.White.toArgb());

    override val preferencesKey = intPreferencesKey(name)
}