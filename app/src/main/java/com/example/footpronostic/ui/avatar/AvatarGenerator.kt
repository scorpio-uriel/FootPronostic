package com.example.footpronostic.data.avatar

import com.example.footpronostic.data.model.AvatarConfig

object AvatarGenerator {

    fun random(): AvatarConfig {
        return AvatarConfig(
            skin = listOf("light", "brown", "dark").random(),
            hair = listOf("short", "long").random(),
            eyes = listOf("default", "happy").random(),
            mouth = listOf("smile", "sad").random(),
            outfit = listOf("hoodie", "shirt").random()
        )
    }
}
