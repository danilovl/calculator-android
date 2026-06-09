package danilovl.calculator.ui.calculator

import androidx.lifecycle.ViewModel
import danilovl.calculator.domain.CalculatorEngine
import danilovl.calculator.ui.util.formatDisplayNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HistoryEntry(
    val expression: String,
    val result: String
)

data class CalculatorState(
    val expression: String = "",
    val result: String = "",
    val isScientific: Boolean = false,
    val isSecondMode: Boolean = false,
    val isDegreeMode: Boolean = true,
    val history: List<HistoryEntry> = emptyList(),
    val justEvaluated: Boolean = false
)

class CalculatorViewModel : ViewModel() {

    private companion object {
        const val MAX_HISTORY = 50
        val OPERATORS = setOf("+", "−", "×", "÷")
    }

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun onKey(rawKey: String) {
        val key = if (rawKey == ",") "." else rawKey
        val s = _state.value
        when (key) {
            "C" -> _state.update { it.copy(expression = "", result = "", justEvaluated = false) }
            "⌫" -> {
                if (s.justEvaluated) {
                    _state.update { it.copy(expression = "", result = "", justEvaluated = false) }
                } else {
                    val newExpr = if (s.expression.isNotEmpty()) s.expression.dropLast(1) else ""
                    val newResult = if (newExpr.isNotEmpty()) computePreview(newExpr) else ""
                    _state.update { it.copy(expression = newExpr, result = newResult) }
                }
            }
            "=" -> {
                val expr = s.expression.ifEmpty { return }
                val value = CalculatorEngine.evaluate(expr, s.isDegreeMode)
                val resultStr = CalculatorEngine.formatResult(value)
                val newHistory = (s.history + HistoryEntry(expr, resultStr)).takeLast(MAX_HISTORY)
                _state.update { it.copy(
                    expression = resultStr,
                    result = "",
                    history = newHistory,
                    justEvaluated = true
                ) }
            }
            "%" -> {
                val expr = s.expression
                if (expr.isEmpty()) return
                val value = CalculatorEngine.evaluate(expr, s.isDegreeMode)
                val percentVal = value / 100.0
                val resultStr = CalculatorEngine.formatResult(percentVal)
                _state.update { it.copy(expression = resultStr, result = "", justEvaluated = true) }
            }
            "+/-" -> {
                val expr = s.expression
                if (expr.isEmpty()) return
                val newExpr = if (expr.startsWith("-")) expr.removePrefix("-") else "-$expr"
                _state.update { it.copy(expression = newExpr, result = computePreview(newExpr)) }
            }
            "2nd" -> _state.update { it.copy(isSecondMode = !it.isSecondMode) }
            "deg", "rad" -> _state.update { it.copy(isDegreeMode = !it.isDegreeMode) }
            "π" -> appendToExpr("π")
            "e" -> appendToExpr("e")
            "x!" -> appendFunc("!")
            "1/x" -> wrapCurrentWithFunc("1/(", ")")
            "x²" -> appendToExpr("^2")
            "x³" -> appendToExpr("^3")
            "xʸ" -> appendToExpr("^")
            "²√x" -> prependFunc("sqrt(")
            "³√x" -> prependFunc("cbrt(")
            "ʸ√x" -> appendToExpr("^(1/")
            "sin" -> appendFunc(if (s.isSecondMode) "asin(" else "sin(")
            "cos" -> appendFunc(if (s.isSecondMode) "acos(" else "cos(")
            "tan" -> appendFunc(if (s.isSecondMode) "atan(" else "tan(")
            "ln" -> appendFunc("ln(")
            "lg" -> appendFunc("log(")
            "(" -> appendToExpr("(")
            ")" -> appendToExpr(")")
            "√x" -> appendFunc("sqrt(")
            else -> {
                val operators = OPERATORS
                val isOperator = key in operators
                val isDecimal = key == "."

                val newExpr: String = when {
                    s.justEvaluated && isOperator -> s.expression + key
                    s.justEvaluated -> if (isDecimal) "0." else key

                    s.expression.isEmpty() && isOperator -> return
                    s.expression.isEmpty() && isDecimal -> "0."
                    s.expression.isEmpty() -> key

                    else -> {
                        val last = s.expression.last().toString()
                        val lastIsOp = last in operators
                        val lastIsDecimal = last == "."
                        val lastIsOpenParen = last == "("

                        when {
                            isOperator && lastIsOp -> s.expression.dropLast(1) + key
                            isOperator && lastIsOpenParen -> return
                            isDecimal && (lastIsOp || lastIsOpenParen) -> s.expression + "0."
                            isDecimal && lastIsDecimal -> return
                            isDecimal -> {
                                val sepIdx = s.expression.indexOfLast { c ->
                                    c == '+' || c == '−' || c == '×' || c == '÷' || c == '(' || c == ')'
                                }
                                val segment = if (sepIdx == -1) s.expression
                                              else s.expression.substring(sepIdx + 1)
                                if (segment.contains(".")) return
                                s.expression + key
                            }
                            key.all { it.isDigit() } && s.expression == "0" -> key
                            key.all { it.isDigit() } && s.expression.length >= 2 -> {
                                val prevLast = s.expression[s.expression.length - 2].toString()
                                val curLast = s.expression.last().toString()
                                if (curLast == "0" && prevLast in operators) {
                                    s.expression.dropLast(1) + key
                                } else {
                                    s.expression + key
                                }
                            }
                            else -> s.expression + key
                        }
                    }
                }
                val newResult = computePreview(newExpr)
                _state.update { it.copy(expression = newExpr, result = newResult, justEvaluated = false) }
                return
            }
        }
    }

    private fun appendToExpr(suffix: String) {
        val s = _state.value
        val newExpr = if (s.justEvaluated) suffix else s.expression + suffix
        _state.update { it.copy(expression = newExpr, result = computePreview(newExpr), justEvaluated = false) }
    }

    private fun appendFunc(funcPrefix: String) {
        val s = _state.value
        val newExpr = s.expression + funcPrefix
        _state.update { it.copy(expression = newExpr, result = computePreview(newExpr), justEvaluated = false) }
    }

    private fun prependFunc(funcPrefix: String) {
        val s = _state.value
        val newExpr = funcPrefix + s.expression + ")"
        _state.update { it.copy(expression = newExpr, result = computePreview(newExpr), justEvaluated = false) }
    }

    private fun wrapCurrentWithFunc(prefix: String, suffix: String) {
        val s = _state.value
        val newExpr = prefix + s.expression + suffix
        _state.update { it.copy(expression = newExpr, result = computePreview(newExpr), justEvaluated = false) }
    }

    private fun computePreview(expr: String): String {
        if (expr.isEmpty()) return ""
        val value = CalculatorEngine.evaluate(expr, _state.value.isDegreeMode)
        if (value.isNaN() || value.isInfinite()) return ""

        return "= ${formatDisplayNumber(CalculatorEngine.formatResult(value))}"
    }

    fun toggleScientific() {
        _state.update { it.copy(isScientific = !it.isScientific) }
    }

    fun clearHistory() {
        _state.update { it.copy(history = emptyList()) }
    }
}
