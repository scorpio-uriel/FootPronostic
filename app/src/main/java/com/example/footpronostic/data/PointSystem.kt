package com.example.footpronostic.data

import kotlin.math.roundToInt

/**
 * Système de points pour les pronostics.
 */
object PointSystem {
    /**
     * Calcule les points gagnés pour un pronostic selon les règles :
     * - Score exact : +3 pts
     * - Bon vainqueur (mais pas score exact) : +1 pt
     * - Mauvais pronostic : -1 pt
     * Le résultat est multiplié par la cote du pari.
     */
    fun calculatePoints(
        realA: Int, realB: Int, 
        predA: Int, predB: Int,
        odds: Double
    ): Int {
        val basePoints = when {
            // 1. Score exact
            realA == predA && realB == predB -> 3
            
            // 2. Vérification du vainqueur
            else -> {
                val realWinner = when {
                    realA > realB -> "teamA"
                    realB > realA -> "teamB"
                    else -> "draw"
                }
                val predWinner = when {
                    predA > predB -> "teamA"
                    predB > predA -> "teamB"
                    else -> "draw"
                }
                
                if (realWinner == predWinner) 1 else -1
            }
        }

        // On multiplie les points de base par la cote et on arrondit à l'entier le plus proche
        return (basePoints * odds).roundToInt()
    }
}
