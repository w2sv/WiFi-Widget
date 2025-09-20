package com.w2sv.widget.utils.logging

import android.widget.RemoteViewsService
import androidx.annotation.CallSuper
import slimber.log.i

/**
 * A [RemoteViewsService.RemoteViewsFactory] that logs upon invocation of its callbacks.
 */
internal abstract class LoggingRemoteViewsFactory : RemoteViewsService.RemoteViewsFactory {

    @CallSuper
    override fun onCreate() {
        i { "onCreate | ${Thread.currentThread()}" }
    }

    @CallSuper
    override fun onDataSetChanged() {
        i { "onDataSetChanged | ${Thread.currentThread()}" }
    }

    @CallSuper
    override fun onDestroy() {
        i { "onDestroy" }
    }
}
