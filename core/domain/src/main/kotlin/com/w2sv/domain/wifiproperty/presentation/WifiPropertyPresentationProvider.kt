package com.w2sv.domain.wifiproperty.presentation

import android.content.res.Resources
import com.w2sv.core.resources.R
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.wifiproperty.AccessPointNames
import com.w2sv.domain.wifiproperty.Text
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyResolver
import com.w2sv.domain.wifiproperty.WifiPropertyValue
import com.w2sv.domain.wifiproperty.settings.IpSetting
import com.w2sv.domain.wifiproperty.txt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WifiPropertyPresentationProvider(private val resolver: WifiPropertyResolver, private val resources: Resources) {
    /**
     * @return List of [WifiPropertyPresentation], the element-order of which corresponds to the one of the [enabledProperties].
     * One [WifiProperty] may result in the creation of multiple [WifiPropertyPresentation] elements.
     * [onUnresolved] can supply a value for a property with no resolved values; by default such properties are omitted.
     * [accessPointNames] replace the BSSIDs resolved for [WifiProperty.AccessPointName] where assigned.
     */
    suspend operator fun invoke(
        enabledProperties: List<WifiProperty>,
        enabledIpSettings: (WifiProperty.IpProperty) -> List<IpSetting>,
        remoteNetworkData: RemoteNetworkData,
        accessPointNames: AccessPointNames = AccessPointNames(),
        onUnresolved: (WifiProperty) -> WifiPropertyValue? = { null }
    ): List<WifiPropertyPresentation> =
        withContext(Dispatchers.Default) {
            resolver(
                enabledProperties = enabledProperties,
                enabledIpSettings = enabledIpSettings,
                remoteNetworkData = remoteNetworkData
            ).flatMap { resolution ->
                val values = when (resolution.property) {
                    WifiProperty.AccessPointName -> resolution.values.map { it.withAccessPointName(accessPointNames) }
                    else -> resolution.values
                }
                resolution.property.toPresentations(
                    values = values.ifEmpty { listOfNotNull(onUnresolved(resolution.property)) },
                    resources = resources
                )
            }
        }
}

/** Swaps a successfully resolved BSSID for its assigned name; error placeholders and unnamed BSSIDs are kept. */
private fun WifiPropertyValue.withAccessPointName(accessPointNames: AccessPointNames): WifiPropertyValue {
    val bssid = (value as? Text.Raw)?.value?.takeIf { resolutionError == null } ?: return this
    return accessPointNames.nameOf(bssid)?.let { copy(value = it.txt) } ?: this
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
