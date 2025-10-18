package com.example.foodrecommendationapps.api

import com.example.foodrecommendationapps.data.ConsumptionSyncRequest
import com.example.foodrecommendationapps.data.ConsumptionSyncResponse
import com.example.foodrecommendationapps.data.FoodSyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SyncApiService {
    @GET("sync/foods")
    suspend fun syncFood(
        @Query("last_sync") lastSync: Int
    ): Response<FoodSyncResponse>
    @GET("sync/foods")
    suspend fun syncFood(): Response<FoodSyncResponse>
    @POST("sync/consumptions")
    suspend fun syncConsumption(@Body request: ConsumptionSyncRequest): Response<ConsumptionSyncResponse>

}
