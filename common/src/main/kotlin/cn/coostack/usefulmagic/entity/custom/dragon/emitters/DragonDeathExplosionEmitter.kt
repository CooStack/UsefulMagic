package cn.coostack.usefulmagic.entity.custom.dragon.emitters

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
import cn.coostack.cooparticlesapi.cparticle.CParticleUpdateMode
import cn.coostack.cooparticlesapi.cparticle.force.CParticleForce
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableCParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.command.curve.BezierFloatKeyframe
import cn.coostack.cooparticlesapi.network.particle.emitters.command.curve.BezierKeyframeFloatCurve
import cn.coostack.cooparticlesapi.particles.ParticleCameraOption
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.ControlableFlashEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableSmallGustEffect
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.*
import kotlin.random.Random

@CooAutoRegister
class DragonDeathExplosionEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    override fun cparticleForces(): List<CParticleForce> = listOf(
        CParticleForce.ExpDrag(damping = 0.03, minSpeed = 0.02, linear = 0.0)
    )

    @CodecField
    var simpleData = SimpleRandomParticleData().apply {
        minAge = 20
        maxAge = 40
        minCount = 100
        maxCount = 200
        minSize = 0.5
        maxSize = 1.0
        minSpeed = 0.8
        maxSpeed = 4.6
        leftColor = Vector3f(0.792157f, 0.247059f, 0.992157f)
        rightColor = Vector3f(0.423529f, 0.278431f, 1.0f)
    }

    private val data2 = SimpleRandomParticleData()

    private val emitter1SizeX = BezierKeyframeFloatCurve(listOf(BezierFloatKeyframe(time = 0.0, value = 3.9, outX = 55.409, outY = -3.395, inX = -33.0, inY = 0.0), BezierFloatKeyframe(time = 1.0, value = 0.0, outX = 33.0, outY = 0.0, inX = -45.383, inY = 0.231)))

    private val emitter2SizeY = BezierKeyframeFloatCurve(listOf(BezierFloatKeyframe(time = 0.0, value = 0.0, outX = 43.213, outY = 4.308, inX = -33.0, inY = 0.0), BezierFloatKeyframe(time = 1.0, value = 0.0, outX = 33.0, outY = 0.0, inX = -62.327, inY = 5.47)))

    @CodecField
    var particleData = ControlableCParticleData().apply {
        velocity = Vec3(0.0, 0.0, 0.0)
        uniformSize = false
        weightSize = 1.0f
        heightSize = 1.0f
        visibleRange = 128.0f
        color = Vector3f(0.792157f, 0.247059f, 0.992157f)
        alpha = (100.0 / 100.0).toFloat()
        light = 15
        setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT_NOT_HDR)
        cameraOption = ParticleCameraOption.BILLBOARD
        roll = (0.0 * PI / 180.0).toFloat()
        speedLimit = 32.0
        sign = 0
        effect = ControlableSmallGustEffect(uuid)
        updateMode = CParticleUpdateMode.STATIC
        blockCollision = false
        scaleCurve = CParticleCurve.fromFloatCurve(emitter1SizeX)
    }


    override fun doTick() {
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()

        // 发射器 #1: 旋风
        if (tick >= 0) {
            res.addAll(
                PointsBuilder()
                    .addWith {
                        val rand = Random.Default
                        val locs = arrayListOf<RelativeLocation>()
                        repeat(simpleData.getRandomCount()) {
                            val u = rand.nextDouble()
                            val v = rand.nextDouble()
                            val theta = 2.0 * PI * u
                            val phi = acos(2.0 * v - 1.0)
                            val dx = sin(phi) * cos(theta)
                            val dy = cos(phi)
                            val dz = sin(phi) * sin(theta)
                            val rr = 0.1 * cbrt(rand.nextDouble())
                            locs.add(RelativeLocation(dx * rr + 0.0, dy * rr + 0.0, dz * rr + 0.0))
                        }
                        locs
                    }
                    .createWithoutClone()
                    .map { rel ->
                        val speed = simpleData.getRandomSpeed()
                        val particleSize = simpleData.getRandomSize()
                        val spawnRelative = rel.toVector().subtract(RelativeLocation(0.0, 0.0, 0.0).toVector())
                        val velocityJitter = Vec3((Random.nextDouble() * 2.0 - 1.0) * 0.04, (Random.nextDouble() * 2.0 - 1.0) * 0.04, (Random.nextDouble() * 2.0 - 1.0) * 0.04)
                        val dir = spawnRelative.add(velocityJitter)
                        val velocity = if (dir.lengthSqr() < 1e-8) Vec3.ZERO else dir.normalize().scale(speed)
                        particleData.clone().apply {
                            maxAge = simpleData.getRandomParticleMaxAge()
                            this.color = Vector3f(0.792157f, 0.247059f, 0.992157f)
                            uniformSize = false
                            weightSize = particleSize * particleData.weightSize
                            heightSize = particleSize * particleData.heightSize
                            this.velocity = velocity
                        } to rel
                    }
            )
        }

        // 发射器 #2: 线条
        if (tick >= 0) {
            data2.apply {
                minAge = 20
                maxAge = 60
                minCount = 90
                maxCount = 150
                minSize = 0.1
                maxSize = 1.0
                minSpeed = 1.5
                maxSpeed = 4.5
                leftColor = Vector3f(0.823529f, 0.443137f, 0.996078f)
                rightColor = Vector3f(0.345098f, 0.454902f, 0.992157f)
            }
            val template2 = ControlableCParticleData().apply {
                velocity = Vec3(0.0, 0.0, 0.0)
                uniformSize = false
                weightSize = 0.2f
                heightSize = 1.0f
                visibleRange = 256.0f
                color = Vector3f(0.823529f, 0.443137f, 0.996078f)
                alpha = (100.0 / 100.0).toFloat()
                light = 15
                setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
                cameraOption = ParticleCameraOption.ROTATION
                roll = (0.0 * PI / 180.0).toFloat()
                yaw = (0.0 * PI / 180.0).toFloat()
                pitch = (0.0 * PI / 180.0).toFloat()
                speedLimit = 32.0
                sign = 1
                effect = ControlableFlashEffect(uuid)
                updateMode = CParticleUpdateMode.STATIC
                blockCollision = false
                scaleYCurve = CParticleCurve.fromFloatCurve(emitter2SizeY)
            }
            res.addAll(
                PointsBuilder()
                    .addWith {
                        val rand = Random.Default
                        val locs = arrayListOf<RelativeLocation>()
                        repeat(data2.getRandomCount()) {
                            val u = rand.nextDouble()
                            val v = rand.nextDouble()
                            val theta = 2.0 * PI * u
                            val phi = acos(2.0 * v - 1.0)
                            val dx = sin(phi) * cos(theta)
                            val dy = cos(phi)
                            val dz = sin(phi) * sin(theta)
                            val rr = 1.0 * cbrt(rand.nextDouble())
                            locs.add(RelativeLocation(dx * rr + 0.0, dy * rr + 0.0, dz * rr + 0.0))
                        }
                        locs
                    }
                    .createWithoutClone()
                    .map { rel ->
                        val speed = data2.getRandomSpeed()
                        val particleSize = data2.getRandomSize()
                        val spawnRelative = rel.toVector().subtract(RelativeLocation(0.0, 0.0, 0.0).toVector())
                        val dir = spawnRelative
                        val velocity = if (dir.lengthSqr() < 1e-8) Vec3.ZERO else dir.normalize().scale(speed)
                        val relativeRotationDir = spawnRelative
                        template2.clone().apply {
                            maxAge = data2.getRandomParticleMaxAge()
                            this.color = Vector3f(0.823529f, 0.443137f, 0.996078f)
                            uniformSize = false
                            weightSize = particleSize * template2.weightSize
                            heightSize = particleSize * template2.heightSize
                            this.velocity = velocity
                            if (relativeRotationDir.lengthSqr() >= 1e-8) {
                                this.pitch = template2.pitch + atan2(relativeRotationDir.z, relativeRotationDir.y).toFloat()
                                this.roll = template2.roll + atan2(-relativeRotationDir.x, hypot(relativeRotationDir.y, relativeRotationDir.z)).toFloat()
                            }
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
    }
}