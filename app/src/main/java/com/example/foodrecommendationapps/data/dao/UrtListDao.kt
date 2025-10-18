package com.example.foodrecommendationapps.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.foodrecommendationapps.data.UrtList
import kotlinx.coroutines.flow.Flow

@Dao
interface UrtListDao {
    @Query("""
        SELECT ul.* FROM urt_list ul
        JOIN data_makanan_urt dmu ON ul.id = dmu.urt_list_id
        WHERE dmu.data_makanan_id = :foodId
        LIMIT :limit
        OFFSET :offset
    """)
    fun getPortionsForFood(foodId: Int, limit: Int = 10, offset: Int = 0): Flow<List<UrtList>>
    @Query("""
        SELECT ul.* FROM urt_list ul
        JOIN data_makanan_urt dmu ON ul.id = dmu.urt_list_id
        WHERE dmu.data_makanan_id = :foodId
        LIMIT :limit
        OFFSET :offset
    """)
    suspend fun getPortionsListForFood(foodId: Int, limit: Int = 10, offset: Int = 0): List<UrtList>

    @Query("SELECT * FROM urt_list ORDER BY nama_urt COLLATE NOCASE")
    fun getAllPortions(): Flow<List<UrtList>>
    @Query("""
        SELECT * FROM urt_list ORDER BY nama_urt COLLATE NOCASE
        LIMIT :limit
        OFFSET :offset
    """)
    fun getAllPortionsPaginated(limit: Int = 10, offset: Int = 0): Flow<List<UrtList>>

    @Query("SELECT * FROM urt_list WHERE id = :urtId")
    suspend fun getPortionsById(urtId: Int): UrtList?

    @Query("SELECT * FROM urt_list WHERE id = :urtId")
    fun getPortionsByIdFlow(urtId: Int): Flow<UrtList?>

    @Query("""
        SELECT * FROM urt_list
        WHERE nama_urt LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY nama_urt COLLATE NOCASE
        LIMIT :limit
        OFFSET :offset
    """)
    fun searchPortionsByName(query: String, limit: Int = 10, offset: Int = 0): Flow<List<UrtList>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(portion: UrtList)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(portion: List<UrtList>)
    @Update
    suspend fun update(portion: UrtList)
    @Delete
    suspend fun delete(portion: UrtList)
    @Query("DELETE FROM urt_list")
    suspend fun deleteAll()
    @Query("SELECT COUNT(*) FROM urt_list")
    suspend fun getCount(): Int
}