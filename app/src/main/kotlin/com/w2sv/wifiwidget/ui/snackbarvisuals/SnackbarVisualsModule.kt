package com.w2sv.wifiwidget.ui.snackbarvisuals

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Module
@InstallIn(ActivityRetainedComponent::class)
object SnackbarVisualsModule {

    @Provides
    @ActivityRetainedScoped
    fun provideSnackbarFlow(): MutableSharedFlow<SnackbarBuilder> =
        MutableSharedFlow(extraBufferCapacity = 16)

    @Provides
    fun provideSnackbarSharedFlow(flow: MutableSharedFlow<SnackbarBuilder>): SharedFlow<SnackbarBuilder> = flow

    @Provides
    fun provideEmitSnackbarVisuals(flow: MutableSharedFlow<SnackbarBuilder>): EmitSnackbarBuilder =
        EmitSnackbarBuilder { value -> flow.emit(value) }
}
