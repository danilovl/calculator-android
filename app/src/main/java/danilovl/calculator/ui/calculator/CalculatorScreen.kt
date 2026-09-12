package danilovl.calculator.ui.calculator

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import danilovl.calculator.R
import danilovl.calculator.ui.components.AutoSizeText
import danilovl.calculator.ui.components.BasicKeypad
import danilovl.calculator.ui.components.TopBar
import danilovl.calculator.ui.theme.*
import danilovl.calculator.ui.util.formatExpression
import kotlin.math.abs

private const val SCIENTIFIC_TOGGLE_THRESHOLD = 40f

@Composable
fun CalculatorScreen(
    onNavigateToConverter: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: CalculatorViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    var showHistory by remember { mutableStateOf(false) }

    if (showHistory) {
        HistoryDialog(
            history = state.history,
            onDismiss = { showHistory = false },
            onClear = { viewModel.clearHistory(); showHistory = false },
            onSelect = { expr ->
                viewModel.onKey("C")
                expr.forEach { viewModel.onKey(it.toString()) }
                showHistory = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        TopBar(
            isCalculator = true,
            onCalculatorClick = {},
            onConverterClick = onNavigateToConverter,
            onMenuClick = onNavigateToSettings,
            extraAction = {
                IconButton(onClick = { showHistory = true }) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = stringResource(R.string.history),
                        tint = TextSecondary
                    )
                }
            }
        )

        if (isLandscape) {
            LandscapeCalculatorLayout(state = state, onKey = viewModel::onKey)
        } else {
            PortraitCalculatorLayout(
                state = state,
                onKey = viewModel::onKey,
                onToggleScientific = viewModel::toggleScientific
            )
        }
    }
}

@Composable
fun PortraitCalculatorLayout(
    state: CalculatorState,
    onKey: (String) -> Unit,
    onToggleScientific: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    detectVerticalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onDragEnd = {
                            if (abs(totalDrag) > SCIENTIFIC_TOGGLE_THRESHOLD) onToggleScientific()
                        }
                    ) { _, dragAmount -> totalDrag += dragAmount }
                }
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalAlignment = Alignment.End
            ) {
                state.history.takeLast(3).forEach { entry ->
                    val formattedExpr = formatExpression(entry.expression).replace("\n", " ")
                    val resultPart = " = ${entry.result}"
                    val maxTotalLength = 35
                    val displayText = if (formattedExpr.length + resultPart.length > maxTotalLength) {
                        val allowedExprLength = maxOf(0, maxTotalLength - resultPart.length - 1)
                        formattedExpr.take(allowedExprLength) + "…" + resultPart
                    } else {
                        formattedExpr + resultPart
                    }
                    Text(
                        text = displayText,
                        fontSize = 16.sp,
                        color = TextSecondary.copy(alpha = 0.6f),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End
            ) {
                AutoSizeText(
                    text = formatExpression(state.expression),
                    fontSize = 40.sp,
                    minFontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.End,
                    maxLines = 5
                )
                if (state.result.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AutoSizeText(
                        text = state.result,
                        fontSize = 22.sp,
                        minFontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }
        }

        AnimatedContent(
            targetState = state.isScientific,
            transitionSpec = {
                if (targetState) {
                    slideInVertically(
                        initialOffsetY = { -it / 3 },
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(240)) togetherWith
                    fadeOut(tween(180))
                } else {
                    fadeIn(tween(200)) togetherWith
                    slideOutVertically(
                        targetOffsetY = { -it / 3 },
                        animationSpec = tween(260, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(180))
                }
            },
            label = "keypad_switch"
        ) { isScientific ->
            if (isScientific) {
                MiuiScientificKeypad(
                    isSecondMode = state.isSecondMode,
                    isDegreeMode = state.isDegreeMode,
                    onKey = onKey,
                    onToggleScientific = onToggleScientific
                )
            } else {
                BasicKeypad(
                    onKey = onKey,
                    onToggleScientific = onToggleScientific,
                    isScientific = false
                )
            }
        }
    }
}

@Composable
fun LandscapeCalculatorLayout(state: CalculatorState, onKey: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalAlignment = Alignment.End
            ) {
                state.history.takeLast(3).forEach { entry ->
                    val formattedExpr = formatExpression(entry.expression).replace("\n", " ")
                    val resultPart = " = ${entry.result}"
                    val maxTotalLength = 30
                    val displayText = if (formattedExpr.length + resultPart.length > maxTotalLength) {
                        val allowedExprLength = maxOf(0, maxTotalLength - resultPart.length - 1)
                        formattedExpr.take(allowedExprLength) + "…" + resultPart
                    } else {
                        formattedExpr + resultPart
                    }
                    Text(
                        text = displayText,
                        fontSize = 14.sp,
                        color = TextSecondary.copy(alpha = 0.6f),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End
            ) {
                AutoSizeText(
                    text = formatExpression(state.expression),
                    fontSize = 30.sp,
                    minFontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.End,
                    maxLines = 3
                )
                if (state.result.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AutoSizeText(
                        text = state.result,
                        fontSize = 18.sp,
                        minFontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(2f)) {
            ScientificKeypad(
                isSecondMode = state.isSecondMode,
                isDegreeMode = state.isDegreeMode,
                onKey = onKey,
                compact = true
            )
            BasicKeypad(onKey = onKey, onToggleScientific = null, isScientific = false, compact = true)
        }
    }
}
