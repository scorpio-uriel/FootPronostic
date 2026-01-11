package com.example.footpronostic.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Représente un match sportif.
 * @param id L'identifiant unique du document Firestore.
 * @param teamA Le nom de l'équipe à domicile.
 * @param teamB Le nom de l'équipe à l'extérieur.
 * @param dateTime Le timestamp (en millisecondes) du début du match.
 * @param status L'état du match (ex: "upcoming", "finished").
 * @param scoreA Le score de l'équipe A.
 * @param scoreB Le score de l'équipe B.
 */
data class SportMatch(
    @DocumentId
    var id: String = "",
    val teamA: String = "",
    val teamB: String = "",
    val dateTime: Long = 0L,
    val status: String = "upcoming",
    val scoreA: Int = 0,
    val scoreB: Int = 0
)
