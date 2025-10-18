package com.example.foodrecommendationapps.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.foodrecommendationapps.data.DataMakananUrt
import kotlinx.coroutines.flow.Flow

@Dao
interface DataMakananUrtDao {

    @Query("SELECT * FROM data_makanan_urt WHERE data_makanan_id = :foodId")
    fun getLinksForFood(foodId: Int): Flow<List<DataMakananUrt>>

    @Query("SELECT * FROM data_makanan_urt WHERE id = :foodId")
    suspend fun getPortionsById(foodId: Int): DataMakananUrt?

    @Query("SELECT * FROM data_makanan_urt WHERE id = :foodId")
    fun getPortionsByIdFlow(foodId: Int): Flow<DataMakananUrt?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: DataMakananUrt)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(links: List<DataMakananUrt>)
    @Update
    suspend fun update(link: DataMakananUrt)
    @Delete
    suspend fun delete(link: DataMakananUrt)
    @Query("DELETE FROM data_makanan_urt")
    suspend fun deleteAll()
}