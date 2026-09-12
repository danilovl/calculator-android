package danilovl.calculator.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 40.sp,
    minFontSize: TextUnit = 12.sp,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = 1
) {
    var scaledFontSize by remember(text) { mutableStateOf(fontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        color = color,
        fontSize = scaledFontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        softWrap = true,
        overflow = TextOverflow.Clip,
        onTextLayout = { textLayoutResult ->
            val hasOverflow = textLayoutResult.hasVisualOverflow || textLayoutResult.lineCount > 1
            if (hasOverflow && scaledFontSize > minFontSize) {
                scaledFontSize = (scaledFontSize.value * 0.9f).sp
            } else {
                readyToDraw = true
            }
        }
    )
}
