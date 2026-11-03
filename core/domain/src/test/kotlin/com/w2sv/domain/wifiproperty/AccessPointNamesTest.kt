package com.w2sv.domain.wifiproperty

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.junit.Test

class AccessPointNamesTest {

    @Test
    fun `normalizeBssid accepts upper case and dash separators`() {
        assertEquals("aa:bb:cc:00:11:22", AccessPointNames.normalizeBssid(" AA-BB-CC-00-11-22 "))
        assertEquals("aa:bb:cc:00:11:22", AccessPointNames.normalizeBssid("aa:bb:cc:00:11:22"))
    }

    @Test
    fun `normalizeBssid rejects invalid addresses`() {
        assertNull(AccessPointNames.normalizeBssid(""))
        assertNull(AccessPointNames.normalizeBssid("aa:bb:cc:00:11"))
        assertNull(AccessPointNames.normalizeBssid("aa:bb:cc:00:11:zz"))
        assertNull(AccessPointNames.normalizeBssid("aabbcc001122"))
    }

    @Test
    fun `nameOf looks up regardless of case`() {
        val names = AccessPointNames().with("02:00:00:00:03:05", "Kitchen 5G")

        assertEquals("Kitchen 5G", names.nameOf("02:00:00:00:03:05"))
        assertEquals("Kitchen 5G", names.nameOf("02-00-00-00-03-05"))
        assertNull(names.nameOf("02:00:00:00:03:02"))
        assertNull(names.nameOf(null))
    }

    @Test
    fun `with blank name removes entry`() {
        val names = AccessPointNames()
            .with("02:00:00:00:03:05", "Kitchen 5G")
            .with("02:00:00:00:03:05", "  ")

        assertEquals(AccessPointNames(), names)
    }

    @Test
    fun `with invalid bssid throws`() {
        assertFailsWith<IllegalArgumentException> { AccessPointNames().with("nope", "Kitchen") }
    }

    @Test
    fun `without removes entry`() {
        val names = AccessPointNames()
            .with("02:00:00:00:03:05", "Kitchen 5G")
            .with("02:00:00:00:04:05", "Garage 5G")
            .without("02:00:00:00:03:05")

        assertEquals(AccessPointNames(mapOf("02:00:00:00:04:05" to "Garage 5G")), names)
    }

    @Test
    fun `mergedWith gives precedence to other`() {
        val existing = AccessPointNames(mapOf("02:00:00:00:03:05" to "Old", "02:00:00:00:04:05" to "Garage"))
        val imported = AccessPointNames(mapOf("02:00:00:00:03:05" to "Kitchen"))

        assertEquals(
            AccessPointNames(mapOf("02:00:00:00:03:05" to "Kitchen", "02:00:00:00:04:05" to "Garage")),
            existing.mergedWith(imported)
        )
    }

    @Test
    fun `parse skips comments, blank and malformed lines`() {
        val text = """
            # Home routers
            02:00:00:00:03:05|Kitchen 5G

            02-00-00-00-04-02 | Garage | 2.4G
            not-a-mac|Ignored
            02:00:00:00:05:05|
            02:00:00:00:06:05
        """.trimIndent()

        assertEquals(
            AccessPointNames(
                mapOf(
                    "02:00:00:00:03:05" to "Kitchen 5G",
                    "02:00:00:00:04:02" to "Garage | 2.4G"
                )
            ),
            AccessPointNames.parse(text)
        )
    }

    @Test
    fun `file content round trip`() {
        val names = AccessPointNames()
            .with("02:00:00:00:04:05", "Garage 5G")
            .with("02:00:00:00:03:05", "Kitchen 5G")

        assertEquals("02:00:00:00:03:05|Kitchen 5G\n02:00:00:00:04:05|Garage 5G\n", names.toFileContent())
        assertEquals(names, AccessPointNames.parse(names.toFileContent()))
    }
}
