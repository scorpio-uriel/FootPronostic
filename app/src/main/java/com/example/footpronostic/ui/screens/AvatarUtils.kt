package com.example.footpronostic.ui.screens

import com.example.footpronostic.R

/**
 * Utilitaires partagés pour mapper les configurations d'avatar vers les ressources drawable.
 */
object AvatarUtils {
    fun getSkinRes(value: String): Int {
        return when(value) {
            "brown" -> R.drawable.avatar_skin_brown
            "dark" -> R.drawable.avatar_skin_dark
            else -> R.drawable.avatar_skin_light
        }
    }

    fun getHairRes(value: String): Int {
        return when(value) {
            "long" -> R.drawable.avatar_hair_long
            else -> R.drawable.avatar_hair_short
        }
    }

    fun getEyesRes(value: String): Int {
        return when(value) {
            "happy" -> R.drawable.avatar_eyes_happy
            else -> R.drawable.avatar_eyes_default
        }
    }

    fun getMouthRes(value: String): Int {
        return when(value) {
            "sad" -> R.drawable.avatar_mouth_sad
            else -> R.drawable.avatar_mouth_smile
        }
    }

    fun getOutfitRes(value: String): Int {
        return when(value) {
            "shirt" -> R.drawable.avatar_outfit_shirt
            else -> R.drawable.avatar_outfit_hoodie
        }
    }
}
