package cn.coostack.usefulmagic.particles.emitters.explosion

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.force.CParticleForce
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableCParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.random.Random

@CooAutoRegister
class ExplosionWaveEmitters(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    @CodecField
    var templateData = ControlableCParticleData()

    // 圆生成时的半径大小
    @CodecField
    var waveSize = 1.0

    // 冲击波的扩散速度
    @CodecField
    var waveSpeed = 0.2

    @CodecField
    var speedDrag = 0.95

    // 生成冲击波圆环时的粒子个数范围
    @CodecField
    var waveCircleCountMin = 60

    @CodecField
    var waveCircleCountMax = 120

    @CodecField
    var discrete = 0.1

    @CodecField
    var randomVector = false

    @CodecField
    var randomSpeed = 0.1

    override fun doTick() {
    }

    val random = Random(System.currentTimeMillis())
    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        return PointsBuilder()
            .addDiscreteCircleXZ(1.0, random.nextInt(waveCircleCountMin, waveCircleCountMax), discrete)
            .create().map {
                it.multiply(waveSize)
                templateData.clone()
                    .apply {
                        velocity = it.clone().multiply(waveSpeed).toVector()
                        maxAge += random.nextInt(-maxAge / 4, (maxAge / 3).coerceAtLeast(1))
                    } to it
            }
    }

    override fun cparticleForces(): List<CParticleForce> {
        val res = arrayListOf<CParticleForce>()

        res += CParticleForce.ExpDrag(1 - speedDrag, 0.0, 0.0)
        if (randomVector) {
            res += CParticleForce.Noise(randomSpeed, clampSpeed = waveSpeed * 1.5)
        }
        return res
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: RelativeLocation,
        spawnWorld: Level,
        particleLerpProgress: Float,
        posLerpProgress: Float
    ) {
    }
}
