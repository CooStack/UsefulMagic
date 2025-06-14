package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.MathUtil
import net.fabricmc.loader.impl.lib.sat4j.core.Vec
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.random.Random

class DiscreteCylinderEmitters(pos: Vec3d, world: World?) : ClassParticleEmitters(pos, world) {
    companion object {
        const val ID = "discrete-cylinder-emitters"
        val CODEC = PacketCodec.ofStatic<PacketByteBuf, ParticleEmitters>(
            { buf, data ->
                data as DiscreteCylinderEmitters
                encodeBase(data, buf)
                ControlableParticleData.Companion.PACKET_CODEC.encode(
                    buf, data.templateData
                )
                buf.writeDouble(data.minDiscrete)
                buf.writeDouble(data.maxDiscrete)
                buf.writeDouble(data.maxRadius)
                buf.writeDouble(data.height)
                buf.writeDouble(data.heightStep)
                buf.writeDouble(data.radiusStep)
                buf.writeInt(data.minCount)
                buf.writeInt(data.maxCount)
                buf.writeVec3d(data.direction)
            }, {
                val container = DiscreteCylinderEmitters(Vec3d.ZERO, null)
                decodeBase(container, it)
                container.templateData = ControlableParticleData.Companion.PACKET_CODEC.decode(it)
                container.minDiscrete = it.readDouble()
                container.maxDiscrete = it.readDouble()
                container.maxRadius = it.readDouble()
                container.height = it.readDouble()
                container.heightStep = it.readDouble()
                container.radiusStep = it.readDouble()
                container.minCount = it.readInt()
                container.maxCount = it.readInt()
                container.direction = it.readVec3d()
                container
            }
        )

    }

    var templateData = ControlableParticleData()
    val random = Random(System.currentTimeMillis())
    var minDiscrete = 1.0
    var maxDiscrete = 10.0
    var maxRadius = 10.0
    var height = 100.0
    var heightStep = 1.0
    var radiusStep = 1.0
    var minCount = 20
    var maxCount = 120
    var direction = Vec3d(0.0, 1.0, 0.0)
    override fun doTick() {
    }

    override fun genParticles(): Map<ControlableParticleData, RelativeLocation> {
        return PointsBuilder.of(
            MathUtil.discreteCylinderGenerator(
                minDiscrete, maxDiscrete, maxRadius, height, heightStep, radiusStep, minCount, maxCount
            )
        ).rotateTo(direction)
            .create().associateBy { templateData.clone() }
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: Vec3d,
        spawnWorld: World
    ) {
        data.velocity = pos.relativize(spawnPos)
            .normalize().add(
                Vec3d(
                    random.nextDouble(-0.5, 0.5),
                    random.nextDouble(-0.5, 0.5),
                    random.nextDouble(-0.5, 0.5),
                )
            ).normalize().multiply(0.1)
        controler.addPreTickAction {
            updatePhysics(pos, data)
        }
    }

    override fun getEmittersID(): String {
        return ID
    }

    override fun getCodec(): PacketCodec<PacketByteBuf, ParticleEmitters> {
        return CODEC
    }

    override fun update(emitters: ParticleEmitters) {
        super.update(emitters)
        if (emitters !is DiscreteCylinderEmitters) {
            return
        }
        direction = emitters.direction
    }

}