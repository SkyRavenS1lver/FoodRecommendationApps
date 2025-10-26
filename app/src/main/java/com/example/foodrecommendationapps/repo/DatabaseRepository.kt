package com.example.foodrecommendationapps.repo

import com.example.foodrecommendationapps.data.ConsumptionHistory
import com.example.foodrecommendationapps.data.DataMakananNameOnly
import com.example.foodrecommendationapps.data.FoodRecommendation
import com.example.foodrecommendationapps.data.FoodRecommendationWithName
import com.example.foodrecommendationapps.data.NutritionDatabase
import com.example.foodrecommendationapps.data.ReportDataClass
import com.example.foodrecommendationapps.data.UrtList
import com.example.foodrecommendationapps.data.UserProfile
import kotlinx.coroutines.flow.Flow


class DatabaseRepository(private val database: NutritionDatabase) {
    private val dataMakananDao =  database.dataMakananDao()
    private val urtListDao =  database.urtListDao()
    private val consumptionHistoryDao =  database.consumptionHistoryDao()
    private val userProfileDao =  database.userProfileDao()
    private val foodRecommendationDao = database.foodRecommendationDao()
    private val PER_PAGE = 10;

    // ========================================================================
    // Food Functions
    // ========================================================================
    fun getAllFoods(): Flow<List<DataMakananNameOnly>> =
        dataMakananDao.getAllFoods()
    // ========================================================================
    // Portion Functions
    // ========================================================================
    suspend fun getPortionsListForFood(foodId: Int,page:Int, perPage:Int = PER_PAGE): List<UrtList> =
        urtListDao.getPortionsListForFood(foodId, offset = page-1*perPage)

    // ========================================================================
    // Consumption History
    // ========================================================================
    suspend fun getConsumptionByDate(
        userId: Int,
        date: String
    ): List<ReportDataClass> =
        consumptionHistoryDao.getConsumptionByDate(userId, date)
    suspend fun addConsumptionHistories(consumptionHistory: List<ConsumptionHistory>) =
        consumptionHistoryDao.insertAll(consumptionHistory)
    suspend fun updateConsumptionHistory(consumptionHistory: ConsumptionHistory) =
        consumptionHistoryDao.update(consumptionHistory)
    suspend fun updateSyncedConsumptionHistory(ids:List<String>) =
        consumptionHistoryDao.updateSyncedConsumptionHistory(ids)
    suspend fun getAllUnSyncedHistory(userId: Int) = consumptionHistoryDao.getConsumptionHistoryUnSynced(userId)

    // ========================================================================
    // User Profile
    // ========================================================================
    fun getLoggedUser(): Flow<List<UserProfile>> =
        userProfileDao.getLoggeduser()
    suspend fun getUser(userId: Int): List<UserProfile> =
        userProfileDao.getUserProfile(userId)
    suspend fun isUserUnsynced(userId: Int): List<UserProfile> =
        userProfileDao.getUserProfileUnsynced(userId)
    suspend fun addUser(profile: UserProfile) =
        userProfileDao.insert(profile)
    suspend fun updateToken(token:String, userId:Int) =
        userProfileDao.updateToken(token, userId)
    suspend fun updateUser(profile: UserProfile) =
        userProfileDao.update(profile)
    suspend fun updateLastSync(lastSync: String, userId:Int) =
        userProfileDao.updateLastSync(lastSync, userId)
    fun getRecommendationForUser(userId: Int): Flow<List<FoodRecommendationWithName>> =
        foodRecommendationDao.getRecommendationForUser(userId)
    suspend fun addAllRecommendation(data: List<FoodRecommendation>) =
        foodRecommendationDao.insertAll(data)
    suspend fun deleteAllRecommendation(userId: Int) =
        foodRecommendationDao.deleteByUserId(userId)
}