package ir.firouzi.trading.data.repo

import ir.firouzi.trading.data.model.Candle
import ir.firouzi.trading.data.model.CRYPTO_SYMBOLS
import ir.firouzi.trading.data.model.FOREX_SYMBOLS
import ir.firouzi.trading.data.model.Market
import ir.firouzi.trading.data.model.Ticker
import ir.firouzi.trading.data.model.Timeframe
import ir.firouzi.trading.data.remote.ForexApi
import ir.firouzi.trading.data.remote.NobitexApi
import ir.firouzi.trading.data.remote.RamzinexApi
import ir.firouzi.trading.data.remote.RamzinexPair
import ir.firouzi.trading.data.remote.WallexApi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MarketRepository(
    private val ramzinex: RamzinexApi,
    private val nobitex: NobitexApi,
    private val wallex: WallexApi,
    private val forex: ForexApi
) {

    @Volatile private var ramzinexPairsCache: List<RamzinexPair>? = null

    suspend fun getTickers(market: Market): List<Ticker> = when (market) {
        Market.RAMZINEX -> ramzinexTickers()
        Market.NOBITEX -> nobitexTickers()
        Market.WALLEX -> wallexTickers()
        Market.FOREX -> forexTickers()
    }

    private suspend fun ramzinexTickers(): List<Ticker> {
        val pairs = ensureRamzinexPairs()
        val byBase = pairs
            .filter { it.pairName?.endsWith("/USDT", ignoreCase = true) == true }
            .associateBy { it.pairName!!.substringBefore("/") }

        return CRYPTO_SYMBOLS.mapNotNull { base ->
            val p = byBase[base] ?: return@mapNotNull null
            val price = (p.financial?.lastPrice ?: p.lastPrice)?.toDoubleOrNull()
                ?: return@mapNotNull null
            val change = (p.financial?.lastChangePercent ?: p.priceChange24h)
                ?.toDoubleOrNull() ?: 0.0
            Ticker(base, price, change, Market.RAMZINEX)
        }
    }

    private suspend fun ensureRamzinexPairs(): List<RamzinexPair> {
        ramzinexPairsCache?.let { return it }
        val list = ramzinex.getPairs().data.orEmpty()
        ramzinexPairsCache = list
        return list
    }

    private suspend fun nobitexTickers(): List<Ticker> {
        val stats = nobitex.getStats().stats ?: return emptyList()
        return CRYPTO_SYMBOLS.mapNotNull { base ->
            val key = "${base.lowercase()}-usdt"
            val s = stats[key] ?: return@mapNotNull null
            val price = s.latest?.toDoubleOrNull() ?: return@mapNotNull null
            val change = (s.dayChange?.toDoubleOrNull() ?: 0.0) * 100.0
            Ticker(base, price, change, Market.NOBITEX)
        }
    }

    private suspend fun wallexTickers(): List<Ticker> {
        val symbols = wallex.getMarkets().symbols ?: return emptyList()
        return CRYPTO_SYMBOLS.mapNotNull { base ->
            val s = symbols["${base.uppercase()}USDT"] ?: return@mapNotNull null
            val price = s.stats?.lastPrice?.toDoubleOrNull() ?: return@mapNotNull null
            val change = (s.stats?.change24h ?: 0.0)
            Ticker(base, price, change, Market.WALLEX)
        }
    }

    private suspend fun forexTickers(): List<Ticker> {
        val url = buildForexSeriesUrl(
            startDaysAgo = 10,
            targets = FOREX_SYMBOLS.joinToString(",")
        )
        val resp = forex.getSeries(url)
        val rates = resp.rates ?: return emptyList()

        val dates = rates.keys.sorted()
        if (dates.isEmpty()) return emptyList()
        val lastDate = dates.last()
        val prevDate = if (dates.size >= 2) dates[dates.size - 2] else lastDate

        return FOREX_SYMBOLS.mapNotNull { code ->
            val last = rates[lastDate]?.get(code) ?: return@mapNotNull null
            val prev = rates[prevDate]?.get(code) ?: last
            val change = if (prev != 0.0) (last - prev) / prev * 100.0 else 0.0
            Ticker(code, last, change, Market.FOREX)
        }
    }

    suspend fun getCandles(
        market: Market,
        symbol: String,
        timeframe: Timeframe
    ): List<Candle> = when (market) {
        Market.RAMZINEX -> ramzinexCandles(symbol, timeframe)
        Market.NOBITEX -> nobitexCandles(symbol, timeframe)
        Market.WALLEX -> wallexCandles(symbol, timeframe)
        Market.FOREX -> forexCandles(symbol, timeframe)
    }

    private suspend fun ramzinexCandles(symbol: String, timeframe: Timeframe): List<Candle> {
        val pairId = resolveRamzinexPairId(symbol) ?: return emptyList()
        val (from, to) = range(timeframe)
        val env = ramzinex.getHistory(
            symbol = pairId.toString(),
            resolution = timeframe.resolution,
            from = from,
            to = to
        )
        val h = env.data ?: ir.firouzi.trading.data.remote.RamzinexHistory(
            s = env.s, t = env.t, o = env.o, h = env.h, l = env.l, c = env.c, v = env.v
        )
        return buildCandles(h.t, h.o, h.h, h.l, h.c, h.v)
    }

    private suspend fun resolveRamzinexPairId(base: String): Long? {
        val pairs = ensureRamzinexPairs()
        return pairs.firstOrNull {
            it.pairName.equals("$base/USDT", ignoreCase = true)
        }?.pairId
    }

    private suspend fun nobitexCandles(symbol: String, timeframe: Timeframe): List<Candle> {
        val (from, to) = range(timeframe)
        val r = nobitex.getHistory(
            symbol = "${symbol.uppercase()}USDT",
            resolution = timeframe.resolution,
            from = from,
            to = to
        )
        return buildCandles(r.t, r.o, r.h, r.l, r.c, r.v)
    }

    private suspend fun wallexCandles(symbol: String, timeframe: Timeframe): List<Candle> {
        val (from, to) = range(timeframe)
        val r = wallex.getHistory(
            symbol = "${symbol.uppercase()}USDT",
            resolution = timeframe.resolution,
            from = from,
            to = to
        )
        return buildCandles(r.t, r.o, r.h, r.l, r.c, r.v)
    }

    private suspend fun forexCandles(symbol: String, timeframe: Timeframe): List<Candle> {
        val days = when (timeframe) {
            Timeframe.M5, Timeframe.M15, Timeframe.H1, Timeframe.H4 -> 60
            Timeframe.D1 -> 180
        }
        val url = buildForexSeriesUrl(startDaysAgo = days, targets = symbol.uppercase())
        val resp = forex.getSeries(url)
        val rates = resp.rates ?: return emptyList()
        val sorted = rates.toSortedMap()
        if (sorted.isEmpty()) return emptyList()

        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val candles = ArrayList<Candle>(sorted.size)
        var prevClose: Double? = null
        for ((dateStr, map) in sorted) {
            val close = map[symbol.uppercase()] ?: continue
            val open = prevClose ?: close
            val high = if (open > close) open else close
            val low = if (open < close) open else close
            val timeSec = try {
                fmt.parse(dateStr)?.time?.div(1000L) ?: 0L
            } catch (t: Throwable) { 0L }
            candles += Candle(timeSec, open, high, low, close, 0.0)
            prevClose = close
        }
        return candles
    }

    private fun range(timeframe: Timeframe): Pair<Long, Long> {
        val now = System.currentTimeMillis() / 1000L
        val from = now - timeframe.seconds * 300L
        return from to now
    }

    private fun buildForexSeriesUrl(startDaysAgo: Int, targets: String): String {
        val endCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val startCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            add(Calendar.DAY_OF_YEAR, -startDaysAgo)
        }
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val s = fmt.format(Date(startCal.timeInMillis))
        val e = fmt.format(Date(endCal.timeInMillis))
        return "https://api.frankfurter.app/$s..$e?from=USD&to=$targets"
    }

    private fun buildCandles(
        t: List<Long>?, o: List<Double>?, h: List<Double>?,
        l: List<Double>?, c: List<Double>?, v: List<Double>?
    ): List<Candle> {
        if (t == null || o == null || h == null || l == null || c == null) return emptyList()
        val n = minOf(t.size, o.size, h.size, l.size, c.size)
        return (0 until n).map { i ->
            Candle(t[i], o[i], h[i], l[i], c[i], v?.getOrNull(i) ?: 0.0)
        }
    }
}
