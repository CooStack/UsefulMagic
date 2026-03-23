package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

@CooAutoRegister
class StarryMeteoriteLocusEmitters(pos: Vec3, world: Level?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()

    companion object {
        const val ID = "starry_meteorite_locus_emitters"

        @JvmStatic
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, ParticleEmitters> = StreamCodec.of<RegistryFriendlyByteBuf, ParticleEmitters>(
            { buf, data ->
                data as StarryMeteoriteLocusEmitters
                encodeBase(data, buf)
                ControlableParticleData.PACKET_CODEC.encode(buf, data.templateData)

            }, {
                val instance = StarryMeteoriteLocusEmitters(Vec3.ZERO, null)
                decodeBase(instance, it)
                instance.templateData = ControlableParticleData.PACKET_CODEC.decode(it)
                instance
            }
        )
    }

    init {
        enableInterpolator = true
        emittersInterpolator.setRefiner(4.0)
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
            val progress = currentAge.toDouble() / lifetime
//            val progress = posLerpProgress.toDouble()
            val colorStart = Math3DUtil.colorOf(255, 0, 0)
            val colorEnd = Math3DUtil.colorOf(255, 104, 138)
            val blue = Vector3f(100 / 255f, 0f, 1f)
            val current = with(GraphMathHelper) {
                mix(lerp(progress, colorStart, colorEnd), blue, progress)
            }
            this.color = current
        }
    }

    override fun getEmittersID(): String {
        return ID
    }

    override fun getCodec(): StreamCodec<RegistryFriendlyByteBuf, ParticleEmitters> {
        return CODEC
    }
}
