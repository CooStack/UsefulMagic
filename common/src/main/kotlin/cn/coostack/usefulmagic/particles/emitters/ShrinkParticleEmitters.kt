package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.emitters.LinearResistanceHelper
import cn.coostack.usefulmagic.extend.multiply
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import kotlin.math.PI
import kotlin.random.Random

class ShrinkParticleEmitters(pos: Vec3, world: Level?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()
    var startRange = 20.0
    var startSpeed = 1.0
    var speedDrag = 1.0
    var countMin = 30
    var countMax = 120
    var ballCountPow = 12

    companion object {
        const val ID = "shrink-particle-emitters"
        val CODEC: StreamCodec<FriendlyByteBuf, ParticleEmitters> = StreamCodec.of<FriendlyByteBuf, ParticleEmitters>(
            { buf, data ->
                data as ShrinkParticleEmitters
                encodeBase(data, buf)
                ControlableParticleData.PACKET_CODEC.encode(buf, data.templateData)
                buf.writeDouble(data.startRange)
                buf.writeDouble(data.startSpeed)
                buf.writeDouble(data.speedDrag)
                buf.writeInt(data.countMin)
                buf.writeInt(data.countMax)
                buf.writeInt(data.ballCountPow)
            }, {
                val container = ShrinkParticleEmitters(Vec3.ZERO, null)
                decodeBase(container, it)
                container.templateData = ControlableParticleData.PACKET_CODEC.decode(it)
                container.startRange = it.readDouble()
                container.startSpeed = it.readDouble()
                container.speedDrag = it.readDouble()
                container.countMin = it.readInt()
                container.countMax = it.readInt()
                container.ballCountPow = it.readInt()
                container
            }
        )
    }

    override fun doTick() {

    }

    val random = Random(System.currentTimeMillis())
    override fun genParticles(): Map<ControlableParticleData, RelativeLocation> {
        val res = HashMap<ControlableParticleData, RelativeLocation>()
        val actualCount = random.nextInt(countMin, countMax)
        val points = PointsBuilder()
            .addBall(startRange, ballCountPow)
            .rotateAsAxis(random.nextDouble(-PI, PI))
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
            .create()
        repeat(actualCount) {
            val rel = points.random()
            res[templateData.clone().also {
                it.velocity = rel.toVector().normalize().multiply(-startSpeed)
            }] = rel.clone()
        }
        return res
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: Vec3,
        spawnWorld: Level
    ) {
        controler.addPreTickAction {
            data.velocity = LinearResistanceHelper.setPercentageVelocity(
                data.velocity, speedDrag
            )
            updatePhysics(this.loc, data)
        }
    }

    override fun update(emitters: ParticleEmitters) {
        super.update(emitters)
        if (emitters !is ShrinkParticleEmitters) {
            return
        }
        this.startRange = emitters.startRange
        this.startSpeed = emitters.startSpeed
        this.speedDrag = emitters.speedDrag
        this.countMin = emitters.countMin
        this.countMax = emitters.countMax
        this.ballCountPow = emitters.ballCountPow
    }

    override fun getEmittersID(): String {
        return ID
    }

    override fun getCodec(): StreamCodec<FriendlyByteBuf, ParticleEmitters> {
        return CODEC
    }

}