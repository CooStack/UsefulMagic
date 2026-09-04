package cn.coostack.usefulmagic.particles.emitters.magic.useful

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@CooAutoRegister
class PushCloudEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    @CodecField
    var direction: Vec3 = Vec3(0.0, 0.0, 1.0)

    @CodecField
    var strength: Double = 1.0

    @CodecField
    var visibleRange: Float = 64.0f

    @CodecField
    var templateData = ControlableParticleData().apply {
        velocity = Vec3.ZERO
        visibleRange = this@PushCloudEmitter.visibleRange
        color = Vector3f(1.0f, 1.0f, 1.0f)
        alpha = 0.8f
        light = 15
        size = 0.35f
        speedLimit = 16.0
        faceToCamera = true
        effect = ControlableCloudEffect(uuid)
        setTextureSheet(TextureSheetsEnum.PARTICLE_SHEET_TRANSLUCENT)
    }

    @CodecField
    var randomData = SimpleRandomParticleData().apply {
        minAge = 14
        maxAge = 24
        minCount = 20
        maxCount = 32
        minSize = 0.45
        maxSize = 1.0
        minSpeed = 0.35
        maxSpeed = 0.75
    }

    init {
        delay = 1
        maxTick = 6
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val facing = safeDirection(direction)
        val count = (randomData.getRandomCount() * strength.coerceIn(0.75, 1.8)).toInt().coerceAtLeast(1)
        val radius = (0.55 + strength * 0.14).coerceIn(0.55, 1.75)
        val length = (1.0 + strength * 0.32).coerceIn(1.0, 3.2)
        return PointsBuilder()
            .addPoints(createCloudVolume(facing, count, radius, length))
            .createWithoutClone()
            .map { rel ->
                val relVector = rel.toVector()
                val forwardOffset = facing.scale(relVector.dot(facing))
                val side = relVector.subtract(forwardOffset)
                val baseDirection = safeDirection(facing.scale(1.2).add(side.scale(0.22)))
                templateData.clone().apply {
                    visibleRange = this@PushCloudEmitter.visibleRange
                    maxAge = randomData.getRandomParticleMaxAge()
                    size = randomData.getRandomSize()
                    velocity = baseDirection.scale(randomData.getRandomSpeed() * strength.coerceIn(0.5, 2.0))
                } to rel
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
            val progress = if (lifetime <= 0) 1f else (currentAge.toFloat() / lifetime.toFloat()).coerceIn(0f, 1f)
            particleAlpha = data.alpha * (1f - progress)
            data.velocity = data.velocity.scale(0.86)
            updatePhysics(pos, data, this)
        }
    }

    override fun doTick() {
        pos = pos.add(safeDirection(direction).scale(strength.coerceIn(0.3, 2.4) * 0.32))
    }

    private fun createCloudVolume(
        facing: Vec3,
        count: Int,
        radius: Double,
        length: Double
    ): Collection<RelativeLocation> {
        val (right, up) = buildBasis(facing)
        return List(count) {
            val progress = Random.nextDouble(0.0, 1.0)
            val angle = Random.nextDouble(0.0, PI * 2.0)
            val localRadius = radius * Random.nextDouble(0.15, 1.0) * (1.0 - progress * 0.2)
            val forward = length * progress + Random.nextDouble(-0.2, 0.2)
            val offset = facing.scale(forward)
                .add(right.scale(cos(angle) * localRadius))
                .add(up.scale(sin(angle) * localRadius))
            RelativeLocation.of(offset)
        }
    }

    private fun buildBasis(facing: Vec3): Pair<Vec3, Vec3> {
        val reference = if (abs(facing.dot(Vec3(0.0, 1.0, 0.0))) > 0.95) {
            Vec3(1.0, 0.0, 0.0)
        } else {
            Vec3(0.0, 1.0, 0.0)
        }
        val right = safeDirection(facing.cross(reference))
        val up = safeDirection(right.cross(facing))
        return right to up
    }

    private fun safeDirection(input: Vec3): Vec3 {
        if (input.lengthSqr() <= 1.0E-8) {
            return Vec3(0.0, 0.0, 1.0)
        }
        return input.normalize()
    }
}
