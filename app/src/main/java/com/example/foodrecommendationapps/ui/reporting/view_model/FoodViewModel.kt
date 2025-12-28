package com.example.foodrecommendationapps.ui.reporting.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodrecommendationapps.data.ConsumptionHistory
import com.example.foodrecommendationapps.data.DataMakananNameOnly
import com.example.foodrecommendationapps.data.FoodRecommendationWithName
import com.example.foodrecommendationapps.recommender.DatabaseFood
import com.example.foodrecommendationapps.recommender.LocalRecommendationEngine
import com.example.foodrecommendationapps.repo.DatabaseRepository
import kotlinx.coroutines.launch


class FoodViewModel(
    application: Application,
    private val repository: DatabaseRepository,
    private val userId: Int
) : AndroidViewModel(application) {

    private val localEngine = LocalRecommendationEngine(application)
    private val _recommendedFoods = MutableLiveData<List<FoodRecommendationWithName>>()
    val recommendedFoods: LiveData<List<FoodRecommendationWithName>> = _recommendedFoods

    val allFoods: LiveData<List<DataMakananNameOnly>> = repository.getAllFoods().asLiveData()

    init {
        generateRecommendations()
    }

    fun generateRecommendations() {
        viewModelScope.launch {
            try {
                val user = repository.getUser(userId).firstOrNull() ?: return@launch
                val recentFoodNames = repository.getRecentlyConsumedFoodNames(userId, 7)
                val allFoods = repository.getAllFoodsWithNutrients()

                if (allFoods.isEmpty()) {
                    _recommendedFoods.value = emptyList()
                    return@launch
                }

                val databaseFoods = allFoods.map {
                    DatabaseFood(
                        id = it.id,
                        nama_bahan = it.nama_bahan,
                        energi = it.energi,
                        protein = it.protein,
                        lemak = it.lemak,
                        kh = it.kh,
                        serat = it.serat
                    )
                }

                val recommendations = localEngine.generateRecommendations(
                    userProfile = user,
                    allFoods = databaseFoods,
                    recentlyConsumedNames = recentFoodNames,
                    goal = "maintain",
                    topN = 10
                )

                _recommendedFoods.value = recommendations.map { rec ->
                    FoodRecommendationWithName(
                        id = rec.foodId,
                        nama_bahan = rec.namaBahan,
                        recommendation_score = rec.matchScore
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _recommendedFoods.value = emptyList()
            }
        }
    }

    fun addFoodConsumptions(history: List<ConsumptionHistory>) {
        viewModelScope.launch {
            repository.addConsumptionHistories(history)
            generateRecommendations()
        }
    }
}

class FoodViewModelFactory(
    private val application: Application,
    private val repository: DatabaseRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodViewModel(application, repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}