package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.helper.emitters.LinearResistanceHelper
import cn.coostack.usefulmagic.extend.multiply
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import kotlin.random.Random

class DirectionShootEmitters(pos: Vec3, world: Level?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()
    var shootType = EmittersShootTypes.point()
    var count = 10
    var shootDirection = Vec3(0.0, 1.0, 0.0)


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
        val CODEC = StreamCodec.of<FriendlyByteBuf, ParticleEmitters>(
            { buf, data ->
                data as DirectionShootEmitters
                encodeBase(data, buf)
                ControlableParticleData.PACKET_CODEC.encode(buf, data.templateData)
                buf.writeInt(data.count)
                buf.writeUtf(data.shootType.getID())
                data.shootType.getCodec().encode(buf, data.shootType)
                buf.writeDouble(data.randomX)
                buf.writeDouble(data.randomY)
                buf.writeDouble(data.randomZ)
                buf.writeDouble(data.randomSpeedOffset)
                buf.writeDouble(data.speedDrag)
                buf.writeVec3(data.shootDirection)
            }, {
                val container = DirectionShootEmitters(Vec3.ZERO, null)
                decodeBase(container, it)
                container.templateData = ControlableParticleData.PACKET_CODEC.decode(it)
                container.count = it.readInt()
                val id = it.readUtf()
                container.shootType = EmittersShootTypes.fromID(id)!!.decode(it)
                container.randomX = it.readDouble()
                container.randomY = it.readDouble()
                container.randomZ = it.readDouble()
                container.randomSpeedOffset = it.readDouble()
                container.speedDrag = it.readDouble()
                container.shootDirection = it.readVec3()
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
        return shootType.getPositions(Vec3.ZERO, tick, count).map { RelativeLocation.of(it) }.shuffled().associateBy {
            templateData.clone().apply {
                velocity = shootDirection
            }
        }
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: Vec3,
        spawnWorld: Level
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
            updatePhysics(pos, data)
        }
    }

    override fun getEmittersID(): String {
        return ID
    }

    override fun getCodec(): StreamCodec<FriendlyByteBuf, ParticleEmitters> {
        return CODEC
    }
}