package com.w2sv.networking.model

import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test

class IfConfigDataTest {

    @Test
    fun fetch() = runTest {
        assertNotNull(IFConfigData.fetch(client = OkHttpClient()))
    }
}