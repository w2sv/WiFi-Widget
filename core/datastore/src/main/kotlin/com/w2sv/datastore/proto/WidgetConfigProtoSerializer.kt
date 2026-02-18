package com.w2sv.datastore.proto

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.w2sv.datastore.WifiWidgetConfigProto
import com.w2sv.datastore.proto.mapping.toProto
import com.w2sv.domain.model.widget.WifiWidgetConfig
import java.io.InputStream
import java.io.OutputStream

internal object WidgetConfigProtoSerializer : Serializer<WifiWidgetConfigProto> {

    override val defaultValue: WifiWidgetConfigProto = WifiWidgetConfig.default.toProto()

    override suspend fun readFrom(input: InputStream): WifiWidgetConfigProto =
        try {
            // readFrom is already called on the data store background thread
            WifiWidgetConfigProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }

    override suspend fun writeTo(t: WifiWidgetConfigProto, output: OutputStream) {
        // writeTo is already called on the data store background thread
        t.writeTo(output)
    }
}
