package com.w2sv.wifiwidget.integration

import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/** In-process transport: production fetchers still create and parse their normal requests. */
@Singleton
class TestRemoteHttpResponses @Inject constructor() : Interceptor {
    enum class Endpoint { IpApi, IpV4, IpV6 }

    private sealed interface Reply {
        data class Body(val value: String, val gate: CountDownLatch? = null) : Reply
        data class Failure(val message: String) : Reply
    }

    private val replies = ConcurrentHashMap<Endpoint, ConcurrentLinkedQueue<Reply>>()
    private val requests = CopyOnWriteArrayList<Endpoint>()
    private val gates = CopyOnWriteArrayList<CountDownLatch>()

    fun respondWithIpApi(isp: String, asn: String = "AS424242 Integration") {
        enqueue(Endpoint.IpApi, Reply.Body("""{"status":"success","isp":"$isp","as":"$asn"}"""))
    }

    fun respondWithDelayedIpApi(isp: String): CountDownLatch =
        CountDownLatch(1).also { gate ->
            gates.add(gate)
            enqueue(Endpoint.IpApi, Reply.Body("""{"status":"success","isp":"$isp","as":"AS424242 Integration"}""", gate))
        }

    fun respondWithIpV4(address: String) =
        enqueue(Endpoint.IpV4, Reply.Body(address))

    fun fail(endpoint: Endpoint) =
        enqueue(endpoint, Reply.Failure("Controlled $endpoint failure"))

    fun count(endpoint: Endpoint): Int =
        requests.count { it == endpoint }

    fun recordedRequests(): List<Endpoint> =
        requests.toList()

    fun reset() {
        gates.forEach(CountDownLatch::countDown)
        gates.clear()
        replies.clear()
        requests.clear()
    }

    private fun enqueue(endpoint: Endpoint, reply: Reply) {
        replies.computeIfAbsent(endpoint) { ConcurrentLinkedQueue() }.add(reply)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val endpoint = when (request.url.host) {
            "ip-api.com" -> Endpoint.IpApi
            "api.ipify.org" -> Endpoint.IpV4
            "api6.ipify.org" -> Endpoint.IpV6
            else -> throw IOException("Unexpected HTTP endpoint: ${request.url}")
        }
        requests.add(endpoint)
        val reply = replies[endpoint]?.poll()
            ?: throw IOException("No $endpoint response configured for ${request.url}; requests=$requests")
        val body = when (reply) {
            is Reply.Failure -> throw IOException(reply.message)
            is Reply.Body -> {
                if (reply.gate != null && !reply.gate.await(30, TimeUnit.SECONDS)) {
                    throw IOException("Timed out waiting to release $endpoint response")
                }
                reply.value
            }
        }
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body.toResponseBody("text/plain".toMediaType()))
            .build()
    }
}
