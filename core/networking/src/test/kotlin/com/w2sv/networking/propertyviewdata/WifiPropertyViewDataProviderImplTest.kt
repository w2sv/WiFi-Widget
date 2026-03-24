package com.w2sv.networking.propertyviewdata

import org.junit.Assert.assertEquals
import org.junit.Test

class WifiPropertyViewDataProviderImplTest {

    @Test
    fun textualIPv4Representation() {
        assertEquals("127.0.0.1", textualIPv4Representation(0x0100007F))
        assertEquals("255.255.255.255", textualIPv4Representation(-1))
        assertEquals("192.168.1.1", textualIPv4Representation(0x0101A8C0))
    }
}
