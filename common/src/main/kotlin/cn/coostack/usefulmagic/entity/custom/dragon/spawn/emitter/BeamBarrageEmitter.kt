package cn.coostack.usefulmagic.entity.custom.dragon.spawn.emitter

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.interpolator.data.InterpolatorDouble
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@CooAutoRegister
class BeamBarrageEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {

    @CodecField
    var radius = InterpolatorDouble(1.0)

    @CodecField
    var radian = InterpolatorDouble(Random.nextDouble(-PI, PI))


    @CodecField
    var rotateSpeed = PI / 16

    @CodecField
    var movement = Vec3.ZERO

    @CodecField
    var settings = SimpleRandomParticleData()

    @CodecField
    var colorLeft = Math3DUtil.colorOf(255, 255, 255)

    @CodecField
    var colorRight = Math3DUtil.colorOf(255, 100, 200)

    @CodecField
    var templateData = ControlableParticleData()
        .apply {
            this.color = colorLeft
            setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
        }

    init {
        enableInterpolator = true
    }

    override fun doTick() {
        teleportTo(pos + movement)
        radian += rotateSpeed
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val rad = radian.getWithInterpolator(lerpProgress)
        val r = radius.getWithInterpolator(lerpProgress)
        val x = cos(rad) * r
        val z = sin(rad) * r

        // 进行环绕插值
        return PointsBuilder()
            .addPoint(RelativeLocation(x, 0.0, z))
            .rotateTo(movement)
            .createWithoutClone()
            .map {
                templateData.clone().apply {
                    this.maxAge = settings.getRandomParticleMaxAge()
                    this.size = settings.getRandomSize()
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
            val lerp = (this.currentAge / this.lifetime.toFloat())
            this.color = GraphMathHelper.lerp(lerp, colorLeft, colorRight)
        }
    }
}