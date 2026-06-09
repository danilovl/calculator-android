package danilovl.calculator.ui.calculator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import danilovl.calculator.R
import danilovl.calculator.ui.theme.DividerColor
import danilovl.calculator.ui.theme.OrangeAccent
import danilovl.calculator.ui.theme.SurfaceColor
import danilovl.calculator.ui.theme.TextPrimary
import danilovl.calculator.ui.theme.TextSecondary

@Composable
fun HistoryDialog(
    history: List<HistoryEntry>,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.history)) },
        text = {
            if (history.isEmpty()) {
                Text(stringResource(R.string.history_empty), color = TextSecondary)
            } else {
                LazyColumn {
                    items(history.reversed()) { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(entry.expression) }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(text = entry.expression, fontSize = 16.sp, color = TextPrimary)
                            Text(text = "= ${entry.result}", fontSize = 14.sp, color = OrangeAccent)
                        }
                        HorizontalDivider(color = DividerColor)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = OrangeAccent)
            }
        },
        dismissButton = {
            if (history.isNotEmpty()) {
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.clear), color = OrangeAccent)
                }
            }
        },
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(20.dp)
    )
}
