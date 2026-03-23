package cn.coostack.usefulmagic.particles.emitters.entity.eye

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.extend.asRelative
import cn.coostack.cooparticlesapi.extend.random
import cn.coostack.cooparticlesapi.extend.times
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.particles.particle.WaveParticleEffect
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.random.Random

@CooAutoRegister
class EyeExclaimEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    @CodecField
    var exclaimCenter = ControlableParticleData()
        .apply {
            effect = WaveParticleEffect(uuid)
            size = 0f
            alpha = 1f
            setTextureSheet(TextureSheetsEnum.PARTICLE_SHEET_TRANSLUCENT)
        }


    override fun doTick() {
    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        return listOf(
            exclaimCenter to RelativeLocation().add(
                (Vec3.ZERO.random() * Random.nextDouble(0.4)).asRelative()
            ),
        )
    }


    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: RelativeLocation,
        spawnWorld: Level,
        particleLerpProgress: Float,
        posLerpProgress: Float
    ) {
        // alpha 从1 -> 0
        // size 一直递增
        controler.addPreTickAction {
            val progress = currentAge.toDouble() / lifetime
            particleAlpha = GraphMathHelper.lerp(progress, 1f, 0.1f)
            size = GraphMathHelper.lerp(progress, 0f, 100f)
        }

    }
}