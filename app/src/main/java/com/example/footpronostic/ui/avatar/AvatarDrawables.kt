package com.example.footpronostic.ui.avatar

import com.example.footpronostic.R

fun skinDrawable(value: String) = when (value) {
    "brown" -> R.drawable.avatar_skin_brown
    "dark" -> R.drawable.avatar_skin_dark
    else -> R.drawable.avatar_skin_light
}

fun hairDrawable(value: String) = when (value) {
    "long" -> R.drawable.avatar_hair_long
    else -> R.drawable.avatar_hair_short
}

fun eyesDrawable(value: String) = when (value) {
    "happy" -> R.drawable.avatar_eyes_happy
    else -> R.drawable.avatar_eyes_default
}

fun mouthDrawable(value: String) = when (value) {
    "sad" -> R.drawable.avatar_mouth_sad
    else -> R.drawable.avatar_mouth_smile
}

fun outfitDrawable(value: String) = when (value) {
    "shirt" -> R.drawable.avatar_outfit_shirt
    else -> R.drawable.avatar_outfit_hoodie
}
