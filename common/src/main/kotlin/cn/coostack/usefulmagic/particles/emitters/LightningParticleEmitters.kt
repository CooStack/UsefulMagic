package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.extend.asRelative
import cn.coostack.cooparticlesapi.extend.minus
import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import kotlin.math.cos
import kotlin.math.sin

// 碎片
@CooAutoRegister
class LightningParticleEmitters(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    @CodecField
    var templateData = ControlableParticleData()

    // 相对位置
    @CodecField
    var targetPos: Vec3 = Vec3.ZERO

    @CodecField
    var simpleData = SimpleRandomParticleData()

    /**
     * 二分次数
     */
    @CodecField
    var subCount = 7 minRangeTo 9

    override fun doTick() {
    }

    val options
        get() = ParticleOption.getParticleCounts()

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val count = options * simpleData.getRandomCount()
        val maxOffset = targetPos.length() * 1 / 5
        return PointsBuilder()
            .addLightningAttenuationPoints(
                targetPos.asRelative(),
                subCount.random(),
                maxOffset,
                0.4, count
            )
            .createWithoutClone().map {
                templateData.clone().apply {
                    this.size = simpleData.getRandomSize()
                    this.maxAge = simpleData.getRandomParticleMaxAge()
                } to it
            }
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: RelativeLocation,
        spawnWorld: Level,
        particleLerpProgress: Float,
        posLerpProgress: Float
    ) {
        controler.addPreTickAction {
            val p = loc.add(this@LightningParticleEmitters.pos)
            val t = currentAge.toDouble() + 0.1
            val frequency = 0.2
            val amplitude = 0.08
            val fx = sin((p.y + t) * frequency) + cos((p.z - t) * frequency)
            val fy = sin((p.z + t) * frequency) + cos((p.x + t) * frequency)
            val fz = sin((p.x - t) * frequency) + cos((p.y - t) * frequency)
            data.velocity = data.velocity.add(Vec3(fx * 0.5, fy * 0.5, fz * 0.5).scale(amplitude))

            val speed = data.velocity.length()
            if (speed <= 0.01) {
                data.velocity = Vec3.ZERO
                return@addPreTickAction
            }
            data.velocity = data.velocity
                .scale(GraphMathHelper.expDampFactor(0.2, 1.0))
                .scale(0.995)
        }
    }

}
