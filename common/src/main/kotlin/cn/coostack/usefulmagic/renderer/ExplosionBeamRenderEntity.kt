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

/**
 * 爆炸光柱在一个渲染帧中的派生状态。
 *
 * @property phaseProgress 当前阶段进度。
 * @property radius 当前半径。
 * @property height 当前高度。
 * @property offsetY 光柱底部的纵向偏移。
 * @property opacity 当前透明度。
 * @property brightness 当前亮度倍率。
 * @property collapse 收束阶段进度。
 */
internal data class ExplosionBeamState(
    val phaseProgress: Float,
    val radius: Float,
    val height: Float,
    val offsetY: Float,
    val opacity: Float,
    val brightness: Float,
    val collapse: Float,
)

/** 保存爆炸光柱的同步状态和阶段生命周期。 */
@CooAutoRegister
class ExplosionBeamRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos) {
    /** 光柱主体和高亮层使用的 RGB 颜色。 */
    @CodecField
    var color: Vector3f = Vector3f(1.0F, 0.20F, 0.06F)

    /** 光柱整体透明度，渲染时限制到有效范围。 */
    @CodecField
    var alpha: Double = 1.0

    /** 光柱完全展开后的世界高度。 */
    @CodecField
    var beamHeight: Float = 150F

    /** 光柱完全展开后的世界半径。 */
    @CodecField
    var maxRadius: Float = 2F

    /** 光柱扩张阶段持续的 tick 数。 */
    @CodecField
    var expandTicks: Int = 5

    /** 光柱完全展开后保持的 tick 数。 */
    @CodecField
    var holdTicks: Int = 10

    /** 光柱收束阶段持续的 tick 数。 */
    @CodecField
    var shrinkTicks: Int = 5

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
     * 根据生命周期时间构建 renderer 使用的派生状态。
     *
     * @return 当前生命周期阶段对应的光柱派生状态
     */
    internal fun buildState(time: Float): ExplosionBeamState {
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdDuration = holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdDuration
        val totalDuration = holdEnd + shrinkDuration
        val clampedTime = time.coerceIn(0F, totalDuration)
        val clampedAlpha = alpha.toFloat().coerceIn(0F, 1F)
        // 展开阶段从底部向上生长，停留阶段保持峰值，收束阶段同时缩半径并平方淡出。
        return when {
            clampedTime < expandDuration -> {
                val phase = smoothstep(0F, expandDuration, clampedTime)
                val eased = easeOutCubic(phase)
                val currentHeight = mix(MIN_HEIGHT, beamHeight, eased)
                ExplosionBeamState(
                    phaseProgress = phase,
                    radius = maxRadius.coerceAtLeast(MIN_RADIUS),
                    height = currentHeight,
                    offsetY = (beamHeight - currentHeight).coerceAtLeast(0F),
                    opacity = mix(0.18F, 1.0F, eased) * clampedAlpha,
                    brightness = mix(1.6F, 2.9F, eased),
                    collapse = 0F,
                )
            }

            clampedTime < holdEnd -> ExplosionBeamState(
                phaseProgress = 1F,
                radius = maxRadius.coerceAtLeast(MIN_RADIUS),
                height = beamHeight,
                offsetY = 0F,
                opacity = clampedAlpha,
                brightness = 2.9F,
                collapse = 0F,
            )

            else -> {
                val phase = ((clampedTime - holdEnd) / shrinkDuration).coerceIn(0F, 1F)
                val collapse = smoothstep(0F, 1F, phase)
                val fade = 1F - collapse
                ExplosionBeamState(
                    phaseProgress = phase,
                    radius = mix(maxRadius.coerceAtLeast(MIN_RADIUS), MIN_RADIUS, collapse),
                    height = mix(beamHeight, beamHeight * 0.82F, collapse),
                    offsetY = mix(0F, beamHeight * 0.09F, collapse),
                    opacity = fade * fade * clampedAlpha,
                    brightness = mix(2.9F, 1.0F, collapse),
                    collapse = collapse,
                )
            }
        }
    }

    companion object {
        /** 爆炸光柱实体的稳定注册路径。 */
        private const val RENDER_ENTITY_ID = "explosion_beam_render_entity"

        /** 实体状态和 renderer 共用的最小有效半径。 */
        internal const val MIN_RADIUS = 0.02F
        /** 实体状态和 renderer 共用的最小有效高度。 */
        internal const val MIN_HEIGHT = 0.05F

        /** 爆炸光柱的稳定 RenderEntity 注册 ID。 */
        @JvmField
        val ID: ResourceLocation =
            ofID(UsefulMagic.MOD_ID, RENDER_ENTITY_ID)

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
         * 在线性区间插值两个标量。
         *
         * @return 按限制后的插值权重计算的标量
         */
        private fun mix(from: Float, to: Float, alpha: Float): Float {
            return from + (to - from) * alpha.coerceIn(0F, 1F)
        }
    }
}
