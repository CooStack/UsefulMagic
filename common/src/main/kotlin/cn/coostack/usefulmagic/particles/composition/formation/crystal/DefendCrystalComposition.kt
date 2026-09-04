package cn.coostack.usefulmagic.particles.composition.formation.crystal

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.FourierSeriesBuilder
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID
import kotlin.math.PI

/**
 * 显示防御水晶的圆环与傅里叶曲线图案。
 *
 * 示例：设置 [crystalPos] 后在对应水晶方块下方生成。
 * 禁止作为未绑定水晶的独立装饰粒子使用。
 */
@CooAutoRegister
class DefendCrystalComposition(
    position: Vec3 = Vec3.ZERO,
    world: Level? = null
) : CrystalComposition(position, world) {
    @CodecField
    override var crystalPos: BlockPos = BlockPos.ZERO

    @CodecField
    override var time: Int = 0

    /** 每刻顺时针旋转防御水晶图案。 */
    override fun displayParticleAnimate() {
        rotateAsAxis(PI / 256)
    }

    /** @return 防御水晶使用的 GPU 粒子位置与配置 */
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return PointsBuilder()
            .addCircle(0.5, 20 * ParticleOption.getParticleCounts())
            .addFourierSeries(
                FourierSeriesBuilder()
                    .scale(0.1)
                    .count(20 * ParticleOption.getParticleCounts())
                    .addFourier(3.0, 2.0, 0.0)
                    .addFourier(2.0, -3.0, 0.0)
            )
            .createWithCompositionData {
                CompositionData()
                    .setDisplayerSupplier { uuid ->
                        ParticleDisplayer.withCParticle(uuid)
                    }
                    .addCParticleInstanceInit {
                        effect = ControlableEndRodEffect(UUID.randomUUID())
                        color = Math3DUtil.colorOf(0, 255, 255)
                        size = 0.1f
                    }
            }
    }
}
