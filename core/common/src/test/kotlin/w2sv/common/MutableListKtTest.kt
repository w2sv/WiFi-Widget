package w2sv.common

import com.w2sv.common.utils.moveElement
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class MutableListKtTest {

    private fun elementMovedList(fromIndex: Int, toIndex: Int): List<Int> =
        (0..5).toMutableList().apply { moveElement(fromIndex, toIndex) }

    @Test
    fun `test moveElement`() {
        assertEquals(listOf(0, 3, 1, 2, 4, 5), elementMovedList(3, 1))
        assertEquals(listOf(0, 2, 1, 3, 4, 5), elementMovedList(1, 3))
        assertEquals(listOf(1, 2, 3, 4, 5, 0), elementMovedList(0, 5))
        assertEquals(listOf(5, 0, 1, 2, 3, 4), elementMovedList(5, 0))
        assertEquals(listOf(0, 1, 2, 3, 4, 5), elementMovedList(0, 0))
        assertFailsWith<IndexOutOfBoundsException> { elementMovedList(6, 0) }
    }
}
