package ir.firouzi.trading.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.firouzi.trading.App
import ir.firouzi.trading.data.model.Market
import ir.firouzi.trading.data.model.Ticker
import ir.firouzi.trading.data.repo.MarketRepository
import ir.firouzi.trading.data.settings.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val market: Market = Market.RAMZINEX,
    val tickers: List<Ticker> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val repo: MarketRepository,
    private val settings: SettingsStore
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settings.marketFlow.collectLatest { m ->
                _state.update { it.copy(market = m, loading = true, error = null) }
                load(m)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch { load(_state.value.market) }
    }

    private suspend fun load(market: Market) {
        runCatching { repo.getTickers(market) }
            .onSuccess { list ->
                _state.update {
                    it.copy(loading = false, tickers = list, error = null)
                }
            }
            .onFailure { e ->
                _state.update {
                    it.copy(
                        loading = false,
                        tickers = emptyList(),
                        error = e.message ?: "خطا در دریافت داده"
                    )
                }
            }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as App
                HomeViewModel(app.repository, app.settings)
            }
        }
    }
}
