package com.example.footpronostic.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Modèle de match enrichi avec les cotes pour le système de points.
 */
data class SportMatch(
    @DocumentId
    var id: String = "",
    val teamA: String = "",
    val teamB: String = "",
    val dateTime: Long = 0L,
    val status: String = "upcoming", // "upcoming" ou "finished"
    val scoreA: Int = 0,
    val scoreB: Int = 0,
    
    // Cotes simulées ou réelles pour le multiplicateur de points
    val oddsA: Double = 1.0,
    val oddsB: Double = 1.0,
    val oddsDraw: Double = 1.0
)
