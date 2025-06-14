package cn.coostack.usefulmagic.particles.animation

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import net.minecraft.server.world.ServerWorld
import java.util.UUID

class ParticleAnimation {
    val uuid = UUID.randomUUID()
    val animations = ArrayList<ParticleAnimate>()
    var currentIndex = 0
    val spawnedAnimations = ArrayList<ParticleAnimate>()
    val cancelConditions = ArrayList<() -> Boolean>()

    /**
     * 如果 输入的animate是 ParticleGroupStyle  则需要预先设置world 和pos属性
     */
    fun addAnimate(animate: ParticleAnimate): ParticleAnimation {
        animations.add(animate)
        return this
    }

    fun addCancelCondition(condition: () -> Boolean): ParticleAnimation {
        cancelConditions.add(condition)
        return this
    }

    fun spawnSingle() {
        if (currentIndex >= animations.size) {
            return
        }
        val animate = animations[currentIndex++]
        spawnedAnimations.add(animate)
        animate.start()
    }

    fun cancel() {
        currentIndex = 0
        spawnedAnimations.onEach {
            it.cancel()
        }.clear()
    }

    /**
     * 执行一次生命周期活动
     */
    fun doTick() {
        val iterator = spawnedAnimations.iterator()
        while (iterator.hasNext()) {
            val animate = iterator.next()
            // 判断有效性
            animate.decreaseDuration()
            if (!animate.valid()) {
                animate.cancel()
                iterator.remove()
            }
        }
        if (cancelConditions.any { it() }) {
            cancel()
        }
    }
}