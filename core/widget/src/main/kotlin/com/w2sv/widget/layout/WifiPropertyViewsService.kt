package com.w2sv.widget.layout

import android.content.Intent
import android.widget.RemoteViewsService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class WifiPropertyViewsService :
    RemoteViewsService() {

    @Inject
    lateinit var wifiPropertyViewsFactory: WifiPropertyViewsFactory

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        wifiPropertyViewsFactory
}
