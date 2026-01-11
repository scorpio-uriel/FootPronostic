package com.example.footpronostic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footpronostic.data.model.SportMatch
import com.example.footpronostic.data.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel gérant la logique métier des matchs et les droits d'accès.
 */
class MatchViewModel(private val repository: MatchRepository = MatchRepository()) : ViewModel() {

    // Liste des matchs observée en temps réel
    private val _matches = MutableStateFlow<List<SportMatch>>(emptyList())
    val matches: StateFlow<List<SportMatch>> = _matches.asStateFlow()

    // État d'administration (déterminé par AuthViewModel)
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    init {
        loadMatches()
    }

    private fun loadMatches() {
        viewModelScope.launch {
            repository.getMatches().collect {
                _matches.value = it
            }
        }
    }

    /**
     * Met à jour le statut admin du ViewModel.
     */
    fun setAdminStatus(admin: Boolean) {
        _isAdmin.value = admin
    }

    fun addMatch(match: SportMatch) {
        viewModelScope.launch { repository.addMatch(match) }
    }

    fun deleteMatch(matchId: String) {
        viewModelScope.launch { repository.deleteMatch(matchId) }
    }

    /**
     * Met à jour un match complet dans Firestore.
     */
    fun updateMatch(match: SportMatch) {
        viewModelScope.launch { repository.updateMatch(match) }
    }

    fun updateScore(match: SportMatch, scoreA: Int, scoreB: Int) {
        val updatedMatch = match.copy(scoreA = scoreA, scoreB = scoreB, status = "finished")
        viewModelScope.launch { repository.updateMatch(updatedMatch) }
    }
}
