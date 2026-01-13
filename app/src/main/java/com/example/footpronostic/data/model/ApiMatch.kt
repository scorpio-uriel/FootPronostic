package com.example.footpronostic.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * Modèles pour l'API football-data.org (v4).
 * Structure complète et correcte pour football-data.org
 */

@Serializable
data class ApiMatchResponse(
    val matches: List<FDMatch> = emptyList()
)

@Serializable
data class FDMatch(
    val id: Int,
    val utcDate: String,          // ex: "2026-01-12T20:00:00Z"
    val status: String,           // "SCHEDULED", "FINISHED", "LIVE", etc.
    val matchday: Int? = null,
    val homeTeam: FDTeam,
    val awayTeam: FDTeam,
    val score: FDScore = FDScore()
)

@Serializable
data class FDTeam(
    val id: Int? = null,
    val name: String? = null,
    val shortName: String? = null,
    val tla: String? = null       // code 3 lettres, ex: "PSG"
)

@Serializable
data class FDScore(
    val fullTime: FDScoreDetail = FDScoreDetail(),
    val halfTime: FDScoreDetail = FDScoreDetail()
)

@Serializable
data class FDScoreDetail(
    val home: Int? = null,
    val away: Int? = null
)

/**
 * Convertit un match football-data.org en SportMatch local.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun FDMatch.toSportMatch(): SportMatch {
    val timestampMillis = parseUtcDateToMillis(utcDate)

    val scoreHome = score.fullTime.home ?: 0
    val scoreAway = score.fullTime.away ?: 0

    val normalizedStatus = when (status) {
        "FINISHED" -> "finished"
        else -> "upcoming"
    }

    return SportMatch(
        id = id.toString(),
        teamA = homeTeam.name ?: homeTeam.tla ?: "Home",
        teamB = awayTeam.name ?: awayTeam.tla ?: "Away",
        dateTime = timestampMillis,
        status = normalizedStatus,
        scoreA = scoreHome,
        scoreB = scoreAway
    )
}

/**
 * Parse une date ISO 8601 UTC (ex: "2026-01-13T15:00:00Z") en millisecondes.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun parseUtcDateToMillis(utcDate: String): Long {
    return try {
        Instant.from(DateTimeFormatter.ISO_INSTANT.parse(utcDate)).toEpochMilli()
    } catch (e: Exception) {
        println("❌ Erreur parsing date '$utcDate': ${e.message}")
        System.currentTimeMillis()
    }
}
