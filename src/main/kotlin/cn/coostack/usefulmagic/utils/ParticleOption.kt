package cn.coostack.usefulmagic.utils

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient


object ParticleOption {
    /**
     * 粒子倍数
     */
    fun getParticleCounts(): Int {
        // 判断环境
        if (FabricLoader.getInstance().environmentType == EnvType.SERVER) {
            return 3
        }
        return 3 - (MinecraftClient.getInstance()?.options?.particles?.value?.id ?: 0)
    }
}
