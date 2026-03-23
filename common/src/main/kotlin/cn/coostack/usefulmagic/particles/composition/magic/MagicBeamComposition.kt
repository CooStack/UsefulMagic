package cn.coostack.usefulmagic.particles.composition.magic

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.CooParticleTextureSheet
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionAlphaHelper
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionBezierScaleHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.random.Random

@CooAutoRegister
class MagicBeamComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    @CodecField
    var direction: RelativeLocation = RelativeLocation(0.0, 0.0, 1.0)

    @CodecField
    var color: Vector3f = Vector3f(0.615686F, 0.360784F, 1F)

    private val scaleHelper = CompositionBezierScaleHelper(10, 0.01, 1.0, RelativeLocation(5.609481, 1.082393, 0.0), RelativeLocation(1.670429, 1.250484, 0.0))

    private val alphaHelper = CompositionAlphaHelper(0.0, 1.0, 10)
    init {
        axis = RelativeLocation.yAxis()
        scaleHelper.loadControler(this)
        alphaHelper.loadControler(this)
        alphaHelper.resetAlphaMax()
        setDisabledInterval(10)
    }
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val result = LinkedHashMap<CompositionData, RelativeLocation>()

        result.putAll(
            PointsBuilder()
                .addCircle(7.0, 240)
                .addCircle(8.0, 240)
                .addPolygonInCircle(6, 80, 7.0)
                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                    PointsBuilder()
                        .addPolygonInCircle(6, 80, 7.0)
                        .rotateAsAxis(0.166667*PI, RelativeLocation(0.0, 1.0, 0.0))
                )
                .addWith {
                    val res = arrayListOf<RelativeLocation>()
                    getPolygonInCircleVertices(6, 4.0)
                        .forEach { it ->
                            val p = PointsBuilder()
                                .addCircle(1.2, 80)
                                .axis(RelativeLocation(0.0, 0.0, 1.0))
                            p.rotateTo(-it)
                            res.addAll(p
                                .pointsOnEach { rel -> rel.add(it) }
                                .createWithoutClone()
                            )
                        }
                    res
                }
                .addPolygonInCircle(6, 40, 4.0)
                .clearAsRoundXZMask(RelativeLocation(4.0, 0.0, 0.0), 1.14, -1.0)
                .clearAsRoundXZMask(RelativeLocation(2.0, 0.0, 3.464102), 1.14, -1.0)
                .clearAsRoundXZMask(RelativeLocation(-2.0, 0.0, 3.464102), 1.14, -1.0)
                .clearAsRoundXZMask(RelativeLocation(-4.0, 0.0, 0.0), 1.14, -1.0)
                .clearAsRoundXZMask(RelativeLocation(-2.0, 0.0, -3.464102), 1.14, -1.0)
                .clearAsRoundXZMask(RelativeLocation(2.0, 0.0, -3.464102), 1.14, -1.0)
                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                    PointsBuilder()
                        .addBuilder(RelativeLocation(4.0, 0.0, 0.0),
                            PointsBuilder()
                                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                    PointsBuilder()
                                        .addLine(RelativeLocation(0.375, 0.0, 0.75), RelativeLocation(-0.375, 0.0, 0.75), 24)
                                        .addLine(RelativeLocation(0.0, 0.0, 1.5), RelativeLocation(0.0, 0.0, -0.625), 24)
                                        .addLine(RelativeLocation(0.375, 0.0, -0.25), RelativeLocation(0.0, 0.0, -0.625), 24)
                                        .addLine(RelativeLocation(-0.375, 0.0, -0.25), RelativeLocation(0.0, 0.0, -0.625), 24)
                                        .scale(0.7)
                                )
                                .pointsOnEach { it.add(0.0, 0.0, -0.375) }
                                .axis(RelativeLocation(0.0, 0.0, 1.0))
                                .rotateTo(RelativeLocation(4.0, 0.0, 0.0))
                        )
                        .addBuilder(RelativeLocation(2.0, 0.0, 3.464102),
                            PointsBuilder()
                                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                    PointsBuilder()
                                        .addLine(RelativeLocation(0.25, 0.0, 0.5625), RelativeLocation(-0.25, 0.0, 0.5625), 12)
                                        .addLine(RelativeLocation(0.625, 0.0, 0.0625), RelativeLocation(-0.625, 0.0, 0.0625), 12)
                                        .addLine(RelativeLocation(0.0, 0.0, 1.1875), RelativeLocation(0.0, 0.0, -1.0625), 12)
                                        .addLine(RelativeLocation(0.5, 0.0, -0.5625), RelativeLocation(-0.5, 0.0, -0.8125), 12)
                                        .scale(0.7)
                                )
                                .pointsOnEach { it.add(0.0, 0.0, 0.0) }
                                .axis(RelativeLocation(0.0, 0.0, 1.0))
                                .rotateTo(RelativeLocation(2.0, 0.0, 3.464102))
                        )
                        .addBuilder(RelativeLocation(-2.0, 0.0, 3.464102),
                            PointsBuilder()
                                .addBuilder(RelativeLocation(-0.022826, 0.0, -0.295652),
                                    PointsBuilder()
                                        .addBezierCurve(RelativeLocation(0.5, 0.0, 1.0), RelativeLocation(-0.75, 0.0, -0.75), RelativeLocation(0.5, 0.0, -0.625), RelativeLocation(-0.125, 0.0, 1.0), 24)
                                        .addBezierCurve(RelativeLocation(-0.75, 0.0, -0.75), RelativeLocation(0.375, 0.0, -0.5), RelativeLocation(0.0, 0.0, -2.0), RelativeLocation(1.25, 0.0, -0.875), 24)
                                        .scale(0.6)
                                )
                                .pointsOnEach { it.add(0.0, 0.0, 0.625) }
                                .axis(RelativeLocation(0.0, 0.0, 1.0))
                                .rotateTo(RelativeLocation(-2.0, 0.0, 3.464102))
                        )
                        .addBuilder(RelativeLocation(-4.0, 0.0, 0.0),
                            PointsBuilder()
                                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                    PointsBuilder()
                                        .addLine(RelativeLocation(0.0, 0.0, 1.0), RelativeLocation(0.0, 0.0, -1.0), 12)
                                        .addLine(RelativeLocation(0.0, 0.0, 1.0), RelativeLocation(-0.75, 0.0, 0.5), 12)
                                        .addLine(RelativeLocation(-0.75, 0.0, 0.5), RelativeLocation(0.0, 0.0, 0.0), 12)
                                        .addLine(RelativeLocation(0.0, 0.0, 0.0), RelativeLocation(-0.75, 0.0, -0.625), 12)
                                        .addLine(RelativeLocation(-0.75, 0.0, -0.625), RelativeLocation(0.0, 0.0, -1.0), 12)
                                        .scale(0.7)
                                )
                                .pointsOnEach { it.add(0.0, 0.0, 0.0) }
                                .axis(RelativeLocation(0.0, 0.0, 1.0))
                                .rotateTo(RelativeLocation(-4.0, 0.0, 0.0))
                        )
                        .addBuilder(RelativeLocation(-2.0, 0.0, -3.464102),
                            PointsBuilder()
                                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                    PointsBuilder()
                                        .addLine(RelativeLocation(0.5, 0.0, -1.0), RelativeLocation(0.0, 0.0, 0.0), 12)
                                        .addLine(RelativeLocation(-0.5, 0.0, -1.0), RelativeLocation(0.0, 0.0, 0.0), 12)
                                        .addLine(RelativeLocation(0.5, 0.0, 1.0), RelativeLocation(0.0, 0.0, 0.0), 12)
                                        .addLine(RelativeLocation(-0.5, 0.0, 1.0), RelativeLocation(0.0, 0.0, 0.0), 12)
                                        .addLine(RelativeLocation(0.5, 0.0, 1.0), RelativeLocation(0.5, 0.0, -1.0), 12)
                                        .addLine(RelativeLocation(-0.5, 0.0, 1.0), RelativeLocation(-0.5, 0.0, -1.0), 12)
                                        .scale(0.7)
                                )
                                .pointsOnEach { it.add(0.0, 0.0, 0.0) }
                                .axis(RelativeLocation(0.0, 0.0, 1.0))
                                .rotateTo(RelativeLocation(-2.0, 0.0, -3.464102))
                        )
                        .addBuilder(RelativeLocation(1.804222, 0.0, -3.276459),
                            PointsBuilder()
                                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                    PointsBuilder()
                                        .addBezierCurve(RelativeLocation(0.375, 0.0, 0.0), RelativeLocation(-0.625, 0.0, 1.0), RelativeLocation(-0.5, 0.0, -1.375), RelativeLocation(0.75, 0.0, 0.125), 24)
                                        .addLine(RelativeLocation(0.125, 0.0, 1.0), RelativeLocation(-0.5, 0.0, 0.375), 12)
                                        .scale(0.7)
                                )
                                .pointsOnEach { it.add(0.0, 0.0, 0.0) }
                                .axis(RelativeLocation(0.0, 0.0, 1.0))
                                .rotateTo(RelativeLocation(2.0, 0.0, -3.464102))
                        )
                )
                .addLine(RelativeLocation(0.0, 0.0, 2.0), RelativeLocation(0.0, 0.0, -2.0), 30)
                .addBezierCurve(RelativeLocation(2.0, 0.0, 0.0), RelativeLocation(0.0, 0.0, 2.0), RelativeLocation(0.0, 0.0, 1.625), RelativeLocation(1.0, 0.0, -2.75), 100)
                .addBezierCurve(RelativeLocation(-2.0, 0.0, 0.0), RelativeLocation(0.0, 0.0, 2.0), RelativeLocation(0.0, 0.0, 1.625), RelativeLocation(-1.0, 0.0, -2.75), 100)
                .addBezierCurve(RelativeLocation(2.0, 0.0, 0.0), RelativeLocation(0.0, 0.0, -1.0), RelativeLocation(-2.0, 0.0, 3.375), RelativeLocation(2.0, 0.0, -1.0), 100)
                .addBezierCurve(RelativeLocation(-2.0, 0.0, 0.0), RelativeLocation(0.0, 0.0, -1.0), RelativeLocation(2.0, 0.0, 3.375), RelativeLocation(-2.0, 0.0, -1.0), 100)
                .createWithCompositionData { rel ->
                    CompositionData()
                        .setDisplayerSupplier {
                            ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                        }
                        .addParticleInstanceInit {
                            size = 0.3F
                            color = this@MagicBeamComposition.color
                            textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                        }
                }
        )

        result.putAll(
            PointsBuilder()
                .addCircle(7.5, 40)
                .createWithCompositionData { rel ->
                    CompositionData()
                        .setDisplayerSupplier {
                            ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
                        }
                        .addParticleInstanceInit {
                            size = 0.5F
                            color = this@MagicBeamComposition.color
                            currentAge = Random.nextInt(lifetime)
                            textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                        }
                }
        )

        return result
    }
    override fun remove() {
        if (status.isDisable()) {
            super.remove()
        } else {
            status.disable()
        }
    }
    override fun onDisplay() {
        addPreTickAction {
            scaleHelper.doScale()
            if (status.isDisable()) {
                alphaHelper.decreaseAlpha()
            } else {
                alphaHelper.increaseAlpha()
            }
            rotateToWithAngle(direction, 0.031831*PI)
        }
    }
}