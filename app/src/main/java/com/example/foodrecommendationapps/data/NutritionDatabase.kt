package com.example.foodrecommendationapps.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
    version = 5,
    exportSchema = false
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

        // Migration from version 2 to 3: Add index on user_id for better performance
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add index on user_id column in food_recommendation table
                database.execSQL("CREATE INDEX IF NOT EXISTS index_food_recommendation_user_id ON food_recommendation(user_id)")
            }
        }
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add index on user_id column in food_recommendation table
                database.execSQL("ALTER TABLE users DROP COLUMN created_at")
            }
        }

        // Migration from version 3 to 4: Remove foreign key constraints from food_recommendation
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // SQLite doesn't support dropping foreign keys directly
                // We need to recreate the table without foreign keys

                // Create new table without foreign keys
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS food_recommendation_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        user_id INTEGER NOT NULL,
                        food_id INTEGER NOT NULL,
                        recommendation_score REAL NOT NULL
                    )
                """)

                // Copy data from old table
                database.execSQL("""
                    INSERT INTO food_recommendation_new (id, user_id, food_id, recommendation_score)
                    SELECT id, user_id, food_id, recommendation_score FROM food_recommendation
                """)

                // Drop old table
                database.execSQL("DROP TABLE food_recommendation")

                // Rename new table to original name
                database.execSQL("ALTER TABLE food_recommendation_new RENAME TO food_recommendation")

                // Recreate indices
                database.execSQL("CREATE INDEX IF NOT EXISTS index_food_recommendation_id ON food_recommendation(id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_food_recommendation_user_id ON food_recommendation(user_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_food_recommendation_food_id ON food_recommendation(food_id)")
            }
        }

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
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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

                // Enable foreign key constraints
                db.execSQL("PRAGMA foreign_keys=ON")

                // Create triggers on database creation
//                createTriggers(db)

                this@Companion.INSTANCE?.let { database ->
                    scope.launch {
                        populateDatabase(context, database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Ensure foreign keys are enabled every time database is opened
                db.execSQL("PRAGMA foreign_keys=ON")
            }

//        private fun createTriggers(db: SupportSQLiteDatabase) {
//                // Auto-update updated_at on consumption_history
//                db.execSQL("""
//                    CREATE TRIGGER IF NOT EXISTS update_consumption_timestamp
//                    AFTER UPDATE ON consumption_history
//                    BEGIN
//                        UPDATE consumption_history SET updated_at = datetime('now', 'localtime') WHERE id = NEW.id;
//                        UPDATE consumption_history SET sync_status = 0;
//                    END
//                """)
//                // Auto-update updated_at on user_profile
//                db.execSQL("""
//                    CREATE TRIGGER IF NOT EXISTS update_user_profile_timestamp
//                    AFTER UPDATE ON user_profile
//                    BEGIN
//                        UPDATE user_profile SET updated_at = datetime('now', 'localtime') WHERE id = NEW.id;
//                        UPDATE user_profile SET sync_status = 0;
//                    END
//                """)
//            }
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