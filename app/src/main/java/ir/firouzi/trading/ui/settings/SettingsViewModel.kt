package ir.firouzi.trading.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.firouzi.trading.App
import ir.firouzi.trading.data.model.Market
import ir.firouzi.trading.data.settings.SettingsStore
import ir.firouzi.trading.data.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settings: SettingsStore) : ViewModel() {

    val market: StateFlow<Market> = settings.marketFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, Market.RAMZINEX)

    val themeMode: StateFlow<ThemeMode> = settings.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    fun setMarket(market: Market) {
        viewModelScope.launch { settings.setMarket(market) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as App
                SettingsViewModel(app.settings)
            }
        }
    }
}
