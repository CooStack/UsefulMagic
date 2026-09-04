package cn.coostack.usefulmagic.particles.composition

import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.AutoSequencedParticleComposition
import cn.coostack.cooparticlesapi.cparticle.CParticleRenderLayer
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableFlashEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.phys.Vec3
import org.joml.Quaterniond
import org.joml.Quaternionf
import java.lang.Math.PI
import java.util.SortedMap
import java.util.TreeMap
import java.util.UUID
import kotlin.collections.forEachIndexed
import kotlin.math.abs
import kotlin.math.roundToInt
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import net.minecraft.world.level.Level
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionScaleHelper

@CooAutoRegister
class LightComposition(
    position: Vec3,
    world: Level?
) : AutoSequencedParticleComposition(position, world) {
    @CodecField
    var color: Vec3 = Vec3.ZERO

    @CodecField
    var maxHeight: Double = 0.0

    @CodecField
    var minSize: Float = 0f

    @CodecField
    var maxSize: Float = 0f

    @CodecField
    var alpha: Float = 0f

    @CodecField
    var maxAge: Int = 0

    private val options: Int
        get() = ParticleOption.getParticleCounts()
    val locations = PointsBuilder()
        .addLine(
            RelativeLocation(0.0, 0.1, 0.0),
            RelativeLocation(0.0, maxHeight, 0.0),
            (4 * maxHeight * options).roundToInt()
        )
        .create()
    var current = 0
    val helper = CompositionScaleHelper(
        1 / 20.0, 1.0, 10
    )

    override fun getParticleSequenced(): SortedMap<CompositionData, RelativeLocation> {
        val sizeStep = (maxSize - minSize) / maxHeight.toFloat()
        val res = TreeMap<CompositionData, RelativeLocation>()
        val size = locations.size
        locations.forEachIndexed { index, it ->
            val data = CompositionData().setDisplayerSupplier {
                ParticleDisplayer.withCParticle(it, CParticleRenderLayer.TRANSLUCENT)
            }.apply { this.order = index }
                .addCParticleInstanceInit {
                    effect = ControlableEndRodEffect(UUID.randomUUID(), true)
                    color = Math3DUtil.colorOf(
                        this@LightComposition.color.x.toInt(),
                        this@LightComposition.color.y.toInt(),
                        this@LightComposition.color.z.toInt()
                    )
                    this.alpha = this@LightComposition.alpha
                    val count = abs(maxHeight - it.y).roundToInt()
                    this.size = minSize + sizeStep * count
                }
            res[data ] = it
        }

        return res
    }

    override fun onDisplay() {
        val spawnCount = count / 30
        addPreTickAction {
            val displayedBefore = displayedParticleCount
            addMultiple(count)
            if (!client && displayedParticleCount != displayedBefore) {
                markDirty()
            }
            rotateAsAxis(0.0)
            if (current++ > maxAge) {
                remove()
            }
            if (current - 1 <= maxAge - helper.scaleTick) {
                helper.doScaleReversed()
            } else {
                helper.doScale()
            }

        }
    }
}
