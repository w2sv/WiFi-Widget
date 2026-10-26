package com.w2sv.widget.utils

import android.widget.Toast
import androidx.annotation.UiThread
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToastManager @Inject constructor() {

    @UiThread
    fun cancelPreviousAndShow(toast: Toast) {
        this.toast?.cancel()
        this.toast = toast.also { it.show() }
    }

    private var toast: Toast? = null
}
