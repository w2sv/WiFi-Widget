package com.w2sv.datastore.proto.di

import android.content.Context
import androidx.datastore.dataStoreFile
import java.io.File

internal fun Context.widgetColoringProtoFile(): File =
    dataStoreFile("widget_coloring.pb")
