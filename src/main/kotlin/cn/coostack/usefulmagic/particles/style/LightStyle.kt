package cn.coostack.usefulmagic.particles.style

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffers
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableFlashEffect
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.usefulmagic.utils.ParticleOption
import kotlinx.io.Buffer
import net.minecraft.block.ShapeContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import org.joml.Quaterniond
import org.joml.Quaternionf
import java.lang.Math.PI
import java.util.SortedMap
import java.util.TreeMap
import java.util.UUID
import kotlin.collections.forEachIndexed
import kotlin.math.abs
import kotlin.math.roundToInt

class LightStyle(
    val color: Vec3d,
    val maxHeight: Double,
    val minSize: Float,
    val maxSize: Float,
    val alpha: Float,
    val maxAge: Int,
    uuid: UUID = UUID.randomUUID(),
) : SequencedParticleStyle(64.0, uuid) {
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
    val helper = HelperUtil.scaleStyle(
        1 / 20.0, 1.0, 10
    )

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            val color = args["color"]!!.loadedValue as Vec3d
            val maxHeight = args["maxHeight"]!!.loadedValue as Double
            val minSize = args["minSize"]!!.loadedValue as Float
            val maxSize = args["maxSize"]!!.loadedValue as Float
            val maxAge = args["maxAge"]!!.loadedValue as Int
            val alpha = args["alpha"]!!.loadedValue as Float
            val style = LightStyle(color, maxHeight, minSize, maxSize, alpha, maxAge, uuid)
                .also { it.readPacketArgs(args) }
            return style
        }

    }

    override fun getParticlesCount(): Int {
        return locations.size
    }

    override fun getCurrentFramesSequenced(): SortedMap<SortedStyleData, RelativeLocation> {
        val sizeStep = (maxSize - minSize) / maxHeight.toFloat()
        val res = TreeMap<SortedStyleData, RelativeLocation>()
        val size = locations.size
        locations.forEachIndexed { index, it ->
            val data = SortedStyleData({
                ParticleDisplayer.withSingle(
                    TestEndRodEffect(it, true)
                )
            }, index)
                .withParticleHandler {
                    textureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
                    colorOfRGB(
                        this@LightStyle.color.x.toInt(),
                        this@LightStyle.color.y.toInt(),
                        this@LightStyle.color.z.toInt()
                    )
                    particleAlpha = alpha
                    val count = abs(maxHeight - it.y).roundToInt()
                    this.size = minSize + sizeStep * count
                }.withParticleControlerHandler {
                    this.addPreTickAction {

//                        previewAngleX = currentAngleX
//                        currentAngleX += (-PI / 36).toFloat()
//                        previewAngleY = currentAngleY
//                        currentAngleY += (PI / 36).toFloat()
                    }
                }
            res[data as SortedStyleData] = it
        }

        return res
    }

    override fun writePacketArgsSequenced(): Map<String, ParticleControlerDataBuffer<*>> {
        return mapOf(
            "color" to ParticleControlerDataBuffers.vec3d(color),
            "maxHeight" to ParticleControlerDataBuffers.double(maxHeight),
            "minSize" to ParticleControlerDataBuffers.float(minSize),
            "maxSize" to ParticleControlerDataBuffers.float(maxSize),
            "maxAge" to ParticleControlerDataBuffers.int(maxAge),
            "alpha" to ParticleControlerDataBuffers.float(alpha),
            "maxAge" to ParticleControlerDataBuffers.int(maxAge),
            "current" to ParticleControlerDataBuffers.int(current)
        )
    }

    override fun readPacketArgsSequenced(args: Map<String, ParticleControlerDataBuffer<*>>) {
        args["current"]?.let {
            current = it.loadedValue as Int
        }
    }


    override fun onDisplay() {
        val spawnCount = getParticlesCount() / 30
        addPreTickAction {
            addMultiple(getParticlesCount())
            rotateParticlesAsAxis(0.0)
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