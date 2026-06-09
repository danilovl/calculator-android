package danilovl.calculator.ui.converter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import danilovl.calculator.R
import danilovl.calculator.ui.components.BasicKeypad
import danilovl.calculator.ui.components.SecondaryTopBar
import danilovl.calculator.ui.theme.*
import danilovl.calculator.data.model.UnitDef
import danilovl.calculator.ui.util.formatConverterResult

@Composable
fun UnitConverterScreen(
    converterType: ConverterType,
    onBack: () -> Unit,
    viewModel: UnitConverterViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(converterType) {
        viewModel.init(converterType)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        SecondaryTopBar(
            title = stringResource(converterType.labelRes),
            onBack = onBack,
            centerTitle = true
        )

        UnitRow(
            label = state.units.getOrNull(state.fromIndex)?.code ?: "",
            value = formatConverterResult(state.inputValue),
            isActive = true,
            onTap = {},
            onUnitSelect = { viewModel.setFrom(it) },
            units = state.units,
            selectedIndex = state.fromIndex
        )
        HorizontalDivider(color = DividerColor)
        UnitRow(
            label = state.units.getOrNull(state.toIndex)?.code ?: "",
            value = formatConverterResult(state.result),
            isActive = false,
            onTap = {},
            onUnitSelect = { viewModel.setTo(it) },
            units = state.units,
            selectedIndex = state.toIndex
        )
        HorizontalDivider(color = DividerColor)

        Spacer(modifier = Modifier.weight(1f))

        BasicKeypad(
            onKey = viewModel::onKey,
            onToggleScientific = null,
            isScientific = false
        )
    }
}

@Composable
fun UnitRow(
    label: String,
    value: String,
    isActive: Boolean,
    onTap: () -> Unit,
    onUnitSelect: (Int) -> Unit,
    units: List<UnitDef>,
    selectedIndex: Int
) {
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        UnitPickerDialog(
            units = units,
            selectedIndex = selectedIndex,
            onSelect = { onUnitSelect(it); showPicker = false },
            onDismiss = { showPicker = false }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = { showPicker = true },
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}

@Composable
fun UnitPickerDialog(
    units: List<UnitDef>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_unit)) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                itemsIndexed(units) { index, unit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(index) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (index == selectedIndex) {
                            Text(text = "✓ ", color = OrangeAccent, fontWeight = FontWeight.Bold)
                        } else {
                            Spacer(modifier = Modifier.width(20.dp))
                        }
                        Text(
                            text = unit.code,
                            fontWeight = FontWeight.Bold,
                            color = if (index == selectedIndex) OrangeAccent else TextPrimary,
                            fontSize = 15.sp,
                            modifier = Modifier.width(72.dp)
                        )
                    }
                    HorizontalDivider(color = DividerColor)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = OrangeAccent)
            }
        },
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(20.dp)
    )
}
