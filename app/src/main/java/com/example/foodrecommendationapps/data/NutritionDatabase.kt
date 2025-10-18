package com.example.foodrecommendationapps.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.foodrecommendationapps.data.dao.ConsumptionHistoryDao
import com.example.foodrecommendationapps.data.dao.DataMakananDao
import com.example.foodrecommendationapps.data.dao.DataMakananUrtDao
import com.example.foodrecommendationapps.data.dao.FoodRecommendationDao
import com.example.foodrecommendationapps.data.dao.UrtListDao
import com.example.foodrecommendationapps.data.dao.UserProfileDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DataMakanan::class,
        UrtList::class,
        DataMakananUrt::class,
        ConsumptionHistory::class,
        UserProfile::class,
        FoodRecommendation::class
    ],
    version = 2
)
abstract class NutritionDatabase : RoomDatabase() {
    abstract fun dataMakananDao(): DataMakananDao
    abstract fun urtListDao(): UrtListDao
    abstract fun dataMakananUrtDao(): DataMakananUrtDao
    abstract fun consumptionHistoryDao(): ConsumptionHistoryDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun foodRecommendationDao(): FoodRecommendationDao
    companion object {
        @Volatile
        private var INSTANCE: NutritionDatabase? = null

        private const val DATABASE_NAME = "food_recommendation_apps_database"

        fun getDatabase(
            context: Context,
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        ): NutritionDatabase {
            return this.INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NutritionDatabase::class.java,
                    this.DATABASE_NAME
                )
                    .addCallback(DatabaseCallback(context.applicationContext, scope))
                    .fallbackToDestructiveMigration() // For development only
                    .build()
                this.INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context,
            private val scope: CoroutineScope
        ):RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // Create triggers on database creation
//                createTriggers(db)

                this@Companion.INSTANCE?.let { database ->
                    scope.launch {
                        populateDatabase(context, database)
                    }
                }
            }

        private fun createTriggers(db: SupportSQLiteDatabase) {
                // Auto-update updated_at on consumption_history
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS update_consumption_timestamp
                    AFTER UPDATE ON consumption_history
                    BEGIN
                        UPDATE consumption_history SET updated_at = datetime('now', 'localtime') WHERE id = NEW.id;
                        UPDATE consumption_history SET sync_status = 0;
                    END
                """)
                // Auto-update updated_at on user_profile
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS update_user_profile_timestamp
                    AFTER UPDATE ON user_profile
                    BEGIN
                        UPDATE user_profile SET updated_at = datetime('now', 'localtime') WHERE id = NEW.id;
                        UPDATE user_profile SET sync_status = 0;
                    END
                """)
            }
        }
        private suspend fun populateDatabase(context:Context, database: NutritionDatabase) {
            try {
                val initializer = DatabaseInitializer(context)

                println("Starting database population...")

                // Load food database from API response (includes foods, urts, and relations)
                initializer.loadFoodDatabaseFirstRun(
                    database.dataMakananDao(),
                    database.urtListDao(),
                    database.dataMakananUrtDao()
                )
                println("Database population completed successfully!")
            } catch (e: Exception) {
                e.printStackTrace()
                println("Failed to populate database: ${e.message}")
            }
        }
    }
}