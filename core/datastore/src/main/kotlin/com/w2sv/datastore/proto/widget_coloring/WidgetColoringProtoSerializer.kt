package com.w2sv.datastore.proto.widget_coloring

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.w2sv.datastore.WidgetColoringProto
import java.io.InputStream
import java.io.OutputStream

internal object WidgetColoringProtoSerializer : Serializer<WidgetColoringProto> {

    override val defaultValue: WidgetColoringProto = defaultWidgetColoringProto

    override suspend fun readFrom(input: InputStream): WidgetColoringProto =
        try {
            // readFrom is already called on the data store background thread
            WidgetColoringProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }

    override suspend fun writeTo(t: WidgetColoringProto, output: OutputStream) {
        // writeTo is already called on the data store background thread
        t.writeTo(output)
    }
}