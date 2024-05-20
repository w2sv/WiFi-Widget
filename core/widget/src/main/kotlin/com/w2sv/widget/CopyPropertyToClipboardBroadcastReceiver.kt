package com.w2sv.widget

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.w2sv.androidutils.notifying.showToast

internal class CopyPropertyToClipboardBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val propertyValue = intent.getStringExtra(Extra.PROPERTY_VALUE)
        val propertyLabel = intent.getStringExtra(Extra.PROPERTY_LABEL)

        Handler(Looper.getMainLooper()).post {
            context.applicationContext.showToast(
                "Copied $propertyLabel to clipboard.",
                Toast.LENGTH_SHORT
            )
        }

        context.getSystemService(ClipboardManager::class.java)
            .setPrimaryClip(
                ClipData.newPlainText(
                    null,
                    propertyValue
                )
            )
    }

    object Extra {
        const val PROPERTY_LABEL = "com.w2sv.wifiwidget.extra.PROPERTY_LABEL"
        const val PROPERTY_VALUE = "com.w2sv.wifiwidget.extra.PROPERTY_VALUE"
    }
}