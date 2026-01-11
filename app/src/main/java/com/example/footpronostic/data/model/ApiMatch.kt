package com.example.footpronostic.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modèle pour les matchs provenant de l'API externe (API-Football).
 */
@Serializable
data class ApiMatchResponse(
    val response: List<ApiFixture>
)

@Serializable
data class ApiFixture(
    val fixture: FixtureDetails,
    val teams: TeamsInfo,
    val goals: GoalsInfo? = null
)

@Serializable
data class FixtureDetails(
    val id: Int,
    val date: String,  // Format ISO: "2026-01-11T20:00:00+00:00"
    val timestamp: Long,
    val status: FixtureStatus
)

@Serializable
data class FixtureStatus(
    @SerialName("short") val short: String,  // "NS" = Not Started, "FT" = Finished
    @SerialName("long") val long: String
)

@Serializable
data class TeamsInfo(
    val home: Team,
    val away: Team
)

@Serializable
data class Team(
    val id: Int,
    val name: String,
    val logo: String
)

@Serializable
data class GoalsInfo(
    val home: Int? = null,
    val away: Int? = null
)

/**
 * Convertit un match API en SportMatch local.
 */
fun ApiFixture.toSportMatch(): SportMatch {
    return SportMatch(
        id = fixture.id.toString(),
        teamA = teams.home.name,
        teamB = teams.away.name,
        dateTime = fixture.timestamp * 1000,  // Converti en millisecondes
        status = if (fixture.status.short == "FT") "finished" else "upcoming",
        scoreA = goals?.home ?: 0,
        scoreB = goals?.away ?: 0
    )
}
