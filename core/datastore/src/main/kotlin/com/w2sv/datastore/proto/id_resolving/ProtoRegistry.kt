package com.w2sv.datastore.proto.id_resolving

import com.w2sv.domain.model.wifiproperty.WithProtoId

/**
 * Generic registry for resolving objects implementing [WithProtoId] from their proto IDs.
 *
 * Subclasses provide the complete list of supported entries, which are then indexed by
 * their `protoId` for fast lookup during proto ↔ external model mapping.
 *
 * The registry validates at initialization that all provided entries use unique proto IDs,
 * helping to detect configuration or schema mistakes early.
 */
internal abstract class ProtoRegistry<T : WithProtoId>(private val entries: List<T>) {

    operator fun invoke(id: Int): T =
        protoIdMap.getValue(id)

    private val protoIdMap: Map<Int, T> by lazy {
        entries
            .associateBy { it.protoId }
            .also { check(it.size == entries.size) { "Duplicate protoId detected in ${this::class.java.simpleName}" } }
    }
}

