package ir.firouzi.trading.data.remote

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Url

interface ForexApi {

    @GET
    suspend fun getSeries(@Url url: String): ForexSeriesResponse
}

data class ForexSeriesResponse(
    val amount: Double? = null,
    val base: String? = null,
    @Json(name = "start_date") val startDate: String? = null,
    @Json(name = "end_date") val endDate: String? = null,
    val rates: Map<String, Map<String, Double>>? = null
)
