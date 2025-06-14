package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.helper.emitters.LinearResistanceHelper
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.random.Random

class DirectionShootEmitters(pos: Vec3d, world: World?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()
    var shootType = EmittersShootTypes.point()
    var count = 10
    var shootDirection = Vec3d(0.0, 1.0, 0.0)


    // 随机粒子偏移范围
    var randomX = 0.1
        set(value) {
            field = value.coerceAtLeast(0.01)
        }
    var randomY = 0.01
        set(value) {
            field = value.coerceAtLeast(0.01)
        }
    var randomZ = 0.1
        set(value) {
            field = value.coerceAtLeast(0.01)
        }

    // 速度偏移范围 -field .. field
    var randomSpeedOffset = 0.5
        set(value) {
            field = value.coerceAtLeast(0.01)
        }

    // 百分比阻塞
    var speedDrag = 1.0

    val random = Random(System.currentTimeMillis())

    companion object {
        const val ID = "direction-shoot-emitters"
        val CODEC = PacketCodec.ofStatic<PacketByteBuf, ParticleEmitters>(
            { buf, data ->
                data as DirectionShootEmitters
                encodeBase(data, buf)
                ControlableParticleData.PACKET_CODEC.encode(buf, data.templateData)
                buf.writeInt(data.count)
                buf.writeString(data.shootType.getID())
                data.shootType.getCodec().encode(buf, data.shootType)
                buf.writeDouble(data.randomX)
                buf.writeDouble(data.randomY)
                buf.writeDouble(data.randomZ)
                buf.writeDouble(data.randomSpeedOffset)
                buf.writeDouble(data.speedDrag)
                buf.writeVec3d(data.shootDirection)
            }, {
                val container = DirectionShootEmitters(Vec3d.ZERO, null)
                decodeBase(container, it)
                container.templateData = ControlableParticleData.PACKET_CODEC.decode(it)
                container.count = it.readInt()
                val id = it.readString()
                container.shootType = EmittersShootTypes.fromID(id)!!.decode(it)
                container.randomX = it.readDouble()
                container.randomY = it.readDouble()
                container.randomZ = it.readDouble()
                container.randomSpeedOffset = it.readDouble()
                container.speedDrag = it.readDouble()
                container.shootDirection = it.readVec3d()
                container
            }
        )
    }

    override fun doTick() {
    }

    override fun update(emitters: ParticleEmitters) {
        super.update(emitters)
        if (emitters !is DirectionShootEmitters) {
            return
        }
        this.templateData = emitters.templateData
        this.randomX = emitters.randomX
        this.randomZ = emitters.randomZ
        this.randomSpeedOffset = emitters.randomSpeedOffset
        this.speedDrag = emitters.speedDrag
        this.count = emitters.count
    }

    override fun genParticles(): Map<ControlableParticleData, RelativeLocation> {
        return shootType.getPositions(Vec3d.ZERO, tick, count).map { RelativeLocation.of(it) }.shuffled().associateBy {
            templateData.clone().apply {
                velocity = shootDirection
            }
        }
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: Vec3d,
        spawnWorld: World
    ) {
        data.velocity = data.velocity.add(
            random.nextDouble(-randomX, randomX),
            random.nextDouble(-randomY, randomY),
            random.nextDouble(-randomZ, randomZ),
        ).normalize().multiply(data.speed + random.nextDouble(-randomSpeedOffset, randomSpeedOffset))
        controler.addPreTickAction {
            data.velocity = LinearResistanceHelper.setPercentageVelocity(
                data.velocity, speedDrag
            )
            updatePhysics(this.pos, data)
        }
    }

    override fun getEmittersID(): String {
        return ID
    }

    override fun getCodec(): PacketCodec<PacketByteBuf, ParticleEmitters> {
        return CODEC
    }
}