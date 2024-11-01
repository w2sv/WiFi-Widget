package com.w2sv.widget

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.widget.Toast
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.androidutils.widget.showToast
import com.w2sv.core.widget.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
internal class CopyPropertyToClipboardBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var clipboardManager: ClipboardManager

    override fun onReceive(context: Context, intent: Intent) {
        val args = Args.fromIntent(intent)

        // Copy to clipboard
        clipboardManager
            .setPrimaryClip(
                ClipData.newPlainText(
                    null,
                    args.propertyValue
                )
            )

        // Show toast
        Handler(Looper.getMainLooper()).postAtFrontOfQueue {
            context.applicationContext.showToast(
                context.getString(R.string.copied_to_clipboard, args.propertyLabel),
                Toast.LENGTH_SHORT
            )
        }
    }

    @Parcelize
    data class Args(val propertyLabel: String, val propertyValue: String) : Parcelable {

        companion object {
            fun getIntent(propertyLabel: String, propertyValue: String): Intent =
                Intent()
                    .putExtra(
                        EXTRA,
                        Args(
                            propertyLabel = propertyLabel,
                            propertyValue = propertyValue
                        )
                    )

            fun fromIntent(intent: Intent): Args =
                intent.getParcelableCompat(EXTRA)!!

            private const val EXTRA =
                "com.w2sv.wifiwidget.extra.CopyPropertyToClipboardBroadcastReceiverArgs"
        }
    }
}