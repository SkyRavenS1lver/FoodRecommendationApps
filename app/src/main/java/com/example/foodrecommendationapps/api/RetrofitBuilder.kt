package com.example.foodrecommendationapps.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {
    private const val BASE_URL = "https://shelba-trinary-luminously.ngrok-free.dev/api/"

    private fun createGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Double::class.java, JsonDeserializer { json, _, _ ->
                val value = json.asString.replace(",", ".")
                value.toDoubleOrNull() ?: 0.0
            })
            .registerTypeAdapter(Double::class.javaObjectType, JsonDeserializer { json, _, _ ->
                val value = json.asString.replace(",", ".")
                value.toDoubleOrNull() ?: 0.0
            })
            .create()
    }

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
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }
}
