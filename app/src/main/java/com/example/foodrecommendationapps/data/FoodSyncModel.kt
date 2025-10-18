package com.example.foodrecommendationapps.data

data class FoodSyncResponse(
    val success: Boolean,
    val message: String,
    val data: FoodSyncResponseData?
)

data class FoodSyncResponseData(
    val foods: List<DataMakanan>?,
    val urts: List<UrtList>?,
    val relations: List<DataMakananUrt>?
)

data class ConsumptionSyncRequest(
    val last_sync: String?,
    val local_changes: List<ConsumptionHistory>
)
data class ConsumptionSyncResponse(
    val success: Boolean,
    val message: String,
    val data: ConsumptionSyncResponseDataBody?
)

data class ConsumptionSyncResponseDataBody(
    val server_changes: List<ConsumptionHistory>,
    val accepted: List<String>,
    val rejected: List<String>,
    val conflicts: List<ConsumptionHistory>,
    val food_recommendation: List<FoodRecommendation>,
    val sync_timestamp: String
    )