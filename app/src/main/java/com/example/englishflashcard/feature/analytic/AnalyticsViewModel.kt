package com.example.englishflashcard.feature.analytic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishflashcard.data.repository.AnalyticsRepository
import com.example.englishflashcard.model.AnalyticsResponse
import kotlinx.coroutines.launch

class AnalyticsViewModel(private val repository: AnalyticsRepository) : ViewModel() {
    var analyticsData by mutableStateOf<AnalyticsResponse?>(null)
        private set
    
    var isLoading by mutableStateOf(false)
        private set

    init {
        fetchAnalytics()
    }

    fun fetchAnalytics() {
        viewModelScope.launch {
            isLoading = true
            repository.fetchAnalyticsRemote()
            analyticsData = repository.analyticsData
            isLoading = false
        }
    }
}
