package com.w2sv.widget.refreshing

import androidx.work.Data
import androidx.work.workDataOf

// Caution: do NOT change [uniqueWorkName] definitions.
// WorkManager persists unique work names across app updates.
internal enum class RefreshWork(val uniqueWorkName: String) {
    Periodic("periodic_widget_refreshing"),
    Immediate("immediate_widget_refreshing"),
    Recovery("remote_widget_recovery");

    val isRecovery: Boolean get() = this == Recovery

    fun toWorkData(): Data =
        workDataOf(WORK_DATA_KEY to name)

    companion object {
        private const val WORK_DATA_KEY = "refresh_work"
        fun fromWorkData(data: Data): RefreshWork =
            entries.firstOrNull { it.name == data.getString(WORK_DATA_KEY) }
                ?: Periodic
    }
}
