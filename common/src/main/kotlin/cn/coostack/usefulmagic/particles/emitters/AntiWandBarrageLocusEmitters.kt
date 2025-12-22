package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

class AntiWandBarrageLocusEmitters(pos: Vec3, world: Level?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()

    companion object {
        const val ID = "anti_wand_barrage_locus_emitters"

        @JvmStatic
        val CODEC: StreamCodec<FriendlyByteBuf, ParticleEmitters> = StreamCodec.of<FriendlyByteBuf, ParticleEmitters>(
            { buf, data ->
                data as AntiWandBarrageLocusEmitters
                encodeBase(data, buf)
                ControlableParticleData.PACKET_CODEC.encode(buf, data.templateData)

            }, {
                val instance = AntiWandBarrageLocusEmitters(Vec3.ZERO, null)
                decodeBase(instance, it)
                instance.templateData = ControlableParticleData.PACKET_CODEC.decode(it)
                instance
            }
        )
    }

    init {
        enableInterpolator = true
        emittersInterpolator.setRefiner(5.0)
    }

    override fun doTick() {
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        return listOf(templateData to RelativeLocation())
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: RelativeLocation,
        spawnWorld: Level,
        particleLerpProgress: Float,
        posLerpProgress: Float
    ) {
        controler.addPreTickAction {
            val ageProgress = currentAge.toFloat() / lifetime
            val colorStart = Math3DUtil.colorOf(255, 255, 255)
            val colorEnd = Math3DUtil.colorOf(204, 104, 138)
            this.color = GraphMathHelper.lerp(ageProgress, colorStart, colorEnd)
        }
    }

    override fun getEmittersID(): String {
        return ID
    }

    override fun getCodec(): StreamCodec<FriendlyByteBuf, ParticleEmitters> {
        return CODEC
    }
}