package com.example.foodrecommendationapps.recommender

import com.example.foodrecommendationapps.data.FoodRecommendationWithName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

object FoodRecommendationManager {

    /**
     * Returns a Flow emitting a hybrid recommendation list:
     * - Top N foods (fixed, ranked by score) - High reliability (85.7% hit rate @ N=5)
     * - K foods (weighted random from remaining) - Discovery & variety
     *
     * Based on temporal validation (N=7 users):
     * - N=5 achieves 85.7% hit rate (reliable)
     * - 70% nutrition weight provides best ranking quality
     * - Hybrid approach balances algorithmic precision with user engagement
     *
     * @param foodFlow Flow of all available Food data from Room (pre-sorted by score DESC).
     * @param fixedTopN Number of top-ranked foods to always show (default: 5).
     * @param randomK Number of weighted-random foods from remaining pool (default: 5).
     */
    fun getWeightedRecommendationsFlow(
        foodFlow: Flow<List<FoodRecommendationWithName>>,
        fixedTopN: Int = 5,
        randomK: Int = 5
    ): Flow<List<FoodRecommendationWithName>> {
        return foodFlow.map { foods ->
            if (foods.isEmpty()) return@map emptyList()

            // Sort by recommendation score descending (highest first)
            val sortedFoods = foods.sortedByDescending { it.recommendation_score }

            val totalDisplay = fixedTopN + randomK

            // If not enough foods, return all sorted
            if (sortedFoods.size <= fixedTopN) {
                return@map sortedFoods
            }

            // Section 1: Fixed top N (always shown, ranked)
            val topPicks = sortedFoods.take(fixedTopN)

            // Section 2: Weighted random K from remaining pool
            val remainingPool = sortedFoods.drop(fixedTopN)
            val discoverMore = if (remainingPool.isEmpty()) {
                emptyList()
            } else {
                weightedRandomSample(remainingPool, k = minOf(randomK, remainingPool.size))
            }

            // Combine: top picks first, then discover more
            topPicks + discoverMore
        }
    }

    /**
     * Sample K items from the pool using weighted randomness (without replacement).
     * Higher recommendation scores have higher probability of being selected.
     *
     * @param foods Pool of foods to sample from.
     * @param k Number of items to sample.
     * @return List of K sampled foods (weighted random, no duplicates).
     */
    private fun weightedRandomSample(
        foods: List<FoodRecommendationWithName>,
        k: Int
    ): List<FoodRecommendationWithName> {
        if (foods.isEmpty()) return emptyList()
        if (k >= foods.size) return foods.shuffled()

        // Normalize scores for weighted sampling
        val totalScore = foods.sumOf { it.recommendation_score }
        if (totalScore <= 0.0) return foods.shuffled().take(k)

        val normalizedFoods = foods.map {
            it.copy(recommendation_score = it.recommendation_score / totalScore)
        }

        val selected = mutableListOf<FoodRecommendationWithName>()
        val available = normalizedFoods.toMutableList()

        repeat(k) {
            val pick = weightedRandomPick(available)
            selected.add(pick)
            available.remove(pick)
        }

        return selected
    }

    /**
     * Pick one item from the list using weighted random selection.
     * Uses cumulative probability distribution.
     */
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