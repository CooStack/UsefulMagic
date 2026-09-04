package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.renderer.AutoRenderEntity
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

/** 保存龙魔法球的同步状态、成长过程和消散生命周期。 */
@CooAutoRegister
class DragonMagicBallRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos) {
    /** 魔法球表面和云层使用的 RGB 颜色。 */
    @CodecField
    var color: Vector3f = Vector3f(1.0F, 1.0F, 1.0F)

    /** 魔法球完全成长后的世界尺寸。 */
    @CodecField
    var size: Float = DEFAULT_SIZE

    /** 魔法球整体透明度，渲染时限制到有效范围。 */
    @CodecField
    var alpha: Double = 1.0

    /** 本体材质和辉光共用的亮度倍率。 */
    @CodecField
    var brightness: Float = 1.0F

    /** 外层云雾材质的不透明度。 */
    @CodecField
    var cloudOpacity: Double = 0.62

    /** 表面流动材质的亮度倍率。 */
    @CodecField
    var surfaceBrightness: Double = 1.18

    /** bloom 合成的强度倍率。 */
    @CodecField
    var bloomIntensity: Double = 0.82

    /** 是否向 mask attachment 写入魔法球辉光。 */
    @CodecField
    var bloomEnabled: Boolean = true

    /** 成长阶段是否通过尺寸缩放表现。 */
    @CodecField
    var growingByScale: Boolean = true

    /** 魔法球完成成长所需的 tick 数。 */
    @CodecField
    var growingTick: Int = DEFAULT_GROWING_TICKS

    /** 主动消散阶段持续的 tick 数。 */
    @CodecField
    var discardTick: Int = 16

    /** 主动消散时是否收缩球体尺寸。 */
    @CodecField
    var discardByShrink: Boolean = true

    /** 是否已经进入主动消散阶段。 */
    @CodecField
    var discarding: Boolean = false

    /** 主动消散开始时的实体年龄。 */
    @CodecField
    var discardStartTick: Int = 0

    init {
        updateRenderRange()
    }

    override fun getRenderID(): ResourceLocation = ID

    override fun clientTick() {
        updateLifecycle()
    }

    override fun serverTick() {
        updateLifecycle()
    }

    /**
     * 配置魔法球的位置、颜色、尺寸和成长方式。
     *
     * @param pos 魔法球中心的世界坐标
     * @param color 表面和云层共用的 RGB 颜色
     * @param size 完全成长后的世界尺寸，过小值按 [MIN_SIZE] 处理
     * @param growingByScale 是否通过尺寸缩放表现成长
     * @param growingTick 成长阶段 tick 数，最小按 `1` 处理
     * @return 当前实体，可继续链式配置
     */
    fun configure(
        pos: Vec3,
        color: Vector3f = this.color,
        size: Float = this.size,
        growingByScale: Boolean = this.growingByScale,
        growingTick: Int = this.growingTick,
    ): DragonMagicBallRenderEntity {
        this.pos = pos
        this.color = Vector3f(color)
        this.size = size.coerceAtLeast(MIN_SIZE)
        this.growingByScale = growingByScale
        this.growingTick = growingTick.coerceAtLeast(1)
        updateRenderRange()
        markDirty()
        return this
    }

    /**
     * 使用 [Vec3] 颜色配置魔法球。
     *
     * @return 当前实体，可继续链式配置
     */
    fun configure(
        pos: Vec3,
        color: Vec3,
        size: Float = this.size,
        growingByScale: Boolean = this.growingByScale,
        growingTick: Int = this.growingTick,
    ): DragonMagicBallRenderEntity {
        return configure(
            pos,
            Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()),
            size,
            growingByScale,
            growingTick,
        )
    }

    /**
     * 使用 JOML 向量设置魔法球颜色并标记同步数据。
     *
     * @return 当前实体，可继续链式配置
     */
    fun setColor(color: Vector3f): DragonMagicBallRenderEntity {
        this.color = Vector3f(color)
        markDirty()
        return this
    }

    /**
     * 使用 Minecraft 向量设置魔法球颜色并标记同步数据。
     *
     * @return 当前实体，可继续链式配置
     */
    fun setColor(color: Vec3): DragonMagicBallRenderEntity {
        return setColor(Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()))
    }

    /**
     * 设置魔法球尺寸并同步更新渲染范围。
     *
     * @return 当前实体，可继续链式配置
     */
    fun setSize(size: Float): DragonMagicBallRenderEntity {
        this.size = size.coerceAtLeast(MIN_SIZE)
        updateRenderRange()
        markDirty()
        return this
    }

    /**
     * 设置非负亮度倍率并标记同步数据。
     *
     * @return 当前实体，可继续链式配置
     */
    fun setBrightness(brightness: Float): DragonMagicBallRenderEntity {
        this.brightness = brightness.coerceAtLeast(0F)
        markDirty()
        return this
    }

    /**
     * 设置是否向 mask attachment 写入辉光。
     *
     * @return 当前实体，可继续链式配置
     */
    fun setBloomEnabled(enabled: Boolean): DragonMagicBallRenderEntity {
        bloomEnabled = enabled
        markDirty()
        return this
    }

    /**
     * 开始主动消散并请求同步。
     *
     * @param shrink 消散时是否收缩球体；为 `false` 时改为透明度淡出
     * @return 当前实体；已经消散时保持原状态
     */
    fun discard(shrink: Boolean = discardByShrink): DragonMagicBallRenderEntity {
        if (discarding) {
            return this
        }
        discardByShrink = shrink
        discarding = true
        discardStartTick = age
        if (discardTick <= 0) {
            canceled = true
        }
        markDirty()
        requestSync()
        return this
    }

    /** 更新渲染范围，并在主动消散结束后终止实体。 */
    private fun updateLifecycle() {
        updateRenderRange()
        if (!discarding) {
            return
        }
        if (age - discardStartTick >= discardTick.coerceAtLeast(0)) {
            canceled = true
            markDirty()
        }
    }

    /** 按最大球体尺寸同步服务端可见范围。 */
    private fun updateRenderRange() {
        val viewPadding = 32.0
        renderRange = size.coerceAtLeast(MIN_SIZE).toDouble() * 24.0 + viewPadding
    }

    /**
     * 计算当前帧魔法球尺寸。
     *
     * @return 叠加成长、脉冲与主动收缩后的世界尺寸
     */
    internal fun currentSize(tickDelta: Float, growProgress: Float = currentGrowProgress(tickDelta)): Float {
        // 成长阶段先应用缩放与呼吸脉冲，主动消散时再用幂曲线收缩。
        val baseSize = size.coerceAtLeast(MIN_SIZE)
        val pulse = 1F + sin((age - 1F + tickDelta) * 0.12F) * 0.018F
        val growFactor = if (growingByScale) max(0.03F, growProgress) else 1F
        if (!discarding || !discardByShrink) {
            return baseSize * pulse * growFactor
        }
        val fade = 1F - currentDiscardProgress(tickDelta)
        return baseSize * pulse * growFactor * fade.pow(1.22F)
    }

    /**
     * 计算当前帧魔法球透明度。
     *
     * @return 叠加成长和主动淡出后的透明度
     */
    internal fun currentAlpha(tickDelta: Float, growProgress: Float = currentGrowProgress(tickDelta)): Float {
        val baseAlpha = alpha.toFloat().coerceIn(0F, 1F)
        val appearFactor = if (growingByScale) 1F else growProgress.coerceIn(0F, 1F)
        if (!discarding || discardByShrink) {
            return baseAlpha * appearFactor
        }
        val fade = 1F - currentDiscardProgress(tickDelta)
        return baseAlpha * appearFactor * fade * fade
    }

    /**
     * 计算消散阶段进度。
     *
     * @return 未消散时为 `0F`，否则返回 `0F..1F` 的进度
     */
    internal fun currentDiscardProgress(tickDelta: Float): Float {
        if (!discarding) {
            return 0F
        }
        val duration = discardTick.coerceAtLeast(1).toFloat()
        val elapsed = age - discardStartTick - 1F + tickDelta
        return (elapsed / duration).coerceIn(0F, 1F)
    }

    /**
     * 计算成长阶段进度。
     *
     * @return 位于 `0F..1F` 的成长进度
     */
    internal fun currentGrowProgress(tickDelta: Float): Float {
        val duration = growingTick.coerceAtLeast(1).toFloat()
        val elapsed = age - 1F + tickDelta
        return (elapsed / duration).coerceIn(0F, 1F)
    }

    /**
     * 计算表面呼吸脉冲。
     *
     * @return 位于 `0F..1F` 的周期脉冲值
     */
    internal fun currentPulse(tickDelta: Float): Float {
        return 0.5F + 0.5F * sin((age - 1F + tickDelta) * 0.30F + 0.8F)
    }

    /**
     * 把同步颜色限制到 shader 接受的范围。
     *
     * @return 可直接传给 shader 的 RGB 颜色副本
     */
    internal fun normalizedColor(): Vector3f {
        val normalized = Vector3f(
            color.x.coerceIn(0F, 1F),
            color.y.coerceIn(0F, 1F),
            color.z.coerceIn(0F, 1F),
        )
        if (max(max(normalized.x, normalized.y), normalized.z) < 0.05F) {
            return Vector3f(1.0F, 1.0F, 1.0F)
        }
        return normalized
    }

    /**
     * 叠加实体亮度设置并限制最终亮度。
     *
     * @return 乘入非负实体亮度后的值
     */
    internal fun currentBrightness(base: Float): Float {
        return base * brightness.coerceAtLeast(0F)
    }

    companion object {
        /** 龙魔法球实体的稳定注册路径。 */
        private const val RENDER_ENTITY_ID = "dragon_magic_ball_render_entity"

        /** 字段默认值和两个 spawn 重载共用的魔法球尺寸。 */
        internal const val DEFAULT_SIZE = 1.6F
        /** 字段默认值和两个 spawn 重载共用的成长 tick 数。 */
        private const val DEFAULT_GROWING_TICKS = 12
        /** 实体状态和 renderer 共用的最小有效尺寸。 */
        internal const val MIN_SIZE = 0.025F

        /** 龙魔法球的稳定 RenderEntity 注册 ID。 */
        @JvmField
        val ID: ResourceLocation =
            ofID(UsefulMagic.MOD_ID, RENDER_ENTITY_ID)

        /**
         * 使用 [Vector3f] 颜色生成并注册一个龙魔法球。
         *
         * @return 已提交到服务端 RenderEntity 管理器的魔法球
         */
        @JvmStatic
        fun spawn(
            world: ServerLevel,
            pos: Vec3,
            color: Vector3f = Vector3f(1.0F, 1.0F, 1.0F),
            size: Float = DEFAULT_SIZE,
            growingByScale: Boolean = true,
            growingTick: Int = DEFAULT_GROWING_TICKS,
        ): DragonMagicBallRenderEntity {
            return DragonMagicBallRenderEntity(world)
                .configure(pos, color, size, growingByScale, growingTick)
                .also(ServerRenderEntityManager::spawn)
        }

        /**
         * 使用 [Vec3] 颜色生成并注册一个龙魔法球。
         *
         * @return 已提交到服务端 RenderEntity 管理器的魔法球
         */
        @JvmStatic
        fun spawn(
            world: ServerLevel,
            pos: Vec3,
            color: Vec3,
            size: Float = DEFAULT_SIZE,
            growingByScale: Boolean = true,
            growingTick: Int = DEFAULT_GROWING_TICKS,
        ): DragonMagicBallRenderEntity {
            return spawn(
                world,
                pos,
                Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()),
                size,
                growingByScale,
                growingTick,
            )
        }
    }
}
