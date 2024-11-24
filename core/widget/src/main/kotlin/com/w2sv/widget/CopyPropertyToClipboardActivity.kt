package com.w2sv.widget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.UiThread
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.androidutils.res.getHtmlFormattedText
import com.w2sv.androidutils.widget.makeToast
import com.w2sv.core.widget.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.parcelize.Parcelize

@AndroidEntryPoint
internal class CopyPropertyToClipboardActivity : ComponentActivity() {

    @Singleton
    class ToastManager @Inject constructor() {

        @UiThread
        fun cancelPreviousAndShow(toast: Toast) {
            this.toast?.cancel()
            this.toast = toast.also { it.show() }
        }

        private var toast: Toast? = null
    }

    @Inject
    lateinit var toastManager: ToastManager

    @Inject
    lateinit var clipboardManager: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = Args.fromIntent(intent)

        // Copy to clipboard
        clipboardManager
            .setPrimaryClip(
                ClipData.newPlainText(
                    args.propertyLabel,
                    args.propertyValue
                )
            )

        // Show toast
        toastManager.cancelPreviousAndShow(
            applicationContext.makeToast(
                resources.getHtmlFormattedText(R.string.copied_to_clipboard, args.propertyLabel),
                Toast.LENGTH_SHORT
            )
        )

        finishAndRemoveTask()
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
