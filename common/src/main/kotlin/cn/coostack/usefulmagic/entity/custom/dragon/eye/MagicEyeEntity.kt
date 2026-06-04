package cn.coostack.usefulmagic.entity.custom.dragon.eye

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.renderer.post.CooPostEffects
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.barrages.entity.skill.TrackedPointBarrage
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.UnlimitHealthEntity
import cn.coostack.usefulmagic.entity.custom.dragon.playDragonSoundOnce
import cn.coostack.usefulmagic.entity.custom.dragon.eye.goal.MagicEyeAttackGoal
import cn.coostack.usefulmagic.entity.custom.dragon.eye.phases.EyeCombatFlightPhase
import cn.coostack.usefulmagic.entity.custom.dragon.eye.skills.*
import cn.coostack.usefulmagic.entity.custom.dragon.spawn.DragonSpawner
import cn.coostack.usefulmagic.entity.util.FlightMovementUtil
import cn.coostack.usefulmagic.entity.util.phases.PhaseManager
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.lookAtPos
import cn.coostack.usefulmagic.extend.serverLevel
import cn.coostack.usefulmagic.extend.serverLevelApply
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.managers.server.SkillManagerManager
import cn.coostack.usefulmagic.particles.composition.explosion.ExplosionStarComposition
import cn.coostack.usefulmagic.particles.emitters.entity.eye.MagicEyeHurtEmitter
import cn.coostack.usefulmagic.renderer.BillboardStarRenderEntity
import cn.coostack.usefulmagic.renderer.UsefulMagicPostEffects
import cn.coostack.usefulmagic.skill.api.EntityRandomSkillManager
import cn.coostack.usefulmagic.skill.api.EntitySkillManager
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.DamageTypeTags
import net.minecraft.world.BossEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * 魔龙的第一阶段（魔力之眼 ）
 * 同样不能离开spawnPosition太远
 *
 * # 特性
 * - 部分攻击时随机传送 在原坐标周围
 * - 不攻击时会与目标玩家保持距离 (8个方块左右)
 * # 技能
 * - 激光
 * - 发射跟踪弹幕
 * - 瞬移(时发射)直线弹幕
 * - 分裂 (出现几个子末影眼， 有基本的攻击能力)
 *
 * TODO 修复还在放激光技能的时候就会直接下阶段的问题 (出现两次)
 *
 */
class MagicEyeEntity(
    entityType: EntityType<MagicEyeEntity>,
    level: Level
) : PathfinderMob(entityType, level), UnlimitHealthEntity {
    val bossBar = ServerBossEvent(
        Component.translatable(entityType.descriptionId),
        BossEvent.BossBarColor.PURPLE,
        BossEvent.BossBarOverlay.PROGRESS
    )
    var skillManager: EntitySkillManager = EntityRandomSkillManager(this)
    val phaseManager = PhaseManager(
        EyeCombatFlightPhase.HOLDER.get(),
        this,
        onPhaseChanged = ::syncPhaseState,
    )

    // 当血量小于10时 不会继续受到伤害， 直到他为true
    var hasLaserSkillActive = false

    companion object {
        private const val MIN_SPAWN_HEALTH = 0.5f
        private const val SPAWNING_TICKS = 200f
        private const val STAR_GLOW_HEIGHT_FACTOR = 0.52
        private const val IDLE_HOVER_HEIGHT = 3.0
        private const val IDLE_ACCELERATION = 0.05
        const val MAX_SUB_EYE_COUNT = 20
        private const val SUB_EYE_SEARCH_RANGE = 128.0
        private const val PHASE_KEY = "phase"
        private const val DEG_TO_RAD = 0.017453292f

        @JvmStatic
        private val ENTITY_SPAWNING = SynchedEntityData.defineId(
            MagicEyeEntity::class.java,
            EntityDataSerializers.BOOLEAN
        )

        @JvmStatic
        private val EYE_HEALTH = SynchedEntityData.defineId(
            MagicEyeEntity::class.java,
            EntityDataSerializers.FLOAT
        )

        @JvmStatic
        private val ENTITY_SPAWNING_POSITION = SynchedEntityData.defineId(
            MagicEyeEntity::class.java,
            EntityDataSerializers.VECTOR3
        )

        @JvmStatic
        private val DAMAGE_REDUCTION = SynchedEntityData.defineId(
            MagicEyeEntity::class.java, EntityDataSerializers.FLOAT
        )

        @JvmStatic
        private val PHASE_STATE = SynchedEntityData.defineId(
            MagicEyeEntity::class.java,
            EntityDataSerializers.COMPOUND_TAG
        )

        @JvmStatic
        fun createDefaultMobAttributes(): AttributeSupplier.Builder {
            return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 512.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.FLYING_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 128.0)
        }


    }

    constructor(level: Level) : this(UsefulMagicEntityTypes.MAGIC_EYE_ENTITY_TYPE.get(), level)

    private val orbitDirection = if (Random.nextBoolean()) 1.0 else -1.0


    init {
        isNoGravity = true
        xpReward = 0
        health = MIN_SPAWN_HEALTH
        initSkillManager()
    }

    var spawnPos: Vec3
        get() = entityData.get(ENTITY_SPAWNING_POSITION).asVec3()
        set(value) = entityData.set(ENTITY_SPAWNING_POSITION, value.toVector3f())
    var damageReduction: Float
        get() = entityData.get(DAMAGE_REDUCTION)
        set(value) = entityData.set(DAMAGE_REDUCTION, value)

    var secondDamage = 0f

    fun positionOnEye() = position().add(0.0, bbHeight.toDouble() / 2, 0.0)

    fun currentTowards(): Vec3 {
        val yawRad = yRot * DEG_TO_RAD
        val pitchRad = xRot * DEG_TO_RAD
        val direction = Vec3(
            (-sin(yawRad) * cos(pitchRad)).toDouble(),
            (-sin(pitchRad)).toDouble(),
            (cos(yawRad) * cos(pitchRad)).toDouble()
        )
        return if (direction.lengthSqr() > 1.0E-6) direction.normalize() else Vec3(0.0, 0.0, 1.0)
    }

    fun targetTowards(targetPos: Vec3): Vec3 {
        val direction = targetPos.subtract(positionOnEye())
        return if (direction.lengthSqr() > 1.0E-6) direction.normalize() else currentTowards()
    }

    fun rotateTowards(current: Vec3, target: Vec3, rotationSpeed: Float): Vec3 {
        val currentDirection = if (current.lengthSqr() > 1.0E-6) current.normalize() else currentTowards()
        val targetDirection = if (target.lengthSqr() > 1.0E-6) target.normalize() else currentDirection
        val dot = currentDirection.dot(targetDirection).coerceIn(-1.0, 1.0)
        val angle = acos(dot)
        if (angle <= 1.0E-6) {
            return targetDirection
        }
        val maxStep = rotationSpeed * DEG_TO_RAD
        val alpha = min(1.0, maxStep / angle)
        return currentDirection.lerp(targetDirection, alpha).normalize()
    }

    fun canAttackWithEyeMagic(entity: LivingEntity?): Boolean {
        return entity != null &&
                entity.uuid != uuid &&
                entity.isAlive &&
                !entity.isRemoved &&
                entity !is MagicSubEyeEntity &&
                entity !is MagicEyeEntity
    }


    override fun registerGoals() {
        super.registerGoals()

        goalSelector.addGoal(0, MagicEyeAttackGoal(this))
        targetSelector.apply {
            addGoal(0, HurtByTargetGoal(this@MagicEyeEntity))
            addGoal(
                1,
                NearestAttackableTargetGoal(
                    this@MagicEyeEntity,
                    Player::class.java,
                    2,
                    true,
                    false
                ) { candidate ->
                    val player = candidate as? Player ?: return@NearestAttackableTargetGoal false
                    !player.hasInfiniteMaterials() &&
                            !player.isSpectator &&
                            this@MagicEyeEntity.distanceToSqr(player) <= 128.0 * 128.0 &&
                            player.distanceToSqr(this@MagicEyeEntity.spawnPos) <= 128.0 * 128.0
                }
            )
        }
    }

    var entitySpawning: Boolean
        get() = entityData.get(ENTITY_SPAWNING)
        set(value) {
            entityData.set(ENTITY_SPAWNING, value)
        }

    private fun initSkillManager() {
        SkillManagerManager.setCache(skillManager)
        skillManager.addSkill(EyeThornSkill(64.0, 12f))
        skillManager.addSkill(EyeTrackedBarrageSkill(64.0, 8.0))
        skillManager.addSkill(EyeSplitSkill())
        skillManager.addSkill(EyeLaserSkill(12.0))
        skillManager.addSkill(EyeTeleportingAttackSkill(12.0))
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(ENTITY_SPAWNING, true)
        builder.define(EYE_HEALTH, MIN_SPAWN_HEALTH)
        builder.define(ENTITY_SPAWNING_POSITION, Vector3f())
        builder.define(DAMAGE_REDUCTION, 0f)
        builder.define(PHASE_STATE, CompoundTag())
    }

    fun syncPhaseState() {
        if (!level().isClientSide) {
            val phaseState = phaseManager.saveAsCompound()
            if (entityData.get(PHASE_STATE) != phaseState) {
                entityData.set(PHASE_STATE, phaseState)
            }
        }
    }

    fun spawnSubEye(pos: Vec3? = null) {
        if (level().isClientSide || countOwnedSubEyes() >= MAX_SUB_EYE_COUNT) {
            return
        }
        // 生成一个SubEyeEntity
        // 魔法阵生成一个
        val sub = MagicSubEyeEntity(level()).apply {
            setPos(pos ?: this@MagicEyeEntity.positionOnEye())
            owner = this@MagicEyeEntity
        }

        level().addFreshEntity(sub)


    }

    fun countOwnedSubEyes(): Int {
        return serverLevel?.getEntitiesOfClass(
            MagicSubEyeEntity::class.java,
            boundingBox.inflate(SUB_EYE_SEARCH_RANGE)
        ) {
            it.isAlive && !it.isRemoved && it.isOwnedBy(this)
        }?.size ?: -1
    }


    override fun dropCustomDeathLoot(level: ServerLevel, damageSource: DamageSource, recentlyHit: Boolean) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit)
        spawnAtLocation {
            UsefulMagicItems.EXPLOSION_WAND.getItem()
        }
    }

    override fun tick() {
        clearFire()
        super.tick()
        clearFire()
        if (tickCount % 20 == 0) {
            secondDamage = 0f
        }
        if (!level().isClientSide && entitySpawning) {
            tickSpawnProgress()
        }
        bossBar.progress = (health / maxHealth.coerceAtLeast(1f)).coerceIn(0f, 1f)
        if (!level().isClientSide && !entitySpawning) {
            phaseManager.tickPhase()
            skillManager.tick()
            shootTarget()
        }
        level().runAsIfType<ServerLevel> {
            if (tickCount % 20 == 0) {
                val count = players().count {
                    it.distanceTo(this@MagicEyeEntity) <= 64.0
                }
                damageReduction = (0.1f * count).coerceAtMost(0.9f)
            }
        }
        isNoGravity = true
        fallDistance = 0f
        yBodyRot = yRot
        yHeadRot = yRot
    }

    internal fun tickCombatFlightPhase() {
        val currentTarget = resolveCombatTarget()
        if (currentTarget == null) {
            deltaMovement = FlightMovementUtil.moveToPoint(
                currentPosition = position(),
                currentVelocity = deltaMovement,
                targetPoint = spawnPos.add(0.0, IDLE_HOVER_HEIGHT, 0.0),
                acceleration = IDLE_ACCELERATION,
                damping = 0.88,
                arriveDistance = 1.2
            )
            hasImpulse = true
            return
        }

        if (currentTarget.distanceToSqr(boxCenterPosition()) >= 64 * 64) {
            target = null
            return
        }
        val target = currentTarget.boxCenterPosition()

        lookAtPos(target, 8f, 8f)

        deltaMovement = FlightMovementUtil.maintainOffset(
            currentPosition = position(),
            currentVelocity = deltaMovement,
            target,
            0.38,
            0.9,
            8.0 minRangeTo 16.0,
            3.0 minRangeTo 8.0
        )

        if (target.y + 4.0 > y) {
            addDeltaMovement(Vec3(0.0, 0.8, 0.0))
        }

        hasImpulse = true
    }

    private fun resolveCombatTarget(updateTarget: Boolean = true): Player? {
        val current = target as? Player
        if (isValidCombatTarget(current)) {
            return current
        }

        val range = getAttributeValue(Attributes.FOLLOW_RANGE).coerceAtLeast(32.0)
        val nearest = level().getEntitiesOfClass(Player::class.java, boundingBox.inflate(range)) {
            isValidCombatTarget(it)
        }.minByOrNull { it.distanceToSqr(this) }

        if (updateTarget) {
            target = nearest
        }
        return nearest
    }

    private fun isValidCombatTarget(entity: Player?): Boolean {
        return entity != null &&
                entity.isAlive &&
                !entity.isRemoved &&
                !entity.isSpectator &&
                !entity.hasInfiniteMaterials()
    }

    private fun tickSpawnProgress() {
        val targetHealth = maxHealth.coerceAtLeast(1f)
        if (health < MIN_SPAWN_HEALTH) {
            health = MIN_SPAWN_HEALTH
        }
        if (health < targetHealth) {
            val step = (targetHealth - MIN_SPAWN_HEALTH).coerceAtLeast(0f) / SPAWNING_TICKS
            health = (health + step).coerceAtMost(targetHealth)

            val r = Random.nextDouble(3.0, 10.0)
            val p = Vec3.ZERO.random() * r
            val composition = ExplosionStarComposition(
                positionOnEye().add(p),
                level()
            )
            ParticleCompositionManager.spawn(composition)

        }
        if (health >= targetHealth) {
            finishSpawn()
        }
    }

    private fun finishSpawn() {
        if (!entitySpawning) {
            return
        }

        playDragonSoundOnce(
            this,
            UsefulMagicSoundEvents.EYE_SPAWN.get(),
            SoundSource.HOSTILE,
            1f,
            1.4f,
            256.0,
        )


        entitySpawning = false
        spawnSpawnStarGlow()
        onSpawn()
    }

    private fun spawnSpawnStarGlow() {
        val star = BillboardStarRenderEntity(
            level(),
            position().add(0.0, bbHeight * STAR_GLOW_HEIGHT_FACTOR, 0.0)
        )
        ServerRenderEntityManager.spawn(star)
    }

    private fun onSpawn() {
        spawnPos = position()
        level().runAsIfType<ServerLevel> {
            val count = players().count {
                it.distanceTo(this@MagicEyeEntity) <= 64.0
            }
            damageReduction = (0.1f * count).coerceAtMost(0.9f)
            ServerCameraUtil.sendShake(
                this, positionOnEye(),
                256.0,
                4.0,
                50,
                10.0,
                false
            )
            getEntitiesOfClass(Player::class.java, boundingBox.inflate(64.0)).forEach {
                CooPostEffects.server.send(
                    it as ServerPlayer, UsefulMagicPostEffects.rgbDashBlur(40, 1f, 2f)
                )
            }
        }
    }

    override fun isPushable(): Boolean {
        return false
    }

    override fun fireImmune(): Boolean {
        return true
    }

    override fun displayFireAnimation(): Boolean {
        return false
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {


        if (entitySpawning || source.`is`(DamageTypeTags.IS_FIRE)) {
            return false
        }
        if (source.`is`(DamageTypes.GENERIC_KILL)) {
            return super.hurt(source, amount)
        }
        // 秒伤最高20 高额减伤
        var passDamage = if (secondDamage > 20) {
            1e-6f
        } else {
            1f
        }
        if (!hasLaserSkillActive && health <= 1) {
            passDamage = 1e-8f
        }

        val id = skillManager.active?.getSkillID() ?: ""
        when (id) {
            EyeTrackedBarrageSkill.ID -> return false
            EyeSplitSkill.ID -> return false
        }

        val afterReduction = amount * (1 - damageReduction)
        var input = (afterReduction * 0.75f * passDamage)
        if (input > 20f) {
            input *= 1e-6f
        }
        val value = super.hurt(source, input)
        if (isDeadOrDying && !hasLaserSkillActive) {
            health = 1f
            return value
        }
        if (value) {
            MagicEyeHurtEmitter(boxCenterPosition(), level()).apply {
                ParticleEmittersManager.spawnEmitters(this)
            }
            secondDamage += input
        }
        return value
    }

    override fun causeFallDamage(
        fallDistance: Float,
        multiplier: Float,
        damageSource: DamageSource
    ): Boolean {
        return false
    }

    override fun removeWhenFarAway(distanceToClosestPlayer: Double): Boolean {
        return false
    }

    override fun addAdditionalSaveData(nbt: CompoundTag) {
        nbt.putBoolean("entity_spawning", entitySpawning)
        nbt.putFloat("eye_health", health)
        nbt.putDouble("spawn_x", spawnPos.x)
        nbt.putDouble("spawn_y", spawnPos.y)
        nbt.putDouble("spawn_z", spawnPos.z)
        nbt.put(PHASE_KEY, phaseManager.saveAsCompound())
        nbt.putUUID("cache_uuid", skillManager.cacheUUID)
        super.addAdditionalSaveData(nbt)
    }

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        if (hasCustomName()) {
            bossBar.name = displayName ?: Component.translatable(type.descriptionId)
        }
        runCatching {
            val cacheUUID = nbt.getUUID("cache_uuid")
            SkillManagerManager.loadFromCache(cacheUUID)?.let { cached ->
                val previousManager = skillManager
                if (previousManager.cacheUUID != cached.cacheUUID) {
                    SkillManagerManager.removeCache(previousManager.cacheUUID)
                }
                cached.interruptActiveSkill(true)
                skillManager = cached
                skillManager.owner = this
            }
        }
        if (nbt.contains("eye_health")) {
            health = nbt.getFloat("eye_health")
        }
        spawnPos = if (nbt.contains("spawn_x")) {
            Vec3(nbt.getDouble("spawn_x"), nbt.getDouble("spawn_y"), nbt.getDouble("spawn_z"))
        } else {
            position()
        }
        if (nbt.contains(PHASE_KEY)) {
            phaseManager.loadFromCompound(nbt.getCompound(PHASE_KEY))
            syncPhaseState()
        }
        entitySpawning = if (nbt.contains("entity_spawning")) nbt.getBoolean("entity_spawning") else true
        if (entitySpawning && health < MIN_SPAWN_HEALTH) {
            health = MIN_SPAWN_HEALTH
        }
        super.readAdditionalSaveData(nbt)
    }

    override fun onSyncedDataUpdated(key: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(key)
        if (key == PHASE_STATE && level().isClientSide) {
            phaseManager.loadFromCompound(entityData.get(PHASE_STATE).copy())
        }
    }

    override fun setCustomName(name: Component?) {
        super.setCustomName(name)
        bossBar.name = name ?: Component.translatable(type.descriptionId)
    }

    override fun die(damageSource: DamageSource) {
        skillManager.setEntityDeath()
        bossBar.progress = 0f
        SkillManagerManager.removeCache(skillManager.cacheUUID)

        // 直接击杀不会用激光
        // 加一个判定防止在第一次死亡的时候误判执行die
        if (hasLaserSkillActive || damageSource.`is`(DamageTypes.GENERIC_KILL)) {
            serverLevelApply {
                DragonSpawner(spawnPos.add(0.0,-3.0,0.0), it)
                    .start()
            }
        }

        super.die(damageSource)
    }

    override fun remove(reason: RemovalReason) {
        bossBar.removeAllPlayers()
        if (reason.shouldDestroy()) {
            skillManager.resetActiveSkill(false)
            SkillManagerManager.removeCache(skillManager.cacheUUID)
        } else {
            skillManager.interruptActiveSkill(true)
            SkillManagerManager.setCache(skillManager)
        }
        super.remove(reason)
    }

    override fun startSeenByPlayer(serverPlayer: ServerPlayer) {
        super.startSeenByPlayer(serverPlayer)
        bossBar.addPlayer(serverPlayer)
    }

    override fun stopSeenByPlayer(serverPlayer: ServerPlayer) {
        super.stopSeenByPlayer(serverPlayer)
        bossBar.removePlayer(serverPlayer)
    }

    override fun isPersistenceRequired(): Boolean {
        return true
    }

    override fun getUnlimitMaxHealth(): Float {
        return maxHealth
    }

    override fun setHealth(health: Float) {
        entityData.set(EYE_HEALTH, health.coerceIn(0f, maxHealth.coerceAtLeast(MIN_SPAWN_HEALTH)))
    }

    override fun getHealth(): Float {
        return entityData.get(EYE_HEALTH)
    }

    override fun getAmbientSound(): SoundEvent {
        return SoundEvents.GLASS_STEP
    }

    override fun getHurtSound(source: DamageSource): SoundEvent {
        return SoundEvents.IRON_GOLEM_REPAIR
    }

    override fun getDeathSound(): SoundEvent {
        return SoundEvents.GLASS_BREAK
    }

    /**
     * 随机传送
     *
     * @param offset 偏移大小最值
     */
    fun teleportRandomly(offset: Double) {
        val actualOffset = Random.nextDouble(offset)
        var vec = position().offsetRandom(level().random, actualOffset.toFloat())
        if (vec.y < spawnPos.y) {
            vec = vec.add(0.0, spawnPos.y - vec.y + Random.nextDouble(actualOffset), 0.0)
        }
        // 判断位置合理性
        val world = level()
        val pos = BlockPos.containing(vec).mutable()
        while (!world.getBlockState(pos).isAir) {
            pos.move(0, 1, 0)
        }

        val finalPos = pos.center
        teleportTo(finalPos.x, finalPos.y, finalPos.z)
    }


    private fun shootTarget() {
        val currentTarget = target ?: return
        if (!canAttackWithEyeMagic(currentTarget)) {
            target = null
            return
        }
        if (skillManager.hasActiveSkill()) return
        if (tickCount % 30 != 0) {
            return
        }
        if (level().isClientSide) return
        if (health < 1f) return
        // 找范围内的玩家
        val world = level() as ServerLevel
        repeat(Random.nextInt(1, 3)) {
            val backward = -forward.normalize()
            val direction = (backward * 2.0 + backward.offsetRandomly(1.0)).normalize()
            val barrage = TrackedPointBarrage(
                currentTarget, positionOnEye(), world, 10.0, this
            )
            barrage.direction = direction
            barrage.options.speed(1.7)
            BarrageManager.spawn(barrage)
        }
    }

}
