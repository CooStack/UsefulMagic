package cn.coostack.usefulmagic.particles.composition.formation

import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.utils.helper.ScaleHelper
import cn.coostack.cooparticlesapi.utils.helper.StatusHelper
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

/**
 * 绑定阵法核心并维护小、中、大阵法共有的显示生命周期。
 *
 * 示例：由具体阵法 Composition 设置 [formationPos] 后在服务端生成。
 * 禁止绑定非 [FormationCoreBlockEntity] 方块位置，否则实例会主动结束。
 *
 * @param position 粒子组合的世界坐标
 * @param world 所属世界；网络解码阶段允许为 `null`
 */
abstract class FormationComposition(position: Vec3, world: Level?) :
    AutoParticleComposition(position, world) {
    /** 阵法当前是否处于工作动画。 */
    enum class FormationStatus {
        WORKING,
        IDLE
    }

    /** 绑定的阵法核心方块位置，由具体可注册类型负责同步。 */
    open var formationPos: BlockPos = BlockPos.ZERO

    /** 用字符串保存工作状态，确保自动 Codec 可以直接同步。 */
    open var formationStatusName: String = FormationStatus.IDLE.name

    /** 服务端累计时间，由具体可注册类型负责同步。 */
    open var time: Int = 0

    /** 当前工作状态；无效网络值按 [FormationStatus.IDLE] 处理。 */
    var formationStatus: FormationStatus
        get() = runCatching { FormationStatus.valueOf(formationStatusName) }
            .getOrDefault(FormationStatus.IDLE)
        private set(value) {
            formationStatusName = value.name
        }

    /** 服务端绑定的阵法方块实体。 */
    protected lateinit var formationEntity: FormationCoreBlockEntity

    init {
        visibleRange = 128.0
        setDisabledInterval(20)
    }

    /**
     * 初始化客户端动画，或在服务端校验绑定的阵法核心。
     *
     * 示例：通过 `spawn(world, position)` 进入此生命周期。
     * 禁止在未设置 [formationPos] 时直接生成。
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
        val entity = level.getBlockEntity(formationPos)
        if (entity !is FormationCoreBlockEntity) {
            remove()
            return
        }
        formationEntity = entity

        addPreTickAction {
            if (client) return@addPreTickAction
            val currentEntity = world?.getBlockEntity(formationPos)
            if (!formationEntity.formation.isActiveFormation()
                || (!formationEntity.formation.inTriggerRangeActive
                        && formationEntity.formation.settings.displayParticleOnlyTrigger)
                || currentEntity !is FormationCoreBlockEntity
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
     * 按当前 [time] 和 [formationStatus] 推进一帧阵法动画。
     *
     * 示例：工作状态下提高旋转速度。
     * 禁止在这里重新生成整组粒子。
     */
    abstract fun displayParticleAnimate()

    /**
     * 反向缩放，并在缩放耗尽时移除当前组合。
     *
     * @param scaleHelper 当前组合使用的缩放辅助器
     */
    protected fun reverseScaleOrRemove(scaleHelper: ScaleHelper) {
        if (scaleHelper.isZero() || scale <= scaleHelper.minScale) {
            remove()
            return
        }
        scaleHelper.doScaleReversed()
        if (scaleHelper.isZero() || scale <= scaleHelper.minScale) {
            remove()
        }
    }

    /**
     * 在服务端切换阵法工作动画状态。
     *
     * @param status 新的阵法工作状态
     */
    fun changeStatus(status: FormationStatus) {
        if (client) return
        formationStatus = status
        markDirty()
    }

    /** 将组合切换到禁用状态，并由缩放动画完成清理。 */
    fun cancel() {
        if (status.isDisable()) return
        status.setStatus(StatusHelper.Status.DISABLE)
    }
}
