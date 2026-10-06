package ir.firouzi.trading.data.remote

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

interface NobitexApi {

    @GET("market/stats")
    suspend fun getStats(): NobitexStatsResponse

    @GET("market/udf/history")
    suspend fun getHistory(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("from") from: Long,
        @Query("to") to: Long
    ): NobitexUdfResponse
}

data class NobitexStatsResponse(
    val status: String? = null,
    val stats: Map<String, NobitexStat>? = null
)

data class NobitexStat(
    @Json(name = "bestBuy") val bestBuy: String? = null,
    @Json(name = "bestSell") val bestSell: String? = null,
    @Json(name = "latest") val latest: String? = null,
    @Json(name = "dayChange") val dayChange: String? = null
)

data class NobitexUdfResponse(
    val s: String? = null,
    val t: List<Long>? = null,
    val o: List<Double>? = null,
    val h: List<Double>? = null,
    val l: List<Double>? = null,
    val c: List<Double>? = null,
    val v: List<Double>? = null
)
