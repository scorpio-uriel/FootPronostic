package com.example.footpronostic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footpronostic.data.model.Pronostic
import com.example.footpronostic.data.model.SportMatch
import com.example.footpronostic.data.model.toPronostic
import com.example.footpronostic.data.repository.PronosticRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel gérant la logique métier des pronostics.
 */
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

    /**
     * Charge les pronostics d'un utilisateur.
     */
    fun loadUserPronostics(userId: String) {
        viewModelScope.launch {
            repository.getUserPronostics(userId).collect { pronosticsList ->
                _pronostics.value = pronosticsList
            }
        }
    }

    /**
     * Vérifie si l'utilisateur peut parier sur un match.
     */
    suspend fun canBetOnMatch(userId: String, matchId: String): Boolean {
        return !repository.hasUserBetOnMatch(userId, matchId)
    }

    /**
     * Crée un nouveau pronostic.
     */
    fun createPronostic(
        match: SportMatch,
        userId: String,
        scoreA: Int,
        scoreB: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val pronostic = match.toPronostic(userId, scoreA, scoreB)
            val result = repository.addPronostic(pronostic)

            result.onSuccess {
                _successMessage.value = "Pronostic enregistré !"
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Erreur lors de l'ajout"
            }

            _isLoading.value = false
        }
    }

    /**
     * Met à jour un pronostic existant.
     */
    fun updatePronostic(
        pronostic: Pronostic,
        newScoreA: Int,
        newScoreB: Int
    ) {
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
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Erreur lors de la modification"
            }

            _isLoading.value = false
        }
    }

    /**
     * Supprime un pronostic.
     */
    fun deletePronostic(pronosticId: String) {
        viewModelScope.launch {
            // Mise à jour optimiste de l'UI : on retire l'élément immédiatement de la liste locale
            val currentList = _pronostics.value.toMutableList()
            val itemToRemove = currentList.find { it.id == pronosticId }
            if (itemToRemove != null) {
                currentList.remove(itemToRemove)
                _pronostics.value = currentList
            }

            val result = repository.deletePronostic(pronosticId)

            result.onSuccess {
                _successMessage.value = "Pronostic supprimé"
            }.onFailure { exception ->
                // En cas d'échec, on peut réinsérer l'élément ou afficher une erreur
                _errorMessage.value = exception.message ?: "Erreur lors de la suppression"
                // Recharger la liste depuis la source de vérité (Firestore) si l'action échoue
                // loadUserPronostics(itemToRemove?.userId ?: "")
            }
        }
    }

    /**
     * Réinitialise les messages.
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
