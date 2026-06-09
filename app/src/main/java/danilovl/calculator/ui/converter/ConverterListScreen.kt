package danilovl.calculator.ui.converter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import danilovl.calculator.ui.components.TopBar
import danilovl.calculator.ui.theme.*

@Composable
fun ConverterListScreen(
    onNavigateToCalculator: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onConverterSelected: (ConverterType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        TopBar(
            isCalculator = false,
            onCalculatorClick = onNavigateToCalculator,
            onConverterClick = {},
            onMenuClick = onNavigateToSettings
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(ConverterType.entries) { type ->
                ConverterItem(
                    type = type,
                    onClick = { onConverterSelected(type) }
                )
            }
        }
    }
}

@Composable
fun ConverterItem(type: ConverterType, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(ButtonWhite)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = type.icon,
            contentDescription = stringResource(type.labelRes),
            modifier = Modifier.size(28.dp),
            tint = OrangeAccent
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(type.labelRes),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 16.sp
        )
    }
}
