package cn.coostack.usefulmagic.particles.composition.formation.crystal

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID
import kotlin.math.PI

/**
 * 显示攻击水晶的双层六边形粒子图案。
 *
 * 示例：设置 [crystalPos] 后在对应水晶方块下方生成。
 * 禁止作为未绑定水晶的独立装饰粒子使用。
 */
@CooAutoRegister
class SwordAttackCrystalComposition(
    position: Vec3 = Vec3.ZERO,
    world: Level? = null
) : CrystalComposition(position, world) {
    @CodecField
    override var crystalPos: BlockPos = BlockPos.ZERO

    @CodecField
    override var time: Int = 0

    /** 每刻逆时针旋转攻击水晶图案。 */
    override fun displayParticleAnimate() {
        rotateAsAxis(-PI / 256)
    }

    /** @return 攻击水晶使用的 GPU 粒子位置与配置 */
    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return PointsBuilder()
            .addPolygonInCircle(6, 10, 0.5)
            .addBuilder(
                RelativeLocation(),
                PointsBuilder().addPolygonInCircle(6, 10, 0.5)
                    .rotateAsAxis(PI / 6, RelativeLocation.yAxis())
            )
            .pointsOnEach { it.y += 0.5 }
            .createWithCompositionData {
                CompositionData()
                    .setDisplayerSupplier { uuid ->
                        ParticleDisplayer.withCParticle(uuid)
                    }
                    .addCParticleInstanceInit {
                        effect = ControlableEndRodEffect(UUID.randomUUID())
                        color = Math3DUtil.colorOf(0, 255, 255)
                        size = 0.05f
                    }
            }
    }
}
