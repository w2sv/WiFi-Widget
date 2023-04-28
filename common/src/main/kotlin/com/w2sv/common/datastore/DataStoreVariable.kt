package com.w2sv.common.datastore

import androidx.datastore.preferences.core.Preferences

interface DataStoreVariable<T> {
    val defaultValue: T
    val preferencesKey: Preferences.Key<T>
}