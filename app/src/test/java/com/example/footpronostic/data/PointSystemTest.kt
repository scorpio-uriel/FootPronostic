package com.example.footpronostic.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PointSystemTest {

    @Test
    fun `score exact should return 3 points multiplied by odds`() {
        // Match real: 2-1, Prono: 2-1, Cote: 2.0 -> 3 * 2.0 = 6
        val result = PointSystem.calculatePoints(2, 1, 2, 1, 2.0)
        assertEquals(6, result)
    }

    @Test
    fun `correct winner but wrong score should return 1 point multiplied by odds`() {
        // Match real: 3-0, Prono: 1-0, Cote: 1.5 -> 1 * 1.5 = 1.5 -> arrondi a 2
        val result = PointSystem.calculatePoints(3, 0, 1, 0, 1.5)
        assertEquals(2, result)
    }

    @Test
    fun `wrong winner should return -1 point multiplied by odds`() {
        // Match real: 0-1, Prono: 2-0, Cote: 2.0 -> -1 * 2.0 = -2
        val result = PointSystem.calculatePoints(0, 1, 2, 0, 2.0)
        assertEquals(-2, result)
    }

    @Test
    fun `correct draw should return 3 points multiplied by odds`() {
        // Match real: 1-1, Prono: 1-1, Cote: 3.0 -> 3 * 3.0 = 9
        val result = PointSystem.calculatePoints(1, 1, 1, 1, 3.0)
        assertEquals(9, result)
    }

    @Test
    fun `correct winner with decimal odds rounding`() {
        // Match real: 2-0, Prono: 1-0, Cote: 2.4 -> 1 * 2.4 = 2.4 -> arrondi a 2
        val result = PointSystem.calculatePoints(2, 0, 1, 0, 2.4)
        assertEquals(2, result)
        
        // Match real: 2-0, Prono: 1-0, Cote: 2.6 -> 1 * 2.6 = 2.6 -> arrondi a 3
        val resultHigh = PointSystem.calculatePoints(2, 0, 1, 0, 2.6)
        assertEquals(3, resultHigh)
    }
}
