package com.example.footpronostic.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footpronostic.data.model.SportMatch
import com.example.footpronostic.data.repository.FootballApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchViewModel(
    private val repository: FootballApiRepository = FootballApiRepository()
) : ViewModel() {

    private val _matches = MutableStateFlow<List<SportMatch>>(emptyList())
    val matches: StateFlow<List<SportMatch>> = _matches.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        refreshMatches()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshMatches() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = repository.getUpcomingMatches()
                _matches.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Erreur de chargement des matchs"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
