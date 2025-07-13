package com.w2sv.datastore.proto.widgetwifistate

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.w2sv.datastore.WidgetWifiStateProto
import com.w2sv.datastore.WifiStatusProto
import com.w2sv.datastore.widgetWifiStateProto
import java.io.InputStream
import java.io.OutputStream

object WidgetWifiStateProtoSerializer : Serializer<WidgetWifiStateProto> {

    override val defaultValue = widgetWifiStateProto { type = WifiStatusProto.NO_CONNECTION }

    override suspend fun readFrom(input: InputStream): WidgetWifiStateProto =
        try {
            // readFrom is already called on the data store background thread
            WidgetWifiStateProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }

    override suspend fun writeTo(t: WidgetWifiStateProto, output: OutputStream) {
        // writeTo is already called on the data store background thread
        t.writeTo(output)
    }
}
