package ir.firouzi.trading.data.remote

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

interface RamzinexApi {

    @GET("exchange/api/v1.0/exchange/pairs")
    suspend fun getPairs(): RamzinexPairsEnvelope

    @GET("exchange/api/v1.0/exchange/chart/tv/history")
    suspend fun getHistory(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("from") from: Long,
        @Query("to") to: Long
    ): RamzinexHistoryEnvelope
}

data class RamzinexPairsEnvelope(val data: List<RamzinexPair>? = null)

data class RamzinexPair(
    @Json(name = "pair_id") val pairId: Long? = null,
    @Json(name = "pair_name") val pairName: String? = null,
    @Json(name = "last_price") val lastPrice: String? = null,
    @Json(name = "price_change_24h") val priceChange24h: String? = null,
    @Json(name = "financial") val financial: RamzinexFinancial? = null
)

data class RamzinexFinancial(
    @Json(name = "last_price") val lastPrice: String? = null,
    @Json(name = "last_change_percent") val lastChangePercent: String? = null
)

data class RamzinexHistoryEnvelope(
    val data: RamzinexHistory? = null,
    val s: String? = null,
    val t: List<Long>? = null,
    val o: List<Double>? = null,
    val h: List<Double>? = null,
    val l: List<Double>? = null,
    val c: List<Double>? = null,
    val v: List<Double>? = null
)

data class RamzinexHistory(
    val s: String? = null,
    val t: List<Long>? = null,
    val o: List<Double>? = null,
    val h: List<Double>? = null,
    val l: List<Double>? = null,
    val c: List<Double>? = null,
    val v: List<Double>? = null
)
