package danilovl.calculator.ui.converter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import danilovl.calculator.R
import danilovl.calculator.data.model.*

enum class ConverterType(val labelRes: Int, val icon: ImageVector) {
    CURRENCY(R.string.conv_currency, Icons.Default.AttachMoney),
    LENGTH(R.string.conv_length, Icons.Default.Straighten),
    WEIGHT(R.string.conv_weight, Icons.Default.FitnessCenter),
    AREA(R.string.conv_area, Icons.Default.SquareFoot),
    TIME(R.string.conv_time, Icons.Default.AccessTime),
    TEMPERATURE(R.string.conv_temperature, Icons.Default.Thermostat),
    SPEED(R.string.conv_speed, Icons.Default.Speed),
    VOLUME(R.string.conv_volume, Icons.Default.LocalDrink),
    DATA(R.string.conv_data, Icons.Default.Storage),
    FINANCE(R.string.conv_finance, Icons.Default.AccountBalance)
}

fun unitsForType(type: ConverterType): List<UnitDef> = when (type) {
    ConverterType.LENGTH -> LENGTH_UNITS
    ConverterType.WEIGHT -> WEIGHT_UNITS
    ConverterType.AREA -> AREA_UNITS
    ConverterType.TIME -> TIME_UNITS
    ConverterType.TEMPERATURE -> TEMPERATURE_UNITS
    ConverterType.SPEED -> SPEED_UNITS
    ConverterType.VOLUME -> VOLUME_UNITS
    ConverterType.DATA -> DATA_UNITS
    ConverterType.FINANCE -> FINANCE_UNITS
    ConverterType.CURRENCY -> LENGTH_UNITS
}
