package com.example.foodrecommendationapps.recommender

import android.content.Context
import com.example.foodrecommendationapps.data.UserProfile
import com.google.gson.Gson
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

class LocalRecommendationEngine(context: Context) {

    private val bundle: CompleteBundle

    init {
        val json = context.assets.open("complete_bundle.json").bufferedReader().use { it.readText() }
        bundle = Gson().fromJson(json, CompleteBundle::class.java)
    }

    fun calculateBMR(userProfile: UserProfile): Double {
        val weight = userProfile.weight
        val height = userProfile.height
        val age = userProfile.age

        return if (userProfile.gender == 1) {
            (10 * weight) + (6.25 * height) - (5 * age) + 5
        } else {
            (10 * weight) + (6.25 * height) - (5 * age) - 161
        }
    }

    fun getActivityMultiplier(activityLevel: Int): Double {
        return when (activityLevel) {
            1 -> bundle.config.activityMultipliers.sedentary
            2 -> bundle.config.activityMultipliers.light
            3 -> bundle.config.activityMultipliers.moderate
            4 -> bundle.config.activityMultipliers.active
            5 -> bundle.config.activityMultipliers.veryActive
            else -> bundle.config.activityMultipliers.light
        }
    }

    fun getGoalAdjustment(goal: String): Int {
        return when (goal.lowercase()) {
            "lose", "menurunkan" -> bundle.config.goalAdjustments.lose
            "maintain", "mempertahankan" -> bundle.config.goalAdjustments.maintain
            "gain", "menambah" -> bundle.config.goalAdjustments.gain
            else -> bundle.config.goalAdjustments.maintain
        }
    }

    fun calculateTDEE(bmr: Double, activityLevel: Int, goal: String = "maintain"): Double {
        val activityMultiplier = getActivityMultiplier(activityLevel)
        val goalAdjustment = getGoalAdjustment(goal)
        return (bmr * activityMultiplier) + goalAdjustment
    }

    fun calculateMacros(tdee: Double): UserNutritionalNeeds {
        val macro = bundle.config.macroDistribution

        val proteinCalories = tdee * macro.proteinPercent
        val fatCalories = tdee * macro.fatPercent
        val carbCalories = tdee * macro.carbohydratePercent

        val proteinGrams = proteinCalories / macro.proteinKcalPerGram
        val fatGrams = fatCalories / macro.fatKcalPerGram
        val carbGrams = carbCalories / macro.carbohydrateKcalPerGram

        val fiberGrams = tdee * 0.014

        return UserNutritionalNeeds(
            tdee = tdee,
            protein = proteinGrams,
            fat = fatGrams,
            carbohydrate = carbGrams,
            fiber = fiberGrams
        )
    }

    fun normalizeValue(value: Double, featureIndex: Int): Double {
        val mean = bundle.config.scaler.mean[featureIndex]
        val std = bundle.config.scaler.std[featureIndex]
        return (value - mean) / std
    }

    fun normalizeNeeds(needs: UserNutritionalNeeds): DoubleArray {
        val values = doubleArrayOf(needs.tdee, needs.protein, needs.fat, needs.carbohydrate, needs.fiber)
        return values.mapIndexed { index, value -> normalizeValue(value, index) }.toDoubleArray()
    }

    fun normalizeNutrients(nutrients: Nutrients): DoubleArray {
        val values = nutrients.toArray()
        return values.mapIndexed { index, value -> normalizeValue(value, index) }.toDoubleArray()
    }

    fun euclideanDistance(a: DoubleArray, b: DoubleArray): Double {
        var sumSquares = 0.0
        for (i in a.indices) {
            sumSquares += (a[i] - b[i]).pow(2)
        }
        return sqrt(sumSquares)
    }

    fun rankClusters(normalizedNeeds: DoubleArray): List<ClusterCentroid> {
        return bundle.clusterCentroids.sortedBy { cluster ->
            val normalizedCentroid = normalizeNutrients(cluster.centroid)
            euclideanDistance(normalizedNeeds, normalizedCentroid)
        }
    }

    fun assignFoodToCluster(foodNutrients: Nutrients): Int {
        val normalizedFood = normalizeNutrients(foodNutrients)
        return bundle.clusterCentroids.minByOrNull { cluster ->
            val normalizedCentroid = normalizeNutrients(cluster.centroid)
            euclideanDistance(normalizedFood, normalizedCentroid)
        }?.clusterId ?: 0
    }

    suspend fun generateRecommendations(
        userProfile: UserProfile,
        allFoods: List<DatabaseFood>,
        recentlyConsumedNames: List<String> = emptyList(),
        goal: String = "maintain",
        topN: Int = 10
    ): List<FoodRecommendationResult> {
        val bmr = calculateBMR(userProfile)
        val tdee = calculateTDEE(bmr, userProfile.activity, goal)
        val needs = calculateMacros(tdee)
        val normalizedNeeds = normalizeNeeds(needs)

        val rankedClusters = rankClusters(normalizedNeeds)
        val topClusterIds = rankedClusters.take(3).map { it.clusterId }

        val recentNamesLower = recentlyConsumedNames.map { it.lowercase() }
        val candidates = allFoods
            .filterNot { it.nama_bahan.lowercase() in recentNamesLower }
            .filter { food ->
                val clusterId = assignFoodToCluster(food.toNutrients())
                clusterId in topClusterIds
            }

        val recommendations = candidates.map { food ->
            val normalizedFoodNutrients = normalizeNutrients(food.toNutrients())
            val distance = euclideanDistance(normalizedNeeds, normalizedFoodNutrients)
            val matchScore = exp(-distance)

            FoodRecommendationResult(
                foodId = food.id,
                namaBahan = food.nama_bahan,
                clusterId = assignFoodToCluster(food.toNutrients()),
                matchScore = matchScore,
                distance = distance
            )
        }

        return recommendations
            .sortedByDescending { it.matchScore }
            .take(topN)
    }
}

data class FoodRecommendationResult(
    val foodId: Int,
    val namaBahan: String,
    val clusterId: Int,
    val matchScore: Double,
    val distance: Double
)
