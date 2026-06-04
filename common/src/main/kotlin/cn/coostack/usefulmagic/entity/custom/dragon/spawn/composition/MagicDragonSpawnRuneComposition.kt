package cn.coostack.usefulmagic.entity.custom.dragon.spawn.composition

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
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
class MagicDragonSpawnRuneComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    @CodecField
    var color: Vector3f = Vector3f(0.964706F, 0.545098F, 0.992157F)

    private val scaleHelper = CompositionBezierScaleHelper(
        10,
        0.01,
        1.0,
        RelativeLocation(1.004515, 1.083232, 0.0),
        RelativeLocation(0.936795, 1.19193, 0.0)
    )

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

        result[
            CompositionData()
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addDiscreteCircleXZ(12.0, 240, 3.5)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        size = 1.0F
                                        currentAge = Random.nextInt(lifetime)
                                        color = this@MagicDragonSpawnRuneComposition.color
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                    }
                            }
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateAsAxis(PI / 128)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 8.0, 0.0)

        result[
            CompositionData()
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addDiscreteCircleXZ(24.0, 240, 3.5)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        size = 1.0F
                                        currentAge = Random.nextInt(lifetime)
                                        color = this@MagicDragonSpawnRuneComposition.color
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                    }
                            }
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateAsAxis(PI / 128)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 64.0, 0.0)

        result[
            CompositionData()
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addDiscreteCircleXZ(72.0, 720, 16.0)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEnchantmentEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                        color = this@MagicDragonSpawnRuneComposition.color
                                        size = 1.5F
                                        currentAge = Random.nextInt(lifetime)
                                    }
                            }
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateAsAxis(-PI / 256)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 32.0, 0.0)

        result[
            CompositionData()
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it).apply {
                            axis = RelativeLocation.yAxis()
                            applyBuilder(
                                PointsBuilder()
                                    .addCircle(2.0, 120)
                                    .applyNoiseOffset(120.0, 80.0, 120.0)
                            ) { shapeRel1 ->
                                CompositionData()
                                    .setDisplayerSupplier {
                                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                                    }
                                    .addParticleInstanceInit {
                                        size = (Random.nextFloat() * 0.6 + 0.4).toFloat()
                                        color = this@MagicDragonSpawnRuneComposition.color
                                        currentAge = Random.nextInt(lifetime)
                                        textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                                    }
                            }
                            loadScaleHelperBezierValue(
                                0.01,
                                1.0,
                                10,
                                RelativeLocation(0.553047, 1.399902, 0.0),
                                RelativeLocation(-9.593679, 0.275922, 0.0)
                            )
                            applyDisplayAction {
                                addPreTickAction {
                                    rotateAsAxis(PI / 512)
                                }
                            }
                        }
                    )
                }
        ] = RelativeLocation(0.0, 48.0, 0.0)

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
        }
    }
}