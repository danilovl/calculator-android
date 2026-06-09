package danilovl.calculator.data.model

data class UnitDef(
    val code: String,
    val labelResKey: String,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double
)

fun linearUnit(code: String, label: String, factor: Double): UnitDef {
    return UnitDef(code, label, { it * factor }, { it / factor })
}

val LENGTH_UNITS = listOf(
    linearUnit("m", "unit_m", 1.0),
    linearUnit("km", "unit_km", 1000.0),
    linearUnit("cm", "unit_cm", 0.01),
    linearUnit("mm", "unit_mm", 0.001),
    linearUnit("mi", "unit_mi", 1609.344),
    linearUnit("yd", "unit_yd", 0.9144),
    linearUnit("ft", "unit_ft", 0.3048),
    linearUnit("in", "unit_in", 0.0254),
    linearUnit("nm", "unit_nm", 1852.0),
    linearUnit("μm", "unit_um", 1e-6),
    linearUnit("ly", "unit_ly", 9.461e15)
)

val WEIGHT_UNITS = listOf(
    linearUnit("kg", "unit_kg", 1.0),
    linearUnit("g", "unit_g", 0.001),
    linearUnit("mg", "unit_mg", 1e-6),
    linearUnit("t", "unit_t", 1000.0),
    linearUnit("lb", "unit_lb", 0.45359237),
    linearUnit("oz", "unit_oz", 0.028349523),
    linearUnit("st", "unit_st", 6.35029),
    linearUnit("ct", "unit_ct", 0.0002)
)

val AREA_UNITS = listOf(
    linearUnit("m²", "unit_m2", 1.0),
    linearUnit("km²", "unit_km2", 1e6),
    linearUnit("cm²", "unit_cm2", 1e-4),
    linearUnit("mm²", "unit_mm2", 1e-6),
    linearUnit("ha", "unit_ha", 10000.0),
    linearUnit("ac", "unit_ac", 4046.856),
    linearUnit("ft²", "unit_ft2", 0.092903),
    linearUnit("in²", "unit_in2", 6.4516e-4),
    linearUnit("mi²", "unit_mi2", 2.59e6),
    linearUnit("yd²", "unit_yd2", 0.836127)
)

val TIME_UNITS = listOf(
    linearUnit("s", "unit_s", 1.0),
    linearUnit("ms", "unit_ms", 0.001),
    linearUnit("min", "unit_min", 60.0),
    linearUnit("h", "unit_h", 3600.0),
    linearUnit("d", "unit_d", 86400.0),
    linearUnit("wk", "unit_wk", 604800.0),
    linearUnit("mo", "unit_mo", 2629800.0),
    linearUnit("yr", "unit_yr", 31557600.0),
    linearUnit("μs", "unit_us", 1e-6),
    linearUnit("ns", "unit_ns", 1e-9)
)

val TEMPERATURE_UNITS = listOf(
    UnitDef("°C", "unit_c", { it }, { it }),
    UnitDef("°F", "unit_f", { (it - 32) * 5.0 / 9.0 }, { it * 9.0 / 5.0 + 32 }),
    UnitDef("K", "unit_k", { it - 273.15 }, { it + 273.15 }),
    UnitDef("°R", "unit_r", { (it - 491.67) * 5.0 / 9.0 }, { (it + 273.15) * 9.0 / 5.0 })
)

val SPEED_UNITS = listOf(
    linearUnit("m/s", "unit_ms_", 1.0),
    linearUnit("km/h", "unit_kmh", 1.0 / 3.6),
    linearUnit("mph", "unit_mph", 0.44704),
    linearUnit("kn", "unit_kn", 0.514444),
    linearUnit("ft/s", "unit_fts", 0.3048),
    linearUnit("Mach", "unit_mach", 340.29)
)

val VOLUME_UNITS = listOf(
    linearUnit("L", "unit_l", 1.0),
    linearUnit("mL", "unit_ml", 0.001),
    linearUnit("m³", "unit_m3", 1000.0),
    linearUnit("cm³", "unit_cm3", 0.001),
    linearUnit("ft³", "unit_ft3", 28.3168),
    linearUnit("in³", "unit_in3", 0.016387),
    linearUnit("gal(US)", "unit_gal", 3.78541),
    linearUnit("qt(US)", "unit_qt", 0.946353),
    linearUnit("pt(US)", "unit_pt", 0.473176),
    linearUnit("fl oz", "unit_floz", 0.029574),
    linearUnit("tbsp", "unit_tbsp", 0.014787),
    linearUnit("tsp", "unit_tsp", 0.004929)
)

val DATA_UNITS = listOf(
    linearUnit("B", "unit_b", 1.0),
    linearUnit("KB", "unit_kb", 1024.0),
    linearUnit("MB", "unit_mb", 1048576.0),
    linearUnit("GB", "unit_gb", 1073741824.0),
    linearUnit("TB", "unit_tb", 1.0995e12),
    linearUnit("bit", "unit_bit", 0.125),
    linearUnit("Kbit", "unit_kbit", 128.0),
    linearUnit("Mbit", "unit_mbit", 131072.0),
    linearUnit("Gbit", "unit_gbit", 1.342e8)
)

val FINANCE_UNITS = listOf(
    linearUnit("%", "unit_pct", 0.01),
    linearUnit("‰", "unit_permille", 0.001),
    linearUnit("bps", "unit_bps", 0.0001)
)
