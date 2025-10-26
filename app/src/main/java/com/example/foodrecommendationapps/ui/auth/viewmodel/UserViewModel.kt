package com.example.foodrecommendationapps.ui.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.foodrecommendationapps.data.FoodRecommendation
import com.example.foodrecommendationapps.data.RegisterResponse
import com.example.foodrecommendationapps.data.UserProfile
import com.example.foodrecommendationapps.repo.DatabaseRepository
import kotlinx.coroutines.launch


class UserViewModel(private val repository: DatabaseRepository) : ViewModel() {
    fun register(registerResponse: RegisterResponse, user: UserProfile) {
        if (registerResponse.success && registerResponse.data != null){
            viewModelScope.launch {
                repository.addUser(user)
                repository.addAllRecommendation(registerResponse.data.food_recommendation)
            }
        }
    }
    fun updateUser(user: UserProfile) {
        if (user.last_sync != null && user.last_sync != ""){
            viewModelScope.launch {
                repository.updateUser(user)
            }
        }
    }
    fun loginUpdate(token:String?, userId:Int, recommendations: List<FoodRecommendation>, email:String) {
        if (token != null && token != ""){
            viewModelScope.launch {
                val user = repository.getUser(userId)
                if (user.isEmpty()){
                    val newUser = UserProfile(
                        id = userId,
                        name = null,
                        email = email,
                        age = 0,
                        gender = 0,
                        height = 0.0,
                        weight = 0.0,
                        activity = 0,
                        latest_token = token,
                        is_logged_in = true,
                        updated_at = "",
                        sync_status = 0,
                        last_sync = null
                    )
                    repository.addUser(newUser)
                }
                else{
                    repository.updateToken(token, userId)
                }
                repository.deleteAllRecommendation(userId)
                repository.addAllRecommendation(recommendations)
            }
        }
    }
    val loggedUser: LiveData<List<UserProfile>> = repository.getLoggedUser().asLiveData()
}

class UserViewModelFactory(
    private val repository: DatabaseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}