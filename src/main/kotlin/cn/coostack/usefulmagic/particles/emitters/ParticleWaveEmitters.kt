package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.emitters.LinearResistanceHelper
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.random.Random

class ParticleWaveEmitters(pos: Vec3d, world: World?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()

    // 圆生成时的半径大小
    var waveSize = 1.0

    // 冲击波的扩散速度
    var waveSpeed = 0.2

    // 生成冲击波圆环时的粒子个数范围
    var waveCircleCountMin = 60
    var waveCircleCountMax = 120

    var waveAxis = Vec3d(0.0, 1.0, 0.0)

    companion object {
        const val ID = "particle-wave-magic-emitters"
        val CODEC = PacketCodec.ofStatic<PacketByteBuf, ParticleEmitters>(
            { buf, data ->
                data as ParticleWaveEmitters
                encodeBase(data, buf)
                ControlableParticleData.Companion.PACKET_CODEC.encode(buf, data.templateData)
                buf.writeDouble(data.waveSize)
                buf.writeDouble(data.waveSpeed)
                buf.writeInt(data.waveCircleCountMin)
                buf.writeInt(data.waveCircleCountMax)
                buf.writeVec3d(data.waveAxis)
            }, {
                val container = ParticleWaveEmitters(Vec3d.ZERO, null)
                decodeBase(container, it)
                container.templateData = ControlableParticleData.Companion.PACKET_CODEC.decode(it)
                container.waveSize = it.readDouble()
                container.waveSpeed = it.readDouble()
                container.waveCircleCountMin = it.readInt()
                container.waveCircleCountMax = it.readInt()
                container.waveAxis = it.readVec3d()
                container
            }
        )

    }

    override fun doTick() {
    }

    val random = Random(System.currentTimeMillis())
    override fun genParticles(): Map<ControlableParticleData, RelativeLocation> {
        return PointsBuilder()
            .addDiscreteCircleXZ(waveSize, random.nextInt(waveCircleCountMin, waveCircleCountMax),0.5)
            .rotateTo(waveAxis)
            .create().associateBy {
                templateData.clone()
                    .apply {
                        velocity = it.normalize().multiply(waveSpeed).toVector()
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
            updatePhysics(pos, data)
        }
    }

    override fun getEmittersID(): String {
        return ID
    }


    override fun update(emitters: ParticleEmitters) {
        super.update(emitters)
        if (emitters !is ParticleWaveEmitters) {
            return
        }
        this.waveSpeed = emitters.waveSpeed
        this.waveCircleCountMin = emitters.waveCircleCountMin
        this.waveCircleCountMax = emitters.waveCircleCountMax
        this.waveAxis = emitters.waveAxis
    }

    override fun getCodec(): PacketCodec<PacketByteBuf, ParticleEmitters> {
        return CODEC
    }
}