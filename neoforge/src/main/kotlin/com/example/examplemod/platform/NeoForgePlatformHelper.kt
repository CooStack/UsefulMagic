package com.example.examplemod.platform

import com.example.examplemod.platform.services.IPlatformHelper
import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLLoader

class NeoForgePlatformHelper : IPlatformHelper {
    override fun getPlatformName(): String {
        return "NeoForge"
    }

    override fun isModLoaded(modId: String): Boolean {
        return ModList.get().isLoaded(modId)
    }

    override fun isDevelopmentEnvironment(): Boolean {
        return !FMLLoader.isProduction()
    }
}