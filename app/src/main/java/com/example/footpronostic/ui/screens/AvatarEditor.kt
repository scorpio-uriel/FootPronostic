package com.example.footpronostic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.example.footpronostic.data.model.AvatarConfig

@Composable
fun AvatarEditor(
    avatar: AvatarConfig,
    onAvatarChange: (AvatarConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Text("Personnalisation de l’avatar", style = MaterialTheme.typography.titleMedium)

        Selector("Cheveux", listOf(
            "shortHairShortFlat",
            "shortHairSides",
            "longHairStraight"
        )) { onAvatarChange(avatar.copy(hair = it)) }

        Selector("Yeux", listOf(
            "default",
            "happy",
            "squint"
        )) { onAvatarChange(avatar.copy(eyes = it)) }

        Selector("Bouche", listOf(
            "smile",
            "default",
            "sad"
        )) { onAvatarChange(avatar.copy(mouth = it)) }

        Selector("Peau", listOf(
            "light",
            "brown",
            "darkBrown"
        )) { onAvatarChange(avatar.copy(skin = it)) }

        Selector("Habits", listOf(
            "hoodie",
            "shirtCrewNeck",
            "blazerShirt"
        )) { onAvatarChange(avatar.copy(outfit = it)) }

    }
}

@Composable
fun Selector(
    label: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Text(label)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach {
            AssistChip(
                onClick = { onSelect(it) },
                label = { Text(it) }
            )
        }
    }
}
