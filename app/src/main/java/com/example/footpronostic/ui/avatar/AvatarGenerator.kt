package com.example.footpronostic.ui.avatar

import com.example.footpronostic.data.model.AvatarConfig
import java.util.UUID

/**
 * Générateur d'avatar utilisant l'API DiceBear.
 */
object AvatarGenerator {
    fun generateRandomAvatar(): AvatarConfig {
        val styles = listOf("avataaars", "bottts", "personas", "pixel-art")
        return AvatarConfig(
            style = styles.random(),
            seed = UUID.randomUUID().toString()
        )
    }
}
