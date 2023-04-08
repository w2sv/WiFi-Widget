package com.w2sv.common.extensions

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

fun LifecycleOwner.addObservers(observers: Iterable<LifecycleObserver>){
    observers.forEach(lifecycle::addObserver)
}