package danilovl.calculator.ui.converter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import danilovl.calculator.data.CurrencyRepository
import danilovl.calculator.data.PreferencesRepository
import danilovl.calculator.data.model.CurrencyInfo
import danilovl.calculator.domain.CalculatorEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CurrencyConverterState(
    val activeCurrencies: List<CurrencyInfo> = emptyList(),
    val rates: Map<String, Double> = emptyMap(),
    val inputIndex: Int = 0,
    val inputValue: String = "0",
    val isLoading: Boolean = false,
    val isOffline: Boolean = true,
    val decimals: Int = 0
)

class CurrencyConverterViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = PreferencesRepository(application)
    private val currencyRepo = CurrencyRepository(prefsRepo)

    private val _state = MutableStateFlow(CurrencyConverterState())
    val state: StateFlow<CurrencyConverterState> = _state.asStateFlow()

    init {
        loadData()
        viewModelScope.launch {
            prefsRepo.currencyDecimalsFlow.collect { decimals ->
                _state.update { it.copy(decimals = decimals) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val currencies = currencyRepo.getActiveCurrencies()
            _state.update { it.copy(activeCurrencies = currencies) }
            val result = currencyRepo.getRatesWithStatus()
            _state.update {
                it.copy(
                    rates = result.rates,
                    isLoading = false,
                    isOffline = !result.isOnline
                )
            }
        }
    }

    fun onKey(rawKey: String) {
        val key = if (rawKey == ",") "." else rawKey
        val s = _state.value
        val current = s.inputValue
        val operators = setOf("+", "−", "×", "÷", "%")

        val newValue = when (key) {
            "C" -> "0"
            "⌫" -> if (current.length > 1) current.dropLast(1) else "0"
            "." -> {
                val sepIdx = current.indexOfLast { it.toString() in operators }
                val segment = if (sepIdx == -1) current else current.substring(sepIdx + 1)
                if ("." in segment) current
                else if (current.isEmpty() || current.last().toString() in operators) current + "0."
                else current + "."
            }
            "00" -> {
                if (current == "0") "0"
                else if (current.last().toString() in operators) current + "0"
                else if (current.length >= 19) current
                else current + "00"
            }
            "=" -> {
                val result = CalculatorEngine.evaluate(current)
                if (result.isNaN() || result.isInfinite()) current
                else CalculatorEngine.formatResult(result)
            }
            else -> {
                val isOperator = key in operators
                if (isOperator) {
                    if (current.isEmpty() || current == "0") current
                    else if (current.last().toString() in operators) current.dropLast(1) + key
                    else current + key
                } else {
                    if (current == "0") key
                    else if (current.length >= 20) current
                    else current + key
                }
            }
        }
        _state.update { it.copy(inputValue = newValue) }
    }

    fun setActiveInput(index: Int) {
        val converted = getConvertedValue(index)
        val newValue = converted.toDoubleOrNull()?.let {
            if (it == Math.floor(it) && it < 1e15) it.toLong().toString()
            else "%.4f".format(java.util.Locale.US, it).trimEnd('0').trimEnd('.')
        } ?: converted
        _state.update { it.copy(inputIndex = index, inputValue = newValue) }
    }

    fun getConvertedValue(targetIndex: Int): String {
        val s = _state.value
        if (s.activeCurrencies.isEmpty() || s.rates.isEmpty()) return "0"
        if (targetIndex == s.inputIndex) return s.inputValue

        val input = s.inputValue
        val operators = setOf("+", "−", "×", "÷", "%")
        
        if (input.isNotEmpty() && input.last().toString() in operators) {
            return "0"
        }

        val amount = if (input.isEmpty()) 0.0 else CalculatorEngine.evaluate(input)
        if (amount.isNaN() || amount.isInfinite()) return "0"

        val fromCode = s.activeCurrencies.getOrNull(s.inputIndex)?.code ?: return "0"
        val toCode = s.activeCurrencies.getOrNull(targetIndex)?.code ?: return "0"

        val result = currencyRepo.convert(amount, fromCode, toCode, s.rates)
        if (result.isNaN() || result.isInfinite()) return "Error"

        val absResult = Math.abs(result)
        if (absResult != 0.0 && (absResult >= 1e15 || (absResult < 1e-4 && absResult > 0))) {
            return "%.8e".format(java.util.Locale.US, result)
                .replace(Regex("0+e"), "e")
                .replace(".e", "e")
                .replace("e+0", "e")
                .replace("e+", "e")
                .replace("e-0", "e-")
        }

        return if (result == Math.floor(result) && absResult < 1e15) {
            result.toLong().toString()
        } else {
            "%.4f".format(java.util.Locale.US, result).trimEnd('0').trimEnd('.')
        }
    }

    fun addCurrency(currency: CurrencyInfo) {
        val s = _state.value
        if (s.activeCurrencies.any { it.code == currency.code }) return
        val updated = s.activeCurrencies + currency
        _state.update { it.copy(activeCurrencies = updated) }
        viewModelScope.launch { currencyRepo.saveActiveCurrencies(updated) }
    }

    fun removeCurrency(currency: CurrencyInfo) {
        val s = _state.value
        if (s.activeCurrencies.size <= 2) return
        val updated = s.activeCurrencies.filter { it.code != currency.code }
        val newInputIndex = if (s.inputIndex >= updated.size) 0 else s.inputIndex
        _state.update { it.copy(activeCurrencies = updated, inputIndex = newInputIndex) }
        viewModelScope.launch { currencyRepo.saveActiveCurrencies(updated) }
    }

    fun moveCurrency(from: Int, to: Int) {
        val s = _state.value
        if (from == to || from < 0 || to < 0 ||
            from >= s.activeCurrencies.size || to >= s.activeCurrencies.size) return
        val list = s.activeCurrencies.toMutableList()
        val item = list.removeAt(from)
        list.add(to, item)
        val newFirstOldIndex = s.activeCurrencies.indexOfFirst { it.code == list[0].code }
        val rawConverted = if (newFirstOldIndex == s.inputIndex) s.inputValue else getConvertedValue(newFirstOldIndex)
        val newInputValue = rawConverted.toDoubleOrNull()?.let {
            if (it == Math.floor(it) && it < 1e15) it.toLong().toString()
            else "%.4f".format(java.util.Locale.US, it).trimEnd('0').trimEnd('.')
        } ?: rawConverted
        _state.update { it.copy(activeCurrencies = list, inputIndex = 0, inputValue = newInputValue) }
        viewModelScope.launch { currencyRepo.saveActiveCurrencies(list) }
    }

    fun getAllAvailable(): List<CurrencyInfo> = currencyRepo.getAllAvailableCurrencies()

    fun setDecimals(decimals: Int) {
        _state.update { it.copy(decimals = decimals) }
        viewModelScope.launch { prefsRepo.saveCurrencyDecimals(decimals) }
    }
}
