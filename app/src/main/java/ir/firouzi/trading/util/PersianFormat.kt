package ir.firouzi.trading.util

import java.text.NumberFormat
import java.util.Locale

private val faLocale = Locale("fa", "IR")

fun Double.formatPriceFa(): String {
    val abs = kotlin.math.abs(this)
    val nf = NumberFormat.getNumberInstance(faLocale).apply {
        maximumFractionDigits = when {
            abs >= 1000.0 -> 2
            abs >= 1.0 -> 4
            abs >= 0.001 -> 6
            else -> 8
        }
        minimumFractionDigits = 0
    }
    return nf.format(this)
}

fun Double.formatPercentFa(): String {
    val nf = NumberFormat.getNumberInstance(faLocale).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }
    val sign = if (this >= 0) "+" else "−"
    return "$sign${nf.format(kotlin.math.abs(this))}٪"
}
