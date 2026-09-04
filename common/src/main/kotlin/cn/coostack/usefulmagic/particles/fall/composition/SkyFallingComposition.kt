package cn.coostack.usefulmagic.particles.fall.composition

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.cparticle.CParticleCurve
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionScaleHelper
import cn.coostack.cooparticlesapi.utils.presets.FourierPresets
import cn.coostack.usefulmagic.particles.fall.composition.client.MagicRingComposition
import cn.coostack.usefulmagic.particles.fall.composition.client.MagicRingWithMagicComposition
import cn.coostack.usefulmagic.particles.fall.composition.client.SkyFallingSub2Composition
import cn.coostack.usefulmagic.particles.fall.composition.client.SkyFallingSubComposition
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.Random
import java.util.SortedMap
import java.util.TreeMap
import java.util.UUID
import kotlin.math.PI
import kotlin.math.roundToInt

@CooAutoRegister
class SkyFallingComposition(position: Vec3 = Vec3.ZERO, world: Level? = null) :
    AutoSequencedParticleComposition(position, world) {
    @CodecField
    var bindPlayer: UUID = UUID.randomUUID()

    @CodecField
    var age = 0

    val scaleHelper = CompositionScaleHelper(0.01, 1.5, 30)
    val random = Random(System.currentTimeMillis())

    init {
        visibleRange = 256.0
        setDisabledInterval(15)
        scaleHelper.loadControler(this)
        animate
            .addAnimate(8) { age > 1 }
            .addAnimate(1) { age > 100 }
            .addAnimate(1) { age > 102 }
            .addAnimate(1) { age > 104 }
            .addAnimate(1) { age > 106 }
            .addAnimate(1) { age > 108 }
            .addAnimate(1) { age > 110 }
            .addAnimate(1) { age > 112 }
            .addAnimate(1) { age > 114 }
            .addAnimate(1) { age > 116 }
            .addAnimate(1) { age > 118 }
            .addAnimate(1) { age > 120 }
    }

    override fun getParticleSequenced(): SortedMap<CompositionData, RelativeLocation> {
        val result = TreeMap<CompositionData, RelativeLocation>()
        var order = 0

        result[
            CompositionData()
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        buildComposition(it)
                            .applyPoint(RelativeLocation()) {
                                CompositionData().setDisplayerSupplier {
                                    ParticleDisplayer.withComposition(
                                        buildComposition(it)
                                            .applyBuilder(
                                                PointsBuilder()
                                                    .addFourierSeries(
                                                        FourierPresets.circlesAndTriangles()
                                                            .scale(0.3)
                                                            .count(
                                                                180 * ParticleOption.getParticleCounts()
                                                            )
                                                    )
                                                    .addPolygonInCircle(
                                                        4,
                                                        15 * ParticleOption.getParticleCounts(),
                                                        4.0
                                                    )
                                                    .rotateAsAxis(PI / 4)
                                                    .addPolygonInCircle(
                                                        4,
                                                        15 * ParticleOption.getParticleCounts(),
                                                        4.0
                                                    )
                                            ) {
                                                buildSingleComposition()
                                            }
                                            .applyDisplayAction {
                                                addPreTickAction {
                                                    rotateAsAxis(-PI / 128)
                                                }
                                            }
                                    )
                                }
                            }
                            .applyPoint(RelativeLocation()) {
                                CompositionData().setDisplayerSupplier {
                                    ParticleDisplayer.withComposition(
                                        buildComposition(it)
                                            .applyBuilder(
                                                PointsBuilder().addCircle(
                                                    5.0,
                                                    30 * ParticleOption.getParticleCounts()
                                                )
                                            ) {
                                                buildSingleComposition()
                                                    .setDisplayerSupplier {
                                                        ParticleDisplayer.withCParticle(
                                                            it,
                                                            CParticleRenderLayer.TRANSLUCENT
                                                        )
                                                    }
                                                    .addCParticleInstanceInit {
                                                        effect = ControlableEnchantmentEffect(
                                                            UUID.randomUUID()
                                                        )
                                                        size = 0.3f
                                                        age = random.nextInt(0, maxAge)
                                                    }
                                            }
                                            .applyDisplayAction {
                                                addPreTickAction {
                                                    rotateAsAxis(PI / 64)
                                                }
                                            }
                                    )
                                }
                            }
                            .applyPoint(RelativeLocation()) {
                                CompositionData().setDisplayerSupplier {
                                    ParticleDisplayer.withComposition(
                                        buildComposition(it)
                                            .applyBuilder(
                                                PointsBuilder()
                                                    .addCircle(
                                                        4.0,
                                                        90 * ParticleOption.getParticleCounts()
                                                    )
                                                    .addCircle(
                                                        6.0,
                                                        120 * ParticleOption.getParticleCounts()
                                                    )
                                            ) {
                                                buildSingleComposition()
                                            }
                                            .applyDisplayAction {
                                                addPreTickAction {
                                                    rotateAsAxis(PI / 64)
                                                }
                                            }
                                    )
                                }
                            }
                    )
                }
                .apply { this.order = order++ }
        ] = RelativeLocation()

        result[
            CompositionData()
                .setDisplayerSupplier {
                    ParticleDisplayer.withComposition(
                        ParticleShapeComposition(it)
                            .applyBuilder(
                                PointsBuilder()
                                    .addPolygonInCircleVertices(4, 13.0)
                                    .pointsOnEach { location -> location.y += 9 }
                            ) { direction ->
                                CompositionData().setDisplayerSupplier {
                                    ParticleDisplayer.withComposition(
                                        SkyFallingSub2Composition(this).apply {
                                            controlUUID = it
                                            this.direction = direction
                                        }
                                    )
                                }
                            }
                            .applyDisplayAction {
                                addPreTickAction {
                                    rotateAsAxis(PI / 128)
                                }
                            }
                    )
                }
                .apply { this.order = order++ }
        ] = RelativeLocation()

        result[buildRingComposition(9.5, 10.0, 0.25, 0.5).withOrder(order++)] =
            RelativeLocation(0, 1, 0)
        result[buildRingComposition(14.0, 14.0, 0.5, 1.0).withOrder(order++)] =
            RelativeLocation(0.0, 5.0, 0.0)
        result[buildRingComposition(6.0, 6.0, 0.5, 1.0).withOrder(order++)] =
            RelativeLocation(0.0, 6.0, 0.0)
        result[
            buildRingComposition(11.0, 9.0, 0.25, 0.5, 0.3f).withOrder(order++)
        ] = RelativeLocation(0.0, 10.0, 0.0)
        result[
            buildRingComposition(6.5, 7.0, 0.0, 0.0, 0.1f, PI / 32)
                .withOrder(order++)
        ] = RelativeLocation(0.0, 12.0, 0.0)
        result[buildSubComposition(3.0).withOrder(order++)] = RelativeLocation(0.0, 13.0, 0.0)

        result[
            buildRingComposition(28.0, 30.0, 0.0, 0.0, 0.4f, -PI / 128)
                .withOrder(order++)
        ] = RelativeLocation(0.0, 4.0, 0.0)
        result[buildSubComposition(8.0).withOrder(order++)] = RelativeLocation(0.0, 15.0, 0.0)
        result[
            buildRingComposition(17.0, 19.0, 0.0, 0.0, 0.4f).withOrder(order++)
        ] = RelativeLocation(0.0, 18.0, 0.0)
        result[
            buildRingComposition(12.0, 14.0, 0.0, 0.0, 0.3f).withOrder(order++)
        ] = RelativeLocation(0.0, 16.0, 0.0)
        result[
            buildRingComposition(22.0, 24.0, 0.0, 0.0, 0.4f, PI / 64)
                .withOrder(order++)
        ] = RelativeLocation(0.0, 14.0, 0.0)
        result[
            buildRingComposition(36.0, 38.5, 0.0, 0.0, 0.2f, PI / 128)
                .withOrder(order++)
        ] = RelativeLocation(0.0, 21.0, 0.0)
        result[buildCircleComposition(34.0).withOrder(order++)] =
            RelativeLocation(0.0, 19.0, 0.0)
        result[
            buildRingComposition(18.0, 20.0, 0.0, 0.0, 0.3f, PI / 128)
                .withOrder(order++)
        ] = RelativeLocation(0.0, 23.0, 0.0)
        result[
            buildRingWithMagicComposition(44.0, 54.0, 5.0, 0.5f, -PI / 128)
                .withOrder(order++)
        ] = RelativeLocation(0.0, 14.0, 0.0)
        result[buildSubComposition(10.0).withOrder(order++)] = RelativeLocation(0.0, 25.0, 0.0)
        result[
            buildRingWithMagicComposition(16.0, 24.0, 4.0, 0.3f, -PI / 128)
                .withOrder(order)
        ] = RelativeLocation(0.0, 30.0, 0.0)

        return result
    }

    fun buildCircleComposition(radius: Double): CompositionData {
        return CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withComposition(
                buildComposition(it)
                    .applyBuilder(
                        PointsBuilder().addCircle(
                            radius,
                            (radius * 15 * ParticleOption.getParticleCounts()).roundToInt()
                        )
                    ) {
                        buildSingleComposition()
                    }
                    .applyDisplayAction {
                        addPreTickAction {
                            rotateAsAxis(PI / 128)
                        }
                    }
            )
        }
    }

    fun buildSubComposition(
        radius: Double,
        rotateSpeed: Double = PI / 64
    ): CompositionData {
        return CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withComposition(
                SkyFallingSubComposition(this).apply {
                    controlUUID = it
                    this.rotateSpeed = rotateSpeed
                    this.r = radius
                }
            )
        }
    }

    fun buildRingWithMagicComposition(
        firstRadius: Double,
        secondRadius: Double,
        subMagicRadius: Double,
        runeSize: Float = 0.2f,
        rotateSpeed: Double = PI / 64
    ): CompositionData {
        return CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withComposition(
                MagicRingWithMagicComposition(this).apply {
                    controlUUID = it
                    firstRingRadius = firstRadius
                    secondRingRadius = secondRadius
                    this.subMagicRadius = subMagicRadius
                    this.runeSize = runeSize
                    this.rotateSpeed = rotateSpeed
                }
            )
        }
    }

    fun buildRingComposition(
        firstRadius: Double,
        secondRadius: Double,
        runeOffset: Double,
        secondRingOffset: Double,
        runeSize: Float = 0.2f,
        rotateSpeed: Double = PI / 64
    ): CompositionData {
        return CompositionData().setDisplayerSupplier {
            ParticleDisplayer.withComposition(
                MagicRingComposition(this).apply {
                    controlUUID = it
                    firstRingRadius = firstRadius
                    secondRingRadius = secondRadius
                    secondRingYOffset = secondRingOffset
                    runeRingYOffset = runeOffset
                    this.runeSize = runeSize
                    this.rotateSpeed = rotateSpeed
                }
            )
        }
    }

    fun buildSingleComposition(): CompositionData {
        return CompositionData()
            .setDisplayerSupplier {
                ParticleDisplayer.withCParticle(it, CParticleRenderLayer.TRANSLUCENT)
            }
            .addCParticleInstanceInit {
                effect = ControlableEndRodEffect(UUID.randomUUID())
                color.set(100f / 255f, 200f / 255f, 1f)
            }
    }

    fun buildComposition(uuid: UUID, scaleTick: Int = 10): ParticleShapeComposition {
        return ParticleShapeComposition(uuid)
            .loadScaleHelper(0.01, 1.0, scaleTick)
            .applyDisplayAction {
                addPreTickAction {
                    if (this@SkyFallingComposition.status.displayStatus == 1) {
                        scaleHelper?.doScale()
                    }
                }
            }
    }

    override fun onDisplay() {
        var syncedAnimationIndex = animate.animationIndex
        addPreTickAction {
            if (client) {
                playCParticleAlphaTransition(
                    durationTicks = 10f,
                    alphaCurve = if (status.isEnable()) {
                        CParticleCurve.linear(0.1f, 1f)
                    } else {
                        CParticleCurve.linear(1f, 0.1f)
                    }
                )
            }
            if (age % 80 == 0) {
                world!!.playSound(
                    null,
                    position.x,
                    position.y,
                    position.z,
                    UsefulMagicSoundEvents.SKY_FALLING_MAGIC_IDLE.get(),
                    SoundSource.PLAYERS,
                    10f,
                    1f
                )
            }
            age++
            if (!client && syncedAnimationIndex != animate.animationIndex) {
                syncedAnimationIndex = animate.animationIndex
                markDirty()
            }
            if (age > 12 * 20) {
                status.setStatus(2)
            }
            if (status.displayStatus == 1 && scale <= 1.0) {
                scaleHelper.doScale()
            }
            if (age > 100 && status.displayStatus == 1) {
                scaleHelper.doScale()
                if (age % 2 == 0 && age < 120) {
                    world!!.playSound(
                        null,
                        position.x,
                        position.y,
                        position.z,
                        UsefulMagicSoundEvents.MAGIC_ACTIVATE.get(),
                        SoundSource.PLAYERS,
                        10f,
                        1f
                    )
                }
            }

            val player = world?.getPlayerByUUID(bindPlayer) ?: return@addPreTickAction
            teleportTo(player.position())
        }
    }

    private fun CompositionData.withOrder(order: Int): CompositionData {
        this.order = order
        return this
    }
}
