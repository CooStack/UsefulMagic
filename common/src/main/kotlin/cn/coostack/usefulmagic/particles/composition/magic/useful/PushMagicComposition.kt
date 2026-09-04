package cn.coostack.usefulmagic.particles.composition.magic.useful

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.SequencedParticleShapeComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableSmallGustEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionAlphaHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import java.util.*
import kotlin.math.PI
import kotlin.random.Random

@CooAutoRegister
class PushMagicComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    @CodecField
    var direction: Vec3 = Vec3(0.0, 1.0, 0.0)

    private val alphaHelper = CompositionAlphaHelper(0.0, 1.0, 10)
    init {
        axis = RelativeLocation.yAxis()
        alphaHelper.loadControler(this)
        setDisabledInterval(10)
    }
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val result = LinkedHashMap<CompositionData, RelativeLocation>()

        // circle
        result[
            CompositionData()
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        SequencedParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(2.0, 120)
                            ) { shapeRel1, order ->
                                CompositionData().apply { this.order = order }
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withCParticle(it, CParticleRenderLayer.ADDITION_BLEND_TRANSLUCENT)
                                    }
                                    .addCParticleInstanceInit {
                                        effect = ControlableSmallGustEffect(UUID.randomUUID())
                                        size = 0.2F
                                        age = Random.nextInt(maxAge)
                                    }
                            }
                            addPreTickAction {
                                if (this@PushMagicComposition.status.isDisable()) {
                                    playCParticleAlphaTransition(
                                        durationTicks = 10.0f,
                                        alphaCurve = CParticleCurve.linear(1.0f, 0.0f)
                                    )
                                } else {
                                    playCParticleAlphaTransition(
                                        durationTicks = 10.0f,
                                        alphaCurve = CParticleCurve.linear(0.0f, 1.0f)
                                    )
                                }
                            }
                            applyDisplayAction {
                                addPreTickAction {
                                    addMultiple(10)
                                    rotateToWithAngle(direction.asRelative(), PI / 64)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 0.0, 0.0)

        // 卡片 2
        result[
            CompositionData()
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        SequencedParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addPolygonInCircle(3, 60, 2.0)
                                    .addBuilder(RelativeLocation(0.0, 0.0, 0.0),
                                        PointsBuilder()
                                            .addPolygonInCircle(3, 60, 2.0)
                                            .rotateAsAxis(0.333333*PI)
                                    )
                            ) { shapeRel1, order ->
                                CompositionData().apply { this.order = order }
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withCParticle(it, CParticleRenderLayer.ADDITION_BLEND_TRANSLUCENT)
                                    }
                                    .addCParticleInstanceInit {
                                        effect = ControlableEnchantmentEffect(UUID.randomUUID())
                                        size = 0.2F
                                        age = Random.nextInt(maxAge)
                                    }
                            }
                            addPreTickAction {
                                if (this@PushMagicComposition.status.isDisable()) {
                                    playCParticleAlphaTransition(
                                        durationTicks = 10.0f,
                                        alphaCurve = CParticleCurve.linear(1.0f, 0.0f)
                                    )
                                } else {
                                    playCParticleAlphaTransition(
                                        durationTicks = 10.0f,
                                        alphaCurve = CParticleCurve.linear(0.0f, 1.0f)
                                    )
                                }
                            }
                            applyDisplayAction {
                                addPreTickAction {
                                    addMultiple(15)
                                    rotateToWithAngle(direction.asRelative(), PI / 64)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 0.0, 0.0)

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
            if (status.isDisable()) {
                alphaHelper.decreaseAlpha()
            } else {
                alphaHelper.increaseAlpha()
            }
        }
    }
}