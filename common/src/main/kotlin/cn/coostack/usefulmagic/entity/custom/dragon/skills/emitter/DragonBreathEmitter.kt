package cn.coostack.usefulmagic.entity.custom.dragon.skills.emitter

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleCommandQueue
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleDragCommand
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleNoiseCommand
import cn.coostack.cooparticlesapi.particles.ControlableParticle
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.PhysicsUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

@CooAutoRegister
class DragonBreathEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    private fun buildCommandQueue(): ParticleCommandQueue {
        return ParticleCommandQueue()
            .add(
                ParticleNoiseCommand()
                    .strength(0.04)
                    .frequency(0.3)
                    .speed(0.2)
                    .affectY(1.0)
                    .clampSpeed(16.0)
                    .useLifeCurve(true)
            )
            .add(
                ParticleDragCommand()
                    .damping(0.06)
                    .minSpeed(0.0)
                    .linear(0.0)
            )
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: RelativeLocation,
        spawnWorld: Level,
        particleLerpProgress: Float,
        posLerpProgress: Float
    ) {
        var command: ParticleCommandQueue? = null
        controler.addPreTickAction {
            val colorLifeProgress =
                if (this.lifetime <= 0) 1f else (this.currentAge.toFloat() / this.lifetime.toFloat()).coerceIn(0f, 1f)
            when (data.sign) {
                0 -> {
                    this.color = Vector3f(
                        0.87451f + (0.482353f - 0.87451f) * colorLifeProgress,
                        0.419608f + (0.141176f - 0.419608f) * colorLifeProgress,
                        1.0f + (1.0f - 1.0f) * colorLifeProgress
                    )
                }
            }
            val queue = command ?: buildCommandQueue().also { command = it }
            queue.applyVelocity(data, this)
            this.particleAlpha = GraphMathHelper.lerp(colorLifeProgress * 3, 0f, 1f)
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
                    minCount = 100
                    maxCount = 300
                    minSize = 0.2
                    maxSize = 0.3
                    minSpeed = 2.0
                    maxSpeed = 3.0
                }

                val template1 = ControlableParticleData().apply {
                    velocity = Vec3(0.0, 0.0, 1.0)
                    visibleRange = 512.0f
                    color = Vector3f(0.87451f, 0.419608f, 1.0f)
                    alpha = 1.0f
                    light = 15
                    faceToCamera = true
                    speedLimit = 32.0
                    setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
                    this.alpha = 0f
                    sign = 0
                }

                template1.apply {
                    effect = ControlableEndRodEffect(uuid)
                }

                val facing = if (direction.lengthSqr() < 1e-8) {
                    Vec3(0.0, 0.0, 1.0)
                } else {
                    direction.normalize()
                }

                res.addAll(
                    PointsBuilder()
                        .addCircle(RelativeLocation(0.0, 0.18, 0.0), 0.14, data1.getRandomCount())
                        .rotateTo(direction)
                        .createWithoutClone()
                        .map { rel ->
                            val speed = data1.getRandomSpeed()
                            template1.clone().apply {
                                maxAge = data1.getRandomParticleMaxAge()
                                size = data1.getRandomSize()
                                val baseDir = facing.scale(1.25).add(rel.toVector().scale(0.55))
                                velocity =
                                    if (baseDir.lengthSqr() < 1e-8) Vec3.ZERO else baseDir.normalize().scale(speed)
                            } to RelativeLocation()
                        }
                )

            }

        }
        return res
    }

    @CodecField
    var direction: Vec3 = Vec3(0.0, 1.0, 0.0)
    override fun moveSingleParticleWithVelocity(
        particle: ControlableParticle,
        data: ControlableParticleData,
        to: Vec3,
        collide: BlockHitResult
    ) {
        if (collide.type != HitResult.Type.MISS) {
            data.velocity = PhysicsUtil.collideMovement(collide, data.velocity)
        }
        particle.moveToWithPhysics(to, collide)
    }

    override fun doTick() {
    }
}
