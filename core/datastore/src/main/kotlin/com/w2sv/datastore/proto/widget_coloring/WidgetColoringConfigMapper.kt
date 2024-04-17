package com.w2sv.datastore.proto.widget_coloring

import com.w2sv.datastore.WidgetColoringProto
import com.w2sv.datastore.proto.Mapper
import com.w2sv.datastore.widgetColoringProto
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetColoring

internal object WidgetColoringConfigMapper : Mapper<WidgetColoringProto, WidgetColoring.Config> {

    override fun toExternal(proto: WidgetColoringProto): WidgetColoring.Config =
        WidgetColoring.Config(
            preset = PresetColoringMapper.toExternal(proto.preset),
            custom = CustomColoringMapper.toExternal(proto.custom),
            isCustomSelected = proto.isCustomSelected
        )

    override fun toProto(external: WidgetColoring.Config): WidgetColoringProto =
        widgetColoringProto {
            preset = PresetColoringMapper.toProto(external.preset)
            custom = CustomColoringMapper.toProto(external.custom)
            isCustomSelected = external.isCustomSelected
        }
}

private object PresetColoringMapper :
    Mapper<WidgetColoringProto.Preset, WidgetColoring.Style.Preset> {

    override fun toProto(external: WidgetColoring.Style.Preset): WidgetColoringProto.Preset =
        WidgetColoringProto.Preset.newBuilder()
            .apply {
                theme = when (external.theme) {
                    Theme.Dark -> WidgetColoringProto.Preset.Theme.Dark
                    Theme.Light -> WidgetColoringProto.Preset.Theme.Light
                    Theme.SystemDefault -> WidgetColoringProto.Preset.Theme.SystemDefault
                }
                useDynamicColors = external.useDynamicColors
            }
            .build()

    override fun toExternal(proto: WidgetColoringProto.Preset): WidgetColoring.Style.Preset =
        WidgetColoring.Style.Preset(
            theme = when (proto.theme) {
                WidgetColoringProto.Preset.Theme.Dark -> Theme.Dark
                WidgetColoringProto.Preset.Theme.Light -> Theme.Light
                else -> Theme.SystemDefault
            },
            useDynamicColors = proto.useDynamicColors
        )
}

private object CustomColoringMapper :
    Mapper<WidgetColoringProto.Custom, WidgetColoring.Style.Custom> {

    override fun toProto(external: WidgetColoring.Style.Custom): WidgetColoringProto.Custom =
        WidgetColoringProto.Custom.newBuilder()
            .apply {
                background = external.background
                primary = external.primary
                secondary = external.secondary
            }
            .build()

    override fun toExternal(proto: WidgetColoringProto.Custom): WidgetColoring.Style.Custom =
        WidgetColoring.Style.Custom(
            background = proto.background,
            primary = proto.primary,
            secondary = proto.secondary
        )
}