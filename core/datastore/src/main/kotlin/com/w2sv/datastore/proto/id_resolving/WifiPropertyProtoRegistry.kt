package com.w2sv.datastore.proto.id_resolving

import com.w2sv.domain.model.wifiproperty.WifiProperty

/**
 * Central lookup for resolving [WifiProperty] instances from their proto IDs.
 */
internal object WifiPropertyProtoRegistry : ProtoRegistry<WifiProperty>(WifiProperty.Companion.entries)
