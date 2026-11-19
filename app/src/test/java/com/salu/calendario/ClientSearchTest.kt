package com.salu.calendario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class ClientSearchTest {

    @Test
    fun testNormalizeAccentsAndCasing() {
        assertEquals("fatima", ClientSearchHelper.normalize("Fátima"))
        assertEquals("fatima", ClientSearchHelper.normalize("FÁTIMA"))
        assertEquals("gloria", ClientSearchHelper.normalize("Glória"))
        assertEquals("joao", ClientSearchHelper.normalize("JOÃO"))
        assertEquals("claudia", ClientSearchHelper.normalize("Cláudia"))
        assertEquals("valeria", ClientSearchHelper.normalize("valéria"))
    }

    @Test
    fun testSearchInsensitiveToAccentsAndCase() {
        val month1Dec2026 = """
            {
              "slots": [
                { "slot": 2, "day": 1, "hasRedX": false, "names": ["Fátima", "Glória"] },
                { "slot": 6, "day": 5, "hasRedX": false, "names": ["Milka Noiva"] }
              ]
            }
        """.trimIndent()

        val month2Nov2026 = """
            {
              "slots": [
                { "slot": 10, "day": 10, "hasRedX": false, "names": ["fatima silva", "Joana"] }
              ]
            }
        """.trimIndent()

        val mockEntries = mapOf(
            "month_data_2026_12" to month1Dec2026,
            "month_data_2026_11" to month2Nov2026,
            "layout_config" to "{}"
        )

        // Query with lowercase and no accents
        val results1 = ClientSearchHelper.search(mockEntries, "fatima")
        assertEquals(2, results1.size)
        // Dec 2026 comes first (descending)
        assertEquals("Fátima", results1[0].name)
        assertEquals(YearMonth.of(2026, 12), results1[0].yearMonth)
        assertEquals(1, results1[0].day)
        assertEquals(2, results1[0].slotIndex)

        // Nov 2026 comes second
        assertEquals("fatima silva", results1[1].name)
        assertEquals(YearMonth.of(2026, 11), results1[1].yearMonth)
        assertEquals(10, results1[1].day)

        // Query with uppercase and accent
        val results2 = ClientSearchHelper.search(mockEntries, "GLÓRIA")
        assertEquals(1, results2.size)
        assertEquals("Glória", results2[0].name)
        assertEquals(YearMonth.of(2026, 12), results2[0].yearMonth)

        // Query with substring without accent
        val results3 = ClientSearchHelper.search(mockEntries, "glor")
        assertEquals(1, results3.size)
        assertEquals("Glória", results3[0].name)
    }

    @Test
    fun testSearchQueryLessThanTwoCharsReturnsEmpty() {
        val mockEntries = mapOf(
            "month_data_2026_12" to """
                {
                  "slots": [
                    { "slot": 2, "day": 1, "names": ["Fátima"] }
                  ]
                }
            """.trimIndent()
        )

        val resultsShort = ClientSearchHelper.search(mockEntries, "f")
        assertTrue(resultsShort.isEmpty())

        val resultsEmpty = ClientSearchHelper.search(mockEntries, "")
        assertTrue(resultsEmpty.isEmpty())

        val resultsWhitespace = ClientSearchHelper.search(mockEntries, "   ")
        assertTrue(resultsWhitespace.isEmpty())
    }
}
