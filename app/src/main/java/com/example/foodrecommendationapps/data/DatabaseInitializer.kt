package com.example.foodrecommendationapps.data

import android.content.Context
import com.example.foodrecommendationapps.api.RetrofitBuilder
import com.example.foodrecommendationapps.api.SyncApiService
import com.example.foodrecommendationapps.data.dao.DataMakananDao
import com.example.foodrecommendationapps.data.dao.DataMakananUrtDao
import com.example.foodrecommendationapps.data.dao.UrtListDao

/**
 * Database initializer for loading data from assets
 */
class DatabaseInitializer(private val context: Context) {
    private var api: SyncApiService = RetrofitBuilder.build().create(SyncApiService::class.java)
    private lateinit var foodList: List<DataMakanan>
    private lateinit var urtList: List<UrtList>
    private lateinit var relationsList: List<DataMakananUrt>
    suspend fun loadFoodDatabaseFirstRun(
        dataMakananDao: DataMakananDao,
        urtListDao: UrtListDao,
        dataMakananUrtDao: DataMakananUrtDao
    ) {
        if (dataMakananDao.getOneFood().isEmpty()){
            try {
                val response = api.syncFood()
                if (response.body()?.success!! && response.isSuccessful){
                    foodList = response.body()?.data?.foods?: listOf()
                    urtList = response.body()?.data?.urts?: listOf()
                    relationsList = response.body()?.data?.relations?: listOf()
                }
                // Insert all data into database
                dataMakananDao.insertAll(foodList)
                urtListDao.insertAll(urtList)
                dataMakananUrtDao.insertAll(relationsList)
                println("Loaded ${foodList.size} foods, ${urtList.size} URTs, and ${relationsList.size} relations from API response")
            } catch (e: Exception) {
                e.printStackTrace()
                println("Failed to load food database from API response: ${e.message}")
            }
        }
    }
}
