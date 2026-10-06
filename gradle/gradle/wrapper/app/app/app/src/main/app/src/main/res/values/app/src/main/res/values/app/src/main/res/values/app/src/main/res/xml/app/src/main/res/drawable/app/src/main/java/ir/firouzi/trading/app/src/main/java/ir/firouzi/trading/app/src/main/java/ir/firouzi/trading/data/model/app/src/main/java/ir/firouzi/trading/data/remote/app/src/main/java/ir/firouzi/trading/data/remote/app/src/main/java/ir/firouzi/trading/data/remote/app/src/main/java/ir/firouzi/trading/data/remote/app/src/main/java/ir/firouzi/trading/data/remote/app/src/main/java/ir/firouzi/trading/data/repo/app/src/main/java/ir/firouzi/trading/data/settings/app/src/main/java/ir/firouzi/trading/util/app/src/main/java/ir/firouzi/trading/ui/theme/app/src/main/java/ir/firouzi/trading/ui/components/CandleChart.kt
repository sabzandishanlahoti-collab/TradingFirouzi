package ir.firouzi.trading.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import ir.firouzi.trading.data.model.Candle
import ir.firouzi.trading.ui.theme.BearRed
import ir.firouzi.trading.ui.theme.BullGreen

@Composable
fun CandleChart(candles: List<Candle>, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (candles.isEmpty()) return@Canvas

            val w = size.width
            val h = size.height
            val minPrice = candles.minOf { it.low }
            val maxPrice = candles.maxOf { it.high }
            val range = (maxPrice - minPrice).coerceAtLeast(1e-9)

            val cw = w / candles.size
            val bodyW = (cw * 0.7f).coerceAtLeast(1.5f)

            candles.forEachIndexed { i, c ->
                val x = i * cw + cw / 2f
                val yHigh = ((maxPrice - c.high) / range * h).toFloat()
                val yLow = ((maxPrice - c.low) / range * h).toFloat()
                val yOpen = ((maxPrice - c.open) / range * h).toFloat()
                val yClose = ((maxPrice - c.close) / range * h).toFloat()
                val color = if (c.close >= c.open) BullGreen else BearRed

                drawLine(
                    color = color,
                    start = Offset(x, yHigh),
                    end = Offset(x, yLow),
                    strokeWidth = (cw * 0.08f).coerceAtLeast(1f)
                )

                val top = if (yOpen < yClose) yOpen else yClose
                val bot = if (yOpen < yClose) yClose else yOpen
                val bodyH = (bot - top).coerceAtLeast(1f)

                drawRect(
                    color = color,
                    topLeft = Offset(x - bodyW / 2f, top),
                    size = Size(bodyW, bodyH)
                )
            }
        }
    }
}
