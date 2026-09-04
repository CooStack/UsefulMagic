package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.extend.ofID
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

/** 保存陨石大气火焰的同步状态、方向和淡入淡出生命周期。 */
@CooAutoRegister
class MeteoriteAtmosphereFireRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos) {
    /** 火焰椭球的朝向，非法零向量会回退为向下。 */
    @CodecField
    var direction: Vec3 = Vec3(0.0, -1.0, 0.0)

    /** 大气火焰的同步 RGB 颜色。 */
    @CodecField
    var color: Vector3f = Vector3f(1.0F, 0.42F, 0.10F)

    /** 火焰椭球的基础世界尺寸。 */
    @CodecField
    var size: Float = 1.35F

    /** 火焰整体透明度，渲染时限制到有效范围。 */
    @CodecField
    var alpha: Double = 1.0

    /** 完成淡入后维持火焰的 tick 数。 */
    @CodecField
    var lifetime: Int = DEFAULT_LIFETIME

    /** 火焰淡入阶段持续的 tick 数。 */
    @CodecField
    var fadeInTicks: Int = DEFAULT_FADE_IN_TICKS

    /** 火焰自然淡出阶段持续的 tick 数。 */
    @CodecField
    var fadeOutTicks: Int = DEFAULT_FADE_OUT_TICKS

    /** 火焰噪声纹理的流动速度倍率。 */
    @CodecField
    var flowSpeed: Float = 1.0F

    /** mask bloom 的强度倍率。 */
    @CodecField
    var bloomStrength: Float = 1.0F

    /** 是否已经进入主动消散阶段。 */
    @CodecField
    var discarding: Boolean = false

    /** 主动消散开始时的实体年龄。 */
    @CodecField
    var discardStartTick: Int = 0

    /** 主动消散阶段持续的 tick 数。 */
    @CodecField
    var discardTicks: Int = DEFAULT_FADE_OUT_TICKS

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
     * 配置大气火焰的位置、方向、尺寸和生命周期。
     *
     * @param center 火焰中心的世界坐标
     * @param moveDirection 陨石移动方向，零向量回退为竖直向下
     * @param meteoriteSize 火焰基础尺寸，过小值按 [MIN_SIZE] 处理
     * @param flameColor 火焰 RGB 颜色
     * @param maxLifetime 总生命周期 tick 数，最小按 `1` 处理
     * @param fadeIn 淡入 tick 数，负数按 `0` 处理
     * @param fadeOut 自然淡出 tick 数，负数按 `0` 处理
     * @param opacity 整体透明度，限制到 `0.0..1.0`
     * @return 当前实体，可继续链式配置
     */
    fun configure(
        center: Vec3,
        moveDirection: Vec3,
        meteoriteSize: Float,
        flameColor: Vector3f = color,
        maxLifetime: Int = lifetime,
        fadeIn: Int = fadeInTicks,
        fadeOut: Int = fadeOutTicks,
        opacity: Double = alpha,
    ): MeteoriteAtmosphereFireRenderEntity {
        pos = center
        direction = safeDirection(moveDirection)
        size = meteoriteSize.coerceAtLeast(MIN_SIZE)
        color = Vector3f(flameColor)
        lifetime = maxLifetime.coerceAtLeast(1)
        fadeInTicks = fadeIn.coerceAtLeast(0)
        fadeOutTicks = fadeOut.coerceAtLeast(0)
        discardTicks = fadeOutTicks
        discarding = false
        discardStartTick = 0
        alpha = opacity.coerceIn(0.0, 1.0)
        updateRenderRange()
        markDirty()
        return this
    }

    /**
     * 更新火焰中心和移动方向。
     *
     * @param center 新中心世界坐标
     * @param moveDirection 新移动方向，零向量回退为竖直向下
     * @return 当前实体
     */
    fun moveTo(center: Vec3, moveDirection: Vec3 = direction): MeteoriteAtmosphereFireRenderEntity {
        pos = center
        direction = safeDirection(moveDirection)
        markDirty()
        return this
    }

    /** 按当前自然淡出时长开始结束火焰。 */
    fun finish() {
        discard()
    }

    /**
     * 开始主动淡出。
     *
     * @param fadeTicks 主动淡出 tick 数，负数按 `0` 处理
     * @return 当前实体；已在消散或已取消时保持原状态
     */
    fun discard(fadeTicks: Int = fadeOutTicks): MeteoriteAtmosphereFireRenderEntity {
        if (discarding || canceled) {
            return this
        }
        discardTicks = fadeTicks.coerceAtLeast(0)
        discardStartTick = age
        discarding = true
        if (discardTicks == 0) {
            canceled = true
        }
        markDirty()
        return this
    }

    /** 更新自然或主动消散生命周期，并同步终止状态。 */
    private fun updateLifecycle() {
        updateRenderRange()
        if (discarding) {
            if (age - discardStartTick >= discardTicks.coerceAtLeast(0)) {
                canceled = true
                markDirty()
            }
            return
        }
        if (age > lifetime.coerceAtLeast(1)) {
            canceled = true
            markDirty()
        }
    }

    /** 按火焰尺寸同步服务端可见范围。 */
    private fun updateRenderRange() {
        renderRange = size.coerceAtLeast(MIN_SIZE).toDouble() * 18.0 + 48.0
    }

    /**
     * 返回包含帧插值的火焰流动时间。
     *
     * @return 从实体首个渲染 tick 开始计算的非负时间
     */
    internal fun currentTime(tickDelta: Float): Float {
        return (age - 1F + tickDelta).coerceAtLeast(0F)
    }

    /**
     * 计算当前帧火焰透明度。
     *
     * @return 叠加自然淡入、自然淡出和主动淡出后的透明度
     */
    internal fun currentAlpha(tickDelta: Float): Float {
        val time = currentTime(tickDelta)
        // 分别计算自然淡入、自然淡出和主动淡出，再相乘保证任一结束路径都能归零。
        val maxLifetime = lifetime.coerceAtLeast(1).toFloat()
        val fadeIn = fadeInTicks.coerceAtLeast(0).toFloat()
        val fadeOut = fadeOutTicks.coerceAtLeast(0).toFloat().coerceAtMost(maxLifetime)
        val inAlpha = if (fadeIn <= 0F) 1F else smoothstep(0F, fadeIn, time)
        val outStart = maxLifetime - fadeOut
        val outAlpha = if (fadeOut <= 0F || time <= outStart) {
            1F
        } else {
            1F - smoothstep(0F, fadeOut, time - outStart)
        }
        val discardAlpha = if (!discarding) {
            1F
        } else {
            val discardDuration = discardTicks.coerceAtLeast(0).toFloat()
            if (discardDuration <= 0F) {
                0F
            } else {
                1F - smoothstep(0F, discardDuration, time - discardStartTick.toFloat())
            }
        }
        return alpha.toFloat().coerceIn(0F, 1F) * inAlpha * outAlpha * discardAlpha
    }

    companion object {
        /** 陨石大气火焰实体的稳定注册路径。 */
        private const val RENDER_ENTITY_ID = "meteorite_atmosphere_fire_render_entity"

        /** 字段默认值和 spawn 入口共用的生命周期。 */
        private const val DEFAULT_LIFETIME = 80
        /** 字段默认值和 spawn 入口共用的淡入 tick 数。 */
        private const val DEFAULT_FADE_IN_TICKS = 6
        /** 字段默认值和 spawn 入口共用的淡出 tick 数。 */
        private const val DEFAULT_FADE_OUT_TICKS = 14
        /** 实体状态和 renderer 共用的最小有效尺寸。 */
        internal const val MIN_SIZE = 0.05F
        /** 陨石大气火焰的稳定 RenderEntity 注册 ID。 */
        @JvmField
        val ID: ResourceLocation =
            ofID(UsefulMagic.MOD_ID, RENDER_ENTITY_ID)

        /**
         * 生成并注册一个沿指定方向流动的大气火焰实体。
         *
         * @return 已提交到服务端 RenderEntity 管理器的火焰实体
         */
        @JvmStatic
        fun spawn(
            world: ServerLevel,
            center: Vec3,
            moveDirection: Vec3,
            size: Float,
            color: Vector3f = Vector3f(1.0F, 0.42F, 0.10F),
            lifetime: Int = DEFAULT_LIFETIME,
            fadeInTicks: Int = DEFAULT_FADE_IN_TICKS,
            fadeOutTicks: Int = DEFAULT_FADE_OUT_TICKS,
            alpha: Double = 1.0,
        ): MeteoriteAtmosphereFireRenderEntity {
            return MeteoriteAtmosphereFireRenderEntity(world)
                .configure(center, moveDirection, size, color, lifetime, fadeInTicks, fadeOutTicks, alpha)
                .also(ServerRenderEntityManager::spawn)
        }

        /**
         * 返回可用于旋转和 shader 计算的安全单位方向。
         *
         * @return 输入方向的单位向量，零向量回退为竖直向下
         */
        @JvmStatic
        fun safeDirection(direction: Vec3): Vec3 {
            if (direction.lengthSqr() <= 0.00000001) {
                return Vec3(0.0, -1.0, 0.0)
            }
            return direction.normalize()
        }

        /**
         * 返回区间内平滑过渡的插值值。
         *
         * @return 位于 `0F..1F` 的平滑插值进度
         */
        @JvmStatic
        fun smoothstep(edge0: Float, edge1: Float, value: Float): Float {
            if (edge0 == edge1) {
                return if (value >= edge1) 1F else 0F
            }
            val x = ((value - edge0) / (edge1 - edge0)).coerceIn(0F, 1F)
            return x * x * (3F - 2F * x)
        }

    }
}
