package com.w2sv.widget.actions

import com.w2sv.domain.model.widget.WidgetRefreshing

interface WidgetActions {
    fun pin(onFailure: () -> Unit)
    fun refresh()
    suspend fun render()
    fun applyRefreshingPolicy(refreshing: WidgetRefreshing)
}
