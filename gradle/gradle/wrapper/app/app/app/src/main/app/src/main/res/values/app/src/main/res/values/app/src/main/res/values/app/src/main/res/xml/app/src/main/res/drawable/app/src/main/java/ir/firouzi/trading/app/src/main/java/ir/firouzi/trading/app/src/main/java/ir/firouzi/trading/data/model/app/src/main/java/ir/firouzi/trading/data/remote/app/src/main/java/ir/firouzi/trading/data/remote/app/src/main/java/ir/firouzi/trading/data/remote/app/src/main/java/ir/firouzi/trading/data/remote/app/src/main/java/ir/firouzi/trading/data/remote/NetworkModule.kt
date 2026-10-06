package ir.firouzi.trading.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val ramzinex: RamzinexApi by lazy {
        retrofit("https://publicapi.ramzinex.com/").create(RamzinexApi::class.java)
    }

    val nobitex: NobitexApi by lazy {
        retrofit("https://api.nobitex.ir/").create(NobitexApi::class.java)
    }

    val wallex: WallexApi by lazy {
        retrofit("https://api.wallex.ir/").create(WallexApi::class.java)
    }

    val forex: ForexApi by lazy {
        retrofit("https://api.frankfurter.app/").create(ForexApi::class.java)
    }
}
