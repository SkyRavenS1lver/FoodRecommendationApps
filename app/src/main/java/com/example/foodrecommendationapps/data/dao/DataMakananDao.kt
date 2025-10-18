package com.example.foodrecommendationapps.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.foodrecommendationapps.data.DataMakanan
import com.example.foodrecommendationapps.data.DataMakananNameOnly
import com.example.foodrecommendationapps.data.FoodWithPortions
import kotlinx.coroutines.flow.Flow

@Dao
interface DataMakananDao {
    @Query("""
            SELECT id as id, nama_bahan as nama_bahan FROM data_makanan
            ORDER BY nama_bahan COLLATE NOCASE
        """)
    fun getAllFoods(): Flow<List<DataMakananNameOnly>>
    @Query("""
        SELECT * FROM data_makanan
        ORDER BY nama_bahan COLLATE NOCASE
        LIMIT :limit
        OFFSET :offset
    """)
    fun getAllFoodsPaginated(limit: Int = 10, offset: Int = 0): Flow<List<DataMakanan>>

    @Query("SELECT * FROM data_makanan WHERE id = :foodId")
    suspend fun getFoodById(foodId: Int): DataMakanan?

    @Query("SELECT * FROM data_makanan WHERE id = :foodId")
    fun getFoodByIdFlow(foodId: Int): Flow<DataMakanan?>
    @Query("SELECT * FROM data_makanan LIMIT 1")
    suspend fun getOneFood(): List<DataMakanan>

    @Query("""
        SELECT * FROM data_makanan
        WHERE nama_bahan LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY nama_bahan COLLATE NOCASE
        LIMIT :limit
        OFFSET :offset
    """)
    fun searchFoodsByName(query: String, limit: Int = 10, offset: Int = 0): Flow<List<DataMakanan>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(food: DataMakanan)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<DataMakanan>)
    @Update
    suspend fun update(food: DataMakanan)
    @Delete
    suspend fun delete(food: DataMakanan)
    @Query("DELETE FROM data_makanan")
    suspend fun deleteAll()
    @Query("SELECT COUNT(*) FROM data_makanan")
    suspend fun getCount(): Int
}