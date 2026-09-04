package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.extend.ofID
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.extend.plus
import cn.coostack.cooparticlesapi.extend.times
import cn.coostack.cooparticlesapi.renderer.AutoRenderEntity
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

/** 保存怪物咆哮锥体的同步起点、方向和生命周期。 */
@CooAutoRegister
class MonsterRoarRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos) {
    /** 发出咆哮的实体网络 ID，负值表示没有来源实体。 */
    @CodecField
    var sourceEntityId: Int = NO_SOURCE_ENTITY

    /** 咆哮锥体尖端的同步世界坐标。 */
    @CodecField
    var start: Vec3 = pos

    /** 咆哮锥体中心轴的单位方向。 */
    @CodecField
    var direction: Vec3 = Vec3(0.0, 0.0, 1.0)

    /** 咆哮锥体的同步 RGB 颜色。 */
    @CodecField
    var color: Vector3f = Vector3f(0.52F, 0.86F, 1.0F)

    /** 咆哮材质的整体透明度。 */
    @CodecField
    var alpha: Double = 1.0

    /** 咆哮锥体沿中心轴延伸的最大距离。 */
    @CodecField
    var maxDistance: Float = DEFAULT_MAX_DISTANCE

    /** 咆哮锥体末端的最大世界半径。 */
    @CodecField
    var maxRadius: Float = DEFAULT_MAX_RADIUS

    /** 咆哮保持完整强度的 tick 数。 */
    @CodecField
    var lifetime: Int = DEFAULT_LIFETIME

    /** 生命周期末尾的淡出 tick 数。 */
    @CodecField
    var fadeTicks: Int = 7

    /** 沿来源实体朝向偏移咆哮起点的距离。 */
    @CodecField
    var mouthOffset: Double = 0.65

    override fun getRenderID(): ResourceLocation = ID

    override fun clientTick() {
        refreshFromSource(syncNetwork = false)
        syncAnchor()
        updateLifecycle()
    }

    override fun serverTick() {
        refreshFromSource(syncNetwork = true)
        syncAnchor()
        updateLifecycle()
    }

    /** 在客户端同步后刷新锚点和渲染范围。 */
    internal fun refreshClientState() {
        refreshFromSource(syncNetwork = false)
        syncAnchor()
    }

    /**
     * 配置跟随来源实体的咆哮锥体。
     *
     * @param sourceEntity 提供位置与后续朝向刷新的来源实体
     * @param direction 初始中心轴方向，零向量回退为正 Z 轴
     * @param lifetime 保持完整强度的 tick 数，最小按 `1` 处理
     * @param color 咆哮 RGB 颜色
     * @param maxDistance 锥体轴向长度
     * @param maxRadius 锥体末端半径
     * @return 当前实体，可继续链式配置
     */
    fun configure(
        sourceEntity: Entity,
        direction: Vec3,
        lifetime: Int = DEFAULT_LIFETIME,
        color: Vector3f = this.color,
        maxDistance: Float = DEFAULT_MAX_DISTANCE,
        maxRadius: Float = DEFAULT_MAX_RADIUS,
    ): MonsterRoarRenderEntity {
        this.sourceEntityId = sourceEntity.id
        this.direction = normalizedDirection(direction)
        this.start = resolveMouthPosition(sourceEntity, this.direction)
        this.lifetime = lifetime.coerceAtLeast(1)
        this.maxDistance = maxDistance.coerceAtLeast(MIN_DISTANCE)
        this.maxRadius = maxRadius.coerceAtLeast(MIN_RADIUS)
        this.color = Vector3f(color)
        syncAnchor()
        markDirty()
        return this
    }

    /**
     * 刷新咆哮方向和颜色，并重新计算来源实体嘴部位置。
     *
     * @param direction 新中心轴方向
     * @param color 新 RGB 颜色
     * @return 当前实体
     */
    fun refresh(
        direction: Vec3,
        color: Vector3f = this.color,
    ): MonsterRoarRenderEntity {
        this.direction = normalizedDirection(direction)
        sourceEntity()?.let { source ->
            this.start = resolveMouthPosition(source, this.direction)
        }
        this.color = Vector3f(color)
        syncAnchor()
        markDirty()
        return this
    }

    /** 立即取消咆哮并标记同步数据。 */
    fun finish() {
        canceled = true
        markDirty()
    }

    /** 从来源实体刷新锥体起点和方向，并按需标记网络同步。 */
    private fun refreshFromSource(syncNetwork: Boolean) {
        val source = sourceEntity() ?: return
        val resolvedDirection = when (source) {
            is MagicDragonEntity -> source.getBreathAimDirection()
            else -> source.forward
        }
        val normalized = normalizedDirection(resolvedDirection)
        val resolvedStart = resolveMouthPosition(source, normalized)
        if (resolvedStart == start && normalized == direction) {
            return
        }
        start = resolvedStart
        direction = normalized
        if (syncNetwork) {
            markDirty()
        }
    }

    /**
     * 解析不同来源实体的咆哮起点。
     *
     * @return 来源实体嘴部或眼部前方的世界坐标
     */
    private fun resolveMouthPosition(source: Entity, direction: Vec3): Vec3 {
        return when (source) {
            is MagicDragonEntity -> source.getMouthPosition(direction)
            else -> source.eyePosition + normalizedDirection(direction) * mouthOffset
        }
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

    /** 按锥体中点更新 RenderEntity 锚点和可见范围。 */
    private fun syncAnchor() {
        val normalized = normalizedDirection(direction)
        pos = start + normalized * (maxDistance * 0.5)
        renderRange = maxDistance * 0.5 + maxRadius * 4.0 + 36.0
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
     * 计算指定生命周期时间的可见透明度。
     *
     * @return 叠加淡入和淡出曲线后的透明度
     */
    internal fun currentAlpha(time: Float): Float {
        val baseAlpha = alpha.toFloat().coerceIn(0F, 1F)
        val fadeIn = smoothstep(0F, 3.5F, time)
        val fadeOutStart = lifetime.coerceAtLeast(1).toFloat()
        val fadeOutDuration = fadeTicks.coerceAtLeast(1).toFloat()
        val fadeOut = 1F - smoothstep(fadeOutStart, fadeOutStart + fadeOutDuration, time)
        return baseAlpha * fadeIn * fadeOut
    }

    /** 在来源消失或淡出结束后终止实体。 */
    private fun updateLifecycle() {
        if (sourceEntityId != NO_SOURCE_ENTITY && sourceEntity() == null) {
            canceled = true
            return
        }
        if (age > lifetime.coerceAtLeast(1) + fadeTicks.coerceAtLeast(1) + 2) {
            canceled = true
        }
    }

    companion object {
        /** 怪物咆哮实体的稳定注册路径。 */
        private const val RENDER_ENTITY_ID = "monster_roar_render_entity"

        /** 字段默认值、配置和 spawn 入口共用的最大距离。 */
        private const val DEFAULT_MAX_DISTANCE = 9.5F
        /** 字段默认值、配置和 spawn 入口共用的最大半径。 */
        private const val DEFAULT_MAX_RADIUS = 3.2F
        /** 字段默认值、配置和 spawn 入口共用的生命周期。 */
        private const val DEFAULT_LIFETIME = 18
        /** 实体状态和 renderer 共用的最小有效距离。 */
        internal const val MIN_DISTANCE = 0.08F
        /** 实体状态和 renderer 共用的最小有效半径。 */
        internal const val MIN_RADIUS = 0.02F
        /** 多个配置和跟随逻辑共用的无来源实体标记。 */
        private const val NO_SOURCE_ENTITY = -1
        /** 怪物咆哮锥体的稳定 RenderEntity 注册 ID。 */
        @JvmField
        val ID: ResourceLocation =
            ofID(UsefulMagic.MOD_ID, RENDER_ENTITY_ID)

        /**
         * 生成并注册一个跟随来源实体的咆哮锥体。
         *
         * @return 已提交到服务端 RenderEntity 管理器的咆哮实体
         */
        @JvmStatic
        fun spawn(
            world: ServerLevel,
            sourceEntity: Entity,
            direction: Vec3,
            lifetime: Int = DEFAULT_LIFETIME,
            color: Vector3f = Vector3f(0.52F, 0.86F, 1.0F),
            maxDistance: Float = DEFAULT_MAX_DISTANCE,
            maxRadius: Float = DEFAULT_MAX_RADIUS,
        ): MonsterRoarRenderEntity {
            return MonsterRoarRenderEntity(world)
                .configure(sourceEntity, direction, lifetime, color, maxDistance, maxRadius)
                .also(ServerRenderEntityManager::spawn)
        }

        /**
         * 返回可用于旋转和 renderer 计算的安全单位方向。
         *
         * @return 输入方向的单位向量，零向量回退为正 Z 轴
         */
        internal fun normalizedDirection(direction: Vec3): Vec3 {
            return if (direction.lengthSqr() <= 0.000001) {
                Vec3(0.0, 0.0, 1.0)
            } else {
                direction.normalize()
            }
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
