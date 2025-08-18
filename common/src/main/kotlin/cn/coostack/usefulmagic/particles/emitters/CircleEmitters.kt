package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.emitters.LinearResistanceHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.network.FriendlyByteBuf

import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import kotlin.math.PI

class CircleEmitters(pos: Vec3, world: Level?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()
    var circleSpeed = 2.0
    var circleCount = 60
    var circleDirection = Vec3(0.0, 1.0, 0.0)
    var particleMaxAge = 20

    /**
     * 速度衰减 (默认15%)每 tick
     */
    var precentDrag = 0.85

    companion object {
        const val ID = "circle-emitters"

        @JvmStatic
        val CODEC = StreamCodec.of<FriendlyByteBuf, ParticleEmitters>(
            { buf, data ->
                data as CircleEmitters
                encodeBase(data, buf)
                ControlableParticleData.PACKET_CODEC.encode(buf, data.templateData)
                buf.writeDouble(data.circleSpeed)
                buf.writeDouble(data.precentDrag)
                buf.writeInt(data.circleCount)
                buf.writeInt(data.particleMaxAge)
                buf.writeVec3(data.circleDirection)

            }, {
                val instance = CircleEmitters(Vec3.ZERO, null)
                decodeBase(instance, it)
                instance.templateData = ControlableParticleData.PACKET_CODEC.decode(it)
                instance.circleSpeed = it.readDouble()
                instance.precentDrag = it.readDouble()
                instance.circleCount = it.readInt()
                instance.particleMaxAge = it.readInt()
                instance.circleDirection = it.readVec3()
                instance
            }
        )
    }

    override fun doTick() {
    }

    override fun genParticles(): Map<ControlableParticleData, RelativeLocation> {
        val res = HashMap<ControlableParticleData, RelativeLocation>()
        val velocityList = PointsBuilder()
            .addCircle(2.0, circleCount)
            .rotateTo(circleDirection)
            .create()

        for (velocity in velocityList) {
            res[templateData.clone().apply {
                this.velocity = velocity.normalize().multiply(circleSpeed).toVector()
            }] = RelativeLocation()
        }

        return res
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: Vec3,
        spawnWorld: Level
    ) {
        data.maxAge = particleMaxAge
        controler.addPreTickAction {
            data.velocity = LinearResistanceHelper.setPercentageVelocity(
                data.velocity, precentDrag
            )
        }
    }

    override fun getEmittersID(): String {
        return ID
    }

    override fun getCodec(): StreamCodec<FriendlyByteBuf, ParticleEmitters> {
        return CODEC
    }
}