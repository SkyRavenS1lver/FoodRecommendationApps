package com.example.foodrecommendationapps.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {
    private const val BASE_URL = "https://shelba-trinary-luminously.ngrok-free.dev/api/"

    fun build(token: String = ""): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                    .addHeader("ngrok-skip-browser-warning", "yes")

                // Add Authorization header if token is provided
                if (token.isNotEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                chain.proceed(requestBuilder.build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
