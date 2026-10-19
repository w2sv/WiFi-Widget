package com.w2sv.domain.wifiproperty.presentation

import android.content.res.Resources
import com.w2sv.core.common.R
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyResolver
import com.w2sv.domain.wifiproperty.WifiPropertyValue
import com.w2sv.domain.wifiproperty.settings.IpSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WifiPropertyPresentationProvider(private val resolver: WifiPropertyResolver, private val resources: Resources) {
    /**
     * @return List of [WifiPropertyPresentation], the element-order of which corresponds to the one of the [enabledProperties].
     * One [WifiProperty] may result in the creation of multiple [WifiPropertyPresentation] elements.
     */
    suspend operator fun invoke(
        enabledProperties: List<WifiProperty>,
        enabledIpSettings: (WifiProperty.IpProperty) -> List<IpSetting>,
        remoteNetworkData: RemoteNetworkData
    ): List<WifiPropertyPresentation> =
        withContext(Dispatchers.Default) {
            resolver(
                enabledProperties = enabledProperties,
                enabledIpSettings = enabledIpSettings,
                remoteNetworkData = remoteNetworkData
            ).flatMap { resolution ->
                resolution.property.toPresentations(
                    values = resolution.values,
                    resources = resources
                )
            }
        }
}

private fun WifiProperty.toPresentations(values: List<WifiPropertyValue>, resources: Resources): List<WifiPropertyPresentation> =
    if (values.size == 1) {
        listOf(toPresentation(values.first(), resources, null))
    } else {
        values.mapIndexed { index, value ->
            toPresentation(value, resources, index + 1)
        }
    }

private fun WifiProperty.toPresentation(
    value: WifiPropertyValue,
    resources: Resources,
    enumeration: Int?
): WifiPropertyPresentation =
    WifiPropertyPresentation(
        label = resolvedLabel(resources::getString, enumeration),
        value = value.value.resolve(resources),
        subValues = value.subValues,
        resolutionError = value.resolutionError
    )

private fun WifiProperty.resolvedLabel(resolveRes: (Int) -> String, enumeration: Int?): SubscriptableText =
    when (this) {
        is WifiProperty.IpProperty -> SubscriptableText(
            text = resolveRes(R.string.ip),
            subscript = enumeratedText(
                text = resolveRes(subscriptResId),
                enumeration = enumeration
            )
        )

        else -> SubscriptableText(enumeratedText(text = resolveRes(labelRes), enumeration = enumeration))
    }

private fun enumeratedText(text: String, enumeration: Int?): String =
    buildString {
        append(text)
        enumeration?.let { append(" $it") }
    }
