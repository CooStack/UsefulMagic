package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.force.CParticleForce
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableCParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.PhysicConstant
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.random.Random

@CooAutoRegister
class ExplodeMagicEmitters(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    var templateData = ControlableCParticleData()

    /**
     * 最小爆炸速度
     */
    @CodecField
    var minSpeed = 0.5

    /**
     * 最大爆炸速度
     */
    @CodecField
    var maxSpeed = 6.0

    @CodecField
    var ballCountPow = 40

    /**
     * 最小随机球点个数
     */
    @CodecField
    var randomCountMin = 800

    /**
     * 最大随机球点的个数
     */
    @CodecField
    var randomCountMax = 1000

    /**
     * 速度衰减（默认每 tick 15%）
     */
    @CodecField
    var precentDrag = 0.85

    @CodecField
    var randomParticleAgeMin = 60

    @CodecField
    var randomParticleAgeMax = 120

    init {
        airDensity = PhysicConstant.SEA_AIR_DENSITY * 10
    }


    override fun cparticleForces(): List<CParticleForce> {
        return listOf(
            CParticleForce.ExpDrag(1 - precentDrag, 0.01)
        )
    }

    override fun doTick() {

    }

    val random = Random(System.currentTimeMillis())
    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val velocityList = PointsBuilder()
            .addBall(2.0, ballCountPow)
            .rotateAsAxis(random.nextDouble(-PI, PI))
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
            .create()
        val res = ArrayList<Pair<ControlableParticleData, RelativeLocation>>()
        val count = random.nextInt(randomCountMin, randomCountMax)
        for (i in 0 until count) {
            val it = velocityList.random()
            res.add(templateData.clone().apply {
                this.velocity = it.normalize().multiply(random.nextDouble(minSpeed, maxSpeed)).toVector()
                this.maxAge = random.nextInt(randomParticleAgeMin, randomParticleAgeMax)
            } to RelativeLocation())
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
