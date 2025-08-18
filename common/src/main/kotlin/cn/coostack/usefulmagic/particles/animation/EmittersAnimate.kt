package cn.coostack.usefulmagic.particles.animation

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import net.minecraft.world.phys.Vec3

class EmittersAnimate(
    val emitterGenerator: (Vec3) -> ParticleEmitters,
    var origin: Vec3,
    var interval: Int,
    duration: Int,
    val preTickAction: (emitter: ParticleEmitters) -> Unit,
) : ParticleAnimate {
    private var animateDuration = duration
    private var valid = true
    private var start = false
    private var tick = 0
    private val spawnedEmitters = HashSet<ParticleEmitters>()
    override fun decreaseDuration() {
        if (!valid()) return
        if (!start) return
        tick++
        if (tick % interval == 0) {
            val emitter = emitterGenerator(origin)
            ParticleEmittersManager.spawnEmitters(emitter)
            preTickAction(emitter)
            spawnedEmitters.add(emitter)
        }
        if (animateDuration == -1) return
        animateDuration--
    }

    override fun valid(): Boolean {
        return (animateDuration == -1 || animateDuration > 0) && valid
    }

    override fun start() {
        if (!valid) return
        start = true
    }

    override fun cancel() {
        valid = false
        start = false
        spawnedEmitters.onEach { emitter ->emitter.cancelled = true }.clear()
    }
}