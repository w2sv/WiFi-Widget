package com.w2sv.common.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

fun broadcastReceiver(onReceiveCallback: (Context, Intent) -> Unit): BroadcastReceiver =
    object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onReceiveCallback(context, intent)
        }
    }
