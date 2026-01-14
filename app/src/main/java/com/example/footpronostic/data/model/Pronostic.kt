package com.example.footpronostic.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Modèle de Pronostic complet pour le système de points avec cotes.
 */
data class Pronostic(
    @DocumentId
    var id: String = "",
    val userId: String = "",
    val matchId: String = "",
    val matchTeamA: String = "",
    val matchTeamB: String = "",
    val matchDateTime: Long = 0L,

    // Pronostic de l'utilisateur
    val predictedScoreA: Int = 0,
    val predictedScoreB: Int = 0,
    val predictedWinner: String = "",

    // Cotes au moment du pari
    val oddsAtBet: Double = 1.0,

    // Statut et points
    val isValidated: Boolean = false,
    val pointsGained: Int = 0,

    @ServerTimestamp
    val createdAt: Date? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Extension pour convertir un SportMatch en Pronostic initial.
 */
fun SportMatch.toPronostic(
    userId: String,
    scoreA: Int,
    scoreB: Int
): Pronostic {
    val winner = when {
        scoreA > scoreB -> "teamA"
        scoreB > scoreA -> "teamB"
        else -> "draw"
    }

    val odds = when (winner) {
        "teamA" -> this.oddsA
        "teamB" -> this.oddsB
        else -> this.oddsDraw
    }

    return Pronostic(
        userId = userId,
        matchId = this.id,
        matchTeamA = this.teamA,
        matchTeamB = this.teamB,
        matchDateTime = this.dateTime,
        predictedScoreA = scoreA,
        predictedScoreB = scoreB,
        predictedWinner = winner,
        oddsAtBet = odds
    )
}
