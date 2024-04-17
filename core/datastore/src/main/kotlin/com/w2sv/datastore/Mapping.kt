package com.w2sv.datastore

import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetColoring

internal interface Mapper<Proto, External> {
    fun toExternal(proto: Proto): External
    fun toProto(external: External): Proto
}

internal object WidgetColoringMapper : Mapper<WidgetColoringProto, WidgetColoring.Config> {
    override fun toExternal(proto: WidgetColoringProto): WidgetColoring.Config =
        WidgetColoring.Config(
            preset = proto.preset.toExternal(),
            custom = proto.custom.toExternal(),
            isCustomSelected = proto.isCustomSelected
        )

    override fun toProto(external: WidgetColoring.Config): WidgetColoringProto =
        widgetColoringProto {
            preset = external.preset.toProto()
            custom = external.custom.toProto()
            isCustomSelected = external.isCustomSelected
        }
}

internal fun WidgetColoringProto.Preset.toExternal(): WidgetColoring.Preset =
    WidgetColoring.Preset(
        theme = when (theme) {
            WidgetColoringProto.Preset.Theme.Dark -> Theme.Dark
            WidgetColoringProto.Preset.Theme.Light -> Theme.Light
            else -> Theme.SystemDefault
        },
        useDynamicColors = useDynamicColors
    )

internal fun WidgetColoring.Preset.toProto(): WidgetColoringProto.Preset =
    WidgetColoringProto.Preset.newBuilder()
        .apply {
            theme = when (this@toProto.theme) {
                Theme.Dark -> WidgetColoringProto.Preset.Theme.Dark
                Theme.Light -> WidgetColoringProto.Preset.Theme.Light
                Theme.SystemDefault -> WidgetColoringProto.Preset.Theme.SystemDefault
            }
            useDynamicColors = this@toProto.useDynamicColors
        }
        .build()

internal fun WidgetColoringProto.Custom.toExternal(): WidgetColoring.Custom =
    WidgetColoring.Custom(
        background = background,
        primary = primary,
        secondary = secondary
    )

internal fun WidgetColoring.Custom.toProto(): WidgetColoringProto.Custom =
    WidgetColoringProto.Custom.newBuilder()
        .apply {
            background = this@toProto.background
            primary = this@toProto.primary
            secondary = this@toProto.secondary
        }
        .build()