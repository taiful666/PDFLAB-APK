package com.example

import com.example.data.compactRanges
import com.example.data.flattenGroups
import com.example.data.normalizeDigits
import com.example.data.parseRanges
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun rangeParser_supportsAsciiAndBengaliDigits() {
        val normalized = normalizeDigits("১-৩, ৫, ৭-৮")
        assertEquals("1-3, 5, 7-8", normalized)

        val parsed = parseRanges("১-৩, ৫, ৭-৮", maxPage = 10)
        assertNull(parsed.errorToken)
        assertEquals(listOf(1, 2, 3, 5, 7, 8), flattenGroups(parsed.groups))
        assertEquals("1-3, 5, 7-8", compactRanges(flattenGroups(parsed.groups)))
    }
}
