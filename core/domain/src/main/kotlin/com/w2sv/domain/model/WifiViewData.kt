package com.w2sv.domain.model

sealed interface WifiViewData {
    val label: String
    val value: String

    val ipPropertyOrNull: IPProperty?
        get() = this as? IPProperty

    data class NonIP(override val value: String, override val label: String) : WifiViewData

    data class IPProperty(override val label: String, override val value: String, private val subPropertyValues: List<String>) :
        WifiViewData {

        val nonEmptySubPropertyValuesOrNull: List<String>?
            get() = subPropertyValues.ifEmpty { null }
    }

    fun interface Provider {
        /**
         * @return List of [WifiViewData], the element-order of which corresponds to the one of the [properties].
         * One [WifiProperty] may result in the the creation of multiple [WifiViewData] elements.
         */
        operator fun invoke(
            properties: Iterable<WifiProperty>,
            ipSubProperties: Collection<WifiProperty.IP.SubProperty>,
            remoteNetworkInfo: RemoteNetworkInfo
        ): List<WifiViewData>
    }
}
