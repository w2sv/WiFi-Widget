package com.w2sv.widget.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WidgetPinSuccessFlow

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class MutableWidgetPinSuccessFlow
