package com.example.foodrecommendationapps.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.foodrecommendationapps.data.FoodRecommendation
import com.example.foodrecommendationapps.data.FoodRecommendationWithName
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodRecommendationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: FoodRecommendation)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<FoodRecommendation>)
    @Query("""SELECT dm.id as id, fr.recommendation_score as recommendation_score, dm.nama_bahan as nama_bahan
         FROM food_recommendation as fr
            JOIN data_makanan as dm on dm.id = fr.food_id
            WHERE user_id = :userId""")
    fun getRecommendationForUser(userId:Int): Flow<List<FoodRecommendationWithName>>
    @Update
    suspend fun update(data: FoodRecommendation)
    @Delete
    suspend fun delete(data: FoodRecommendation)
    @Query("DELETE FROM food_recommendation WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: Int)
    @Query("DELETE FROM food_recommendation")
    suspend fun deleteAll()
}