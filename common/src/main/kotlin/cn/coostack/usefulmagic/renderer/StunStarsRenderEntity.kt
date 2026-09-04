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
import kotlin.math.max

/** 同步眩晕星星的轨道、拖尾和消散状态。 */
@CooAutoRegister
class StunStarsRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos) {
    /** 星体和拖尾共用的同步 RGB 颜色。 */
    @CodecField
    var color: Vector3f = Vector3f(1.0F, 0.92F, 0.42F)

    /** 星体和拖尾的整体透明度。 */
    @CodecField
    var alpha: Double = 1.0

    /** 星体材质和 bloom 共用的亮度倍率。 */
    @CodecField
    var brightness: Float = DEFAULT_BRIGHTNESS

    /** 星体绕中心旋转的世界半径。 */
    @CodecField
    var orbitRadius: Float = DEFAULT_ORBIT_RADIUS

    /** 轨道中心相对实体位置的纵向偏移。 */
    @CodecField
    var verticalOffset: Float = DEFAULT_VERTICAL_OFFSET

    /** 每个星体公告板的世界尺寸。 */
    @CodecField
    var starScale: Float = DEFAULT_STAR_SCALE

    /** 每个星体后方保留的拖尾世界长度。 */
    @CodecField
    var trailLength: Float = DEFAULT_TRAIL_LENGTH

    /** 轨道拖尾的世界宽度。 */
    @CodecField
    var trailWidth: Float = 0.105F

    /** 星体从最小尺寸展开所需的 tick 数。 */
    @CodecField
    var appearTicks: Int = DEFAULT_APPEAR_TICKS

    /** 主动消散阶段持续的 tick 数。 */
    @CodecField
    var discardFadeTicks: Int = DEFAULT_DISCARD_FADE_TICKS

    /** 是否已经进入主动消散阶段。 */
    @CodecField
    var discarding: Boolean = false

    /** 主动消散开始时的实体年龄。 */
    @CodecField
    var discardStartTick: Int = 0

    /** 星体绕中心运动的角速度。 */
    @CodecField
    var orbitSpeed: Float = 0.22F

    /** 每个星体公告板的自转速度。 */
    @CodecField
    var starSpinSpeed: Float = 0.30F

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
     * 配置眩晕星星的中心、外观、轨道和消散参数。
     *
     * @param pos 轨道中心的世界坐标
     * @param color 星体和拖尾共用的 RGB 颜色
     * @param alpha 整体透明度，限制到 `0.0..1.0`
     * @param brightness 本体和辉光共用的亮度倍率
     * @param orbitRadius 轨道世界半径
     * @param verticalOffset 轨道中心的纵向偏移
     * @param starScale 星形公告板的世界尺寸
     * @param trailLength 拖尾世界长度
     * @param trailWidth 拖尾世界宽度
     * @param appearTicks 出现阶段 tick 数，最小按 `1` 处理
     * @param discardFadeTicks 主动消散 tick 数，最小按 `1` 处理
     * @param orbitSpeed 轨道角速度
     * @param starSpinSpeed 星形公告板自转速度
     * @return 当前实体，可继续链式配置
     */
    fun configure(
        pos: Vec3,
        color: Vector3f = this.color,
        alpha: Double = this.alpha,
        brightness: Float = this.brightness,
        orbitRadius: Float = this.orbitRadius,
        verticalOffset: Float = this.verticalOffset,
        starScale: Float = this.starScale,
        trailLength: Float = this.trailLength,
        trailWidth: Float = this.trailWidth,
        appearTicks: Int = this.appearTicks,
        discardFadeTicks: Int = this.discardFadeTicks,
        orbitSpeed: Float = this.orbitSpeed,
        starSpinSpeed: Float = this.starSpinSpeed,
    ): StunStarsRenderEntity {
        this.pos = pos
        this.color = Vector3f(color)
        this.alpha = alpha.coerceIn(0.0, 1.0)
        this.brightness = brightness.coerceIn(0F, MAX_BRIGHTNESS)
        this.orbitRadius = orbitRadius.coerceAtLeast(MIN_SIZE)
        this.verticalOffset = verticalOffset
        this.starScale = starScale.coerceAtLeast(MIN_SIZE)
        this.trailLength = trailLength.coerceAtLeast(MIN_SIZE)
        this.trailWidth = trailWidth.coerceAtLeast(MIN_SIZE)
        this.appearTicks = appearTicks.coerceAtLeast(1)
        this.discardFadeTicks = discardFadeTicks.coerceAtLeast(1)
        this.orbitSpeed = orbitSpeed
        this.starSpinSpeed = starSpinSpeed
        updateRenderRange()
        markDirty()
        return this
    }

    /**
     * 使用 JOML 向量设置星体颜色并标记同步数据。
     *
     * @return 当前实体，可继续链式配置
     */
    fun setColor(color: Vector3f): StunStarsRenderEntity {
        this.color = Vector3f(color)
        markDirty()
        return this
    }

    /**
     * 使用 Minecraft 向量设置星体颜色并标记同步数据。
     *
     * @return 当前实体，可继续链式配置
     */
    fun setColor(color: Vec3): StunStarsRenderEntity {
        return setColor(Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()))
    }

    /**
     * 设置限制到有效范围的亮度倍率。
     *
     * @return 当前实体，可继续链式配置
     */
    fun withBrightness(value: Float): StunStarsRenderEntity {
        brightness = value.coerceIn(0F, MAX_BRIGHTNESS)
        markDirty()
        return this
    }

    /**
     * 设置拖尾长度并同步更新渲染范围。
     *
     * @return 当前实体，可继续链式配置
     */
    fun withTrailLength(value: Float): StunStarsRenderEntity {
        trailLength = value.coerceAtLeast(MIN_SIZE)
        updateRenderRange()
        markDirty()
        return this
    }

    /**
     * 设置星体尺寸并同步更新渲染范围。
     *
     * @return 当前实体，可继续链式配置
     */
    fun withStarScale(value: Float): StunStarsRenderEntity {
        starScale = value.coerceAtLeast(MIN_SIZE)
        updateRenderRange()
        markDirty()
        return this
    }

    /**
     * 移动轨道中心并请求立即同步。
     *
     * @param pos 新中心世界坐标
     * @return 当前实体；坐标未变化时不标记同步数据
     */
    fun moveTo(pos: Vec3): StunStarsRenderEntity {
        if (this.pos.distanceToSqr(pos) <= 0.000001) {
            return this
        }
        this.pos = pos
        markDirty()
        requestSync()
        return this
    }

    /**
     * 设置主动消散时长，最小按 `1` tick 处理。
     *
     * @return 当前实体，可继续链式配置
     */
    fun setDiscardFadeTicks(ticks: Int): StunStarsRenderEntity {
        discardFadeTicks = ticks.coerceAtLeast(1)
        markDirty()
        return this
    }

    /**
     * 开始主动消散并请求立即同步。
     *
     * @param fadeTicks 本次消散 tick 数，最小按 `1` 处理
     * @return 当前实体；已经消散时保持原状态
     */
    fun discard(fadeTicks: Int = discardFadeTicks): StunStarsRenderEntity {
        if (discarding) {
            return this
        }
        discardFadeTicks = fadeTicks.coerceAtLeast(1)
        discarding = true
        discardStartTick = age
        markDirty()
        requestSync()
        return this
    }

    /** 更新渲染范围，并在主动消散结束后终止实体。 */
    private fun updateLifecycle() {
        updateRenderRange()
        if (discarding && age - discardStartTick >= discardFadeTicks.coerceAtLeast(1)) {
            canceled = true
            markDirty()
        }
    }

    /** 按轨道、星体和拖尾尺寸同步服务端可见范围。 */
    private fun updateRenderRange() {
        renderRange = max(32.0, (orbitRadius + starScale + trailLength).toDouble() * 16.0)
    }

    /**
     * 计算主动丢弃淡出后的当前透明度。
     *
     * @return 当前渲染帧的透明度
     */
    internal fun currentAlpha(tickDelta: Float): Float {
        val baseAlpha = alpha.toFloat().coerceIn(0F, 1F)
        if (!discarding) {
            return baseAlpha
        }
        val elapsed = age - discardStartTick - 1F + tickDelta
        val fade = 1F - smoothstep(0F, discardFadeTicks.coerceAtLeast(1).toFloat(), elapsed)
        return baseAlpha * fade
    }

    /**
     * 计算星形公告板的出现缩放。
     *
     * @return 不小于最小可见值的出现缩放
     */
    internal fun currentAppearScale(tickDelta: Float): Float {
        val appear = smoothstep(0F, appearTicks.coerceAtLeast(1).toFloat(), timeline(tickDelta))
        return appear.coerceAtLeast(0.03F)
    }

    /**
     * 返回包含帧插值的生命周期时间。
     *
     * @return 从实体首个渲染 tick 开始计算的非负时间
     */
    internal fun timeline(tickDelta: Float): Float {
        return (age - 1F + tickDelta).coerceAtLeast(0F)
    }

    companion object {
        /** 眩晕星星实体的稳定注册路径。 */
        private const val RENDER_ENTITY_ID = "stun_stars_render_entity"

        /** 字段默认值和两个 spawn 重载共用的出现 tick 数。 */
        private const val DEFAULT_APPEAR_TICKS = 6
        /** 字段默认值和两个 spawn 重载共用的消散 tick 数。 */
        private const val DEFAULT_DISCARD_FADE_TICKS = 12
        /** 字段默认值和两个 spawn 重载共用的轨道半径。 */
        private const val DEFAULT_ORBIT_RADIUS = 0.72F
        /** 字段默认值和两个 spawn 重载共用的垂直偏移。 */
        private const val DEFAULT_VERTICAL_OFFSET = 0.35F
        /** 字段默认值和两个 spawn 重载共用的星形尺寸。 */
        private const val DEFAULT_STAR_SCALE = 0.24F
        /** 字段默认值和两个 spawn 重载共用的拖尾长度。 */
        private const val DEFAULT_TRAIL_LENGTH = 0.95F
        /** 字段默认值和两个 spawn 重载共用的亮度。 */
        private const val DEFAULT_BRIGHTNESS = 1.0F
        /** 实体状态和 renderer 共用的最小几何尺寸。 */
        internal const val MIN_SIZE = 0.01F

        /** 实体状态和 renderer 共用的最大亮度。 */
        internal const val MAX_BRIGHTNESS = 4.0F
        /** 眩晕星星的稳定 RenderEntity 注册 ID。 */
        @JvmField
        val ID: ResourceLocation =
            ofID(UsefulMagic.MOD_ID, RENDER_ENTITY_ID)

        /**
         * 使用 [Vector3f] 颜色生成并注册一组眩晕星星。
         *
         * @return 已提交到服务端 RenderEntity 管理器的星星实体
         */
        @JvmStatic
        fun spawn(
            world: ServerLevel,
            pos: Vec3,
            color: Vector3f = Vector3f(1.0F, 0.92F, 0.42F),
            brightness: Float = DEFAULT_BRIGHTNESS,
            appearTicks: Int = DEFAULT_APPEAR_TICKS,
            discardFadeTicks: Int = DEFAULT_DISCARD_FADE_TICKS,
            orbitRadius: Float = DEFAULT_ORBIT_RADIUS,
            verticalOffset: Float = DEFAULT_VERTICAL_OFFSET,
            starScale: Float = DEFAULT_STAR_SCALE,
            trailLength: Float = DEFAULT_TRAIL_LENGTH,
        ): StunStarsRenderEntity {
            return StunStarsRenderEntity(world)
                .configure(
                    pos = pos,
                    color = color,
                    brightness = brightness,
                    appearTicks = appearTicks,
                    discardFadeTicks = discardFadeTicks,
                    orbitRadius = orbitRadius,
                    verticalOffset = verticalOffset,
                    starScale = starScale,
                    trailLength = trailLength,
                )
                .also(ServerRenderEntityManager::spawn)
        }

        /**
         * 使用 [Vec3] 颜色生成并注册一组眩晕星星。
         *
         * @return 已提交到服务端 RenderEntity 管理器的星星实体
         */
        @JvmStatic
        fun spawn(
            world: ServerLevel,
            pos: Vec3,
            color: Vec3,
            brightness: Float = DEFAULT_BRIGHTNESS,
            appearTicks: Int = DEFAULT_APPEAR_TICKS,
            discardFadeTicks: Int = DEFAULT_DISCARD_FADE_TICKS,
            orbitRadius: Float = DEFAULT_ORBIT_RADIUS,
            verticalOffset: Float = DEFAULT_VERTICAL_OFFSET,
            starScale: Float = DEFAULT_STAR_SCALE,
            trailLength: Float = DEFAULT_TRAIL_LENGTH,
        ): StunStarsRenderEntity {
            return spawn(
                world = world,
                pos = pos,
                color = Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()),
                brightness = brightness,
                appearTicks = appearTicks,
                discardFadeTicks = discardFadeTicks,
                orbitRadius = orbitRadius,
                verticalOffset = verticalOffset,
                starScale = starScale,
                trailLength = trailLength,
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
    }
}
