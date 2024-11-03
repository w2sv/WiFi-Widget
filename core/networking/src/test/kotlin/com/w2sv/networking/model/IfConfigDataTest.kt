package com.w2sv.networking.model

import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test

class IfConfigDataTest {

    @Test
    fun fetch() =
        runTest {
            assertTrue(IFConfigData.fetch(client = OkHttpClient()).isSuccess)
        }
}
