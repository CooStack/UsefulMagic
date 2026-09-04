package cn.coostack.usefulmagic.particles.composition.formation.crystal

import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.utils.helper.StatusHelper
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.formation.api.FormationCrystal
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

/**
 * 绑定阵法水晶并维护水晶粒子组合的共有生命周期。
 *
 * 示例：具体水晶 Composition 设置 [crystalPos] 后在服务端生成。
 * 禁止绑定非 [FormationCrystal] 方块位置，否则实例会主动结束。
 *
 * @param position 粒子组合的世界坐标
 * @param world 所属世界；网络解码阶段允许为 `null`
 */
abstract class CrystalComposition(position: Vec3, world: Level?) :
    AutoParticleComposition(position, world) {
    /** 绑定的水晶方块位置，由具体可注册类型负责同步。 */
    open var crystalPos: BlockPos = BlockPos.ZERO

    /** 服务端累计时间，由具体可注册类型负责同步。 */
    open var time: Int = 0

    /** 服务端绑定的阵法水晶。 */
    protected lateinit var crystal: FormationCrystal

    init {
        visibleRange = 128.0
        setDisabledInterval(10)
    }

    /**
     * 初始化客户端动画，或在服务端校验绑定的阵法水晶。
     *
     * 示例：通过 `spawn(world, position)` 进入此生命周期。
     * 禁止在未设置 [crystalPos] 时直接生成。
     */
    override fun onDisplay() {
        if (client) {
            addPreTickAction {
                displayParticleAnimate()
            }
            return
        }

        val level = world ?: run {
            remove()
            return
        }
        val entity = level.getBlockEntity(crystalPos)
        if (entity !is FormationCrystal) {
            remove()
            return
        }
        val activeFormation = entity.activeFormation ?: return
        val core = level.getBlockEntity(ofFloored(activeFormation.formationCore))
        crystal = entity

        addPreTickAction {
            if (client) return@addPreTickAction
            if (!(crystal.activeFormation?.isActiveFormation() ?: false)
                || core !is FormationCoreBlockEntity
            ) {
                cancel()
            }
            time++
        }
        addPreTickAction {
            displayParticleAnimate()
        }
    }

    /**
     * 推进一帧水晶动画。
     *
     * 示例：每刻绕 Y 轴旋转粒子图形。
     * 禁止在这里重新生成整组粒子。
     */
    abstract fun displayParticleAnimate()

    /** 将组合切换到禁用状态，并在保留期结束后清理。 */
    fun cancel() {
        if (status.isDisable()) return
        status.setStatus(StatusHelper.Status.DISABLE)
    }
}
