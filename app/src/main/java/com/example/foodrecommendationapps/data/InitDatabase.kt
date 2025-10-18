package com.example.foodrecommendationapps.data

import android.app.Application
import com.example.foodrecommendationapps.repo.DatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class InitDatabase : Application() {

    // Application scope for long-running operations
    val applicationScope = CoroutineScope(SupervisorJob())

    // Database instance (lazy initialization)
    val database by lazy {
        NutritionDatabase.getDatabase(this, applicationScope)
    }

    // Repository instance
    val repository by lazy {
        DatabaseRepository(database)
    }

    override fun onCreate() {
        super.onCreate()
        // Database will be initialized on first access
    }
}