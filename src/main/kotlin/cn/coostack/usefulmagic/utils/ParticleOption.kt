package cn.coostack.usefulmagic.utils

import net.minecraft.client.MinecraftClient

object ParticleOption {
    /**
     * 粒子倍数
     */
    fun getParticleCounts(): Int =
        3 - (MinecraftClient.getInstance()?.options?.particles?.value?.id ?: 0)


}