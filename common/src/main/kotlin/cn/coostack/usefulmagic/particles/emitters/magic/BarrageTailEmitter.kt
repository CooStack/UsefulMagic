package cn.coostack.usefulmagic.particles.emitters.magic

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleColorCurve
import cn.coostack.cooparticlesapi.cparticle.force.CParticleForce
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableCParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
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

    override fun cparticleForces(): List<CParticleForce> {
        return listOf(
            CParticleForce.Noise(
                0.25,1.3,2.0,20.0,1.0,true
            ),
            CParticleForce.ExpDrag(0.15,0.01,0.0)
        )
    }


    @CodecField
    var template = ControlableCParticleData().apply {
        velocity = Vec3(0.0, 0.0, 0.0)
        visibleRange = 256.0f
        alpha = 1.0f
        light = 15
        faceToCamera = true
        speedLimit = 32.0
        sign = 0
    }

    @CodecField
    var left = Vector3f(0.996078f, 0.329412f, 0.164706f)

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
                            colorCurve = CParticleColorCurve.linear(left, right)
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

    override fun doTick() {
        // 朝着dir移动？ x
    }

}
