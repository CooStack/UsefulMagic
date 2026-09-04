package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleColorCurve
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoTransformableCParticleEmitter
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableCParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.command.curve.BezierFloatKeyframe
import cn.coostack.cooparticlesapi.network.particle.emitters.command.curve.BezierKeyframeFloatCurve
import cn.coostack.cooparticlesapi.particles.ParticleCameraOption
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * 通过计算当前生命周期 和 固定velocity 得出对应的相对距离
 *
 * 为了计算方便不会采用任何的command修改velocity的大小
 *
 * 用于能量聚集
 *
 * @constructor
 * @param pos
 * @param world
 */
@CooAutoRegister
class CollectLineParticleEmitter(pos: Vec3, world: Level?) : AutoTransformableCParticleEmitter(pos, world) {

    private val sizeYCurve = BezierKeyframeFloatCurve(
        listOf(
            BezierFloatKeyframe(
                time = 0.0,
                value = 0.0,
                outX = 32.964,
                outY = 7.431,
                inX = -33.0,
                inY = 0.0
            ), BezierFloatKeyframe(time = 1.0, value = 0.0, outX = 33.0, outY = 0.0, inX = -67.313, inY = 7.539)
        )
    )
    private val translucentCurve = BezierKeyframeFloatCurve(
        listOf(
            BezierFloatKeyframe(
                time = 0.0,
                value = 0.0,
                outX = 36.675,
                outY = 1.11306,
                inX = -33.0,
                inY = 0.0
            ),
            BezierFloatKeyframe(time = 0.8, value = 1.0, outX = 54.153, outY = -0.02414, inX = -35.293, inY = 0.2274),
            BezierFloatKeyframe(time = 1.0, value = 0.0005, outX = 33.0, outY = 0.0, inX = -2.639, inY = 0.91869)
        )
    )


    @CodecField
    var template = ControlableCParticleData().apply {
        setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
        alphaCurve = CParticleCurve.fromFloatCurve(sizeYCurve)
        scaleYCurve = CParticleCurve.fromFloatCurve(translucentCurve)
    }

    @CodecField
    var simpleData = SimpleRandomParticleData()
        .apply {
            minAge = 10
            maxAge = 20
        }

    @CodecField
    var disappearRadius = 1.0


    override fun doTick() {
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableCParticleData, RelativeLocation>> {
        return PointsBuilder()
            .addBallSurface(disappearRadius, simpleData.getRandomCount())
            .applyNoiseOffset(Vec3(1.0, 1.0, 1.0))
            .createWithoutClone()
            .map {
                val speed = simpleData.getRandomSpeed()
                val age = simpleData.getRandomParticleMaxAge()
                val velocity = it.normalize()
                val shouldOffset = velocity * speed * age
                template.clone().apply {
                    this.colorCurve = CParticleColorCurve.linear(simpleData.leftColor, simpleData.rightColor)
                    this.maxAge = age
                    this.uniformSize = false
                    this.velocity = velocity.toVector() * -speed
                    // 这里设置一下不透明度曲线和大小
                    this.cameraOption = ParticleCameraOption.ROTATION
                    this.pitch += atan2(velocity.z, velocity.y).toFloat()
                    this.roll += atan2(-velocity.x, hypot(velocity.y, velocity.z)).toFloat()
                    this.heightSize = 1f
                    this.weightSize = 0.2f
                } to it + shouldOffset
            }
    }

}
