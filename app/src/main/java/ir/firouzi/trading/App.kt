package ir.firouzi.trading

import android.app.Application
import ir.firouzi.trading.data.remote.NetworkModule
import ir.firouzi.trading.data.repo.MarketRepository
import ir.firouzi.trading.data.settings.SettingsStore

class App : Application() {

    lateinit var repository: MarketRepository
        private set

    lateinit var settings: SettingsStore
        private set

    override fun onCreate() {
        super.onCreate()
        settings = SettingsStore(this)
        repository = MarketRepository(
            ramzinex = NetworkModule.ramzinex,
            nobitex = NetworkModule.nobitex,
            wallex = NetworkModule.wallex,
            forex = NetworkModule.forex
        )
    }
}
