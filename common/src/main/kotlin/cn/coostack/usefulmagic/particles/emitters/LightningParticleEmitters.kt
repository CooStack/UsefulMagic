package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.extend.asRelative
import cn.coostack.cooparticlesapi.extend.minus
import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleCommandQueue
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleDragCommand
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleFlowFieldCommand
import cn.coostack.cooparticlesapi.network.particle.emitters.command.ParticleNoiseCommand
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import kotlin.math.roundToInt
import kotlin.random.Random

// 碎片
@CooAutoRegister
class LightningParticleEmitters(pos: Vec3, world: Level?) : AutoParticleEmitters(pos, world) {
    @CodecField
    var templateData = ControlableParticleData()

    // 相对位置
    @CodecField
    var targetPos: Vec3 = Vec3.ZERO

    @CodecField
    var simpleData = SimpleRandomParticleData()

    /**
     * 二分次数
     */
    @CodecField
    var subCount = 7 minRangeTo 9

    val command = ParticleCommandQueue()
        .add(
            ParticleFlowFieldCommand()
                .amplitude(0.08)
                .frequency(0.2)
                .timeScale(1.0)
                .phaseOffset(0.1)
                .worldOffset(Vec3(0.0, 0.0, 0.0))
        )
        .add(
            ParticleDragCommand()
                .damping(0.2)
                .linear(0.005)
                .minSpeed(0.01)
        )


    override fun doTick() {
        command.updateWithTypes<ParticleFlowFieldCommand> {
            worldOffset = this@LightningParticleEmitters.pos
        }
    }

    val options
        get() = ParticleOption.getParticleCounts()

    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        val count = options * simpleData.getRandomCount()
        val maxOffset = targetPos.length() * 1 / 5
        return PointsBuilder()
            .addLightningAttenuationPoints(
                targetPos.asRelative(),
                subCount.random(),
                maxOffset,
                0.4, count
            )
            .createWithoutClone().map {
                templateData.clone().apply {
                    this.size = simpleData.getRandomSize()
                    this.maxAge = simpleData.getRandomParticleMaxAge()
                } to it
            }
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
            command.applyVelocity(data, this)
        }
    }

}