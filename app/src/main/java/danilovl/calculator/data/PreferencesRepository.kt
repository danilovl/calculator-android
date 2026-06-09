package danilovl.calculator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "calculator_prefs")

class PreferencesRepository(private val context: Context) {

    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_CURRENCY_RATES = stringPreferencesKey("currency_rates")
        val KEY_ACTIVE_CURRENCIES = stringPreferencesKey("active_currencies")
        val KEY_RATES_TIMESTAMP = longPreferencesKey("rates_timestamp")
        const val DEFAULT_LANGUAGE = "en"
    }

    val languageFlow: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_LANGUAGE] ?: DEFAULT_LANGUAGE }

    val currencyRatesFlow: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_CURRENCY_RATES] ?: "" }

    val activeCurrenciesFlow: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_ACTIVE_CURRENCIES] ?: "" }

    val ratesTimestampFlow: Flow<Long> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_RATES_TIMESTAMP] ?: 0L }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { prefs -> prefs[KEY_LANGUAGE] = language }
    }

    suspend fun saveCurrencyRates(ratesJson: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CURRENCY_RATES] = ratesJson
            prefs[KEY_RATES_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    suspend fun saveActiveCurrencies(currenciesJson: String) {
        context.dataStore.edit { prefs -> prefs[KEY_ACTIVE_CURRENCIES] = currenciesJson }
    }
}
