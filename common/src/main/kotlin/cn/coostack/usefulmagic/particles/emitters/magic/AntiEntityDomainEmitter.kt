package cn.coostack.usefulmagic.particles.emitters.magic

import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.particles.ParticleCameraOption
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.random.Random

@CooAutoRegister
class AntiEntityDomainEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    companion object {
        val source = PointsBuilder()
            .addRoundShape(48.0, 0.1, 50, 240)
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
            val colorLifeProgress =
                if (this.lifetime <= 0) 1f else (this.currentAge.toFloat() / this.lifetime.toFloat()).coerceIn(0f, 1f)
            when (data.sign) {
                0 -> {
                    this.color = Vector3f(
                        0.87451f + (0.580392f - 0.87451f) * colorLifeProgress,
                        0.721569f + (0.219608f - 0.721569f) * colorLifeProgress,
                        1.0f + (0.941176f - 1.0f) * colorLifeProgress
                    )
                }
            }
        }
    }

    init {
        delay = 1
        maxTick = -1
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()

        // 发射器 #1: Emitter 1
        if (tick >= 0) {
            run {
                val data1 = SimpleRandomParticleData().apply {
                    minAge = 10
                    maxAge = 20
                    minCount = 10
                    maxCount = 20
                    minSize = 0.2
                    maxSize = 0.5
                    minSpeed = 4.0
                    maxSpeed = 5.0
                }

                val template1 = ControlableParticleData().apply {
                    velocity = Vec3(0.0, -0.15, 0.0)
                    visibleRange = 512f
                    color = Vector3f(0.87451f, 0.721569f, 1.0f)
                    alpha = 1.0f
                    light = 15
                    faceToCamera = true
                    speedLimit = 32.0
                    sign = 0
                }

                template1.apply {
                    effect = ControlableEndRodEffect(uuid)
                    cameraOption = ParticleCameraOption.AXIS_BILLBOARD
                    axis = Vec3(0.0, 1.0, 0.0)
                    uniformSize = false
                    weightSize = data1.getRandomSize() / 2
                    heightSize = 0.9f + weightSize
                    setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
                }

                res.addAll(
                    PointsBuilder()
                        .addWith {
                            val locs = arrayListOf<RelativeLocation>()
                            val source = source.createWithoutClone()
                            val count = (data1.getRandomCount()).coerceAtLeast(1)
                            if (source.isEmpty()) {
                                repeat(count) {
                                    locs.add(RelativeLocation(0.0, 0.0, 0.0))
                                }
                            } else {
                                val rand = Random.Default
                                repeat(count) {
                                    val base = source[rand.nextInt(source.size)]
                                    locs.add(RelativeLocation(base.x + 0.0, base.y + 0.0, base.z + 0.0))
                                }
                            }
                            locs
                        }
                        .createWithoutClone()
                        .map { rel ->
                            val speed = data1.getRandomSpeed()
                            template1.clone().apply {
                                maxAge = data1.getRandomParticleMaxAge()
                                val baseDir = Vec3(0.0, -0.15, 0.0)
                                velocity =
                                    if (baseDir.lengthSqr() < 1e-8) Vec3.ZERO else baseDir.normalize().scale(speed)
                            } to rel
                        }
                )

            }

        }

        return res
    }

    override fun doTick() {
        // modify emitter variables here
    }
}
