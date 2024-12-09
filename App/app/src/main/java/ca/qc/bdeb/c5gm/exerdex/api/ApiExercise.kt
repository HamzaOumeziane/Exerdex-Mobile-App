package ca.qc.bdeb.c5gm.exerdex.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiExercise {
    private const val BASE_URL: String = "https://api.api-ninjas.com/v1/"
    private const val API_KEY: String =  "9k+sevfwAe2cmbuSjd9P6Q==YiRRhw4XpFLzq6TU"


    private val apiKeyInterceptor: Interceptor by lazy {    // Pour ajouter la cle API à chaque requête
        Interceptor { chain ->
            val original: Request = chain.request()
            val requestWithApiKey: Request = original.newBuilder()
                .header("X-Api-Key", API_KEY)
                .build()
            chain.proceed(requestWithApiKey)
        }
    }

    private val gson: Gson by lazy {
        GsonBuilder().setLenient().create()
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiExerciseService: ApiExerciseService by lazy {
        retrofit.create(ApiExerciseService::class.java)
    }

}