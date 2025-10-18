package com.example.foodrecommendationapps.ui.reporting.view_model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class ReportViewModel() : ViewModel() {
    val selectedDate: MutableLiveData<String> = MutableLiveData("")

}

class ReportViewModelFactory(
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}