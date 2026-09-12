package danilovl.calculator.domain

import java.util.Locale
import kotlin.math.*

object CalculatorEngine {

    private val FUNCTIONS = listOf(
        "asin", "acos", "atan", "sinh", "cosh", "tanh",
        "sin", "cos", "tan",
        "sqrt", "cbrt", "log10", "log2", "log", "ln",
        "abs", "ceil", "floor", "round", "exp", "sgn"
    )

    private val DEGREE_INPUT_FUNCTIONS = setOf("sin", "cos", "tan")
    private val DEGREE_OUTPUT_FUNCTIONS = setOf("asin", "acos", "atan")

    fun evaluate(expression: String, degreeMode: Boolean = false): Double {
        return try {
            val normalized = normalize(expression)
            Parser(normalized, degreeMode).parse()
        } catch (e: Exception) {
            Double.NaN
        }
    }

    fun formatResult(value: Double, maxFractionDigits: Int = 10): String {
        if (value.isNaN() || value.isInfinite()) return "Error"

        val absValue = abs(value)
        if (absValue != 0.0 && (absValue >= 1e15 || (absValue < 1e-7 && absValue > 0))) {
            return "%.${maxFractionDigits}e".format(Locale.US, value)
                .replace(Regex("0+e"), "e")
                .replace(".e", "e")
                .replace("e+0", "e")
                .replace("e+", "e")
                .replace("e-0", "e-")
        }

        val isWholeNumber = value == floor(value) && absValue < 1e15
        if (isWholeNumber) {
            return value.toLong().toString()
        }

        val result = "%.${maxFractionDigits}f".format(Locale.US, value).trimEnd('0').trimEnd('.')
        return if (result == "-0") "0" else result
    }

    fun formatHistoryResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        val absValue = abs(value)
        // For history, we use a lower threshold for scientific notation and less precision to fit in one line
        if (absValue != 0.0 && (absValue >= 1e10 || (absValue < 1e-7 && absValue > 0))) {
            return "%.6e".format(Locale.US, value)
                .replace(Regex("0+e"), "e")
                .replace(".e", "e")
                .replace("e+0", "e")
                .replace("e+", "e")
                .replace("e-0", "e-")
        }
        return formatResult(value, maxFractionDigits = 6)
    }

    private fun normalize(expression: String): String {
        return expression
            .replace('×', '*')
            .replace('÷', '/')
            .replace('−', '-')
            .replace(',', '.')
    }

    private fun factorial(n: Int): Long {
        if (n < 0) return -1
        if (n > 20) return Long.MAX_VALUE

        var result = 1L
        for (i in 2..n) {
            result *= i
        }

        return result
    }

    private class Parser(private val input: String, private val degreeMode: Boolean) {

        private var pos = 0

        fun parse(): Double {
            val result = parseExpression()
            if (pos < input.length) {
                throw IllegalArgumentException("Unexpected char at $pos")
            }

            return result
        }

        private fun parseExpression(): Double {
            var result = parseTerm()
            while (pos < input.length) {
                when {
                    input[pos] == '+' -> { pos++; result += parseTerm() }
                    input[pos] == '-' -> { pos++; result -= parseTerm() }
                    else -> break
                }
            }

            return result
        }

        private fun parseTerm(): Double {
            var result = parseFactor()
            while (pos < input.length) {
                when (input.getOrNull(pos)) {
                    '*' -> { pos++; result *= parseFactor() }
                    '/' -> {
                        pos++
                        val divisor = parseFactor()
                        result = if (divisor == 0.0) Double.NaN else result / divisor
                    }
                    '%' -> { pos++; result %= parseFactor() }
                    else -> break
                }
            }

            return result
        }

        private fun parseFactor(): Double {
            skipSpaces()
            if (pos >= input.length) {
                throw IllegalArgumentException("Unexpected end of expression")
            }

            if (input[pos] == '-') {
                pos++
                return -parseFactor()
            }
            if (input[pos] == '+') {
                pos++
                return parseFactor()
            }

            val functionResult = tryParseFunction()
            if (functionResult != null) {
                return functionResult
            }

            if (input[pos] == '(') {
                pos++
                val grouped = parseExpression()
                if (pos < input.length && input[pos] == ')') {
                    pos++
                }

                return tryParseSuffix(grouped)
            }

            if (input[pos] == 'π') {
                pos++
                return tryParseSuffix(PI)
            }
            if (input[pos] == 'e') {
                pos++
                return tryParseSuffix(E)
            }

            val number = parseNumber()
            return tryParseSuffix(number)
        }

        private fun tryParseFunction(): Double? {
            for (function in FUNCTIONS) {
                if (!input.startsWith(function, pos)) continue

                val openParen = skipSpacesFrom(pos + function.length)
                if (input.getOrNull(openParen) != '(') continue

                pos = openParen + 1
                val arg = parseExpression()
                if (pos < input.length && input[pos] == ')') {
                    pos++
                }
                val result = applyFunction(function, arg)

                return tryParseSuffix(result)
            }

            return null
        }

        private fun applyFunction(function: String, rawArg: Double): Double {
            val arg = if (degreeMode && function in DEGREE_INPUT_FUNCTIONS) {
                Math.toRadians(rawArg)
            } else {
                rawArg
            }

            val result = when (function) {
                "sin" -> sin(arg)
                "cos" -> cos(arg)
                "tan" -> tan(arg)
                "asin" -> asin(arg)
                "acos" -> acos(arg)
                "atan" -> atan(arg)
                "sinh" -> sinh(arg)
                "cosh" -> cosh(arg)
                "tanh" -> tanh(arg)
                "sqrt" -> sqrt(arg)
                "cbrt" -> cbrt(arg)
                "log", "log10" -> log10(arg)
                "log2" -> log2(arg)
                "ln" -> ln(arg)
                "abs" -> abs(arg)
                "ceil" -> ceil(arg)
                "floor" -> floor(arg)
                "round" -> round(arg)
                "exp" -> exp(arg)
                "sgn" -> sign(arg)
                else -> arg
            }

            return if (degreeMode && function in DEGREE_OUTPUT_FUNCTIONS) {
                Math.toDegrees(result)
            } else {
                result
            }
        }

        private fun tryParseSuffix(base: Double): Double {
            skipSpaces()
            if (pos < input.length && input[pos] == '^') {
                pos++
                val exponent = parseFactor()
                return base.pow(exponent)
            }
            if (pos < input.length && input[pos] == '!') {
                pos++
                return factorial(base.toInt()).toDouble()
            }

            return base
        }

        private fun parseNumber(): Double {
            skipSpaces()
            val start = pos
            while (pos < input.length) {
                val c = input[pos]
                if (c.isDigit() || c == '.') {
                    pos++
                } else if (c == 'e' && pos + 1 < input.length && (input[pos + 1].isDigit() || input[pos + 1] == '+' || input[pos + 1] == '-')) {
                    pos++
                    if (input[pos] == '+' || input[pos] == '-') {
                        pos++
                    }
                } else {
                    break
                }
            }
            if (pos == start) {
                throw IllegalArgumentException("Expected number at $pos, got '${input.getOrNull(pos)}'")
            }

            return input.substring(start, pos).toDouble()
        }

        private fun skipSpaces() {
            while (pos < input.length && input[pos] == ' ') {
                pos++
            }
        }

        private fun skipSpacesFrom(from: Int): Int {
            var cursor = from
            while (cursor < input.length && input[cursor] == ' ') {
                cursor++
            }

            return cursor
        }
    }
}
