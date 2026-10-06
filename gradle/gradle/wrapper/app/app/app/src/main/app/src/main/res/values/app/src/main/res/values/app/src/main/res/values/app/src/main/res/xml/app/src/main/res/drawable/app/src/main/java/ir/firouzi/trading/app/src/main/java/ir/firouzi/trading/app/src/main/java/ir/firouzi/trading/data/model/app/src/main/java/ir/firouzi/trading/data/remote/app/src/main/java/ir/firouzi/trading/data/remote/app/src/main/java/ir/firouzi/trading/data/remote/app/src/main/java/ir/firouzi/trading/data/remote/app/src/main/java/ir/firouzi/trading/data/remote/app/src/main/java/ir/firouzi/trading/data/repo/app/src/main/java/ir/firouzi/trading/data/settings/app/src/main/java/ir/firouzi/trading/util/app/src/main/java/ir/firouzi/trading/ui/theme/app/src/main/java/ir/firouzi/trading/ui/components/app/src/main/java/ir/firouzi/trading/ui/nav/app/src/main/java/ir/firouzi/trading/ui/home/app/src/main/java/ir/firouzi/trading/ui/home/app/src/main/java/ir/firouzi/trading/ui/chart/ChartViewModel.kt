package ir.firouzi.trading.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.firouzi.trading.App
import ir.firouzi.trading.data.model.Candle
import ir.firouzi.trading.data.model.Market
import ir.firouzi.trading.data.model.Timeframe
import ir.firouzi.trading.data.model.symbolsFor
import ir.firouzi.trading.data.repo.MarketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChartUiState(
    val loading: Boolean = true,
    val market: Market = Market.RAMZINEX,
    val symbol: String = "BTC",
    val timeframe: Timeframe = Timeframe.H1,
    val candles: List<Candle> = emptyList(),
    val error: String? = null
)

class ChartViewModel(private val repo: MarketRepository) : ViewModel() {

    private val _state = MutableStateFlow(ChartUiState())
    val state: StateFlow<ChartUiState> = _state.asStateFlow()

    init { load() }

    fun selectMarket(market: Market) {
        val newSymbols = symbolsFor(market)
        _state.update {
            it.copy(
                market = market,
                symbol = if (it.symbol in newSymbols) it.symbol else newSymbols.first()
            )
        }
        load(force = true)
    }

    fun selectTimeframe(tf: Timeframe) {
        if (tf == _state.value.timeframe) return
        _state.update { it.copy(timeframe = tf) }
        load()
    }

    fun selectSymbol(symbol: String) {
        if (symbol == _state.value.symbol) return
        _state.update { it.copy(symbol = symbol) }
        load()
    }

    fun refresh() = load(force = true)

    private fun load(force: Boolean = false) {
        val s = _state.value
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching { repo.getCandles(s.market, s.symbol, s.timeframe) }
                .onSuccess { list ->
                    _state.update { it.copy(loading = false, candles = list) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            loading = false,
                            candles = emptyList(),
                            error = e.message ?: "خطا در دریافت نمودار"
                        )
                    }
                }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as App
                ChartViewModel(app.repository)
            }
        }
    }
}
