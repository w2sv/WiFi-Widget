package com.w2sv.wifiwidget.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity

fun Context.requireActivity(): ComponentActivity =
    getActivity()!!

@Suppress("UNCHECKED_CAST")
fun <A : ComponentActivity> Context.requireCastActivity(): A =
    requireActivity() as A

private tailrec fun Context.getActivity(): ComponentActivity? =
    when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }