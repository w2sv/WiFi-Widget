package com.w2sv.widget.data

import android.content.Context
import com.w2sv.androidutils.res.isNightModeActiveCompat
import com.w2sv.common.utils.mapFlow
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.kotlinutils.coroutines.flow.stateInWithBlockingInitial
import com.w2sv.widget.di.WallpaperChangedFlow
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetBottomBarElement
import com.w2sv.widget.model.WidgetColors
import com.w2sv.widget.model.WidgetRefreshing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class InternalWidgetRepository @Inject constructor(
    widgetRepository: WidgetRepository,
    @WallpaperChangedFlow wallpaperChangedFlow: SharedFlow<Unit>
) :
    WidgetRepository by widgetRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val widgetAppearance by lazy {
        combine(
            coloringConfig,
            opacity,
            fontSize,
            propertyValueAlignment,
            bottomRowElementEnablementMap.mapFlow(),
            transform = { t1, t2, t3, t4, t5 ->
                WidgetAppearance(
                    coloringConfig = t1,
                    backgroundOpacity = t2,
                    fontSize = t3,
                    propertyValueAlignment = t4,
                    bottomBar = WidgetBottomBarElement(t5)
                )
            }
        )
            .stateInWithBlockingInitial(scope)
    }

    private var widgetColorParametersToColorsCache: Pair<WidgetColorParameters, WidgetColors>? = null
    private var wallpaperHasChanged = false

    init {
        wallpaperChangedFlow.collectOn(scope) {
            i { "Collected from wallpaperChangedFlow; Setting wallpaperHasChanged=true" }
            wallpaperHasChanged = true
        }
    }

    fun widgetColors(context: Context): WidgetColors {
        val appliedStyle = widgetAppearance.value.coloringConfig.appliedStyle
        val parameters = WidgetColorParameters.get(appliedStyle, context)

        widgetColorParametersToColorsCache?.let { (cachedParameters, cachedColors) ->
            if (cachedParameters == parameters) {
                if (cachedParameters.style.asPresetOrNull?.useDynamicColors == true && wallpaperHasChanged) {
                    i { "Wallpaper has changed; Setting wallpaperHasChanged=false" }
                    wallpaperHasChanged = false
                } else {
                    i { "Returning cached colors" }
                    return cachedColors
                }
            }
        }
        return WidgetColors.fromStyle(appliedStyle, context).also { colors ->
            widgetColorParametersToColorsCache = parameters to colors
            i { "Computed and cached $colors with $parameters" }
        }
    }

    val refreshing by lazy {
        combine(
            refreshingParametersEnablementMap.mapFlow(),
            refreshInterval
        ) { t1, t2 -> WidgetRefreshing(t1, t2) }
            .stateInWithBlockingInitial(scope)
    }
}

private sealed interface WidgetColorParameters {

    val style: WidgetColoring.Style

    data class DefaultTheme(override val style: WidgetColoring.Style, val isNightModeActive: Boolean) : WidgetColorParameters

    @JvmInline
    value class Other(override val style: WidgetColoring.Style) : WidgetColorParameters

    companion object {
        fun get(style: WidgetColoring.Style, context: Context): WidgetColorParameters =
            if ((style as? WidgetColoring.Style.Preset)?.theme == Theme.Default) {
                DefaultTheme(
                    style,
                    context.resources.configuration.isNightModeActiveCompat
                )
            } else {
                Other(style)
            }
    }
}
