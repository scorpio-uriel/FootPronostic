package com.example.footpronostic.data.repository

import com.example.footpronostic.data.model.ApiFixture
import com.example.footpronostic.data.model.ApiMatchResponse
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
 * Repository pour récupérer les matchs depuis API-Football.
 */
class FootballApiRepository {

    private val apiKey = "d343d904339f4390bada645f7ed9c01c"
    private val baseUrl = "https://v3.football.api-sports.io"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    /**
     * Récupère les matchs du jour entre 16h et minuit (5 matchs max).
     */
    suspend fun getTodayMatches(): List<ApiFixture> {
        return try {
            val today = getCurrentDate()

            val response: ApiMatchResponse = client.get("$baseUrl/fixtures") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                    append("x-rapidapi-key", apiKey)
                    append("x-rapidapi-host", "v3.football.api-sports.io")
                }
                url {
                    parameters.append("date", today)
                    parameters.append("timezone", "Europe/Paris")
                    parameters.append("season", "2026")
                }
            }.body()

            // Filtrer les matchs entre 16h et 23h59
            response.response
                .filter { isMatchInTimeRange(it.fixture.timestamp) }
                .take(5)

        } catch (e: Exception) {
            println("❌ Erreur API Football: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Vérifie si un match est entre 16h et minuit.
     */
    private fun isMatchInTimeRange(timestamp: Long): Boolean {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        return hour in 16..23
    }

    /**
     * Obtient la date actuelle au format YYYY-MM-DD.
     */
    private fun getCurrentDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return "%04d-%02d-%02d".format(year, month, day)
    }

    fun closeClient() {
        client.close()
    }
}
