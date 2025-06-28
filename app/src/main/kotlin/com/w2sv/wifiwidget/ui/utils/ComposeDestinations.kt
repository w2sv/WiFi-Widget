package com.w2sv.wifiwidget.ui.utils

import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.TypedRoute

val TypedRoute<*>.direction: Direction
    get() = Direction(route)
