package cn.coostack.usefulmagic.particles.composition.magic

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.*
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.*
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionBezierScaleHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@CooAutoRegister
class BlockBorderComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    @CodecField
    var tickCount: Int = 0

    private val scaleHelper = CompositionBezierScaleHelper(
        10,
        0.01,
        1.0,
        RelativeLocation(5.074694, 1.0, 0.0),
        RelativeLocation(5.672241, 1.2, 0.0)
    )

    init {
        axis = RelativeLocation.yAxis()
        scaleHelper.loadControler(this)
        setDisabledInterval(10)
    }

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        val result = LinkedHashMap<CompositionData, RelativeLocation>()

        result.putAll(
            PointsBuilder()
                .addLine(RelativeLocation(1.0, -1.083333, -0.5), RelativeLocation(1.0, -1.083333, -1.0), 30)
                .addLine(RelativeLocation(1.0, 1.083333, -0.5), RelativeLocation(1.0, 1.083333, -1.0), 30)
                .addLine(RelativeLocation(-1.0, -1.083333, -0.5), RelativeLocation(-1.0, -1.083333, -1.0), 30)
                .addLine(RelativeLocation(-1.0, 1.083333, -0.5), RelativeLocation(-1.0, 1.083333, -1.0), 30)
                .addLine(RelativeLocation(1.0, -1.083333, 0.5), RelativeLocation(1.0, -1.083333, 1.0), 30)
                .addLine(RelativeLocation(1.0, 1.083333, 0.5), RelativeLocation(1.0, 1.083333, 1.0), 30)
                .addLine(RelativeLocation(-1.0, -1.083333, 0.5), RelativeLocation(-1.0, -1.083333, 1.0), 30)
                .addLine(RelativeLocation(-1.0, 1.083333, 0.5), RelativeLocation(-1.0, 1.083333, 1.0), 30)
                .addLine(RelativeLocation(1.0, -1.083333, -1.0), RelativeLocation(0.5, -1.083333, -1.0), 30)
                .addLine(RelativeLocation(1.0, 1.083333, -1.0), RelativeLocation(0.5, 1.083333, -1.0), 30)
                .addLine(RelativeLocation(-1.0, -1.083333, -1.0), RelativeLocation(-0.5, -1.083333, -1.0), 30)
                .addLine(RelativeLocation(-1.0, 1.083333, -1.0), RelativeLocation(-0.5, 1.083333, -1.0), 30)
                .addLine(RelativeLocation(1.0, -1.083333, 1.0), RelativeLocation(0.5, -1.083333, 1.0), 30)
                .addLine(RelativeLocation(1.0, 1.083333, 1.0), RelativeLocation(0.5, 1.083333, 1.0), 30)
                .addLine(RelativeLocation(-1.0, -1.083333, 1.0), RelativeLocation(-0.5, -1.083333, 1.0), 30)
                .addLine(RelativeLocation(-1.0, 1.083333, 1.0), RelativeLocation(-0.5, 1.083333, 1.0), 30)
                .addLine(RelativeLocation(1.0, -1.083333, -1.0), RelativeLocation(1.0, -0.583333, -1.0), 30)
                .addLine(RelativeLocation(1.0, 1.083333, -1.0), RelativeLocation(1.0, 0.583333, -1.0), 30)
                .addLine(RelativeLocation(-1.0, -1.083333, -1.0), RelativeLocation(-1.0, -0.583333, -1.0), 30)
                .addLine(RelativeLocation(-1.0, 1.083333, -1.0), RelativeLocation(-1.0, 0.583333, -1.0), 30)
                .addLine(RelativeLocation(1.0, -1.083333, 1.0), RelativeLocation(1.0, -0.583333, 1.0), 30)
                .addLine(RelativeLocation(1.0, 1.083333, 1.0), RelativeLocation(1.0, 0.583333, 1.0), 30)
                .addLine(RelativeLocation(-1.0, -1.083333, 1.0), RelativeLocation(-1.0, -0.583333, 1.0), 30)
                .addLine(RelativeLocation(-1.0, 1.083333, 1.0), RelativeLocation(-1.0, 0.583333, 1.0), 30)
                .scale(0.5)
                .createWithCompositionData { rel ->
                    CompositionData()
                        .setDisplayerSupplier {
                            ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                        }
                        .addParticleInstanceInit {
                            color = Vector3f(1f)
                        }
                }
        )

        return result
    }

    override fun remove() {
        if (!status.isDisable()) {
            status.disable()
        } else {
            super.remove()
        }
    }

    override fun onDisplay() {
        addPreTickAction {
            tickCount++
            if (status.isEnable()) {
                scaleHelper.doScale()
            } else {
                scaleHelper.doScaleReversed()
            }
            val radian = tickCount * PI / 180
            val r2 = (360 - tickCount % 360) * PI / 180
            val to = RelativeLocation(
                sin(radian) * sin(r2),
                cos(radian),
                cos(radian) * cos(r2)
            )
            val speed = cos(4 * tickCount * PI / 180) * PI / 12
            rotateToWithAngle(to, speed)
        }
    }
}