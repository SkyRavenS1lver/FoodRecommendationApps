package com.example.foodrecommendationapps.ui.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodrecommendationapps.data.RegisterResponse
import com.example.foodrecommendationapps.data.UserProfile
import com.example.foodrecommendationapps.repo.DatabaseRepository
import kotlinx.coroutines.launch


class UserViewModel(private val repository: DatabaseRepository) : ViewModel() {
    suspend fun register(registerResponse: RegisterResponse, user: UserProfile) {
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
    fun updateToken(token:String?, userId:Int) {
        if (token != null && token != ""){
            viewModelScope.launch {
                repository.updateToken(token, userId)
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