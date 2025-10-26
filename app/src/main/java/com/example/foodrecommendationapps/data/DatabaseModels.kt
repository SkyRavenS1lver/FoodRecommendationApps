package com.example.foodrecommendationapps.data

import androidx.lifecycle.MutableLiveData
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Sync: One-way from backend, weekly
 */
@Entity(
    tableName = "data_makanan",
    indices = [
        Index(value = ["nama_bahan"]),
        Index(value = ["id"])
    ]
)
data class DataMakanan(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "nama_bahan")
    val nama_bahan: String?,
    @ColumnInfo(name = "energi")
    val energi: Double = 0.0,
    @ColumnInfo(name = "protein")
    val protein: Double = 0.0,
    @ColumnInfo(name = "lemak")
    val lemak: Double = 0.0,
    @ColumnInfo(name = "karbohidrat")
    val karbohidrat: Double = 0.0,
    @ColumnInfo(name = "bdd")
    val bdd: Double = 100.0,
    // Sync metadata
    @ColumnInfo(name = "updated_at")
    val updated_at: String,
)

data class DataMakananNameOnly(
    val id: Int,
    val nama_bahan: String?
)

/**
 * Sync: One-way from backend, weekly
 */
@Entity(
    tableName = "urt_list",
    indices = [
        Index(value = ["id"])
    ]
    )
data class UrtList(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "nama_urt")
    val nama_urt: String?,
    @ColumnInfo(name = "gram_ml_per_porsi")
    val gram_ml_per_porsi: Double = 0.0,
    // Sync metadata
    @ColumnInfo(name = "updated_at")
    val updated_at: String
)

/**
 * Sync: One-way from backend, weekly
 */
@Entity(
    tableName = "data_makanan_urt",
    foreignKeys = [
        ForeignKey(
            entity = DataMakanan::class,
            parentColumns = ["id"],
            childColumns = ["data_makanan_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UrtList::class,
            parentColumns = ["id"],
            childColumns = ["urt_list_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["data_makanan_id"]),
        Index(value = ["urt_list_id"])
    ]
)
data class DataMakananUrt(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "data_makanan_id")
    val data_makanan_id: Int,
    @ColumnInfo(name = "urt_list_id")
    val urt_list_id: Int,
    // Sync metadata
    @ColumnInfo(name = "updated_at")
    val updated_at: String
)

/**
 * Sync: Two-way with backend, realtime when online
 */
@Entity(
    tableName = "consumption_history",
    foreignKeys = [
        ForeignKey(
            entity = DataMakanan::class,
            parentColumns = ["id"],
            childColumns = ["food_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UrtList::class,
            parentColumns = ["id"],
            childColumns = ["urt_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(value = ["user_id", "date_report"]),
        Index(value = ["food_id"]),
        Index(value = ["urt_id"]),
        Index(value = ["sync_status"])
    ]
)
data class ConsumptionHistory(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")
    val user_id: Int,
    @ColumnInfo(name = "food_id")
    val food_id: Int,
    @ColumnInfo(name = "urt_id")
    val urt_id: Int,
    @ColumnInfo(name = "portion_quantity")
    val portion_quantity: Double,
    @ColumnInfo(name = "percentage")
    val percentage: Double,
    @ColumnInfo(name = "date_report")
    val date_report: String,
    @ColumnInfo(name = "updated_at")
    val updated_at: String,
    // Sync metadata
    @ColumnInfo(name = "sync_status")
    val sync_status: Int = 0, // Pending default
)
data class ReportDataClass(
    val food_name: String,
    val urt_name:String,
    val date_report: String,
    val portion_quantity: Double,
    val percentage: Double,

)

@Entity(
    tableName = "user_profile",
    indices = [
        Index(value = ["id"]),
    ]
)
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String?,
    @ColumnInfo(name = "email")
    val email: String?,
    @ColumnInfo(name = "age")
    val age: Int,
    @ColumnInfo(name = "gender")
    val gender: Int,
    @ColumnInfo(name = "height")
    val height: Double,  // cm
    @ColumnInfo(name = "weight")
    val weight: Double,  // kg
    @ColumnInfo(name = "activity")
    val activity: Int = 2, // 1, 2, 3, 4
    @ColumnInfo(name = "latest_token")
    val latest_token: String?,
    @ColumnInfo(name = "is_logged_in")
    val is_logged_in: Boolean,
    // Sync metadata
    @ColumnInfo(name = "updated_at")
    val updated_at: String,
    @ColumnInfo(name = "sync_status")
    val sync_status: Int = 1, // Pending default
    @ColumnInfo(name = "last_sync")
    val last_sync: String?, // Pending default
)

@Entity(
    tableName = "food_recommendation",
    indices = [
        Index(value = ["id"]),
        Index(value = ["user_id"]),
        Index(value = ["food_id"])
    ]
)
data class FoodRecommendation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "user_id")
    val user_id: Int,
    @ColumnInfo(name = "food_id")
    val food_id: Int,
    @ColumnInfo(name = "recommendation_score")
    val recommendation_score: Double,
)

data class FoodRecommendationWithName(
    val id: Int,
    val nama_bahan: String?,
    val recommendation_score: Double
)
data class FoodWithPortions(
    @Embedded val food: DataMakanan,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = DataMakananUrt::class,
            parentColumn = "data_makanan_id",
            entityColumn = "urt_list_id"
        )
    )
    val portions: List<UrtList>
)
// Updated FoodConsumed data class
data class FoodConsumed(
    var foodId: Int = 0,
    var urtId: Int = 0,
    var foodName: String = "",
    var portionType: String = "",
    var portionQuantity: Double = 0.0,
    var percentage: Double = 100.0,
    val urtListLiveData: MutableLiveData<List<UrtList>> = MutableLiveData(emptyList())
)