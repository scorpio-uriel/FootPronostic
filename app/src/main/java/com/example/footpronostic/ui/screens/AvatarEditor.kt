package com.example.footpronostic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.footpronostic.data.model.AvatarConfig
import java.util.UUID

/**
 * Éditeur d'avatar simplifié pour l'API DiceBear.
 */
@Composable
fun AvatarEditor(
    avatar: AvatarConfig,
    onAvatarChange: (AvatarConfig) -> Unit
) {
    val styles = listOf("avataaars", "bottts", "personas", "pixel-art", "lorelei", "micah")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Text("Personnalisation de l’avatar", style = MaterialTheme.typography.titleMedium)

        // Bouton pour changer la seed (aléatoire)
        Button(
            onClick = { onAvatarChange(avatar.copy(seed = UUID.randomUUID().toString())) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Casino, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Générer aléatoirement")
        }

        Text("Choisir un style", style = MaterialTheme.typography.labelLarge)

        // Sélecteur de style
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            styles.take(3).forEach { style ->
                FilterChip(
                    selected = avatar.style == style,
                    onClick = { onAvatarChange(avatar.copy(style = style)) },
                    label = { Text(style.replaceFirstChar { it.uppercase() }) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            styles.drop(3).forEach { style ->
                FilterChip(
                    selected = avatar.style == style,
                    onClick = { onAvatarChange(avatar.copy(style = style)) },
                    label = { Text(style.replaceFirstChar { it.uppercase() }) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
