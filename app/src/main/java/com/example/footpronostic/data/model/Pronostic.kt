package com.example.footpronostic.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Représente un pronostic d'un utilisateur sur un match.
 */
data class Pronostic(
    @DocumentId
    var id: String = "",

    val userId: String = "",              // ID de l'utilisateur
    val matchId: String = "",             // ID du match (de l'API)
    val matchTeamA: String = "",          // Nom équipe A (pour affichage)
    val matchTeamB: String = "",          // Nom équipe B (pour affichage)
    val matchDateTime: Long = 0L,         // Timestamp du match

    // Le pronostic de l'utilisateur
    val predictedScoreA: Int = 0,
    val predictedScoreB: Int = 0,
    val predictedWinner: String = "",     // "teamA", "teamB", ou "draw"

    // Métadonnées
    @ServerTimestamp
    val createdAt: Date? = null,
    val updatedAt: Long = System.currentTimeMillis(),

    // Statut du pronostic
    val isValidated: Boolean = false,     // Devient true après le match
    val points: Int = 0                   // Points gagnés (0 si pas encore validé)
)

/**
 * Crée un pronostic à partir d'un match.
 */
fun SportMatch.toPronostic(
    userId: String,
    predictedScoreA: Int,
    predictedScoreB: Int
): Pronostic {
    val winner = when {
        predictedScoreA > predictedScoreB -> "teamA"
        predictedScoreB > predictedScoreA -> "teamB"
        else -> "draw"
    }

    return Pronostic(
        userId = userId,
        matchId = this.id,
        matchTeamA = this.teamA,
        matchTeamB = this.teamB,
        matchDateTime = this.dateTime,
        predictedScoreA = predictedScoreA,
        predictedScoreB = predictedScoreB,
        predictedWinner = winner
    )
}
