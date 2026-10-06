package ir.firouzi.trading.data.model

data class Ticker(
    val symbol: String,
    val price: Double,
    val changePercent: Double,
    val market: Market
)

data class Candle(
    val timeSec: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

enum class Market(val titleFa: String) {
    RAMZINEX("رمزینکس"),
    NOBITEX("نوبیتکس"),
    WALLEX("والکس"),
    FOREX("فارکس")
}

enum class Timeframe(val label: String, val resolution: String, val seconds: Long) {
    M5("۵ دقیقه", "5", 300L),
    M15("۱۵ دقیقه", "15", 900L),
    H1("۱ ساعت", "60", 3600L),
    H4("۴ ساعت", "240", 14400L),
    D1("۱ روز", "D", 86400L)
}

val CRYPTO_SYMBOLS: List<String> = listOf(
    "BTC", "ETH", "BNB", "SOL", "XRP", "DOGE", "ADA", "TON", "TRX", "AVAX"
)

val FOREX_SYMBOLS: List<String> = listOf(
    "EUR", "GBP", "JPY", "CHF", "AUD", "CAD", "CNY", "TRY"
)

fun symbolsFor(market: Market): List<String> =
    if (market == Market.FOREX) FOREX_SYMBOLS else CRYPTO_SYMBOLS
