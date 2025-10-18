package com.example.foodrecommendationapps.recommender

import com.example.foodrecommendationapps.data.FoodRecommendationWithName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

object FoodRecommendationManager {

    /**
     * Returns a Flow emitting a weighted-randomized list of Food items.
     *
     * @param foodFlow Flow of all available Food data from Room.
     * @param limit How many recommendations to return.
     */
    fun getWeightedRecommendationsFlow(
        foodFlow: Flow<List<FoodRecommendationWithName>>,
        limit: Int = 10
    ): Flow<List<FoodRecommendationWithName>> {
        return foodFlow.map { foods ->
            if (foods.isEmpty()) return@map emptyList()

            val totalScore = foods.sumOf { it.recommendation_score }
            if (totalScore <= 0.0) return@map foods.shuffled().take(limit)

            val normalizedFoods = foods.map {
                it.copy(recommendation_score = it.recommendation_score / totalScore)
            }

            val selected = mutableListOf<FoodRecommendationWithName>()
            val available = normalizedFoods.toMutableList()

            repeat(minOf(limit, available.size)) {
                val pick = weightedRandomPick(available)
                selected.add(pick)
                available.remove(pick)
            }

            selected
        }
    }

    private fun weightedRandomPick(foods: List<FoodRecommendationWithName>): FoodRecommendationWithName {
        val r = Random.nextDouble()
        var cumulative = 0.0
        for (food in foods) {
            cumulative += food.recommendation_score
            if (r <= cumulative) return food
        }
        return foods.last()
    }
}