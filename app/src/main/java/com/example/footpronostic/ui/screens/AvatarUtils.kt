package com.example.footpronostic.ui.screens

import com.example.footpronostic.data.model.AvatarConfig
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Utilitaires partagés pour l'avatar utilisant l'API DiceBear.
 */
object AvatarUtils {
    /**
     * Génère l'URL de l'avatar à partir de la configuration.
     * Utilise URLEncoder pour sécuriser les caractères spéciaux dans la seed.
     */
    fun getAvatarUrl(config: AvatarConfig): String {
        val encodedSeed = URLEncoder.encode(config.seed, StandardCharsets.UTF_8.toString())
        return "https://api.dicebear.com/7.x/${config.style}/png?seed=$encodedSeed"
    }
}
