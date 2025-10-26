package com.example.foodrecommendationapps.data


data class ProfileSyncResponse(
    val success: Boolean,
    val message: String,
    val data: UserProfile?
)