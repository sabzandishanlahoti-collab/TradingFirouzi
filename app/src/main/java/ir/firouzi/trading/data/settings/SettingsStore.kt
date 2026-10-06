package ir.firouzi.trading.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ir.firouzi.trading.data.model.Market
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class SettingsStore(private val context: Context) {

    private val keyMarket = stringPreferencesKey("market")
    private val keyTheme = stringPreferencesKey("theme_mode")

    val marketFlow: Flow<Market> = context.dataStore.data.map { p ->
        val v = p[keyMarket] ?: Market.RAMZINEX.name
        runCatching { Market.valueOf(v) }.getOrDefault(Market.RAMZINEX)
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { p ->
        val v = p[keyTheme] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(v) }.getOrDefault(ThemeMode.SYSTEM)
    }

    suspend fun setMarket(market: Market) {
        context.dataStore.edit { it[keyMarket] = market.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[keyTheme] = mode.name }
    }
}
