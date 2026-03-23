package cn.coostack.usefulmagic.particles.composition.explosion

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.particles.CooParticleTextureSheet
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.Minecraft
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.extend.*
import java.util.*
import kotlin.math.PI

@CooAutoRegister
class ExplosionStarComposition(
    position: Vec3,
    world: Level?
) : AutoSequencedParticleComposition(position, world) {

    @CodecField
    var age = 0
    val maxAge = 20

    init {
        setDisabledInterval(8)
    }

    override fun remove() {
        if (status.isDisable()) {
            super.remove()
        } else {
            status.disable()
        }
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    val random = Random(System.currentTimeMillis())
    override fun getParticleSequenced(): SortedMap<CompositionData, RelativeLocation> {
        val res = TreeMap<CompositionData, RelativeLocation>()
        var order = 1
        res[
            CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    ParticleShapeComposition(it)
                        .applyBuilder(
                            PointsBuilder()
                                .addLine(
                                    RelativeLocation(-0.5, 0.0, 0.0),
                                    RelativeLocation(0.5, 0.0, 0.0),
                                    options * 5
                                )
                                .addLine(
                                    RelativeLocation(0.0, 0.35, 0.0),
                                    RelativeLocation(0.0, -0.35, 0.0),
                                    options * 5
                                )
                        ) { it ->
                            CompositionData().setDisplayerSupplier { it ->
                                ParticleDisplayer.withSingle(
                                    ControlableFireworkEffect(it)
                                )
                            }.addParticleInstanceInit {
                                this.size = 0.1f
                                colorOfRGB(
                                    random.nextInt(170, 255),
                                    random.nextInt(180, 255),
                                    random.nextInt(230, 255),
                                )
                                textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                            }
                        }
                        .applyDisplayAction {
                            this.axis = RelativeLocation.zAxis()
                            var subAge = 0

                            addPreTickAction {
                                subAge++
                                if (subAge >= 10) {
                                    scaleReversed = false
                                }
                                val cameraRotation = Minecraft.getInstance().gameRenderer.mainCamera.rotation()
                                val forward = Vector3f(0f, 0f, -1f)
                                forward.rotate(cameraRotation)
                                rotateToWithAngle(forward.asRelative(), PI / 8)
                                rotateToPoint(forward.asRelative())
                            }
                            setReversedScaleOnCompositionStatus(this@ExplosionStarComposition)
                        }
                        .loadScaleHelperBezierValue(
                            0.01, 1.0, 10,
                            RelativeLocation(5.0, 0.99, 0.0),
                            RelativeLocation(-5.0, 0.0, 0.0),
                        )
                )
            }.apply { this.order = order++ }] = RelativeLocation(0.0, 0.01, 0.0)
        res[
            CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    ParticleShapeComposition(it)
                        .applyBuilder(
                            PointsBuilder()
                                .addCircle(
                                    0.4, 10 * options
                                )
                        ) { it ->
                            CompositionData().setDisplayerSupplier { it ->
                                ParticleDisplayer.withSingle(
                                    ControlableFireworkEffect(it)
                                )
                            }.addParticleInstanceInit {
                                colorOfRGB(
                                    random.nextInt(230, 255),
                                    random.nextInt(130, 180),
                                    random.nextInt(130, 180),
                                )
                                textureSheet = CooParticleTextureSheet.ADDITION_BLEND_TRANSLUCENT
                            }
                        }
                        .applyDisplayAction {
                            val cameraRotation = Minecraft.getInstance().gameRenderer.mainCamera.rotation()
                            val forward = Vector3f(0f, 0f, -1f)
                            forward.rotate(cameraRotation)
                            rotateToPoint(forward.asRelative())
                            var currentTick = 0
                            addPreTickAction {
                                val cameraRotation = Minecraft.getInstance().gameRenderer.mainCamera.rotation()
                                val forward = Vector3f(0f, 0f, -1f)
                                forward.rotate(cameraRotation)
                                rotateToWithAngle(forward.asRelative(), -PI / 8)
                                currentTick++
                                if (currentTick >= 5) {
                                    scaleReversed = false
                                }
                            }
                            setReversedScaleOnCompositionStatus(this@ExplosionStarComposition)
                        }
                        .loadScaleHelperBezierValue(
                            0.0001, 1.0, 5,
                            RelativeLocation(2.5, 0.99, 0.0),
                            RelativeLocation(-2.5, 0.0, 0.0),
                        )
                )
            }.apply { this.order = order++ }] = RelativeLocation(0.0, 0.01, 0.0)
        return res
    }

    override fun onDisplay() {
        addPreTickAction {
            if (age == 1 && !client) {
                addSingle()
            }
            if (age++ > maxAge) {
                status.disable()
            }
            if (age == maxAge / 2 && !client) {
                addSingle()
            }
        }
    }
}

