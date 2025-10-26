package com.example.foodrecommendationapps.data

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: DataBodyResponse?
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val gender: Int, // 0:"male" or 1:"female"
    val age: Int,
    val height: Double,
    val weight: Double,
    val activity: Int // 1-5
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val data: DataBodyResponse?,
)
data class DataBodyResponse(
    val token: String?,
    val user_id: Int,
    val updated_at: String,
    val food_recommendation: List<FoodRecommendation>,
)

