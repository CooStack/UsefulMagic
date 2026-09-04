package cn.coostack.usefulmagic.entity.custom.dragon.skills.composition

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionBezierScaleHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.UUID
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.PI
import kotlin.random.Random

@CooAutoRegister
class MagicConjureEyeComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    @CodecField
    var color: Vector3f = Vector3f(0.517647F, 0.12549F, 0.996078F)

    @CodecField
    var direction: Vec3 = Vec3(0.0, 1.0, 0.0)

    private val scaleHelper = CompositionBezierScaleHelper(10, 0.01, 1.0, RelativeLocation(4.492099, 1.346079, 0.0), RelativeLocation(4.920993, 1.444882, 0.0))
    private var alphaProgressTicks = 0

    init {
        axis = RelativeLocation(0.0, 1.0, 0.0)
        scaleHelper.loadControler(this)
        setDisabledInterval(10)
    }
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val result = LinkedHashMap<CompositionData, RelativeLocation>()

        result.putAll(
            PointsBuilder()
                .addCircle(4.0, 240)
                .addPolygonInCircle(6, 35, 4.0)
                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                    PointsBuilder()
                        .addPolygonInCircle(6, 35, 4.0)
                        .rotateAsAxis(-0.166667*PI, RelativeLocation(0.0, 1.0, 0.0))
                )
                .addBezierCurve(RelativeLocation(0.0, 0.0, 1.0), RelativeLocation(2.0, 0.0, 0.0), RelativeLocation(1.125, 0.0, -0.25), RelativeLocation(-0.875, 0.0, 0.75), 150)
                .addBezierCurve(RelativeLocation(0.0, 0.0, -1.0), RelativeLocation(2.0, 0.0, 0.0), RelativeLocation(1.125, 0.0, 0.25), RelativeLocation(-0.875, 0.0, -0.75), 150)
                .addBezierCurve(RelativeLocation(0.0, 0.0, 1.0), RelativeLocation(-2.0, 0.0, 0.0), RelativeLocation(-1.125, 0.0, -0.25), RelativeLocation(0.875, 0.0, 0.75), 150)
                .addBezierCurve(RelativeLocation(0.0, 0.0, -1.0), RelativeLocation(-2.0, 0.0, 0.0), RelativeLocation(-1.125, 0.0, 0.25), RelativeLocation(0.875, 0.0, -0.75), 150)
                .addLine(RelativeLocation(1.0, 0.0, 0.0), RelativeLocation(-1.0, 0.0, 0.0), 30)
                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                    PointsBuilder()
                        .addBuilder(RelativeLocation(2.8, 0.0, 0.0),
                            PointsBuilder()
                                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                    PointsBuilder()
                                        .addLine(RelativeLocation(0.25, 0.0, 1.0), RelativeLocation(-0.25, 0.0, 1.0), 5)
                                        .addLine(RelativeLocation(0.25, 0.0, -1.0), RelativeLocation(-0.25, 0.0, -1.0), 5)
                                        .addLine(RelativeLocation(0.0, 0.0, -1.0), RelativeLocation(0.0, 0.0, 1.0), 5)
                                        .scale(0.25)
                                )
                                .pointsOnEach { it.add(0.0, 0.0, 0.0) }
                                .axis(RelativeLocation(0.0, 0.0, 1.0))
                                .rotateTo(RelativeLocation(-2.8, 0.0, 0.0))
                        )
                        .addBuilder(RelativeLocation(0.865248, 0.0, 2.662958),
                            PointsBuilder()
                                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                    PointsBuilder()
                                        .addLine(RelativeLocation(0.25, 0.0, 1.0), RelativeLocation(-0.25, 0.0, 1.0), 5)
                                        .addLine(RelativeLocation(0.25, 0.0, -1.0), RelativeLocation(-0.25, 0.0, -1.0), 5)
                                        .addLine(RelativeLocation(0.125, 0.0, -1.0), RelativeLocation(0.125, 0.0, 1.0), 5)
                                        .addLine(RelativeLocation(-0.125, 0.0, -1.0), RelativeLocation(-0.125, 0.0, 1.0), 5)
                                        .scale(0.25)
                                )
                                .pointsOnEach { it.add(0.0, 0.0, 0.0) }
                                .axis(RelativeLocation(0.0, 0.0, 1.0))
                                .rotateTo(RelativeLocation(-0.865248, 0.0, -2.662958))
                        )
                        .addBuilder(RelativeLocation(-2.265248, 0.0, 1.645799),
                            PointsBuilder()
                                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                    PointsBuilder()
                                        .addLine(RelativeLocation(0.625, 0.0, 1.0), RelativeLocation(-0.625, 0.0, 1.0), 5)
                                        .addLine(RelativeLocation(0.625, 0.0, -1.0), RelativeLocation(-0.625, 0.0, -1.0), 5)
                                        .addLine(RelativeLocation(0.375, 0.0, -1.0), RelativeLocation(0.375, 0.0, 1.0), 5)
                                        .addLine(RelativeLocation(-0.375, 0.0, -1.0), RelativeLocation(-0.375, 0.0, 1.0), 5)
                                        .addLine(RelativeLocation(0.0, 0.0, -1.0), RelativeLocation(0.0, 0.0, 1.0), 5)
                                        .scale(0.25)
                                )
                                .pointsOnEach { it.add(0.0, 0.0, 0.0) }
                                .axis(RelativeLocation(0.0, 0.0, 1.0))
                                .rotateTo(RelativeLocation(2.265248, 0.0, -1.645799))
                        )
                        .addBuilder(RelativeLocation(-2.265248, 0.0, -1.645799),
                            PointsBuilder()
                                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                    PointsBuilder()
                                        .addLine(RelativeLocation(0.03125, 0.0, -1.0625), RelativeLocation(-0.59375, 0.0, 1.0625), 5)
                                        .addLine(RelativeLocation(0.53125, 0.0, 1.0625), RelativeLocation(0.03125, 0.0, -1.0625), 5)
                                        .scale(0.25)
                                )
                                .pointsOnEach { it.add(0.0, 0.0, 0.0) }
                                .axis(RelativeLocation(0.0, 0.0, 1.0))
                                .rotateTo(RelativeLocation(2.265248, 0.0, 1.645799))
                        )
                        .addBuilder(RelativeLocation(0.865248, 0.0, -2.662958),
                            PointsBuilder()
                                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                    PointsBuilder()
                                        .addLine(RelativeLocation(0.5, 0.0, 1.0), RelativeLocation(-0.5, 0.0, -1.0), 5)
                                        .addLine(RelativeLocation(0.5, 0.0, -1.0), RelativeLocation(-0.5, 0.0, 1.0), 5)
                                        .scale(0.25)
                                )
                                .pointsOnEach { it.add(0.0, 0.0, 0.0) }
                                .axis(RelativeLocation(0.0, 0.0, 1.0))
                                .rotateTo(RelativeLocation(-0.865248, 0.0, 2.662958))
                        )
                )
                .addCircle(12.0, 360)
                .addCircle(10.0, 360)
                .addPolygonInCircle(3, 120, 10.0)
                .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                    PointsBuilder()
                        .addPolygonInCircle(3, 120, 10.0)
                        .rotateAsAxis(-0.333333*PI, RelativeLocation(0.0, 1.0, 0.0))
                )
                .createWithCompositionData { rel ->
                    CompositionData()
                        .setDisplayerSupplier {
                            ParticleDisplayer.withCParticle(it)
                        }
                        .addCParticleInstanceInit {
                            effect = ControlableEndRodEffect(UUID.randomUUID())
                            size = 0.2F
                            color = this@MagicConjureEyeComposition.color
                        }
                }
        )

        result.putAll(
            PointsBuilder()
                .addCircle(11.0, 80)
                .createWithCompositionData { rel ->
                    CompositionData()
                        .setDisplayerSupplier {
                            ParticleDisplayer.withCParticle(it)
                        }
                        .addCParticleInstanceInit {
                            effect = ControlableEnchantmentEffect(UUID.randomUUID())
                            size = 0.8F
                            color = this@MagicConjureEyeComposition.color
                            age = Random.nextInt(maxAge)
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
                playCParticleAlphaTransition(
                    alphaProgressTicks.coerceAtLeast(1).toFloat(),
                    CParticleCurve.linear(alphaProgressTicks / 10f, 0f)
                )
            } else {
                alphaProgressTicks = (alphaProgressTicks + 1).coerceAtMost(10)
                playCParticleAlphaTransition(10f, CParticleCurve.linear(0f, 1f))
            }
            rotateToWithAngle(direction.asRelative(), 0.025465*PI)
        }
    }
}
