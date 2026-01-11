package com.example.footpronostic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footpronostic.data.model.SportMatch
import com.example.footpronostic.data.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran de la liste des matchs.
 * Il gère l'état de l'UI et interagit avec le MatchRepository.
 */
class MatchViewModel : ViewModel() {

    private val repository = MatchRepository()

    // Un StateFlow privé pour contenir l'état de la liste des matchs.
    private val _matches = MutableStateFlow<List<SportMatch>>(emptyList())
    // Un StateFlow public et en lecture seule pour que l'UI observe les changements.
    val matches: StateFlow<List<SportMatch>> = _matches.asStateFlow()

    // Variable pour simuler le rôle de l'utilisateur (admin ou non).
    // Dans une vraie application, cette valeur proviendrait de votre logique d'authentification.
    val isAdmin: Boolean = true // Mettez à false pour tester la vue non-admin

    init {
        // Au démarrage du ViewModel, on lance une coroutine pour observer les matchs.
        viewModelScope.launch {
            repository.getMatches()
                .catch { exception ->
                    // Gérer les erreurs de récupération (ex: afficher un message)
                    println("Error fetching matches: $exception")
                }
                .collect { matchList ->
                    // Met à jour le StateFlow avec la nouvelle liste de matchs.
                    _matches.value = matchList
                }
        }
    }

    /**
     * Ajoute un nouveau match.
     */
    fun addMatch(match: SportMatch) {
        viewModelScope.launch {
            repository.addMatch(match)
        }
    }

    /**
     * Met à jour un match existant.
     */
    fun updateMatch(match: SportMatch) {
        viewModelScope.launch {
            repository.updateMatch(match)
        }
    }

    /**
     * Supprime un match.
     */
    fun deleteMatch(matchId: String) {
        viewModelScope.launch {
            repository.deleteMatch(matchId)
        }
    }
}
