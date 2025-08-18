package cn.coostack.usefulmagic.particles.group.client

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.control.group.ControlableParticleGroup
import cn.coostack.cooparticlesapi.particles.control.group.ControlableParticleGroupProvider
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.world.phys.Vec3
import java.util.Random
import java.util.UUID
import kotlin.math.PI

class GoldenBallBarrageParticleClient(
    val pColor: Vec3,
    val size: Float,
    val r: Double,
    val countPow: Int,
    uuid: UUID
) :
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
            return GoldenBallBarrageParticleClient(color, size, r, countPow, uuid)
        }

        override fun changeGroup(
            group: ControlableParticleGroup,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ) {
        }

    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    //    val helper = GroupScaleHelper(1.0 / 40, 1.0, 40)
    val random = Random(System.currentTimeMillis())
    override fun loadParticleLocations(): Map<ParticleRelativeData, RelativeLocation> {
        val res = HashMap<ParticleRelativeData, RelativeLocation>()
        res[
            withEffect({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(
                        it
                    ).appendBuilder(PointsBuilder().addDiscreteCircleXZ(4.0, 80 * options, 2.5)) {
                        withEffectEnchant()
                    }
                        .loadScaleHelper(1 / 40.0, 1.0, 40)
                        .toggleOnDisplay {
                            addPreTickAction {
                                rotateParticlesAsAxis(-PI / 18)
                            }
                        }
                )
            }) {}
        ] = RelativeLocation()
        res[
            withEffect({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(
                        it
                    ).appendBuilder(PointsBuilder().addBall(r, countPow * options / 2)) {
                        withEffectEndRod()
                    }
                        .loadScaleHelper(1 / 40.0, 1.0, 40)
                        .toggleOnDisplay {
                            addPreTickAction {
                                rotateParticlesAsAxis(PI / 36)
                            }
                        }
                )
            }) {}
        ] = RelativeLocation()

        res[
            withEffect({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(
                        it
                    ).appendBuilder(
                        PointsBuilder()
                            .addCircle(4.0, countPow * countPow * options)
                    ) {
                        withEffectEndRod()
                    }
                        .loadScaleHelper(1 / 40.0, 1.0, 40)
                        .toggleOnDisplay {
                            axis = RelativeLocation(1.0, 0.0, 0.0)
                            rotateParticlesToPoint(RelativeLocation(-1.0, 0.0, -1.0))
                            addPreTickAction {
                                rotateParticlesAsAxis(PI / 36)
                            }
                        }
                )
            }) {}
        ] = RelativeLocation()

        res[
            withEffect({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(
                        it
                    ).appendBuilder(
                        PointsBuilder()
                            .addCircle(4.0, countPow * countPow * options)
                    ) {
                        withEffectEndRod()
                    }
                        .loadScaleHelper(1 / 40.0, 1.0, 40)
                        .toggleOnDisplay {
                            axis = RelativeLocation(1.0, 0.0, 0.0)
                            this.rotateParticlesToPoint(RelativeLocation(1.0, -1.0, 1.0))
                            addPreTickAction {
                                rotateParticlesAsAxis(PI / 36)
                            }
                        }
                )
            }) {}
        ] = RelativeLocation()

        return res
    }

    private fun withEffectEnchant(): ParticleGroupStyle.StyleData {
        return ParticleGroupStyle.StyleData { it ->
            ParticleDisplayer.withSingle(
                ControlableEnchantmentEffect(it)
            )
        }.withParticleHandler {
            colorOfRGB(255, 100, 20)
            particleAlpha = 0.4f
            this.size = 0.3f
            this.currentAge = random.nextInt(0, lifetime)
            textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
        }.withParticleControlerHandler {
            var tick = true
            this@withParticleControlerHandler.addPreTickAction {
                if (this@withParticleControlerHandler.particle.particleAlpha >= 0.9f) {
                    tick = false
                }
                if (this@withParticleControlerHandler.particle.particleAlpha <= 0.2f) {
                    tick = true
                }
                this@withParticleControlerHandler.particle.particleAlpha += if (tick) 0.05f else -0.05f
            }
        }
    }

    private fun withEffectEndRod(): ParticleGroupStyle.StyleData {
        return ParticleGroupStyle.StyleData { it ->
            ParticleDisplayer.withSingle(
                ControlableEndRodEffect(it)
            )
        }.withParticleHandler {
            colorOfRGB(pColor.x.toInt(), pColor.y.toInt(), pColor.z.toInt())
            particleAlpha = 0.4f
            this.size = this.size
            this.currentAge = random.nextInt(0, lifetime)
            textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
        }.withParticleControlerHandler {
            var tick = true
            this@withParticleControlerHandler.addPreTickAction {
                if (this@withParticleControlerHandler.particle.particleAlpha >= 0.9f) {
                    tick = false
                }
                if (this@withParticleControlerHandler.particle.particleAlpha <= 0.2f) {
                    tick = true
                }
                this@withParticleControlerHandler.particle.particleAlpha += if (tick) 0.05f else -0.05f
            }
        }
    }

    override fun onGroupDisplay() {
    }
}