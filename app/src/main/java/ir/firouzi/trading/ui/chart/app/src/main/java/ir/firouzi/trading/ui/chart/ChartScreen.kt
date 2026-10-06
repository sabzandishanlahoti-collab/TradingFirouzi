package ir.firouzi.trading.ui.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.firouzi.trading.data.model.Market
import ir.firouzi.trading.data.model.Timeframe
import ir.firouzi.trading.data.model.symbolsFor
import ir.firouzi.trading.ui.components.CandleChart
import ir.firouzi.trading.ui.theme.BearRed
import ir.firouzi.trading.util.formatPriceFa

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(vm: ChartViewModel = viewModel(factory = ChartViewModel.Factory)) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${state.symbol} — ${state.market.titleFa}") },
                actions = {
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "به‌روزرسانی")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 12.dp)
        ) {
            LazyRow(
                contentPadding = PaddingValues(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(Market.entries.toList()) { m ->
                    FilterChip(
                        selected = m == state.market,
                        onClick = { vm.selectMarket(m) },
                        label = { Text(m.titleFa) }
                    )
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(symbolsFor(state.market)) { sym ->
                    FilterChip(
                        selected = sym == state.symbol,
                        onClick = { vm.selectSymbol(sym) },
                        label = { Text(sym) }
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            if (state.market != Market.FOREX) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(Timeframe.entries.toList()) { tf ->
                        FilterChip(
                            selected = tf == state.timeframe,
                            onClick = { vm.selectTimeframe(tf) },
                            label = { Text(tf.label) }
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            } else {
                Text(
                    "فارکس فقط روزانه پشتیبانی می‌شود",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(6.dp))
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when {
                    state.loading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                    state.error != null && state.candles.isEmpty() -> Text(
                        text = state.error ?: "خطا",
                        color = BearRed,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    state.candles.isEmpty() -> Text(
                        text = "کندلی موجود نیست",
                        modifier = Modifier.align(Alignment.Center)
                    )
                    else -> CandleChart(
                        candles = state.candles,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (state.candles.isNotEmpty()) {
                val last = state.candles.last()
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "آخرین قیمت: ${last.close.formatPriceFa()}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
