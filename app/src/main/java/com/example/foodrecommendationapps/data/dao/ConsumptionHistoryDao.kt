package com.example.foodrecommendationapps.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.foodrecommendationapps.data.ConsumptionHistory
import com.example.foodrecommendationapps.data.ReportDataClass

@Dao
interface ConsumptionHistoryDao {
    @Query("SELECT * FROM consumption_history WHERE user_id = :userId AND sync_status = 0 ORDER BY date_report DESC")
    suspend fun getConsumptionHistoryUnSynced(userId: Int): List<ConsumptionHistory>
    @Query("""
        SELECT dm.nama_bahan as food_name, ul.nama_urt as urt_name, ch.date_report as date_report, ch.portion_quantity as portion_quantity, ch.percentage as percentage
        FROM consumption_history ch
        JOIN data_makanan dm on ch.food_id = dm.id
        JOIN urt_list ul on ch.urt_id = ul.id
        WHERE user_id = :userId
        AND date_report LIKE :date || '%' COLLATE NOCASE
        ORDER BY date_report DESC
    """)
    suspend fun getConsumptionByDate(userId: Int, date: String): List<ReportDataClass>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ConsumptionHistory)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(record: List<ConsumptionHistory>)
    @Update
    suspend fun update(record: ConsumptionHistory)
    @Query("UPDATE consumption_history SET sync_status = 1 WHERE id in (:ids)")
    suspend fun updateSyncedConsumptionHistory(ids:List<String>)
    @Delete
    suspend fun delete(record: ConsumptionHistory)
}