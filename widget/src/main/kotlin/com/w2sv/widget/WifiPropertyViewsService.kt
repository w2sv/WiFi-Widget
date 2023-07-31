package com.w2sv.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.w2sv.widget.ui.WifiPropertyViewsFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WifiPropertyViewsService :
    RemoteViewsService() {

    @Inject
    lateinit var wifiPropertyViewsFactory: WifiPropertyViewsFactory

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        wifiPropertyViewsFactory
}