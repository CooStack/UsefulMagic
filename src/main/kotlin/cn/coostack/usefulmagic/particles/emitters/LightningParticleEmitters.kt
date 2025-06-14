package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters.Companion.decodeBase
import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters.Companion.encodeBase
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.emitters.LinearResistanceHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.roundToInt
import kotlin.random.Random

class LightningParticleEmitters(pos: Vec3d, world: World?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()
    var endPos = RelativeLocation()

    companion object {
        const val ID = "lightning-particle-emitters"

        @JvmStatic
        val CODEC = PacketCodec.ofStatic<PacketByteBuf, ParticleEmitters>(
            { buf, data ->
                data as LightningParticleEmitters
                encodeBase(data, buf)
                buf.writeVec3d(data.endPos.toVector())
                ControlableParticleData.PACKET_CODEC.encode(buf, data.templateData)

            }, {
                val instance = LightningParticleEmitters(Vec3d.ZERO, null)
                decodeBase(instance, it)
                instance.endPos = RelativeLocation.of(it.readVec3d())
                instance.templateData = ControlableParticleData.PACKET_CODEC.decode(it)
                instance
            }
        )
    }

    override fun doTick() {
    }

    val options
        get() = ParticleOption.getParticleCounts()

    override fun genParticles(): Map<ControlableParticleData, RelativeLocation> {
        val count = (endPos.length() / 10).roundToInt().coerceIn(3, 6)
        return PointsBuilder()
            .addLightningAttenuationPoints(endPos, count, 5.5 * endPos.length() / 50, 0.3, options * 8 * 3 / count)
            .create().associateBy {
                templateData.clone()
            }
    }

    override fun update(emitters: ParticleEmitters) {
        super.update(emitters)
        if (emitters !is LightningParticleEmitters) {
            return
        }
        endPos = emitters.endPos
    }

    val random = Random(System.currentTimeMillis())
    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: Vec3d,
        spawnWorld: World
    ) {
        data.maxAge = 5
        val r = (data.color.x * 255).toInt()
        val g = (data.color.y * 255).toInt()
        val b = (data.color.z * 255).toInt()
        data.alpha = 0.8f
        data.textureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
        data.color = Math3DUtil.colorOf(
            (r + random.nextInt(10, 60)).coerceIn(0, 255),
            (g + random.nextInt(10, 60)).coerceIn(0, 255),
            b
        )

    }

    override fun getEmittersID(): String {
        return ID
    }

    override fun getCodec(): PacketCodec<PacketByteBuf, ParticleEmitters> {
        return CODEC
    }
}