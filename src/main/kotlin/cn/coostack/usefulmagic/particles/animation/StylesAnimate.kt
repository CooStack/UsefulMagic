package cn.coostack.usefulmagic.particles.animation

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

class StylesAnimate(
    val emitterGenerator: (Vec3d) -> Pair<ParticleGroupStyle, Vec3d>,
    var origin: Vec3d,
    val world: ServerWorld,
    var interval: Int,
    duration: Int,
    val spawnAction: (emitter: ParticleGroupStyle) -> Unit,
) : ParticleAnimate {
    private var animateDuration = duration
    private var valid = true
    private var start = false
    private var tick = 0
    override fun decreaseDuration() {
        if (!valid()) return
        if (!start) return
        tick++
        if (tick % interval == 0) {
            val styleToPos = emitterGenerator(origin)
            val style = styleToPos.first
            val pos = styleToPos.second
            ParticleStyleManager.spawnStyle(world, pos, style)
            spawnAction(style)
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
    }
}