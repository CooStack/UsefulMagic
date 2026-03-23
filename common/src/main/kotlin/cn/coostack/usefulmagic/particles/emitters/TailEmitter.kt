package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleCommandQueue
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleDragCommand
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleNoiseCommand
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

@CooAutoRegister
class TailEmitter(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {

    @CodecField
    var tailTemplate = ControlableParticleData().apply {
        setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
    }

    @CodecField
    var simpleData = SimpleRandomParticleData().apply {
        minCount = 1
        maxCount = 4
    }

    @CodecField
    var leftColor = Vector3f(1f)

    @CodecField
    var rightColor = Vector3f(1f)


    val command = ParticleCommandQueue()
        .add(
            ParticleNoiseCommand()
                .strength(0.02)
                .clampSpeed(0.2)
                .speed(1.0)
        ).add(
            ParticleDragCommand()
                .damping(0.15)
                .linear(0.0)
                .minSpeed(0.01)
        )

    override fun doTick() {

    }

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val res = mutableListOf<Pair<ControlableParticleData, RelativeLocation>>()
        repeat(simpleData.getRandomCount()) {
            res.add(tailTemplate.clone().apply {
                maxAge = simpleData.getRandomParticleMaxAge()
                color = leftColor
            } to RelativeLocation().offsetRandomly(0.1))
        }
        return res
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
            val progress = this.currentAge.toFloat() / this.lifetime
            val color = GraphMathHelper.lerp(progress, leftColor, rightColor)
            this.color = color
            command.applyVelocity(data, this)
        }
    }
}