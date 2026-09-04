package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.renderer.AutoRenderEntity
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.cooparticlesapi.utils.interpolator.data.InterpolatorVec3d
import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.sin

/** 保存直线激光的同步端点、生命周期和渲染范围。 */
@CooAutoRegister
class StraightLaserRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos) {
    @CodecField
    var startInterpolator = InterpolatorVec3d(pos)

    @CodecField
    var endInterpolator = InterpolatorVec3d(pos)

    /** 跟随实体的网络 ID，负值表示使用固定起点。 */
    @CodecField
    var sourceEntityId: Int = NO_SOURCE_ENTITY

    /** 激光扩张和收束阶段的持续 tick 数。 */
    @CodecField
    var phaseTicks: Int = 6

    /** 激光完全展开后的保持 tick 数。 */
    @CodecField
    var lifetime: Int = 12

    /** 激光的同步 RGB 颜色。 */
    @CodecField
    var color: Vector3f = Vector3f(0.28F, 0.82F, 1.0F)

    /** 激光整体透明度，渲染时限制到有效范围。 */
    @CodecField
    var alpha: Double = 1.0

    /** 激光本体和辉光共用的亮度倍率。 */
    @CodecField
    var brightness: Float = 1.0F

    /** 激光完全展开时的世界半径。 */
    @CodecField
    var maxRadius: Float = DEFAULT_MAX_RADIUS

    override fun getRenderID(): ResourceLocation = ID

    override fun clientTick() {
        updateLifecycle()
    }

    override fun serverTick() {
        updateLifecycle()
    }

    /**
     * 使用固定起点和终点配置激光生命周期。
     *
     * @param start 激光起点的世界坐标
     * @param end 激光终点的世界坐标
     * @param tick 扩张和收束阶段各自的 tick 数，最小按 `1` 处理
     * @param lifetime 完全展开后的保持 tick 数，负数按 `0` 处理
     * @return 当前实体，可继续链式配置
     */
    fun configure(
        start: Vec3,
        end: Vec3,
        tick: Int,
        lifetime: Int,
    ): StraightLaserRenderEntity {
        this.sourceEntityId = NO_SOURCE_ENTITY
        this.pos = start
        this.startInterpolator.uploadData(start).uploadData(start)
        this.endInterpolator.uploadData(end).uploadData(end)
        this.phaseTicks = tick.coerceAtLeast(1)
        this.lifetime = lifetime.coerceAtLeast(0)
        markDirty()
        return this
    }

    /**
     * 以来源实体当前坐标作为固定起点配置激光。
     *
     * @param sourceEntity 提供初始起点的实体，不会在后续 tick 自动跟随
     * @param end 激光终点的世界坐标
     * @param tick 扩张和收束阶段各自的 tick 数，最小按 `1` 处理
     * @param lifetime 完全展开后的保持 tick 数，负数按 `0` 处理
     * @param color 激光 RGB 颜色
     * @param maxRadius 完全展开时的世界半径
     * @param brightness 本体和辉光共用的亮度倍率
     * @return 当前实体，可继续链式配置
     */
    fun configure(
        sourceEntity: Entity,
        end: Vec3,
        tick: Int,
        lifetime: Int,
        color: Vector3f = this.color,
        maxRadius: Float = this.maxRadius,
        brightness: Float = this.brightness,
    ): StraightLaserRenderEntity {
        this.sourceEntityId = NO_SOURCE_ENTITY
        this.pos = sourceEntity.position()
        this.startInterpolator.uploadData(pos).uploadData(pos)
        this.endInterpolator.uploadData(end).uploadData(end)
        this.phaseTicks = tick.coerceAtLeast(1)
        this.lifetime = lifetime.coerceAtLeast(0)
        setColor(color)
        setMaxRadius(maxRadius)
        setBrightness(brightness)
        markDirty()
        return this
    }

    /**
     * 使用 [Vec3] 颜色配置来源实体起点的激光。
     *
     * @return 当前实体，可继续链式配置
     */
    fun configure(
        sourceEntity: Entity,
        end: Vec3,
        tick: Int,
        lifetime: Int,
        color: Vec3,
        maxRadius: Float = this.maxRadius,
        brightness: Float = this.brightness,
    ): StraightLaserRenderEntity {
        return configure(
            sourceEntity = sourceEntity,
            end = end,
            tick = tick,
            lifetime = lifetime,
            color = Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()),
            maxRadius = maxRadius,
            brightness = brightness,
        )
    }

    /**
     * 使用 [Vector3f] 颜色配置固定端点的激光。
     *
     * @return 当前实体，可继续链式配置
     */
    fun configure(
        start: Vec3,
        end: Vec3,
        tick: Int,
        lifetime: Int,
        color: Vector3f,
        maxRadius: Float = this.maxRadius,
        brightness: Float = this.brightness,
    ): StraightLaserRenderEntity {
        configure(start, end, tick, lifetime)
        setColor(color)
        setMaxRadius(maxRadius)
        setBrightness(brightness)
        return this
    }

    /**
     * 使用 [Vec3] 颜色配置固定端点的激光。
     *
     * @return 当前实体，可继续链式配置
     */
    fun configure(
        start: Vec3,
        end: Vec3,
        tick: Int,
        lifetime: Int,
        color: Vec3,
        maxRadius: Float = this.maxRadius,
        brightness: Float = this.brightness,
    ): StraightLaserRenderEntity {
        return configure(
            start = start,
            end = end,
            tick = tick,
            lifetime = lifetime,
            color = Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()),
            maxRadius = maxRadius,
            brightness = brightness,
        )
    }

    /**
     * 使用 Minecraft 向量设置激光颜色并标记同步数据。
     *
     * @return 当前实体，可继续链式配置
     */
    fun setColor(color: Vec3): StraightLaserRenderEntity {
        return setColor(Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()))
    }

    /**
     * 使用 JOML 向量设置激光颜色并标记同步数据。
     *
     * @return 当前实体，可继续链式配置
     */
    fun setColor(color: Vector3f): StraightLaserRenderEntity {
        this.color = Vector3f(color)
        markDirty()
        return this
    }

    /**
     * 设置完全展开半径，过小值按 [MIN_RADIUS] 处理。
     *
     * @return 当前实体，可继续链式配置
     */
    fun setMaxRadius(maxRadius: Float): StraightLaserRenderEntity {
        this.maxRadius = maxRadius.coerceAtLeast(MIN_RADIUS)
        markDirty()
        return this
    }

    /**
     * 设置激光亮度并标记同步数据为已修改。
     *
     * @param brightness 新亮度，负数按 `0` 处理
     * @return 当前实体，可继续链式配置
     */
    fun setBrightness(brightness: Float): StraightLaserRenderEntity {
        this.brightness = brightness.coerceAtLeast(0F)
        markDirty()
        return this
    }

    /**
     * 更新固定激光的两个端点，并保留旧端点供客户端帧插值。
     *
     * @param start 新起点
     * @param end 新终点
     * @return 当前实体；端点未变化时不标记同步数据
     */
    fun updateBeam(start: Vec3, end: Vec3): StraightLaserRenderEntity {
        this.sourceEntityId = NO_SOURCE_ENTITY
        this.pos = start
        this.startInterpolator.uploadData(start)
        this.endInterpolator.uploadData(end)
        markDirty()
        return this
    }

    /**
     * 跳到收束阶段并请求立即同步。
     *
     * @return 当前实体
     */
    fun discard(): StraightLaserRenderEntity {
        val collapseStart = phaseTicks.coerceAtLeast(1) + lifetime.coerceAtLeast(0)
        if (age < collapseStart) {
            age = collapseStart
            requestSync()
        }
        return this
    }

    /**
     * 解析当前同步的来源实体。
     *
     * @return 当前世界中的来源实体，没有来源或实体已移除时返回 `null`
     */
    private fun sourceEntity(): Entity? {
        if (sourceEntityId == NO_SOURCE_ENTITY) {
            return null
        }
        return world?.getEntity(sourceEntityId)
    }

    /**
     * 在上一帧和当前起点之间插值。
     *
     * @return 当前渲染帧的激光起点
     */
    internal fun renderStart(tickDelta: Float): Vec3 {
        return startInterpolator.getWithInterpolator(tickDelta.toDouble().coerceIn(0.0, 1.0))
    }

    /**
     * 在上一帧和当前终点之间插值。
     *
     * @return 当前渲染帧的激光终点
     */
    /**
     * 在上一帧和当前终点之间插值。
     *
     * @return 当前渲染帧的激光终点
     */
    internal fun renderEnd(tickDelta: Float): Vec3 {
        return endInterpolator.getWithInterpolator(tickDelta.toDouble().coerceIn(0.0, 1.0))
    }

    /**
     * 返回指定端点之间的有效激光长度。
     *
     * @return 不小于 [MIN_BEAM_LENGTH] 的世界长度
     */
    internal fun beamLength(start: Vec3 = pos, end: Vec3 = this.endInterpolator.getCurrent()): Float {
        return start.distanceTo(end).toFloat().coerceAtLeast(MIN_BEAM_LENGTH)
    }

    /** 保存上一 tick 端点，并在完整动画结束后终止实体。 */
    private fun updateLifecycle() {
        startInterpolator.flushFrame()
        endInterpolator.flushFrame()
        if (age > totalDurationTicks() + 1) {
            canceled = true
        }
    }

    /**
     * 计算扩张、停留和收束阶段的总时长。
     *
     * @return 至少包含两个阶段 tick 的总时长
     */
    private fun totalDurationTicks(): Int {
        val grow = phaseTicks.coerceAtLeast(1)
        return grow + lifetime.coerceAtLeast(0) + grow
    }

    /**
     * 返回包含帧插值的生命周期时间。
     *
     * @return 从实体首个渲染 tick 开始计算的非负时间
     */
    private fun currentTimeline(tickDelta: Float): Float {
        return (age - 1F + tickDelta).coerceAtLeast(0F)
    }

    /**
     * 计算当前帧激光半径。
     *
     * @return 当前生命周期阶段的世界半径
     */
    internal fun currentRadius(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        // 扩张阶段缓出到最大半径，停留阶段保持，最后反向插值到最小半径。
        val growDuration = phaseTicks.coerceAtLeast(1).toFloat()
        val holdEnd = growDuration + lifetime.coerceAtLeast(0).toFloat()
        return when {
            time < growDuration -> {
                val grow = easeOutCubic(smoothstep(0F, growDuration, time))
                mix(MIN_RADIUS, maxRadius.coerceAtLeast(MIN_RADIUS), grow)
            }

            time < holdEnd -> maxRadius.coerceAtLeast(MIN_RADIUS)
            else -> {
                val collapse = currentCollapse(tickDelta)
                mix(maxRadius.coerceAtLeast(MIN_RADIUS), MIN_RADIUS, collapse)
            }
        }
    }

    /**
     * 计算当前帧激光主体透明度。
     *
     * @return 叠加扩张与收束曲线后的主体透明度
     */
    internal fun currentBodyAlpha(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val growDuration = phaseTicks.coerceAtLeast(1).toFloat()
        val holdEnd = growDuration + lifetime.coerceAtLeast(0).toFloat()
        val baseAlpha = alpha.toFloat().coerceIn(0F, 1F)
        return when {
            time < growDuration -> {
                val grow = easeOutCubic(smoothstep(0F, growDuration, time))
                mix(0.24F, 1.0F, grow) * baseAlpha
            }

            time < holdEnd -> baseAlpha
            else -> {
                val fade = 1F - currentCollapse(tickDelta)
                fade * fade * baseAlpha
            }
        }
    }

    /**
     * 计算当前帧写入泛光遮罩的透明度。
     *
     * @return 叠加呼吸脉冲和收束曲线后的 mask 透明度
     */
    internal fun currentBloomAlpha(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val growDuration = phaseTicks.coerceAtLeast(1).toFloat()
        val holdEnd = growDuration + lifetime.coerceAtLeast(0).toFloat()
        val baseAlpha = alpha.toFloat().coerceIn(0F, 1F)
        return when {
            time < growDuration -> mix(0.34F, 1F, easeOutCubic(smoothstep(0F, growDuration, time))) * baseAlpha
            time < holdEnd -> mix(1F, 1.16F, currentPulse(tickDelta)) * baseAlpha
            else -> {
                val fade = 1F - currentCollapse(tickDelta)
                fade * fade * fade * baseAlpha
            }
        }
    }

    /**
     * 计算生长或收束阶段进度。
     *
     * @return 扩张时递增、收束时递减的阶段进度
     */
    internal fun currentPhaseProgress(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val growDuration = phaseTicks.coerceAtLeast(1).toFloat()
        val holdEnd = growDuration + lifetime.coerceAtLeast(0).toFloat()
        return when {
            time < growDuration -> smoothstep(0F, growDuration, time)
            time < holdEnd -> 1F
            else -> 1F - currentCollapse(tickDelta)
        }
    }

    /**
     * 计算激光收束阶段进度。
     *
     * @return 收束前为 `0F`，收束期间位于 `0F..1F`
     */
    internal fun currentCollapse(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val growDuration = phaseTicks.coerceAtLeast(1).toFloat()
        val holdEnd = growDuration + lifetime.coerceAtLeast(0).toFloat()
        return if (time < holdEnd) {
            0F
        } else {
            smoothstep(0F, growDuration, time - holdEnd)
        }
    }

    /**
     * 计算激光稳定阶段的泛光脉冲。
     *
     * @return 位于 `0F..1F` 的周期脉冲值
     */
    private fun currentPulse(tickDelta: Float): Float {
        return 0.5F + 0.5F * sin(currentTimeline(tickDelta) * 0.84F + 0.6F)
    }

    companion object {
        /** 直线激光实体的稳定注册路径。 */
        private const val RENDER_ENTITY_ID = "straight_laser_render_entity"

        /** 实体默认值和四个 spawn 重载共用的最大半径。 */
        internal const val DEFAULT_MAX_RADIUS = 0.22F

        /** 实体状态和 renderer 共用的最小有效半径。 */
        internal const val MIN_RADIUS = 0.01F

        /** 实体状态和 renderer 共用的最小有效长度。 */
        internal const val MIN_BEAM_LENGTH = 0.05F

        /** 多个配置和跟随逻辑共用的无来源实体标记。 */
        private const val NO_SOURCE_ENTITY = -1

        /** 直线激光的稳定 RenderEntity 注册 ID。 */
        @JvmField
        val ID: ResourceLocation =
            ofID(UsefulMagic.MOD_ID, RENDER_ENTITY_ID)

        /**
         * 使用来源实体当前坐标和 [Vec3] 颜色生成并注册一条激光。
         *
         * @return 已提交到服务端 RenderEntity 管理器的激光实体
         */
        @JvmStatic
        fun spawn(
            world: ServerLevel,
            sourceEntity: Entity,
            end: Vec3,
            tick: Int,
            lifetime: Int,
            color: Vec3 = Vec3(0.28, 0.82, 1.0),
            maxRadius: Float = DEFAULT_MAX_RADIUS,
            brightness: Float = 1.0F,
        ): StraightLaserRenderEntity {
            return StraightLaserRenderEntity(world)
                .configure(sourceEntity, end, tick, lifetime, color, maxRadius, brightness)
                .also(ServerRenderEntityManager::spawn)
        }

        /**
         * 使用来源实体当前坐标和 [Vector3f] 颜色生成并注册一条激光。
         *
         * @return 已提交到服务端 RenderEntity 管理器的激光实体
         */
        @JvmStatic
        fun spawn(
            world: ServerLevel,
            sourceEntity: Entity,
            end: Vec3,
            tick: Int,
            lifetime: Int,
            color: Vector3f,
            maxRadius: Float = DEFAULT_MAX_RADIUS,
            brightness: Float = 1.0F,
        ): StraightLaserRenderEntity {
            return StraightLaserRenderEntity(world)
                .configure(sourceEntity, end, tick, lifetime, color, maxRadius, brightness)
                .also(ServerRenderEntityManager::spawn)
        }

        /**
         * 使用固定端点和 [Vec3] 颜色生成并注册一条激光。
         *
         * @return 已提交到服务端 RenderEntity 管理器的激光实体
         */
        @JvmStatic
        fun spawn(
            world: ServerLevel,
            start: Vec3,
            end: Vec3,
            tick: Int,
            lifetime: Int,
            color: Vec3 = Vec3(0.28, 0.82, 1.0),
            maxRadius: Float = DEFAULT_MAX_RADIUS,
            brightness: Float = 1.0F,
        ): StraightLaserRenderEntity {
            return StraightLaserRenderEntity(world)
                .configure(start, end, tick, lifetime, color, maxRadius, brightness)
                .also(ServerRenderEntityManager::spawn)
        }

        /**
         * 使用固定端点和 [Vector3f] 颜色生成并注册一条激光。
         *
         * @return 已提交到服务端 RenderEntity 管理器的激光实体
         */
        @JvmStatic
        fun spawn(
            world: ServerLevel,
            start: Vec3,
            end: Vec3,
            tick: Int,
            lifetime: Int,
            color: Vector3f,
            maxRadius: Float = DEFAULT_MAX_RADIUS,
            brightness: Float = 1.0F,
        ): StraightLaserRenderEntity {
            return StraightLaserRenderEntity(world)
                .configure(start, end, tick, lifetime, color, maxRadius, brightness)
                .also(ServerRenderEntityManager::spawn)
        }

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
         * 在线性区间插值两个标量。
         *
         * @return 按限制后的插值权重计算的标量
         */
        internal fun mix(from: Float, to: Float, alpha: Float): Float {
            return from + (to - from) * alpha.coerceIn(0F, 1F)
        }

    }
}
