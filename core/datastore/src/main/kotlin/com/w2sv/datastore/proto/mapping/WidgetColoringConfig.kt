package com.w2sv.datastore.proto.mapping

import com.w2sv.datastore.WidgetColoringProto
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.widget.WidgetColoring
import com.w2sv.domain.model.widget.WidgetColoringConfig

/* -------------------------
 * WidgetColoringConfig
 * ------------------------- */

internal fun WidgetColoringConfig.toProto(): WidgetColoringProto =
    WidgetColoringProto.newBuilder()
        .also { builder ->
            builder.preset = preset.toProto()
            builder.custom = custom.toProto()
            builder.isCustomSelected = isCustomSelected
        }
        .build()

internal fun WidgetColoringProto.toExternal(): WidgetColoringConfig =
    WidgetColoringConfig(
        preset = preset.toExternal(),
        custom = custom.toExternal(),
        isCustomSelected = isCustomSelected
    )

/* -------------------------
 * Preset coloring
 * ------------------------- */

private fun WidgetColoring.Preset.toProto(): WidgetColoringProto.Preset =
    WidgetColoringProto.Preset.newBuilder()
        .also { builder ->
            builder.theme = theme.toProto()
            builder.useDynamicColors = useDynamicColors
        }
        .build()

private fun WidgetColoringProto.Preset.toExternal(): WidgetColoring.Preset =
    WidgetColoring.Preset(
        theme = theme.toExternal(),
        useDynamicColors = useDynamicColors
    )

/* -------------------------
 * Theme
 * ------------------------- */

private fun Theme.toProto(): WidgetColoringProto.Preset.Theme =
    when (this) {
        Theme.Dark -> WidgetColoringProto.Preset.Theme.DARK
        Theme.Light -> WidgetColoringProto.Preset.Theme.LIGHT
        Theme.Default -> WidgetColoringProto.Preset.Theme.SYSTEM_DEFAULT
    }

private fun WidgetColoringProto.Preset.Theme.toExternal(): Theme =
    when (this) {
        WidgetColoringProto.Preset.Theme.DARK -> Theme.Dark
        WidgetColoringProto.Preset.Theme.LIGHT -> Theme.Light
        WidgetColoringProto.Preset.Theme.SYSTEM_DEFAULT,
        WidgetColoringProto.Preset.Theme.UNRECOGNIZED -> Theme.Default
    }

/* -------------------------
 * Custom coloring
 * ------------------------- */

private fun WidgetColoring.Custom.toProto(): WidgetColoringProto.Custom =
    WidgetColoringProto.Custom.newBuilder()
        .also { builder ->
            builder.background = background
            builder.primary = primary
            builder.secondary = secondary
        }
        .build()

private fun WidgetColoringProto.Custom.toExternal(): WidgetColoring.Custom =
    WidgetColoring.Custom(
        background = background,
        primary = primary,
        secondary = secondary
    )
