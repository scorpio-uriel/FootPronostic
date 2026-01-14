package com.example.footpronostic.ui.avatar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import com.example.footpronostic.data.model.AvatarConfig
import com.example.footpronostic.ui.screens.AvatarUtils

/**
 * Vue simplifiée pour l'affichage de l'avatar utilisant DiceBear.
 */
@Composable
fun AvatarView(
    avatarConfig: AvatarConfig,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = AvatarUtils.getAvatarUrl(avatarConfig),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
