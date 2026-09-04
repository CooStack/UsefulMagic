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
import kotlin.math.PI
import kotlin.random.Random

/** 保存冲击波公告板的同步状态、缩放曲线和淡入淡出生命周期。 */
@CooAutoRegister
class ShotWaveBillboardRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos) {
    /** 冲击波淡入阶段持续的 tick 数。 */
    @CodecField
    var fadeInTick: Int = DEFAULT_FADE_IN_TICK

    /** 到达超时点后的淡出 tick 数。 */
    @CodecField
    var fadeOutTick: Int = DEFAULT_FADE_OUT_TICK

    /** 冲击波自然播放时的目标世界尺寸。 */
    @CodecField
    var limitScale: Float = DEFAULT_LIMIT_SCALE

    /** 冲击波生成时的世界尺寸。 */
    @CodecField
    var initialScale: Float = DEFAULT_INITIAL_SCALE

    /** 冲击波开始自然淡出的实体年龄。 */
    @CodecField
    var timeoutTick: Int = DEFAULT_TIMEOUT_TICK

    /** 冲击波每 tick 的尺寸变化量。 */
    @CodecField
    var scaleSpeed: Float = DEFAULT_SCALE_SPEED

    /** 冲击波公告板当前的旋转弧度。 */
    @CodecField
    var roll: Float = randomRoll()

    /** 冲击波公告板每 tick 的旋转弧度。 */
    @CodecField
    var rollSpeed: Float = DEFAULT_ROLL_SPEED

    /** 是否使用第二套冲击波纹理。 */
    @CodecField
    var useWave2Texture: Boolean = DEFAULT_USE_WAVE_2_TEXTURE

    /** 冲击波整体透明度，渲染时限制到有效范围。 */
    @CodecField
    var alpha: Double = 1.0

    /** 主动消散阶段持续的 tick 数。 */
    @CodecField
    var discardFadeTick: Int = DEFAULT_FADE_OUT_TICK

    /** 是否已经进入主动消散阶段。 */
    @CodecField
    var discarding: Boolean = false

    /** 主动消散开始时的实体年龄。 */
    @CodecField
    var discardStartTick: Int = 0

    /** 主动消散开始时记录的透明度。 */
    @CodecField
    var discardStartAlpha: Float = 1.0F

    /** `limitScale` 的兼容属性。 */
    @Deprecated("Use limitScale instead.")
    var maxScale: Float
        get() = limitScale
        set(value) {
            limitScale = value
        }

    init {
        syncRenderRange()
    }

    override fun getRenderID(): ResourceLocation = ID

    override fun clientTick() {
        updateLifecycle()
        syncRenderRange()
    }

    override fun serverTick() {
        updateLifecycle()
        syncRenderRange()
    }

    /**
     * 配置冲击波的缩放、旋转和淡入淡出曲线。
     *
     * @param fadeInTick 淡入 tick 数，负数按 `0` 处理
     * @param fadeOutTick 自然淡出 tick 数，负数按 `0` 处理
     * @param limitScale 自然播放时的目标尺寸
     * @param timeoutTick 完成淡入后等待的 tick 数
     * @param scaleSpeed 每 tick 尺寸变化量，零值会转换为最小正速度
     * @param roll 初始旋转弧度；为 `null` 时随机生成
     * @param alpha 整体透明度，限制到 `0.0..1.0`
     * @param initialScale 初始尺寸；为 `null` 时按目标尺寸和速度推导
     * @param useWave2Texture 是否使用第二套冲击波纹理
     * @param rollSpeed 每 tick 旋转弧度
     * @return 当前实体，可继续链式配置
     */
    fun configure(
        fadeInTick: Int = this.fadeInTick,
        fadeOutTick: Int = this.fadeOutTick,
        limitScale: Float = this.limitScale,
        timeoutTick: Int = this.timeoutTick,
        scaleSpeed: Float = this.scaleSpeed,
        roll: Float? = null,
        alpha: Double = this.alpha,
        initialScale: Float? = null,
        useWave2Texture: Boolean = this.useWave2Texture,
        rollSpeed: Float = this.rollSpeed,
    ): ShotWaveBillboardRenderEntity {
        this.fadeInTick = fadeInTick.coerceAtLeast(0)
        this.fadeOutTick = fadeOutTick.coerceAtLeast(0)
        this.timeoutTick = timeoutTick.coerceAtLeast(0)
        this.limitScale = limitScale.coerceAtLeast(MIN_SCALE)
        this.scaleSpeed = normalizeScaleSpeed(scaleSpeed)
        this.initialScale = normalizeInitialScale(
            initialScale ?: defaultInitialScale(this.limitScale, this.scaleSpeed, totalDurationTicks()),
            this.limitScale,
            this.scaleSpeed,
        )
        this.roll = roll ?: randomRoll()
        this.rollSpeed = rollSpeed
        this.useWave2Texture = useWave2Texture
        this.alpha = alpha.coerceIn(0.0, 1.0)
        this.discardFadeTick = this.fadeOutTick
        this.discarding = false
        this.discardStartTick = 0
        this.discardStartAlpha = 1.0F
        syncRenderRange()
        markDirty()
        return this
    }

    /**
     * 从当前透明度开始主动淡出。
     *
     * @param fadeTicks 淡出 tick 数，负数按 `0` 处理
     * @return 当前实体
     */
    fun discard(fadeTicks: Int = fadeOutTick): ShotWaveBillboardRenderEntity {
        startFadeOut(fadeTicks, sync = true)
        return this
    }

    /** 在达到目标尺寸、主动淡出结束或总时长结束时终止实体。 */
    private fun updateLifecycle() {
        val scale = currentScale(0F)
        if (!discarding && reachedLimit(scale)) {
            startFadeOut(fadeOutTick, sync = false)
            return
        }
        if (discarding && age - discardStartTick >= discardFadeTick.coerceAtLeast(0)) {
            canceled = true
            return
        }
        if (age > totalDurationTicks() + 1) {
            canceled = true
        }
    }

    /** 按动画可能达到的最大尺寸同步服务端可见范围。 */
    private fun syncRenderRange() {
        renderRange = expectedMaxScale().coerceAtLeast(MIN_SCALE).toDouble() * 2.4 + 16.0
    }

    /**
     * 计算当前缩放方向下动画可能达到的最大尺寸。
     *
     * @return 用于渲染范围估算的最大世界尺寸
     */
    private fun expectedMaxScale(): Float {
        if (scaleSpeed <= 0F) {
            return maxOf(initialScale, limitScale)
        }
        return maxOf(initialScale, limitScale, initialScale + totalDurationTicks().coerceAtLeast(1) * scaleSpeed)
    }

    /**
     * 计算自然淡入、停留和淡出的总时长。
     *
     * @return 非负的总动画 tick 数
     */
    private fun totalDurationTicks(): Int {
        return fadeInTick.coerceAtLeast(0) + timeoutTick.coerceAtLeast(0) + fadeOutTick.coerceAtLeast(0)
    }

    /**
     * 返回包含帧插值的生命周期时间。
     *
     * @return 从实体首个渲染 tick 开始计算的非负时间
     */
    internal fun currentTimeline(tickDelta: Float): Float {
        return (age - 1F + tickDelta).coerceAtLeast(0F)
    }

    /**
     * 计算当前帧公告板尺寸。
     *
     * @return 按缩放方向限制后的非负世界尺寸
     */
    internal fun currentScale(tickDelta: Float): Float {
        // 沿配置方向线性推进；反向缩放只在目标尺寸处钳制，正向缩放由生命周期负责结束。
        val rawScale = initialScale + currentTimeline(tickDelta) * scaleSpeed
        return if (scaleSpeed >= 0F) {
            rawScale
        } else {
            rawScale.coerceAtLeast(limitScale)
        }.coerceAtLeast(0F)
    }

    /**
     * 计算淡入淡出和主动丢弃后的当前透明度。
     *
     * @return 当前渲染帧的透明度
     */
    internal fun currentAlpha(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val scheduledAlpha = scheduledAlpha(time)
        // 主动淡出从触发帧的计划透明度重新计时，避免切换路径时亮度跳变。
        if (discarding) {
            val elapsed = age - discardStartTick - 1F + tickDelta
            val fade = if (discardFadeTick <= 0) {
                0F
            } else {
                1F - (elapsed / discardFadeTick.toFloat()).coerceIn(0F, 1F)
            }
            return discardStartAlpha * fade
        }
        return scheduledAlpha
    }

    /**
     * 计算自然时间表上的淡入淡出透明度。
     *
     * @return 未叠加主动淡出的计划透明度
     */
    private fun scheduledAlpha(time: Float): Float {
        // 先把三个阶段换算为边界时间，再分别计算淡入与淡出权重。
        val fadeIn = fadeInTick.coerceAtLeast(0).toFloat()
        val alive = timeoutTick.coerceAtLeast(0).toFloat()
        val fadeOut = fadeOutTick.coerceAtLeast(0).toFloat()
        val fadeOutStart = fadeIn + alive
        val baseAlpha = alpha.toFloat().coerceIn(0F, 1F)
        val inAlpha = if (fadeIn <= 0F) 1F else smoothstep(0F, fadeIn, time)
        val outAlpha = if (fadeOut <= 0F) {
            if (time <= fadeOutStart) 1F else 0F
        } else {
            1F - smoothstep(fadeOutStart, fadeOutStart + fadeOut, time)
        }
        return baseAlpha * inAlpha * outAlpha
    }

    /** 从当前计划透明度开始主动淡出，并按调用方要求请求同步。 */
    private fun startFadeOut(fadeTicks: Int, sync: Boolean) {
        if (discarding || canceled) {
            return
        }
        discardFadeTick = fadeTicks.coerceAtLeast(0)
        discardStartAlpha = scheduledAlpha(currentTimeline(0F))
        discardStartTick = age
        discarding = true
        if (discardFadeTick == 0) {
            canceled = true
        }
        markDirty()
        if (sync) {
            requestSync()
        }
    }

    /**
     * 判断当前尺寸是否沿既定方向到达目标值。
     *
     * @return 已到达或越过目标尺寸时返回 `true`
     */
    private fun reachedLimit(scale: Float): Boolean {
        return if (scaleSpeed >= 0F) {
            scale >= limitScale.coerceAtLeast(MIN_SCALE)
        } else {
            scale <= limitScale.coerceAtLeast(MIN_SCALE)
        }
    }

    /**
     * 保留缩放方向并避免零速度造成永不结束的动画。
     *
     * @return 绝对值不小于最小缩放速度的结果
     */
    private fun normalizeScaleSpeed(value: Float): Float {
        val minimumScaleSpeed = 0.0001F
        return when {
            value > 0F -> value.coerceAtLeast(minimumScaleSpeed)
            value < 0F -> value.coerceAtMost(-minimumScaleSpeed)
            else -> minimumScaleSpeed
        }
    }

    /**
     * 修正与缩放方向冲突的初始尺寸。
     *
     * @return 能沿指定速度到达目标尺寸的初始值
     */
    private fun normalizeInitialScale(value: Float, limit: Float, speed: Float): Float {
        val scale = value.coerceAtLeast(MIN_SCALE)
        // 初始值若已经位于目标的错误一侧，就重建为能沿当前速度穿过目标的起点。
        return if (speed >= 0F && scale >= limit) {
            MIN_SCALE
        } else if (speed < 0F && scale <= limit) {
            defaultInitialScale(limit, speed, totalDurationTicks())
        } else {
            scale
        }
    }

    companion object {
        /** 冲击波公告板实体的稳定注册路径。 */
        private const val RENDER_ENTITY_ID = "shot_wave_billboard_render_entity"

        /** 字段默认值和 spawn 入口共用的淡入 tick 数。 */
        private const val DEFAULT_FADE_IN_TICK = 4
        /** 字段默认值、丢弃逻辑和 spawn 入口共用的淡出 tick 数。 */
        private const val DEFAULT_FADE_OUT_TICK = 8
        /** 字段默认值和 spawn 入口共用的目标尺寸。 */
        private const val DEFAULT_LIMIT_SCALE = 8.0F
        /** 字段默认值和反向缩放计算共用的初始尺寸。 */
        private const val DEFAULT_INITIAL_SCALE = 0.0F
        /** 字段默认值和 spawn 入口共用的停留 tick 数。 */
        private const val DEFAULT_TIMEOUT_TICK = 12
        /** 字段默认值和 spawn 入口共用的缩放速度。 */
        private const val DEFAULT_SCALE_SPEED = 0.34F
        /** 字段默认值和 spawn 入口共用的旋转速度。 */
        private const val DEFAULT_ROLL_SPEED = 0.16F
        /** 字段默认值和 spawn 入口共用的纹理选择。 */
        private const val DEFAULT_USE_WAVE_2_TEXTURE = true
        /** 多个缩放计算共用的最小有效尺寸。 */
        private const val MIN_SCALE = 0.001F
        /** 冲击波公告板的稳定 RenderEntity 注册 ID。 */
        @JvmField
        val ID: ResourceLocation =
            ofID(UsefulMagic.MOD_ID, RENDER_ENTITY_ID)

        /**
         * 生成并注册一个按指定缩放曲线播放的冲击波公告板。
         *
         * @return 已提交到服务端 RenderEntity 管理器的冲击波实体
         */
        @JvmStatic
        fun spawn(
            world: ServerLevel,
            pos: Vec3,
            fadeInTick: Int = DEFAULT_FADE_IN_TICK,
            fadeOutTick: Int = DEFAULT_FADE_OUT_TICK,
            limitScale: Float = DEFAULT_LIMIT_SCALE,
            timeoutTick: Int = DEFAULT_TIMEOUT_TICK,
            scaleSpeed: Float = DEFAULT_SCALE_SPEED,
            roll: Float? = null,
            alpha: Double = 1.0,
            initialScale: Float? = null,
            useWave2Texture: Boolean = DEFAULT_USE_WAVE_2_TEXTURE,
            rollSpeed: Float = DEFAULT_ROLL_SPEED,
        ): ShotWaveBillboardRenderEntity {
            return ShotWaveBillboardRenderEntity(world, pos)
                .configure(
                    fadeInTick = fadeInTick,
                    fadeOutTick = fadeOutTick,
                    limitScale = limitScale,
                    timeoutTick = timeoutTick,
                    scaleSpeed = scaleSpeed,
                    roll = roll,
                    alpha = alpha,
                    initialScale = initialScale,
                    useWave2Texture = useWave2Texture,
                    rollSpeed = rollSpeed,
                )
                .also(ServerRenderEntityManager::spawn)
        }

        /**
         * 根据缩放方向和总时长推导默认初始尺寸。
         *
         * @return 正向缩放的默认最小值，或反向缩放所需的起始值
         */
        private fun defaultInitialScale(limit: Float, speed: Float, durationTicks: Int): Float {
            return if (speed >= 0F) {
                DEFAULT_INITIAL_SCALE
            } else {
                limit + -speed * durationTicks.coerceAtLeast(1)
            }
        }

        /**
         * 生成一个完整圆周内的随机初始旋转角。
         *
         * @return 位于一周弧度范围内的随机角度
         */
        private fun randomRoll(): Float {
            return Random.nextFloat() * (PI.toFloat() * 2F)
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
    }
}
