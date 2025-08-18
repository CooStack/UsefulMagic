package cn.coostack.usefulmagic.particles.group.client

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.control.group.ControlableParticleGroup
import cn.coostack.cooparticlesapi.particles.control.group.ControlableParticleGroupProvider
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.world.phys.Vec3
import java.util.Random
import java.util.UUID
import kotlin.math.PI

class EnchantBallBarrageParticleClient(val pColor: Vec3, val size: Float, val r: Double, val countPow: Int, uuid: UUID) :
    ControlableParticleGroup(uuid) {
    class Provider : ControlableParticleGroupProvider {
        override fun createGroup(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ControlableParticleGroup {
            val r = args["r"]!!.loadedValue as Double
            val countPow = args["count_pow"]!!.loadedValue as Int
            val color = args["color"]!!.loadedValue as Vec3
            val size = args["size_particle"]!!.loadedValue as Float
            return EnchantBallBarrageParticleClient(color, size, r, countPow, uuid)
        }

        override fun changeGroup(
            group: ControlableParticleGroup,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ) {
        }

    }
    val options: Int
        get() = ParticleOption.getParticleCounts()
    val random = Random(System.currentTimeMillis())
    override fun loadParticleLocations(): Map<ParticleRelativeData, RelativeLocation> {
        return PointsBuilder().addBall(r, countPow * options / 2).createWithParticleEffects {
            withEffect(
                {
                    ParticleDisplayer.withSingle(
                        ControlableEnchantmentEffect(it)
                    )
                }
            ) {
                colorOfRGB(pColor.x.toInt(), pColor.y.toInt(), pColor.z.toInt())
                particleAlpha = 0.4f
                this.size = this@EnchantBallBarrageParticleClient.size
                this.currentAge = random.nextInt(0, lifetime)
                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
            }.withControler {
                var tick = true
                it.addPreTickAction {
                    if (it.particle.particleAlpha >= 0.9f) {
                        tick = false
                    }
                    if (it.particle.particleAlpha <= 0.2f) {
                        tick = true
                    }
                    it.particle.particleAlpha += if (tick) 0.05f else -0.05f
                }
            }
        }
    }


    override fun onGroupDisplay() {
        addPreTickAction {
            rotateParticlesAsAxis(PI / 36)
        }
    }
}