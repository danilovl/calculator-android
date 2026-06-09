package danilovl.calculator.ui.util

import java.math.BigDecimal
import java.math.RoundingMode

private val PLAIN_NUMBER_PATTERN = Regex("-?\\d*\\.?\\d*")

fun formatExpression(expr: String): String {
    if (expr.isEmpty()) return expr
    val sb = StringBuilder()
    var i = 0
    while (i < expr.length) {
        val c = expr[i]
        if (c.isDigit()) {
            val start = i
            while (i < expr.length && expr[i].isDigit()) i++
            val intPart = expr.substring(start, i)
            val fracPart = if (i < expr.length && expr[i] == '.') {
                val dotStart = i
                i++
                while (i < expr.length && expr[i].isDigit()) i++
                "," + expr.substring(dotStart + 1, i)
            } else ""
            sb.append(formatIntegerPart(intPart))
            sb.append(fracPart)
        } else {
            sb.append(c)
            i++
        }
    }
    return sb.toString()
}

private fun formatIntegerPart(s: String): String {
    if (s.length <= 3) return s
    return s.reversed().chunked(3).joinToString("\u00A0").reversed()
}

fun formatDisplayNumber(s: String): String {
    if (s.isBlank()) return s
    if (s == "-" || s == "." || s == "-.") return s
    if (!s.matches(PLAIN_NUMBER_PATTERN)) return s

    val negative = s.startsWith("-")
    val abs = if (negative) s.removePrefix("-") else s

    val dotIndex = abs.indexOf('.')
    val intPart = if (dotIndex >= 0) abs.substring(0, dotIndex) else abs
    val fracPart = if (dotIndex >= 0) abs.substring(dotIndex) else ""

    val formattedInt = if (intPart.isEmpty()) ""
    else intPart.reversed().chunked(3).joinToString("\u00A0").reversed()

    return buildString {
        if (negative) append("-")
        append(formattedInt)
        append(fracPart)
    }
}

fun formatConverterResult(s: String): String {
    if (s.isBlank()) return s
    if (s == "-" || s == "." || s == "-.") return s
    val d = s.toDoubleOrNull() ?: return s

    val rounded = try {
        BigDecimal(d).setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
    } catch (e: Exception) {
        s
    }
    return formatDisplayNumber(rounded)
}

fun formatCurrencyResult(s: String): String {
    if (s.isBlank()) return s
    if (s == "-" || s == "." || s == "-.") return s
    val d = s.toDoubleOrNull() ?: return s
    val rounded = try {
        BigDecimal(d).setScale(0, RoundingMode.HALF_UP).toPlainString()
    } catch (e: Exception) {
        s
    }
    return formatDisplayNumber(rounded)
}
