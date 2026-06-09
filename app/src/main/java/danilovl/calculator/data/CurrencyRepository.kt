package danilovl.calculator.data

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import danilovl.calculator.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

private const val RATES_URL = "https://open.er-api.com/v6/latest/USD"
private const val CACHE_MAX_AGE_MS = 3_600_000L

data class RatesResult(val rates: Map<String, Double>, val isOnline: Boolean)

class CurrencyRepository(private val preferencesRepository: PreferencesRepository) {

    private val gson = Gson()

    suspend fun getActiveCurrencies(): List<CurrencyInfo> {
        val json = preferencesRepository.activeCurrenciesFlow.first()
        if (json.isBlank()) return emptyList()
        
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            val codes: List<String> = gson.fromJson(json, type)
            codes.mapNotNull { code -> ALL_CURRENCIES.find { it.code == code } }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveActiveCurrencies(currencies: List<CurrencyInfo>) {
        val codes = currencies.map { it.code }
        preferencesRepository.saveActiveCurrencies(gson.toJson(codes))
    }

    suspend fun getRatesWithStatus(): RatesResult {
        val cachedJson = preferencesRepository.currencyRatesFlow.first()
        val timestamp = preferencesRepository.ratesTimestampFlow.first()
        val now = System.currentTimeMillis()

        if (cachedJson.isNotBlank() && (now - timestamp) < CACHE_MAX_AGE_MS) {
            return RatesResult(parseCachedRates(cachedJson), isOnline = false)
        }

        val online = tryFetchOnlineRates()
        if (online != null) {
            preferencesRepository.saveCurrencyRates(gson.toJson(online))
            
            return RatesResult(online, isOnline = true)
        }

        if (cachedJson.isNotBlank()) {
            return RatesResult(parseCachedRates(cachedJson), isOnline = false)
        }

        return RatesResult(emptyMap(), isOnline = false)
    }

    private fun parseCachedRates(json: String): Map<String, Double> {
        return try {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private suspend fun tryFetchOnlineRates(): Map<String, Double>? {
        return withContext(Dispatchers.IO) {
            try {
                val conn = URL(RATES_URL).openConnection() as HttpURLConnection
                conn.connectTimeout = 5_000
                conn.readTimeout = 5_000
                conn.requestMethod = "GET"
                val body = conn.inputStream.bufferedReader().readText()
                conn.disconnect()

                val root = JsonParser.parseString(body).asJsonObject
                if (root.get("result")?.asString != "success") return@withContext null
                val ratesObj = root.getAsJsonObject("rates") ?: return@withContext null
                buildMap {
                    for ((key, value) in ratesObj.entrySet()) {
                        put(key, value.asDouble)
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun convert(amount: Double, fromCode: String, toCode: String, rates: Map<String, Double>): Double {
        val fromRate = rates[fromCode] ?: return 0.0
        val toRate = rates[toCode] ?: return 0.0
        if (fromRate == 0.0) {
            return 0.0
        }
        
        return amount / fromRate * toRate
    }

    fun getAllAvailableCurrencies(): List<CurrencyInfo> = ALL_CURRENCIES
}
