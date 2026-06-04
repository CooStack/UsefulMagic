package cn.coostack.usefulmagic.particles.emitters.magic

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleCommandQueue
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleDragCommand
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleNoiseCommand
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

/**
 * 弹幕法球的 弹幕emitter
 *
 * @constructor Create empty Barrage ball emitter
 */
@CooAutoRegister
class BarrageTailEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    init {
        enableInterpolator = true
    }

    val command = ParticleCommandQueue()
        .add(
            ParticleNoiseCommand()
                .strength(0.25)
                .frequency(1.3)
                .speed(2.0)
                .affectY(1.0)
                .clampSpeed(15.0)
                .useLifeCurve(true)
        ) { data, particle ->
            (run {
                val age = particle.currentAge;
                val maxAge = particle.lifetime; ((age > 10))
            })
        }
        .add(
            ParticleDragCommand()
                .damping(0.15)
                .linear(0.0)
                .minSpeed(0.01)
        )

    @CodecField
    var template = ControlableParticleData().apply {
        velocity = Vec3(0.0, 0.0, 0.0)
        visibleRange = 256.0f
        color = Vector3f(0.996078f, 0.329412f, 0.164706f)
        alpha = 1.0f
        light = 15
        faceToCamera = true
        speedLimit = 32.0
        sign = 0
    }

    @CodecField
    var right = Vector3f(1f)

    @CodecField
    var randomData = SimpleRandomParticleData().apply {
        minAge = 5
        maxAge = 10
        minCount = 1
        maxCount = 3
        minSize = 0.1
        maxSize = 0.3
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()
        // 发射器 1: Emitter 1
        run {
            res.addAll(
                PointsBuilder()
                    .addWith {
                        val locs = arrayListOf<RelativeLocation>()
                        val count = randomData.getRandomCount()
                        repeat(count) {
                            locs.add(RelativeLocation(0.0, 0.0, 0.0))
                        }
                        locs
                    }
                    .createWithoutClone()
                    .map { rel ->
                        template.clone().apply {
                            maxAge = randomData.getRandomParticleMaxAge()
                            size = randomData.getRandomSize()
                        } to rel
                    }
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
        val maxAge = data.maxAge
        val left = data.color
        controler.addPreTickAction {
            command.applyVelocity(data, this)
            val progress = (currentAge.toDouble() / maxAge)
            color = GraphMathHelper.lerp(progress, left, right)
        }
    }

    override fun doTick() {
        // 朝着dir移动？ x
    }

}