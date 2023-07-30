package com.w2sv.widget.properties

import android.content.Intent
import android.widget.RemoteViewsService
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