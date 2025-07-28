package cn.coostack.usefulmagic.particles.emitters.explosion

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.particles.Controlable
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.emitters.LinearResistanceHelper
import net.fabricmc.loader.impl.lib.sat4j.core.Vec
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.random.Random
import kotlin.random.nextInt

class ExplosionWaveEmitters(pos: Vec3d, world: World?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()

    // 圆生成时的半径大小
    var waveSize = 1.0

    // 冲击波的扩散速度
    var waveSpeed = 0.2

    var speedDrag = 0.95

    // 生成冲击波圆环时的粒子个数范围
    var waveCircleCountMin = 60
    var waveCircleCountMax = 120

    var discrete = 0.1

    var randomVector = false
    var randomSpeed = 0.1

    companion object {
        const val ID = "explosion-wave-magic-emitters"
        val CODEC = PacketCodec.ofStatic<PacketByteBuf, ParticleEmitters>(
            { buf, data ->
                data as ExplosionWaveEmitters
                encodeBase(data, buf)
                ControlableParticleData.PACKET_CODEC.encode(buf, data.templateData)
                buf.writeDouble(data.waveSize)
                buf.writeDouble(data.waveSpeed)
                buf.writeDouble(data.speedDrag)
                buf.writeInt(data.waveCircleCountMin)
                buf.writeInt(data.waveCircleCountMax)
                buf.writeDouble(data.discrete)
                buf.writeBoolean(data.randomVector)
                buf.writeDouble(data.randomSpeed)
            }, {
                val container = ExplosionWaveEmitters(Vec3d.ZERO, null)
                decodeBase(container, it)
                container.templateData = ControlableParticleData.PACKET_CODEC.decode(it)
                container.waveSize = it.readDouble()
                container.waveSpeed = it.readDouble()
                container.speedDrag = it.readDouble()
                container.waveCircleCountMin = it.readInt()
                container.waveCircleCountMax = it.readInt()
                container.discrete = it.readDouble()
                container.randomVector = it.readBoolean()
                container.randomSpeed = it.readDouble()
                container
            }
        )

    }

    override fun doTick() {
    }

    val random = Random(System.currentTimeMillis())
    override fun genParticles(): Map<ControlableParticleData, RelativeLocation> {
        return PointsBuilder()
            .addDiscreteCircleXZ(1.0, random.nextInt(waveCircleCountMin, waveCircleCountMax), discrete)
            .create().associateBy {
                it.multiply(waveSize)
                templateData.clone()
                    .apply {
                        velocity = it.clone().multiply(waveSpeed).toVector()
                    }
            }
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: Vec3d,
        spawnWorld: World
    ) {
        controler.addPreTickAction {
            data.velocity = LinearResistanceHelper.setPercentageVelocity(
                data.velocity, speedDrag
            )
            if (randomVector) {
                data.velocity = data.velocity.add(
                    Vec3d(
                        random.nextDouble(-1.0, 1.0),
                        random.nextDouble(-1.0, 1.0),
                        random.nextDouble(-1.0, 1.0),
                    ).normalize().multiply(random.nextDouble(-randomSpeed, randomSpeed))
                )
            }
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