package danilovl.calculator.domain

import danilovl.calculator.data.model.CurrencyInfo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CurrencySearchServiceTest {

    private lateinit var service: CurrencySearchService

    private val currencies = listOf(
        CurrencyInfo("USD", "US Dollar"),
        CurrencyInfo("EUR", "Euro"),
        CurrencyInfo("GBP", "British Pound"),
        CurrencyInfo("CZK", "Czech Koruna"),
        CurrencyInfo("RON", "Romanian Leu"),
        CurrencyInfo("MDL", "Moldovan Leu"),
        CurrencyInfo("AUD", "Australian Dollar"),
        CurrencyInfo("CAD", "Canadian Dollar")
    )

    @Before
    fun setUp() {
        service = CurrencySearchService()
    }

    @Test
    fun `empty query returns all non-excluded currencies`() {
        val result = service.search(currencies, emptyList(), "")
        assertEquals(currencies.size, result.size)
    }

    @Test
    fun `empty query excludes active currencies`() {
        val result = service.search(currencies, listOf("USD", "EUR"), "")
        assertEquals(currencies.size - 2, result.size)
        assertFalse(result.any { it.code == "USD" })
        assertFalse(result.any { it.code == "EUR" })
    }

    @Test
    fun `search by exact code is case insensitive`() {
        val result = service.search(currencies, emptyList(), "czk")
        assertTrue(result.any { it.code == "CZK" })
    }

    @Test
    fun `search by code prefix puts exact match first`() {
        val result = service.search(currencies, emptyList(), "eu")
        assertEquals("EUR", result.first().code)
    }

    @Test
    fun `search eu does not return RON or MDL first`() {
        val result = service.search(currencies, emptyList(), "eu")
        val eurIndex = result.indexOfFirst { it.code == "EUR" }
        val ronIndex = result.indexOfFirst { it.code == "RON" }
        val mdlIndex = result.indexOfFirst { it.code == "MDL" }
        assertTrue("EUR должен быть выше RON", eurIndex < ronIndex)
        assertTrue("EUR должен быть выше MDL", eurIndex < mdlIndex)
    }

    @Test
    fun `search by currency name`() {
        val result = service.search(currencies, emptyList(), "Dollar")
        assertTrue(result.any { it.code == "USD" })
        assertTrue(result.any { it.code == "AUD" })
        assertTrue(result.any { it.code == "CAD" })
        assertFalse(result.any { it.code == "EUR" })
    }

    @Test
    fun `search by name is case insensitive`() {
        val result = service.search(currencies, emptyList(), "dollar")
        assertTrue(result.any { it.code == "USD" })
    }

    @Test
    fun `no results for unknown query`() {
        val result = service.search(currencies, emptyList(), "XYZ123")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `excluded currency does not appear even if matches query`() {
        val result = service.search(currencies, listOf("EUR"), "eu")
        assertFalse(result.any { it.code == "EUR" })
    }

    @Test
    fun `exact code match ranked first`() {
        val result = service.search(currencies, emptyList(), "GBP")
        assertEquals("GBP", result.first().code)
    }

    @Test
    fun `search leu returns both RON and MDL`() {
        val result = service.search(currencies, emptyList(), "Leu")
        assertTrue(result.any { it.code == "RON" })
        assertTrue(result.any { it.code == "MDL" })
    }

    @Test
    fun `whitespace trimmed from query`() {
        val result = service.search(currencies, emptyList(), "  USD  ")
        assertTrue(result.any { it.code == "USD" })
        assertEquals("USD", result.first().code)
    }
}
