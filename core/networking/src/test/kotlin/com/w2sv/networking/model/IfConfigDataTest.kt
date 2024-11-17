package com.w2sv.networking.model

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test

class IfConfigDataTest {

    @Test
    fun fetch() =
        runTest {
            println(IFConfigData.fetch(client = OkHttpClient()).getOrThrow())
        }
}
