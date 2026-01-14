package com.example.footpronostic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footpronostic.data.model.Pronostic
import com.example.footpronostic.data.model.SportMatch
import com.example.footpronostic.data.model.toPronostic
import com.example.footpronostic.data.repository.PronosticRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PronosticViewModel(
    private val repository: PronosticRepository = PronosticRepository()
) : ViewModel() {

    private val _pronostics = MutableStateFlow<List<Pronostic>>(emptyList())
    val pronostics: StateFlow<List<Pronostic>> = _pronostics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private var collectionJob: Job? = null

    /**
     * Charge les pronostics d'un utilisateur et écoute les changements en temps réel.
     * Annule toute collection précédente pour éviter les doublons.
     */
    fun loadUserPronostics(userId: String) {
        if (userId.isBlank()) return

        // Annuler la collection précédente si elle existe
        collectionJob?.cancel()

        collectionJob = viewModelScope.launch {
            repository.getUserPronostics(userId).collect { list ->
                _pronostics.value = list
            }
        }
    }

    suspend fun canBetOnMatch(userId: String, matchId: String): Boolean {
        return !repository.hasUserBetOnMatch(userId, matchId)
    }

    fun createPronostic(match: SportMatch, userId: String, scoreA: Int, scoreB: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val pronostic = match.toPronostic(userId, scoreA, scoreB)
            val result = repository.addPronostic(pronostic)

            result.onSuccess {
                _successMessage.value = "Pronostic enregistré !"
            }.onFailure {
                _errorMessage.value = it.message ?: "Erreur lors de l'ajout"
            }
            _isLoading.value = false
        }
    }

    fun updatePronostic(pronostic: Pronostic, newScoreA: Int, newScoreB: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val winner = when {
                newScoreA > newScoreB -> "teamA"
                newScoreB > newScoreA -> "teamB"
                else -> "draw"
            }

            val updatedPronostic = pronostic.copy(
                predictedScoreA = newScoreA,
                predictedScoreB = newScoreB,
                predictedWinner = winner
            )

            val result = repository.updatePronostic(updatedPronostic)
            result.onSuccess {
                _successMessage.value = "Pronostic modifié !"
            }.onFailure {
                _errorMessage.value = it.message ?: "Erreur lors de la modification"
            }
            _isLoading.value = false
        }
    }

    fun deletePronostic(id: String) {
        viewModelScope.launch {
            // Mise à jour optimiste locale immédiate
            val currentList = _pronostics.value.toMutableList()
            val itemToRemove = currentList.find { it.id == id }
            if (itemToRemove != null) {
                currentList.remove(itemToRemove)
                _pronostics.value = currentList
            }

            val result = repository.deletePronostic(id)
            result.onFailure {
                _errorMessage.value = it.message ?: "Erreur lors de la suppression"
                // En cas d'échec, la collection en temps réel (loadUserPronostics) 
                // remettra la liste à jour correctement depuis Firestore.
            }
        }
    }

    fun validateMatch(match: SportMatch) {
        viewModelScope.launch {
            repository.validateMatchPronostics(match)
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
