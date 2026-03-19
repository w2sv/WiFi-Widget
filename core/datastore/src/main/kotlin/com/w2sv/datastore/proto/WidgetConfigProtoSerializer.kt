package com.w2sv.datastore.proto

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.w2sv.datastore.WidgetConfigProto
import com.w2sv.datastore.proto.mapping.toProto
import com.w2sv.domain.model.widget.WidgetConfig
import java.io.InputStream
import java.io.OutputStream

internal object WidgetConfigProtoSerializer : Serializer<WidgetConfigProto> {

    override val defaultValue: WidgetConfigProto = WidgetConfig.default.toProto()

    override suspend fun readFrom(input: InputStream): WidgetConfigProto =
        try {
            // readFrom is already called on the data store background thread
            WidgetConfigProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }

    override suspend fun writeTo(t: WidgetConfigProto, output: OutputStream) {
        // writeTo is already called on the data store background thread
        t.writeTo(output)
    }
}
