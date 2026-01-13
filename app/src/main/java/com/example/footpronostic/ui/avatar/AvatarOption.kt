package com.example.footpronostic.ui.avatar

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun AvatarOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Text(label)
    }
}
