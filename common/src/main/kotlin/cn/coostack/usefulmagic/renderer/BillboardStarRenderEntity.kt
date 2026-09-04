package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.extend.ofID
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.renderer.AutoRenderEntity
import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.sin

/** 保存公告板星芒的同步状态、生命周期和渲染范围。 */
@CooAutoRegister
class BillboardStarRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos) {
    /** 星芒主体和辉光使用的 RGB 颜色。 */
    @CodecField
    var color: Vector3f = Vector3f(1.0F, 0.94F, 0.72F)

    /** 星芒整体透明度，渲染时限制到有效范围。 */
    @CodecField
    var alpha: Double = 1.0

    /** 星芒完全展开时的世界尺寸。 */
    @CodecField
    var maxScale: Float = 3.5F

    /** 从最小尺寸扩张到完整尺寸的 tick 数。 */
    @CodecField
    var expandTicks: Int = 4

    /** 完全展开后保持显示的 tick 数。 */
    @CodecField
    var holdTicks: Int = 8

    /** 从完整尺寸收束到消失的 tick 数。 */
    @CodecField
    var shrinkTicks: Int = 6

    /** 星芒公告板每 tick 的自转弧度。 */
    @CodecField
    var spinSpeed: Float = 0.24F

    init {
        renderRange = 96.0
    }

    override fun getRenderID(): ResourceLocation = ID

    override fun clientTick() {
        updateLifecycle()
    }

    override fun serverTick() {
        updateLifecycle()
    }

    /** 在完整动画结束后终止实体。 */
    private fun updateLifecycle() {
        if (age > totalDurationTicks() + 1) {
            canceled = true
        }
    }

    /**
     * 计算展开、停留和收束阶段的总时长。
     *
     * @return 至少包含一个展开 tick 和一个收束 tick 的总时长
     */
    private fun totalDurationTicks(): Int {
        return expandTicks.coerceAtLeast(1) + holdTicks.coerceAtLeast(0) + shrinkTicks.coerceAtLeast(1)
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
     * 计算当前帧星芒尺寸。
     *
     * @return 当前帧的世界空间尺寸
     */
    internal fun currentScale(tickDelta: Float): Float {
        val minimumScale = 0.05F
        // 先确定三个生命周期区间，再分别使用缓出、轻微脉冲和收束曲线计算尺寸。
        val time = currentTimeline(tickDelta)
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdDuration = holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdDuration
        val pulse = currentTwinkle(tickDelta)
        return when {
            time < expandDuration -> maxScale.coerceAtLeast(minimumScale) *
                    mix(0.16F, 1.0F, easeOutQuint(smoothstep(0F, expandDuration, time)))

            time < holdEnd -> maxScale.coerceAtLeast(minimumScale) * mix(0.96F, 1.08F, pulse)
            else -> {
                val collapse = smoothstep(0F, shrinkDuration, time - holdEnd)
                maxScale.coerceAtLeast(minimumScale) * mix(1.0F, 0.12F, collapse)
            }
        }
    }

    /**
     * 计算当前帧主体透明度。
     *
     * @return 叠加生命周期淡入淡出后的主体透明度
     */
    internal fun currentBodyAlpha(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        // 透明度与尺寸共用阶段边界，但收束阶段使用平方衰减以避免尾帧突变。
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdDuration = holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdDuration
        val baseAlpha = alpha.toFloat().coerceIn(0F, 1F)
        return when {
            time < expandDuration -> mix(
                0.18F,
                1.0F,
                easeOutCubic(smoothstep(0F, expandDuration, time)),
            ) * baseAlpha

            time < holdEnd -> mix(0.84F, 1.0F, currentTwinkle(tickDelta)) * baseAlpha
            else -> {
                val collapse = smoothstep(0F, shrinkDuration, time - holdEnd)
                val fade = 1F - collapse
                fade * fade * baseAlpha
            }
        }
    }

    /**
     * 计算当前帧写入泛光 mask 的透明度。
     *
     * @return 叠加独立淡入、脉冲与三次淡出后的 mask 透明度
     */
    internal fun currentBloomAlpha(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdDuration = holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdDuration
        val baseAlpha = alpha.toFloat().coerceIn(0F, 1F)
        return when {
            time < expandDuration -> mix(
                0.28F,
                1.0F,
                easeOutQuint(smoothstep(0F, expandDuration, time)),
            ) * baseAlpha

            time < holdEnd -> mix(0.92F, 1.12F, currentTwinkle(tickDelta)) * baseAlpha
            else -> {
                val collapse = smoothstep(0F, shrinkDuration, time - holdEnd)
                val fade = 1F - collapse
                fade * fade * fade * baseAlpha
            }
        }
    }

    /**
     * 计算星芒射线从尖锐到收束的形变进度。
     *
     * @return 当前帧的射线形变权重
     */
    internal fun currentRayMorph(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdDuration = holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdDuration
        return when {
            time < expandDuration -> mix(0F, 1.0F, easeOutCubic(smoothstep(0F, expandDuration, time)))
            time < holdEnd -> mix(0.84F, 1.0F, currentTwinkle(tickDelta))
            else -> mix(1.0F, 0.24F, smoothstep(0F, shrinkDuration, time - holdEnd))
        }
    }

    /**
     * 计算中心白色区域的强度。
     *
     * @return 当前帧的白色核心强度
     */
    internal fun currentWhiteCore(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdDuration = holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdDuration
        return when {
            time < expandDuration -> mix(0.22F, 1.0F, easeOutCubic(smoothstep(0F, expandDuration, time)))
            time < holdEnd -> mix(0.76F, 1.0F, currentTwinkle(tickDelta))
            else -> mix(1.0F, 0.42F, smoothstep(0F, shrinkDuration, time - holdEnd))
        }
    }

    /**
     * 计算当前帧闪烁倍率。
     *
     * @return 位于 `0F..1F` 的周期闪烁值
     */
    internal fun currentTwinkle(tickDelta: Float): Float {
        return 0.5F + 0.5F * sin(currentTimeline(tickDelta) * 0.72F + 0.8F)
    }

    /**
     * 计算消散阶段的收束进度。
     *
     * @return 收束前为 `0F`，收束期间位于 `0F..1F`
     */
    internal fun currentCollapse(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        return if (time < holdEnd) {
            0F
        } else {
            smoothstep(0F, shrinkDuration, time - holdEnd)
        }
    }

    /**
     * 计算当前帧公告板旋转角。
     *
     * @return 当前帧的旋转弧度
     */
    internal fun currentRoll(tickDelta: Float): Float {
        return currentTimeline(tickDelta) * spinSpeed
    }

    companion object {
        /** 星芒实体的稳定注册路径。 */
        private const val RENDER_ENTITY_ID = "billboard_star_render_entity"

        /** 公告板星芒的稳定 RenderEntity 注册 ID。 */
        @JvmField
        val ID: ResourceLocation =
            ofID(UsefulMagic.MOD_ID, RENDER_ENTITY_ID)

        /**
         * 返回区间内平滑过渡的插值值。
         *
         * @return 位于 `0F..1F` 的平滑插值进度
         */
        internal fun smoothstep(edge0: Float, edge1: Float, value: Float): Float {
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
        internal fun mix(from: Float, to: Float, alpha: Float): Float {
            return from + (to - from) * alpha.coerceIn(0F, 1F)
        }
    }
}
