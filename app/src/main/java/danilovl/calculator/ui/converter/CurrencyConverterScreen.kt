package danilovl.calculator.ui.converter

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import danilovl.calculator.R
import danilovl.calculator.domain.CurrencySearchService
import danilovl.calculator.data.model.CurrencyInfo
import danilovl.calculator.ui.components.AutoSizeText
import danilovl.calculator.ui.components.BasicKeypad
import danilovl.calculator.ui.components.SecondaryTopBar
import danilovl.calculator.ui.theme.*
import danilovl.calculator.ui.util.formatCurrencyResult
import danilovl.calculator.ui.util.formatDisplayNumber
import kotlin.math.roundToInt

@Composable
fun CurrencyConverterScreen(
    onBack: () -> Unit,
    viewModel: CurrencyConverterViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val itemHeightPx = with(density) { 68.dp.toPx() }

    var dragSourceIndex by remember { mutableStateOf(-1) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    val currencies = state.activeCurrencies

    val slotShift by remember(currencies) {
        derivedStateOf {
            if (dragSourceIndex < 0) 0
            else (dragOffsetY / itemHeightPx).roundToInt()
                .coerceIn(-dragSourceIndex, currencies.size - 1 - dragSourceIndex)
        }
    }

    val targetIndex by remember(currencies) {
        derivedStateOf {
            if (dragSourceIndex < 0) -1
            else (dragSourceIndex + slotShift).coerceIn(0, currencies.size - 1)
        }
    }

    val displayList = remember(currencies, dragSourceIndex, targetIndex) {
        if (dragSourceIndex < 0 || targetIndex < 0 || currencies.isEmpty()) {
            currencies
        } else {
            val list = currencies.toMutableList()
            val item = list.removeAt(dragSourceIndex)
            list.add(targetIndex, item)
            list
        }
    }

    if (showAddDialog) {
        AddCurrencyDialog(
            allCurrencies = viewModel.getAllAvailable(),
            activeCodes = currencies.map { it.code },
            onAdd = { viewModel.addCurrency(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        SecondaryTopBar(
            title = stringResource(R.string.conv_currency),
            onBack = onBack,
            centerTitle = true
        ) {
            IconButton(onClick = { showAddDialog = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_currency),
                    tint = OrangeAccent
                )
            }
        }

        val displayValues = remember(state.inputValue, state.inputIndex, currencies, state.rates) {
            currencies.indices.map { index ->
                if (index == state.inputIndex) formatDisplayNumber(state.inputValue)
                else formatCurrencyResult(viewModel.getConvertedValue(index))
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            state = rememberLazyListState()
        ) {
            items(displayList.size, key = { displayList[it].code }) { visualIndex ->
                val currency = displayList[visualIndex]
                val originalIndex = currencies.indexOfFirst { it.code == currency.code }
                val displayValue = if (originalIndex >= 0 && originalIndex < displayValues.size)
                    displayValues[originalIndex] else "0"
                val isActive = originalIndex == state.inputIndex

                val isDragging = dragSourceIndex >= 0 && currency.code == currencies.getOrNull(dragSourceIndex)?.code
                val isTarget = dragSourceIndex >= 0 && !isDragging && visualIndex == targetIndex

                CurrencyRow(
                    currency = currency,
                    value = displayValue,
                    isActive = isActive,
                    isDragging = isDragging,
                    isTarget = isTarget,
                    onTap = { viewModel.setActiveInput(originalIndex) },
                    onDragStarted = {
                        dragSourceIndex = originalIndex
                        dragOffsetY = 0f
                    },
                    onDrag = { dy -> dragOffsetY += dy },
                    onDragStopped = {
                        val from = dragSourceIndex
                        val to = targetIndex
                        if (from >= 0 && to >= 0 && from != to) {
                            viewModel.moveCurrency(from, to)
                        }
                        dragSourceIndex = -1
                        dragOffsetY = 0f
                    },
                    onRemove = { viewModel.removeCurrency(currency) },
                    canRemove = currencies.size > 2
                )

                if (visualIndex < displayList.size - 1) {
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                }
            }
        }

        if (state.isOffline) {
            Text(
                text = stringResource(R.string.offline_rates_notice),
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        BasicKeypad(
            onKey = viewModel::onKey,
            onToggleScientific = null,
            isScientific = false
        )
    }
}

@Composable
fun CurrencyRow(
    currency: CurrencyInfo,
    value: String,
    isActive: Boolean,
    isDragging: Boolean,
    isTarget: Boolean,
    onTap: () -> Unit,
    onDragStarted: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragStopped: () -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    val currentOnDragStarted by rememberUpdatedState(onDragStarted)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragStopped by rememberUpdatedState(onDragStopped)

    val rowBackground = when {
        isDragging -> OrangeAccent.copy(alpha = 0.10f)
        isTarget -> OrangeAccent.copy(alpha = 0.06f)
        else -> Color.Transparent
    }

    val handleTint = when {
        isDragging -> OrangeAccent
        isTarget -> OrangeAccent.copy(alpha = 0.6f)
        else -> Color(0xFF9E9E9E)
    }

    val rowModifier = if (isDragging) {
        Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp))
            .background(SurfaceColor, RoundedCornerShape(8.dp))
            .padding(vertical = 12.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .padding(vertical = 12.dp)
    }

    if (isTarget) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(OrangeAccent)
        )
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isDragging) OrangeAccent.copy(alpha = 0.20f) else Color(0xFFEEEEEE))
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { dy -> currentOnDrag(dy) },
                    onDragStarted = { currentOnDragStarted() },
                    onDragStopped = { currentOnDragStopped() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = null,
                tint = handleTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onTap() }
        ) {
            Text(
                text = currency.code,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isActive) OrangeAccent else TextPrimary
            )
            Text(
                text = currency.name,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        AutoSizeText(
            text = value,
            fontSize = 22.sp,
            minFontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) OrangeAccent else TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(1f)
                .clickable { onTap() },
            maxLines = 1
        )

        if (canRemove) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove),
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AddCurrencyDialog(
    allCurrencies: List<CurrencyInfo>,
    activeCodes: List<String>,
    onAdd: (CurrencyInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }

    val activeSet = activeCodes.toHashSet()

    val filtered = remember(allCurrencies, activeCodes, search) {
        CurrencySearchService().search(allCurrencies, emptyList(), search)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_currency)) },
        text = {
            Column {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text(stringResource(R.string.search_currency)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        cursorColor = OrangeAccent
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(
                        count = filtered.size,
                        key = { i -> filtered[i].code }
                    ) { i ->
                        val currency = filtered[i]
                        val isAlreadyActive = currency.code in activeSet
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isAlreadyActive) Modifier
                                    else Modifier.clickable { onAdd(currency) }
                                )
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currency.code,
                                fontWeight = FontWeight.Bold,
                                color = if (isAlreadyActive) TextSecondary else TextPrimary,
                                fontSize = 15.sp,
                                modifier = Modifier.width(56.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currency.name,
                                color = TextSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (isAlreadyActive) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = OrangeAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
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
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(20.dp)
    )
}
