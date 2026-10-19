package com.w2sv.wifiwidget.ui.screen.home.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.ViewModelLifecycle
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelScopeModule {

    @Provides
    @ViewModelScoped
    fun viewModelScope(lifecycle: ViewModelLifecycle): CoroutineScope {
        val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
        lifecycle.addOnClearedListener { scope.cancel() }
        return scope
    }
}
