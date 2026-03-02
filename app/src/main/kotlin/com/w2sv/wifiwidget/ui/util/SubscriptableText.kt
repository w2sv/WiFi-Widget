package com.w2sv.wifiwidget.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import com.w2sv.domain.model.wifiproperty.viewdata.SubscriptableText

fun SubscriptableText.toAnnotatedString(subscriptFontSize: TextUnit): AnnotatedString =
    buildAnnotatedString {
        append(text)
        subscript?.let {
            withStyle(
                SpanStyle(
                    baselineShift = BaselineShift.Subscript,
                    fontSize = subscriptFontSize
                )
            ) {
                append(it)
            }
        }
    }
