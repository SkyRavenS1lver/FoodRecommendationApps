package com.example.foodrecommendationapps.ui.reporting.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodrecommendationapps.data.ConsumptionHistory
import com.example.foodrecommendationapps.data.DataMakananNameOnly
import com.example.foodrecommendationapps.data.FoodRecommendationWithName
import com.example.foodrecommendationapps.recommender.FoodRecommendationManager
import com.example.foodrecommendationapps.repo.DatabaseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class FoodViewModel(private val repository: DatabaseRepository, private val userId:Int) : ViewModel() {
    val recommendedFoods: LiveData<List<FoodRecommendationWithName>> =
        FoodRecommendationManager.getWeightedRecommendationsFlow(
        repository.getRecommendationForUser(userId)
    ).stateIn(viewModelScope, SharingStarted.Lazily, emptyList()).asLiveData()
    val allFoods: LiveData<List<DataMakananNameOnly>> = repository.getAllFoods().asLiveData()

    fun addFoodConsumptions(history: List<ConsumptionHistory>) {
        viewModelScope.launch {
            repository.addConsumptionHistories(history)
        }
    }
}

class FoodViewModelFactory(
    private val repository: DatabaseRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}