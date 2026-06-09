package danilovl.calculator.ui.components

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
import danilovl.calculator.ui.theme.ButtonWhite
import danilovl.calculator.ui.theme.OrangeAccent
import danilovl.calculator.ui.theme.TextPrimary

private val OPERATOR_KEYS = setOf("÷", "×", "−", "+", "%")
private val CLEAR_KEYS = setOf("C", "⌫")

@Composable
fun BasicKeypad(
    onKey: (String) -> Unit,
    onToggleScientific: (() -> Unit)?,
    isScientific: Boolean,
    compact: Boolean = false,
    sideMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val buttonHeight = when {
        sideMode -> 50.dp
        compact -> 44.dp
        else -> 64.dp
    }
    val spacing = when {
        sideMode -> 5.dp
        compact -> 5.dp
        else -> 8.dp
    }
    val fontSize = when {
        sideMode -> 17.sp
        compact -> 16.sp
        else -> 20.sp
    }

    val rows = listOf(
        listOf("C", "⌫", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf(if (onToggleScientific != null) "sci" else "00", "0", ",", "=")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (sideMode) 4.dp else 8.dp, vertical = if (sideMode) 0.dp else 8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                row.forEach { key ->
                    val isEquals = key == "="
                    val isOperator = key in OPERATOR_KEYS
                    val isClear = key in CLEAR_KEYS
                    val isSci = key == "sci"
                    CalcButton(
                        label = if (isSci) (if (isScientific) "▼" else "▲") else key,
                        modifier = Modifier.weight(1f).height(buttonHeight),
                        backgroundColor = when {
                            isEquals -> OrangeAccent
                            else -> ButtonWhite
                        },
                        textColor = when {
                            isEquals -> Color.White
                            isOperator || isClear || isSci -> OrangeAccent
                            else -> TextPrimary
                        },
                        fontSize = fontSize,
                        fontWeight = if (key.all { it.isDigit() } || key == ",") FontWeight.Normal else FontWeight.Medium,
                        isRounded = isEquals,
                        onClick = {
                            if (isSci) onToggleScientific?.invoke() else onKey(key)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(spacing))
        }
    }
}
