package danilovl.calculator.ui.converter

import androidx.lifecycle.ViewModel
import danilovl.calculator.data.model.UnitDef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

data class UnitConverterState(
    val units: List<UnitDef> = emptyList(),
    val fromIndex: Int = 0,
    val toIndex: Int = 1,
    val inputValue: String = "1",
    val result: String = ""
)

class UnitConverterViewModel : ViewModel() {

    private val _state = MutableStateFlow(UnitConverterState())
    val state: StateFlow<UnitConverterState> = _state.asStateFlow()

    fun init(type: ConverterType) {
        val units = unitsForType(type)
        val toIndex = if (units.size > 1) 1 else 0
        _state.update {
            it.copy(
                units = units,
                fromIndex = 0,
                toIndex = toIndex,
                inputValue = "1",
                result = convert("1", 0, toIndex, units)
            )
        }
    }

    fun onKey(key: String) {
        val s = _state.value
        val current = s.inputValue
        val newValue = when (key) {
            "C" -> "0"
            "⌫" -> if (current.length > 1) current.dropLast(1) else "0"
            "," -> if ("." !in current) "$current." else current
            "00" -> if (current == "0") "0" else current + "00"
            "=" -> current
            "%" -> {
                val value = current.toDoubleOrNull() ?: 0.0
                formatValue(value / 100.0)
            }
            else -> appendDigit(current, key)
        }
        val result = convert(newValue, s.fromIndex, s.toIndex, s.units)
        _state.update { it.copy(inputValue = newValue, result = result) }
    }

    fun setFrom(index: Int) {
        val s = _state.value
        val result = convert(s.inputValue, index, s.toIndex, s.units)
        _state.update { it.copy(fromIndex = index, result = result) }
    }

    fun setTo(index: Int) {
        val s = _state.value
        val result = convert(s.inputValue, s.fromIndex, index, s.units)
        _state.update { it.copy(toIndex = index, result = result) }
    }

    private fun appendDigit(current: String, key: String): String {
        if (current.length >= 20) return current
        val replacingLeadingZero = current == "0" && key != "."

        return if (replacingLeadingZero) key else current + key
    }

    private fun convert(input: String, fromIndex: Int, toIndex: Int, units: List<UnitDef>): String {
        val value = input.replace(",", ".").toDoubleOrNull() ?: return "0"
        val from = units.getOrNull(fromIndex) ?: return "0"
        val to = units.getOrNull(toIndex) ?: return "0"
        val baseValue = from.toBase(value)
        val result = to.fromBase(baseValue)

        return formatValue(result)
    }

    private fun formatValue(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"

        val absValue = abs(value)
        if (absValue != 0.0 && (absValue >= 1e15 || (absValue < 1e-7 && absValue > 0))) {
            return "%.8e".format(Locale.US, value)
                .replace(Regex("0+e"), "e")
                .replace(".e", "e")
                .replace("e+0", "e")
                .replace("e+", "e")
                .replace("e-0", "e-")
        }

        val isWholeNumber = value == floor(value) && absValue < 1e12
        if (isWholeNumber) {
            return value.toLong().toString()
        }

        return "%.8f".format(Locale.US, value).trimEnd('0').trimEnd('.')
    }
}
