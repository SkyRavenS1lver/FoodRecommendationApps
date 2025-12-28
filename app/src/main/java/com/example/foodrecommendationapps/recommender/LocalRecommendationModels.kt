package com.example.foodrecommendationapps.recommender

import com.google.gson.annotations.SerializedName

data class CompleteBundle(
    val config: Config,
    @SerializedName("cluster_centroids")
    val clusterCentroids: List<ClusterCentroid>
)

data class Config(
    @SerializedName("model_info")
    val modelInfo: ModelInfo,
    val scaler: Scaler,
    @SerializedName("bmr_formulas")
    val bmrFormulas: BmrFormulas,
    @SerializedName("activity_multipliers")
    val activityMultipliers: ActivityMultipliers,
    @SerializedName("macro_distribution")
    val macroDistribution: MacroDistribution,
    @SerializedName("goal_adjustments")
    val goalAdjustments: GoalAdjustments
)

data class ModelInfo(
    val algorithm: String,
    @SerializedName("n_clusters")
    val nClusters: Int,
    val features: List<String>,
    @SerializedName("distance_metric")
    val distanceMetric: String,
    val normalization: String
)

data class Scaler(
    val mean: List<Double>,
    val std: List<Double>,
    @SerializedName("feature_names")
    val featureNames: List<String>
)

data class BmrFormulas(
    val male: String,
    val female: String,
    val source: String
)

data class ActivityMultipliers(
    val sedentary: Double,
    val light: Double,
    val moderate: Double,
    val active: Double,
    @SerializedName("very_active")
    val veryActive: Double
)

data class MacroDistribution(
    @SerializedName("protein_percent")
    val proteinPercent: Double,
    @SerializedName("fat_percent")
    val fatPercent: Double,
    @SerializedName("carbohydrate_percent")
    val carbohydratePercent: Double,
    @SerializedName("protein_kcal_per_gram")
    val proteinKcalPerGram: Int,
    @SerializedName("fat_kcal_per_gram")
    val fatKcalPerGram: Int,
    @SerializedName("carbohydrate_kcal_per_gram")
    val carbohydrateKcalPerGram: Int
)

data class GoalAdjustments(
    val lose: Int,
    val maintain: Int,
    val gain: Int
)

data class ClusterCentroid(
    @SerializedName("cluster_id")
    val clusterId: Int,
    val centroid: Nutrients,
    val characterization: String
)

data class Nutrients(
    @SerializedName("ENERGI")
    val energi: Double,
    @SerializedName("PROTEIN")
    val protein: Double,
    @SerializedName("LEMAK")
    val lemak: Double,
    @SerializedName("KH")
    val kh: Double,
    @SerializedName("SERAT")
    val serat: Double
) {
    fun toArray(): DoubleArray = doubleArrayOf(energi, protein, lemak, kh, serat)
}

data class DatabaseFood(
    val id: Int,
    val nama_bahan: String,
    val energi: Double,
    val protein: Double,
    val lemak: Double,
    val kh: Double,
    val serat: Double
) {
    fun toNutrients() = Nutrients(energi, protein, lemak, kh, serat)
}

data class UserNutritionalNeeds(
    val tdee: Double,
    val protein: Double,
    val fat: Double,
    val carbohydrate: Double,
    val fiber: Double
) {
    fun toNutrients(): Nutrients = Nutrients(tdee, protein, fat, carbohydrate, fiber)
}
