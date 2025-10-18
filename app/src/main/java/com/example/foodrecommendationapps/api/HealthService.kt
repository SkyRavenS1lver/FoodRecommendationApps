package com.example.foodrecommendationapps.api

import retrofit2.Response
import retrofit2.http.GET

interface HealthService {
    @GET("")
    suspend fun healthCheck(): Response<Unit>
}
