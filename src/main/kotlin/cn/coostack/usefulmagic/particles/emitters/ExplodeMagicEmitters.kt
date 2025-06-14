package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.PhysicConstant
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.ExplodeClassParticleEmitters
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.emitters.LinearResistanceHelper
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.PI
import kotlin.random.Random

class ExplodeMagicEmitters(pos: Vec3d, world: World?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()

    /**
     * 最小爆炸半径
     */
    var minSpeed = 0.5

    /**
     * 最大爆炸半径
     */
    var maxSpeed = 6.0

    var ballCountPow = 40

    /**
     * 最小随机球点个数
     */
    var randomCountMin = 800

    /**
     * 最大随机球点的个数
     */
    var randomCountMax = 1000

    /**
     * 速度衰减 (默认15%)每 tick
     */
    var precentDrag = 0.85

    var randomParticleAgeMin = 60

    var randomParticleAgeMax = 120

    init {
        airDensity = PhysicConstant.SEA_AIR_DENSITY * 10
    }

    companion object {
        const val ID = "explode-magic-particle-emitters"

        @JvmStatic
        val CODEC = PacketCodec.ofStatic<PacketByteBuf, ParticleEmitters>(
            { buf, data ->
                data as ExplodeMagicEmitters
                encodeBase(data, buf)
                ControlableParticleData.PACKET_CODEC.encode(buf, data.templateData)
                buf.writeDouble(data.minSpeed)
                buf.writeDouble(data.maxSpeed)
                buf.writeInt(data.ballCountPow)
                buf.writeInt(data.randomCountMin)
                buf.writeInt(data.randomCountMax)
                buf.writeDouble(data.precentDrag)
                buf.writeInt(data.randomParticleAgeMin)
                buf.writeInt(data.randomParticleAgeMax)
            }, {
                val instance = ExplodeMagicEmitters(Vec3d.ZERO, null)
                decodeBase(instance, it)
                instance.templateData = ControlableParticleData.PACKET_CODEC.decode(it)
                instance.minSpeed = it.readDouble()
                instance.maxSpeed = it.readDouble()
                instance.ballCountPow = it.readInt()
                instance.randomCountMin = it.readInt()
                instance.randomCountMax = it.readInt()
                instance.precentDrag = it.readDouble()
                instance.randomParticleAgeMin = it.readInt()
                instance.randomParticleAgeMax = it.readInt()
                instance
            }
        )
    }

    override fun doTick() {
    }

    val random = Random(System.currentTimeMillis())
    override fun genParticles(): Map<ControlableParticleData, RelativeLocation> {
        val velocityList = PointsBuilder()
            .addBall(2.0, ballCountPow)
            .rotateAsAxis(random.nextDouble(-PI, PI))
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
            .create()
        val res = HashMap<ControlableParticleData, RelativeLocation>()
        val count = random.nextInt(randomCountMin, randomCountMax)
        for (i in 0 until count) {
            val it = velocityList.random()
            res[templateData.clone().apply {
                this.velocity = it.normalize().multiply(random.nextDouble(minSpeed, maxSpeed)).toVector()
            }] = RelativeLocation()
        }
        return res
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: Vec3d,
        spawnWorld: World
    ) {
        data.maxAge = random.nextInt(randomParticleAgeMin, randomParticleAgeMax)
        controler.addPreTickAction {
            data.velocity = LinearResistanceHelper.setPercentageVelocity(
                data.velocity, precentDrag
            )
            updatePhysics(pos, data)
        }
    }


    override fun getEmittersID(): String {
        return ID
    }

    override fun getCodec(): PacketCodec<PacketByteBuf, ParticleEmitters> {
        return CODEC
    }
}