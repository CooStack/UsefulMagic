package cn.coostack.usefulmagic.particles.animation

import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

class StyleAnimate(val style: ParticleGroupStyle, val world: ServerLevel, val pos: Vec3, duration: Int) :
    ParticleAnimate {
    private var animateDuration = duration
    private var valid = true
    override fun decreaseDuration() {
        if (!valid()) return
        if (animateDuration == -1) return
        animateDuration--
    }

    override fun valid(): Boolean {
        return (animateDuration == -1 || animateDuration > 0) && valid
    }

    override fun start() {
        if (!valid) return
        ParticleStyleManager.spawnStyle(
            world, pos, style
        )
    }

    override fun cancel() {
        style.remove()
        valid = false
    }
}