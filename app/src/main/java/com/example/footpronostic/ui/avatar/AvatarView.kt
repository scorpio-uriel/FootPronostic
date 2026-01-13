package com.example.footpronostic.ui.avatar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.footpronostic.data.model.AvatarConfig

@Composable
fun AvatarView(
    avatar: AvatarConfig,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(painterResource(skinDrawable(avatar.skin)), null)
        Image(painterResource(outfitDrawable(avatar.outfit)), null)
        Image(painterResource(hairDrawable(avatar.hair)), null)
        Image(painterResource(eyesDrawable(avatar.eyes)), null)
        Image(painterResource(mouthDrawable(avatar.mouth)), null)
    }
}
