package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.extend.ofID
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.renderer.AutoRenderEntity
import cn.coostack.usefulmagic.items.prop.SkyFallingRuneItem
import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

/** 天坠光柱的生命周期阶段。 */
enum class SkyFallingStage {
    EXPAND,
    HOLD,
    SHRINK,
}

/**
 * 天坠光柱在一个渲染帧中的派生状态。
 *
 * @property phase 当前生命周期阶段。
 * @property phaseProgress 当前阶段进度。
 * @property radius 当前半径。
 * @property height 当前高度。
 * @property opacity 当前透明度。
 * @property bloomAlpha 写入泛光 mask 的透明度。
 * @property collapse 收束阶段进度。
 */
data class SkyFallingCylinderState(
    val phase: SkyFallingStage,
    val phaseProgress: Float,
    val radius: Float,
    val height: Float,
    val opacity: Float,
    val bloomAlpha: Float,
    val collapse: Float,
)

/** 保存天坠光柱的同步状态和扩张、停留、收束生命周期。 */
@CooAutoRegister
class SkyFallingRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos) {
    /** 天坠光柱的同步 RGB 颜色。 */
    @CodecField
    var color: Vector3f = Vector3f(56F / 255F, 104F / 255F, 1.0F)

    /** 光柱当前同步的 XZ 半径和 Y 轴高度。 */
    @CodecField
    var r: Vector3f = Vector3f(1F, CYLINDER_HEIGHT, 1F)

    /** 光柱整体透明度，渲染时限制到有效范围。 */
    @CodecField
    var alpha: Double = 1.0

    /** 客户端上一 tick 的光柱尺寸，用于帧插值。 */
    var prevR: Vector3f = Vector3f(r)

    /** shader 流动时间的额外速度参数。 */
    @CodecField
    var speed: Float = 0F

    override fun getRenderID(): ResourceLocation = ID

    override fun clientTick() {
        updateCylinderState()
    }

    override fun serverTick() {
        updateCylinderState()
    }

    /** 更新同步尺寸，并在生命周期结束后终止实体。 */
    private fun updateCylinderState() {
        prevR.set(r)
        val state = buildState(age.toFloat(), alpha.toFloat())
        r.set(state.radius, state.height, state.radius)
        if (age > MAX_AGE) {
            canceled = true
        }
    }

    /**
     * 返回包含帧插值的生命周期时间。
     *
     * @return 从动画起点开始计算的非负 tick 时间
     */
    internal fun currentTimeline(tickDelta: Float): Float {
        return (age - 1F + tickDelta).coerceAtLeast(0F)
    }

    /**
     * 按同步速度换算 shader 使用的流动时间。
     *
     * @return 应传给 shader 的非反向流动时间
     */
    internal fun currentFlowTime(timeline: Float): Float {
        val speedScale = (1.0F + speed * 0.08F).coerceAtLeast(0.12F)
        return timeline * speedScale
    }

    companion object {
        /** 天坠光柱实体的稳定注册路径。 */
        private const val RENDER_ENTITY_ID = "sky_falling_render_entity"

        /** 生命周期和状态构建共用的最大年龄。 */
        const val MAX_AGE = 168
        /** 同步半径、状态构建和 renderer 共用的光柱高度。 */
        const val CYLINDER_HEIGHT = 200F
        /** 生命周期和状态构建共用的扩张结束时间。 */
        const val EXPAND_END = 24F
        /** 生命周期和状态构建共用的停留结束时间。 */
        const val HOLD_END = 132F
        /** 状态构建和 renderer 共用的最大半径。 */
        val MAX_RADIUS = SkyFallingRuneItem.EXPLOSION_MAX_RADIUS.toFloat()
        /** 天坠光柱的稳定 RenderEntity 注册 ID。 */
        @JvmField
        val ID: ResourceLocation =
            ofID(UsefulMagic.MOD_ID, RENDER_ENTITY_ID)

        /**
         * 根据生命周期时间和透明度构建 renderer 使用的状态。
         *
         * @return 当前阶段对应的光柱派生状态
         */
        internal fun buildState(time: Float, alpha: Float): SkyFallingCylinderState {
            val clampedTime = time.coerceIn(0F, MAX_AGE.toFloat())
            val clampedAlpha = alpha.coerceIn(0F, 1F)
            // 只在这里划分阶段边界，具体曲线交给各阶段构造函数，避免边界计算漂移。
            return when {
                clampedTime < EXPAND_END -> buildExpandState(clampedTime, clampedAlpha)
                clampedTime < HOLD_END -> buildHoldState(clampedAlpha)
                else -> buildShrinkState(clampedTime, clampedAlpha)
            }
        }

        /**
         * 构建扩张阶段的光柱状态。
         *
         * @return 经过五次缓出的扩张状态
         */
        private fun buildExpandState(time: Float, alpha: Float): SkyFallingCylinderState {
            // 半径使用更快接近峰值的五次缓出，透明度使用三次缓出以保留可见淡入。
            val phase = smoothstep(0F, EXPAND_END, time)
            val eased = easeOutQuint(phase)
            return SkyFallingCylinderState(
                phase = SkyFallingStage.EXPAND,
                phaseProgress = phase,
                radius = mix(0.75F, MAX_RADIUS, eased),
                height = CYLINDER_HEIGHT,
                opacity = mix(0.16F, 1.0F, easeOutCubic(phase)) * alpha,
                bloomAlpha = mix(0.34F, 1.0F, eased) * alpha,
                collapse = 0F,
            )
        }

        /**
         * 构建稳定停留阶段的光柱状态。
         *
         * @return 保持最大半径和完整透明度的状态
         */
        private fun buildHoldState(alpha: Float): SkyFallingCylinderState {
            return SkyFallingCylinderState(
                phase = SkyFallingStage.HOLD,
                phaseProgress = 1F,
                radius = MAX_RADIUS,
                height = CYLINDER_HEIGHT,
                opacity = alpha,
                bloomAlpha = alpha,
                collapse = 0F,
            )
        }

        /**
         * 构建收束阶段的光柱状态。
         *
         * @return 半径收束并平方淡出的状态
         */
        private fun buildShrinkState(time: Float, alpha: Float): SkyFallingCylinderState {
            // 将剩余生命周期归一化，半径缓出收束，透明度平方衰减以平滑结束。
            val phase = ((time - HOLD_END) / (MAX_AGE - HOLD_END)).coerceIn(0F, 1F)
            val collapse = smoothstep(0F, 1F, phase)
            val fade = 1F - collapse
            return SkyFallingCylinderState(
                phase = SkyFallingStage.SHRINK,
                phaseProgress = phase,
                radius = mix(MAX_RADIUS, 6F, easeOutCubic(phase)),
                height = CYLINDER_HEIGHT,
                opacity = fade * fade * alpha,
                bloomAlpha = fade * fade * fade * alpha,
                collapse = collapse,
            )
        }

        /**
         * 返回区间内平滑过渡的插值值。
         *
         * @return 位于 `0F..1F` 的平滑插值进度
         */
        private fun smoothstep(edge0: Float, edge1: Float, value: Float): Float {
            if (edge0 == edge1) {
                return if (value >= edge1) 1F else 0F
            }
            val x = ((value - edge0) / (edge1 - edge0)).coerceIn(0F, 1F)
            return x * x * (3F - 2F * x)
        }

        /**
         * 对插值进度应用三次缓出。
         *
         * @return 位于 `0F..1F` 的三次缓出结果
         */
        private fun easeOutCubic(value: Float): Float {
            val x = value.coerceIn(0F, 1F)
            val inverse = 1F - x
            return 1F - inverse * inverse * inverse
        }

        /**
         * 对插值进度应用五次缓出。
         *
         * @return 位于 `0F..1F` 的五次缓出结果
         */
        private fun easeOutQuint(value: Float): Float {
            val x = value.coerceIn(0F, 1F)
            val inverse = 1F - x
            return 1F - inverse * inverse * inverse * inverse * inverse
        }

        /**
         * 在线性区间插值两个标量。
         *
         * @return 按限制后的插值权重计算的标量
         */
        private fun mix(from: Float, to: Float, alpha: Float): Float {
            return from + (to - from) * alpha.coerceIn(0F, 1F)
        }

    }
}
