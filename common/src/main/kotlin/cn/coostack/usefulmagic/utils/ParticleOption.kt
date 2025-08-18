package cn.coostack.usefulmagic.utils

import cn.coostack.usefulmagic.platform.EnvHelper
import cn.coostack.usefulmagic.platform.UsefulMagicServices
import net.minecraft.client.Minecraft


object ParticleOption {
    /**
     * 粒子倍数
     */
    fun getParticleCounts(): Int {
        // 判断环境
        if (UsefulMagicServices.ENV_HELPER.isServerSide()) {
            return 3
        }
        val client = Minecraft.getInstance()
        val particles = client.options?.particles()?.get()// ParticleStatus 枚举
        return 3 - ((particles?.ordinal) ?: 0)
    }
}
