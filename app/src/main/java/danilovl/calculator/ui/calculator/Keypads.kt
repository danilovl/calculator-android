package danilovl.calculator.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import danilovl.calculator.ui.components.CalcButton
import danilovl.calculator.ui.theme.ButtonWhite
import danilovl.calculator.ui.theme.OrangeAccent
import danilovl.calculator.ui.theme.TextPrimary
import danilovl.calculator.ui.theme.TextSecondary

private val OPERATOR_KEYS = setOf("÷", "×", "−", "+", "%")
private val CLEAR_KEYS = setOf("C", "⌫")

@Composable
fun MiuiScientificKeypad(
    isSecondMode: Boolean,
    isDegreeMode: Boolean,
    onKey: (String) -> Unit,
    onToggleScientific: () -> Unit
) {
    val spacing = 7.dp
    val sciRowHeight = 52.dp
    val mainRowHeight = 60.dp
    val sciFontSize = 14.sp
    val mainFontSize = 20.sp

    val sciTopRows = listOf(
        listOf("2nd", if (isDegreeMode) "deg" else "rad", if (isSecondMode) "asin" else "sin", if (isSecondMode) "acos" else "cos", if (isSecondMode) "atan" else "tan"),
        listOf("xʸ", "lg", "ln", "(", ")")
    )

    val sciLeftCol = listOf("√x", "x!", "1/x", "π", "🔁")

    val basicRows = listOf(
        listOf("C", "⌫", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf("e", "0", ",", "=")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        sciTopRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                row.forEach { key ->
                    CalcButton(
                        label = key,
                        modifier = Modifier.weight(1f).height(sciRowHeight),
                        backgroundColor = ButtonWhite,
                        textColor = if (key == "2nd" && isSecondMode) OrangeAccent else TextSecondary,
                        fontSize = sciFontSize,
                        onClick = { onKey(key) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(spacing))
        }

        sciLeftCol.forEachIndexed { rowIdx, sciKey ->
            val basicRow = basicRows[rowIdx]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                val isSciToggle = sciKey == "🔁"
                CalcButton(
                    label = if (isSciToggle) "▼" else sciKey,
                    modifier = Modifier.weight(1f).height(mainRowHeight),
                    backgroundColor = ButtonWhite,
                    textColor = TextSecondary,
                    fontSize = if (isSciToggle) 18.sp else 14.sp,
                    onClick = { if (isSciToggle) onToggleScientific() else onKey(sciKey) }
                )
                basicRow.forEach { key ->
                    val isEquals = key == "="
                    val isOperator = key in OPERATOR_KEYS
                    val isClear = key in CLEAR_KEYS
                    CalcButton(
                        label = key,
                        modifier = Modifier.weight(1f).height(mainRowHeight),
                        backgroundColor = if (isEquals) OrangeAccent else ButtonWhite,
                        textColor = when {
                            isEquals -> Color.White
                            isOperator || isClear -> OrangeAccent
                            else -> TextPrimary
                        },
                        fontSize = mainFontSize,
                        fontWeight = if (key.all { it.isDigit() } || key == "," || key == "e") FontWeight.Normal else FontWeight.Medium,
                        isRounded = isEquals,
                        onClick = { onKey(key) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(spacing))
        }
    }
}

@Composable
fun ScientificKeypad(
    isSecondMode: Boolean,
    isDegreeMode: Boolean,
    onKey: (String) -> Unit,
    compact: Boolean = false,
    sideMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val buttonSize = when {
        sideMode -> 50.dp
        compact -> 44.dp
        else -> 56.dp
    }
    val fontSize = when {
        sideMode -> 12.sp
        compact -> 13.sp
        else -> 15.sp
    }
    val spacing = if (sideMode) 5.dp else 6.dp
    val hPadding = if (sideMode) 4.dp else 8.dp

    Column(modifier = modifier.padding(horizontal = hPadding)) {
        listOf(
            listOf("2nd", if (isDegreeMode) "deg" else "rad", if (isSecondMode) "asin" else "sin", if (isSecondMode) "acos" else "cos", if (isSecondMode) "atan" else "tan"),
            listOf("xʸ", "lg", "ln", "(", ")"),
            listOf("√x", "x²", "x³", "1/x", "π"),
            listOf("x!", "e", "x³", "(", ")")
        ).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                row.forEach { key ->
                    CalcButton(
                        label = key,
                        modifier = Modifier.weight(1f).height(buttonSize),
                        backgroundColor = ButtonWhite,
                        textColor = if (key == "2nd" && isSecondMode) OrangeAccent else TextSecondary,
                        fontSize = fontSize,
                        onClick = { onKey(key) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(spacing))
        }
    }
}
