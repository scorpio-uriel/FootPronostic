package com.example.footpronostic.data.repository

import com.example.footpronostic.data.model.ApiMatchResponse
import com.example.footpronostic.data.model.FDMatch
import com.example.footpronostic.data.model.FDTeam
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Repository pour récupérer les matchs depuis football-data.org.
 */
class FootballApiRepository {

    private val apiKey = "d343d904339f4390bad6a45f7ed9c01c"
    private val baseUrl = "https://api.football-data.org/v4"
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                }
            )
        }
    }

    /**
     * Récupère des matchs (fixtures) pour la Premier League (PL) OU Ligue 1 (FL1)
     * en utilisant l'endpoint /matches de football-data.org
     * On limite à 5 matchs.
     */
    suspend fun getTodayMatches(): List<FDMatch> {
        return try {
            val competitionCode = "PL"
            val dateFrom = getTodayDate()
            val url = "\$baseUrl/competitions/\$competitionCode/matches?dateFrom=\$dateFrom"

            println("Url: $url")
            val response: ApiMatchResponse = client.get(url) {
                headers {
                    append("X-Auth-Token", apiKey)
                }
            }.body()

            // Si pas de matchs (plan gratuit), utiliser les matchs factices
            if (response.matches.isEmpty()) {
                println("⚠️ Pas de matchs via l'API, utilisation des données locales")
                return fakeMatches().take(5)
            }

            response.matches.take(5)

        } catch (e: Exception) {
            println("❌ Erreur API, données de test utilisées")
            return fakeMatches().take(5)
        }
    }

    private fun fakeMatches(): List<FDMatch> {
        val nowSeconds = System.currentTimeMillis() / 1000
        return listOf(
            FDMatch(
                id = 1, utcDate = "2026-01-13T20:00:00Z", status = "SCHEDULED",
                homeTeam = FDTeam(name = "Manchester United"),
                awayTeam = FDTeam(name = "Brighton")
            ),
            FDMatch(
                id = 2, utcDate = "2026-01-13T21:00:00Z", status = "SCHEDULED",
                homeTeam = FDTeam(name = "Arsenal"),
                awayTeam = FDTeam(name = "Chelsea")
            ),
            FDMatch(
                id = 3, utcDate = "2026-01-13T22:00:00Z", status = "SCHEDULED",
                homeTeam = FDTeam(name = "Liverpool"),
                awayTeam = FDTeam(name = "Manchester City")
            )
        )
    }


    fun closeClient() {
        client.close()
    }

    private fun getTodayDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = String.format("%02d", calendar.get(java.util.Calendar.MONTH) + 1)
        val day = String.format("%02d", calendar.get(java.util.Calendar.DAY_OF_MONTH))
        return "$year-$month-$day"
    }

}
