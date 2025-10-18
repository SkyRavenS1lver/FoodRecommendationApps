package com.example.foodrecommendationapps.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.foodrecommendationapps.data.ConsumptionHistory
import com.example.foodrecommendationapps.data.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = :userId")
    fun getUserProfile(userId: Int): Flow<UserProfile?>
    @Query("SELECT * FROM user_profile WHERE is_logged_in = 1")
    fun getLoggeduser(): Flow<List<UserProfile>>
    @Query("""
        SELECT * FROM consumption_history
        WHERE user_id = :userId
        ORDER BY date_report DESC
        LIMIT :limit
        OFFSET :offset
        """)
    fun getConsumptionHistoryPaginated(userId: Int, limit: Int = 10, offset: Int = 0): Flow<List<ConsumptionHistory>>

    @Query("""
        SELECT * FROM consumption_history
        WHERE user_id = :userId
        AND date_report BETWEEN :startTime AND :endTime
        ORDER BY date_report DESC
    """)
    fun getConsumptionByDateRange(userId: Int, startTime: Long, endTime: Long): Flow<List<ConsumptionHistory>>

    @Query("""
        SELECT * FROM consumption_history
        WHERE user_id = :userId
        AND date_report BETWEEN :startTime AND :endTime
        ORDER BY date_report DESC
        LIMIT :limit
        OFFSET :offset
    """)
    fun getConsumptionByDateRangePaginated(userId: Int, startTime: Long, endTime: Long, limit: Int = 10, offset: Int = 0): Flow<List<ConsumptionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfile): Long
    @Update
    suspend fun update(profile: UserProfile)

    @Query("UPDATE user_profile SET latest_token = :token, is_logged_in = 1 WHERE  id = :userId")
    suspend fun updateToken(token:String, userId: Int)
    @Delete
    suspend fun delete(profile: UserProfile)
    @Query("DELETE FROM user_profile WHERE id = :userId")
    suspend fun deleteByUserId(userId: Int)
    @Query("DELETE FROM user_profile")
    suspend fun deleteAll()
}