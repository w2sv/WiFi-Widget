package com.w2sv.widget.actions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.w2sv.androidutils.content.getParcelableCompat
import com.w2sv.androidutils.res.getHtmlFormattedText
import com.w2sv.androidutils.widget.makeToast
import com.w2sv.common.utils.ToastManager
import com.w2sv.core.common.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.parcelize.Parcelize
import slimber.log.i

@AndroidEntryPoint
internal class CopyPropertyToClipboardActivity : ComponentActivity() {
    @Inject
    lateinit var toastManager: ToastManager

    @Inject
    lateinit var clipboardManager: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = Args.fromIntent(intent)
        i { "onCreate | $args" }

        clipboardManager.setPrimaryClip(
            ClipData.newPlainText(
                args.propertyLabel,
                args.propertyValue
            )
        )
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
                Intent().putExtra(
                    EXTRA,
                    Args(
                        propertyLabel = propertyLabel,
                        propertyValue = propertyValue
                    )
                )

            fun fromIntent(intent: Intent): Args =
                checkNotNull(intent.getParcelableCompat(EXTRA))

            private const val EXTRA =
                "com.w2sv.wifiwidget.extra.CopyPropertyToClipboardBroadcastReceiverArgs"
        }
    }

    companion object {
        fun intent(
            context: Context,
            propertyLabel: String,
            propertyValue: String
        ): Intent =
            Args.getIntent(propertyLabel, propertyValue)
                .setClass(context, CopyPropertyToClipboardActivity::class.java)
    }
}
