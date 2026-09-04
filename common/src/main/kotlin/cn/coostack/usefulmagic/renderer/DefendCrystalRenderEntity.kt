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
import kotlin.math.pow

/**
 * 保存防御水晶屏障的同步状态、生命周期和渲染范围。
 *
 * @property maxRange 初始屏障半径，非正数表示沿用默认半径
 */
@CooAutoRegister
class DefendCrystalRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
    @CodecField var maxRange: Double = 0.0,
) : AutoRenderEntity(world, pos) {
    /** 屏障主体和辉光使用的 RGB 颜色。 */
    @CodecField
    var color: Vector3f = Vector3f(0.42F, 0.72F, 1.0F)

    /** 屏障三个轴向的当前世界半径。 */
    @CodecField
    var r: Vector3f = Vector3f(2F)

    /** 屏障整体透明度，渲染时限制到有效范围。 */
    @CodecField
    var alpha: Double = 1.0

    /** 屏障暗色底层的不透明度。 */
    @CodecField
    var darkOpacity: Double = 0.12

    /** 屏障能量亮层的不透明度。 */
    @CodecField
    var brightOpacity: Double = 0.86

    /** 屏障边缘亮层的不透明度。 */
    @CodecField
    var rimOpacity: Double = 1.22

    /** 六边形能量网格的密度。 */
    @CodecField
    var gridDensity: Double = 15.0

    /** 能量网格线的归一化宽度。 */
    @CodecField
    var gridWidth: Double = 0.032

    /** 高亮边缘的归一化宽度。 */
    @CodecField
    var highlightWidth: Double = 0.11

    /** 屏障亮度脉冲的强度。 */
    @CodecField
    var pulseStrength: Double = 0.35

    /** 客户端上一 tick 的屏障半径，用于帧插值。 */
    var prevR: Vector3f = Vector3f()

    /** 是否已经开始屏障消散阶段。 */
    @CodecField
    var over: Boolean = false

    /** 进入消散阶段后的累计 tick 数。 */
    @CodecField
    var overTick: Int = 0

    /** 屏障法阵中心的同步世界坐标。 */
    @CodecField
    var formationPos: Vec3 = pos

    init {
        if (maxRange > 0.0) {
            r.set(maxRange.toFloat())
        }
        prevR.set(r)
    }

    override fun getRenderID(): ResourceLocation = ID

    override fun clientTick() {
        updateLifecycle()
    }

    override fun serverTick() {
        updateLifecycle()
    }

    /** 记录屏障开始消散的年龄，并请求同步该状态。 */
    fun over() {
        if (over) {
            return
        }
        over = true
        overTick = age
        markDirty()
    }

    /** 更新插值半径，并在消散动画结束后同步终止状态。 */
    private fun updateLifecycle() {
        prevR.set(r)
        if (!over) {
            return
        }
        if (age - overTick > DISSIPATE_TICKS.toInt() && !canceled) {
            canceled = true
            markDirty()
        }
    }

    /**
     * 返回包含帧插值的实体年龄。
     *
     * @return 从实体首个渲染 tick 开始计算的非负年龄
     */
    internal fun frameAge(tickDelta: Float): Float {
        return (age - 1F + tickDelta).coerceAtLeast(0F)
    }

    /**
     * 计算屏障部署阶段的缩放倍率。
     *
     * @return 位于 `0F..1F` 的部署缩放倍率
     */
    internal fun deployScale(frameAge: Float): Float {
        return smoothstep(0F, DEPLOY_TICKS, frameAge)
    }

    /**
     * 计算屏障消散阶段的进度。
     *
     * @return 尚未消散时为 `0F`，否则返回 `0F..1F` 的进度
     */
    internal fun collapseProgress(frameAge: Float): Float {
        if (!over) {
            return 0F
        }
        return smoothstep(0F, DISSIPATE_TICKS, frameAge - overTick.toFloat())
    }

    /**
     * 在消散完成时结束实体生命周期。
     *
     * @return 本次调用是否完成了生命周期终止
     */
    internal fun finishCollapse(collapse: Float): Boolean {
        if (!over || collapse < 0.999F) {
            return false
        }
        canceled = true
        return true
    }

    /**
     * 计算当前帧屏障透明度。
     *
     * @return 叠加部署与消散曲线后的透明度
     */
    internal fun visibleAlpha(frameAge: Float, collapse: Float): Float {
        val deployVisibility = smoothstep(0F, DEPLOY_TICKS * 0.62F, frameAge)
        val collapseVisibility = (1F - collapse).coerceIn(0F, 1F).pow(1.18F)
        return alpha.toFloat().coerceIn(0F, 1F) * deployVisibility * collapseVisibility
    }

    /**
     * 计算消散时的外扩倍率。
     *
     * @return 从 `1F` 逐步增加的外扩倍率
     */
    internal fun collapseExpansion(collapse: Float): Float {
        return 1F + collapse.coerceIn(0F, 1F) * 0.10F
    }

    /**
     * 在上一帧和当前半径之间插值。
     *
     * @return 当前渲染帧的三轴半径副本
     */
    internal fun interpolatedRadius(tickDelta: Float): Vector3f {
        return Vector3f(prevR).lerp(r, tickDelta.coerceIn(0F, 1F))
    }

    companion object {
        /** 防御水晶实体的稳定注册路径。 */
        private const val RENDER_ENTITY_ID = "defend_crystal_render_entity"

        /** 部署动画与状态计算共用的持续 tick 数。 */
        private const val DEPLOY_TICKS = 18F
        /** 消散动画与状态计算共用的持续 tick 数。 */
        private const val DISSIPATE_TICKS = 18F

        /** 防御水晶屏障的稳定 RenderEntity 注册 ID。 */
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
    }
}
