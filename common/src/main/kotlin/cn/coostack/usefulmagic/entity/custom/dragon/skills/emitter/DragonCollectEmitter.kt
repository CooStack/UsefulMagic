package cn.coostack.usefulmagic.entity.custom.dragon.skills.emitter

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.PhysicsUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.extend.lerpAsProgress
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.abs
import kotlin.random.Random

@CooAutoRegister
class DragonCollectEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    @CodecField
    var collectTarget = Vec3.ZERO

    @CodecField
    var collectNoiseOffset = 0.45

    @CodecField
    var strength = 0.2

    @CodecField
    var colorLeft = Vector3f()

    @CodecField
    var colorRight = Vector3f()

    @CodecField
    var template = ControlableParticleData()

    @CodecField
    var randomData = SimpleRandomParticleData()


    @CodecField
    var particleDefaultVelocity = Vec3.ZERO

    override fun doTick() {
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val count = randomData.getRandomCount()
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()

        repeat(count) {
            res.add(
                template.clone().apply {
                    maxAge = randomData.getRandomParticleMaxAge()
                    size = randomData.getRandomSize()
                    color = colorLeft
                    velocity = particleDefaultVelocity * randomData.getRandomSpeed()
                    alpha = 0f
                } to RelativeLocation().offsetRandomly(
                    Random.nextDouble(collectNoiseOffset)
                )
            )
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
        controler.addPreTickAction {
            // 吸引， 但是不透明度存在生命周期变化
            val progress = currentAge.toDouble() / data.maxAge
            // 线性变换 映射到 0 - 1 - 0
            val liner = 1.0 - abs(progress.coerceIn(0.0, 1.0) / 2 - 0.5) * 2
            particleAlpha = liner.lerpAsProgress(0, 1).toFloat()
            color = GraphMathHelper.lerp(progress, colorLeft, colorRight)

            // 这里做吸引力
            val nextVelocity = PhysicsUtil.nextAttractVelocityNullable(
                this.loc,
                data.velocity,
                collectTarget,
                strength = strength,
                arriveRadius = 1.5,
                falloffPow = 1
            ) ?: let {
                this.remove() // 移除粒子 （寿命已到）
                return@addPreTickAction
            }
            data.velocity = nextVelocity

        }
    }
}