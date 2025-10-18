package com.example.foodrecommendationapps.data

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: DataLoginBody?
)

data class DataLoginBody(
    val user_id:Int?,
    val token: String?
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
    val data: DataBodyRegisterResponse?,
)
data class DataBodyRegisterResponse(
    val token: String?,
    val user_id: Int,
    val updated_at: String,
    val food_recommendation: List<FoodRecommendation>,
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String
)

data class ChangePasswordRequest(
    val token: String,
    val newPassword: String
)

data class ChangePasswordResponse(
    val success: Boolean,
    val message: String
)
