package com.w2sv.widget.ui.actions.clipboard

import android.widget.Toast
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.Test

class ToastManagerTest {
    @Test fun `a new clipboard confirmation cancels the previous toast before showing`() {
        val manager = ToastManager()
        val first = mockk<Toast>(relaxed = true)
        val second = mockk<Toast>(relaxed = true)

        manager.cancelPreviousAndShow(first)
        manager.cancelPreviousAndShow(second)

        verifyOrder {
            first.show()
            first.cancel()
            second.show()
        }
    }
}
