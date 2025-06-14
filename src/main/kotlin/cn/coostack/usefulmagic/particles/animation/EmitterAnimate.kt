package cn.coostack.usefulmagic.particles.animation

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager

class EmitterAnimate(val emitters: ParticleEmitters, duration: Int) : ParticleAnimate {
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
        ParticleEmittersManager.spawnEmitters(emitters)
    }

    override fun cancel() {
        emitters.cancelled = true
        valid = false
    }
}