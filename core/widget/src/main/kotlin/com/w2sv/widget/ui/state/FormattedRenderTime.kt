package com.w2sv.widget.ui.state

import android.content.Context
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.time.Clock
import java.util.Date
import java.util.TimeZone

data class FormattedRenderTime(val normal: String, val compact: String) {
    companion object {
        operator fun invoke(
            context: Context,
            date: Date,
            timeZone: TimeZone
        ): FormattedRenderTime {
            val time = DateFormat.getTimeFormat(context).apply { this.timeZone = timeZone }.format(date)
            val day = SimpleDateFormat("EEE", context.resources.configuration.locales[0])
                .apply { this.timeZone = timeZone }
                .format(date)

            return FormattedRenderTime("$time $day", time)
        }

        operator fun invoke(context: Context, clock: Clock): FormattedRenderTime =
            FormattedRenderTime(
                context = context,
                date = Date.from(clock.instant()),
                timeZone = TimeZone.getTimeZone(clock.zone)
            )
    }
}
