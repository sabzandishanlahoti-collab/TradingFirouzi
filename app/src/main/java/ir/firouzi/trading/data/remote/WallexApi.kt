package ir.firouzi.trading.data.remote

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

interface WallexApi {

    @GET("v1/markets")
    suspend fun getMarkets(): WallexMarketsResponse

    @GET("v1/udf/history")
    suspend fun getHistory(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("from") from: Long,
        @Query("to") to: Long
    ): WallexUdfResponse
}

data class WallexMarketsResponse(val symbols: Map<String, WallexSymbol>? = null)

data class WallexSymbol(
    val symbol: String? = null,
    val stats: WallexStats? = null
)

data class WallexStats(
    val bidPrice: String? = null,
    val askPrice: String? = null,
    val lastPrice: String? = null,
    @Json(name = "24h_ch") val change24h: Double? = null
)

data class WallexUdfResponse(
    val s: String? = null,
    val t: List<Long>? = null,
    val o: List<Double>? = null,
    val h: List<Double>? = null,
    val l: List<Double>? = null,
    val c: List<Double>? = null,
    val v: List<Double>? = null
)
