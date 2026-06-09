package danilovl.calculator.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import danilovl.calculator.R
import danilovl.calculator.ui.theme.TextPrimary
import danilovl.calculator.ui.theme.TextSecondary

@Composable
fun TopBar(
    isCalculator: Boolean,
    onCalculatorClick: () -> Unit,
    onConverterClick: () -> Unit,
    onMenuClick: () -> Unit,
    extraAction: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        extraAction?.invoke()
        Spacer(modifier = Modifier.width(4.dp))
        TabButton(
            text = stringResource(R.string.calculator),
            selected = isCalculator,
            onClick = onCalculatorClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        TabButton(
            text = stringResource(R.string.converter),
            selected = !isCalculator,
            onClick = onConverterClick
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onMenuClick) {
            Text(text = "⋮", fontSize = 22.sp, color = TextSecondary)
        }
    }
}

@Composable
fun TabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = text,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) TextPrimary else TextSecondary,
            fontSize = 18.sp
        )
    }
}
