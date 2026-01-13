package com.example.footpronostic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footpronostic.data.model.SportMatch
import com.example.footpronostic.data.model.FDMatch
import com.example.footpronostic.data.model.toSportMatch
import com.example.footpronostic.data.repository.FootballApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel gérant l'affichage des matchs depuis l'API externe.
 */
class MatchViewModel : ViewModel() {
    private val apiRepository = FootballApiRepository()

    private val _matches = MutableStateFlow<List<SportMatch>>(emptyList())
    val matches: StateFlow<List<SportMatch>> = _matches.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadMatchesFromApi()
    }

    fun loadMatchesFromApi() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                println("\n🔵 --- DÉBUT CHARGEMENT MATCHS ---")

                // getTodayMatches() renvoie List<FDMatch>
                val apiMatches = apiRepository.getTodayMatches()

                println("📊 Nombre de matchs bruts reçus: ${apiMatches.size}")

                // Log chaque match avant conversion
                apiMatches.forEachIndexed { index, match ->
                    println("  Match $index: ${match.homeTeam.name} vs ${match.awayTeam.name}")
                    println("    - utcDate: ${match.utcDate}")
                    println("    - status: ${match.status}")
                }

                // On convertit chaque FDMatch en SportMatch
                val convertedMatches = apiMatches.map {
                    val converted = it.toSportMatch()
                    println("  ✅ Converti: ${converted.teamA} vs ${converted.teamB} @ ${converted.dateTime}")
                    converted
                }

                _matches.value = convertedMatches

                println("✅ ${_matches.value.size} matchs chargés avec succès")
                println("🔵 --- FIN CHARGEMENT MATCHS ---\n")

            } catch (e: Exception) {
                _errorMessage.value = "Erreur de connexion: ${e.message}"
                println("❌ Erreur: ${e.message}")
                e.printStackTrace()

            } finally {
                _isLoading.value = false
            }
        }
    }


    fun refreshMatches() {
        loadMatchesFromApi()
    }

    override fun onCleared() {
        super.onCleared()
        apiRepository.closeClient()
    }
}
