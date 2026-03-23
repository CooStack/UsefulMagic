package cn.coostack.usefulmagic.entity.custom

import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.goal.dragon.MagicDragonAttackGoal
import cn.coostack.usefulmagic.entity.custom.skills.dragon.DragonBreathSkill
import cn.coostack.usefulmagic.entity.custom.skills.dragon.DragonImpactSkill
import cn.coostack.usefulmagic.entity.custom.skills.dragon.DragonLaserSkill
import cn.coostack.usefulmagic.entity.custom.skills.dragon.DragonMeteoriteSkill
import cn.coostack.usefulmagic.entity.util.phases.PhaseManager
import cn.coostack.usefulmagic.entity.util.phases.dragon.DragonSimpleFlightPhase
import cn.coostack.usefulmagic.managers.server.SkillManagerManager
import cn.coostack.usefulmagic.skill.api.EntitySkillManager
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerBossEvent
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
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.PlayState
import software.bernie.geckolib.util.GeckoLibUtil
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.max
import kotlin.random.Random

/**
 * 想法更正
 * 1. 该实体本身就是2阶段的状态
 * 2. 原地不动作为二阶段的生成动画
 * 3. 一阶段使用 MagicEyeEntity
 * 4. 某个技能会生成多个MagicEyeEntity的变种Entity / DisplayEntity (短暂的 生命周期的)
 *
 * 自然恢复， 一秒3点血量， 禁魔的时候不恢复
 *
 * 龙的移动要进行角度偏移（不是锐角转弯）
 *
 * @constructor
 *
 * @param entityType
 * @param level
 */
class MagicDragonEntity(
    entityType: EntityType<MagicDragonEntity>,
    level: Level
) : PathfinderMob(entityType, level), GeoEntity, UnlimitHealthEntity {

    val bossBar = ServerBossEvent(
        Component.translatable(entityType.descriptionId),
        BossEvent.BossBarColor.PINK,
        BossEvent.BossBarOverlay.PROGRESS
    )
    var skillManager = EntitySkillManager(this)

    var dragonMaxHealth: Float
        get() = entityData.get(ENTITY_MAX_HEALTH)
        set(value) {
            entityData.set(ENTITY_MAX_HEALTH, value.coerceAtLeast(1f))
        }


    var spawnPosition: Vec3
        get() =
            entityData.get(SPAWN_POS).asVec3()
        set(value) {
            entityData.set(SPAWN_POS, value.toVector3f())
            entityData.set(HAS_SPAWN_POSITION, true)
            simpleFlightSpawnGroundYCache = null
        }


    var animateTicking: Int
        get() = entityData.get(ANIMATE_TICKING)
        set(value) = entityData.set(ANIMATE_TICKING, value)
    var stunProcess: Float
        get() = entityData.get(STUN_PROCESS)
        set(value) = entityData.set(STUN_PROCESS, value)
    var dizzinessTick: Int
        get() = entityData.get(DIZZINESS_TICK)
        set(value) = entityData.set(DIZZINESS_TICK, value)

    var impactCD: Int
        get() = entityData.get(IMPACT_CD)
        set(value) = entityData.set(IMPACT_CD, value)

    var damageReduction: Float
        get() = entityData.get(DAMAGE_REDUCTION)
        set(value) = entityData.set(DAMAGE_REDUCTION, value)

    var entityDeath: Boolean
        get() = entityData.get(ENTITY_DEATH)
        set(value) = entityData.set(ENTITY_DEATH, value)

    val simpleFlightTargetHistory = SimpleFlightTargetHistory()
    var simpleFlightSpawnGroundYCache: Double? = null
    var simpleFlightArrivalCooldownTick = 0
    var simpleFlightOrbitDirection = 1
    var simpleFlightOrbitAngle = 0.0
    var simpleFlightOrbitCenter: Vec3? = null
    var simpleFlightOrbitRadius = 0.0

    private val cache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)
    private var ramTarget: LivingEntity? = null
    private var flySoundCooldown = 0

    constructor(level: Level) : this(UsefulMagicEntityTypes.MAGIC_DRAGON_ENTITY_TYPE.get(), level)

    class SimpleFlightTargetHistory {
        val usedAnchors = mutableSetOf<String>()
        var lastAnchor: String? = null

        fun mark(anchor: String) {
            usedAnchors.add(anchor)
            lastAnchor = anchor
        }

        fun resetKeepingLast() {
            val last = lastAnchor
            usedAnchors.clear()
            if (last != null) {
                usedAnchors.add(last)
            }
        }
    }

    fun getOrResolveSimpleFlightSpawnGroundY(): Double? {
        simpleFlightSpawnGroundYCache?.let { return it }
        if (!entityData.get(HAS_SPAWN_POSITION)) {
            return null
        }

        val world = level()
        val groundPos = BlockPos.containing(spawnPosition).mutable()
        while (groundPos.y > world.minBuildHeight && world.getBlockState(groundPos).isAir) {
            groundPos.move(0, -1, 0)
        }
        if (world.getBlockState(groundPos).isAir) {
            return null
        }

        val surfaceY = groundPos.y + 1.0
        simpleFlightSpawnGroundYCache = surfaceY
        return surfaceY
    }

    fun resetSimpleFlightOrbitState() {
        simpleFlightArrivalCooldownTick = 0
        simpleFlightOrbitDirection = 1
        simpleFlightOrbitAngle = 0.0
        simpleFlightOrbitCenter = null
        simpleFlightOrbitRadius = 0.0
    }

    companion object {
        private const val DEFAULT_MAX_HEALTH = 1024f


        @JvmStatic
        private val ENTITY_DEATH = SynchedEntityData.defineId(
            MagicDragonEntity::class.java,
            EntityDataSerializers.BOOLEAN
        )

        @JvmStatic
        private val ENTITY_MAX_HEALTH = SynchedEntityData.defineId(
            MagicDragonEntity::class.java,
            EntityDataSerializers.FLOAT
        )

        @JvmStatic
        private val ENTITY_REAL_HEALTH = SynchedEntityData.defineId(
            MagicDragonEntity::class.java,
            EntityDataSerializers.FLOAT
        )

        /**
         * 眩晕百分比
         * 到达1的时候就开始眩晕
         */
        @JvmStatic
        private val STUN_PROCESS = SynchedEntityData.defineId(
            MagicDragonEntity::class.java, EntityDataSerializers.FLOAT
        )

        /**
         * 眩晕到达1时 眩晕的时间 （10秒）
         */
        @JvmStatic
        private val DIZZINESS_TICK = SynchedEntityData.defineId(
            MagicDragonEntity::class.java, EntityDataSerializers.INT
        )

        @JvmStatic
        private val ANIMATE_TICKING = SynchedEntityData.defineId(
            MagicDragonEntity::class.java, EntityDataSerializers.INT
        )

        @JvmStatic
        private val DAMAGE_REDUCTION = SynchedEntityData.defineId(
            MagicDragonEntity::class.java, EntityDataSerializers.FLOAT
        )

        @JvmStatic
        private val IMPACT_CD = SynchedEntityData.defineId(
            MagicDragonEntity::class.java, EntityDataSerializers.INT
        )

        @JvmStatic
        private val HAS_SPAWN_POSITION = SynchedEntityData.defineId(
            MagicDragonEntity::class.java,
            EntityDataSerializers.BOOLEAN
        )

        @JvmStatic
        private val SPAWN_POS = SynchedEntityData.defineId(
            MagicDragonEntity::class.java,
            EntityDataSerializers.VECTOR3
        )

        @JvmStatic
        private val PHASE_STATE = SynchedEntityData.defineId(
            MagicDragonEntity::class.java,
            EntityDataSerializers.COMPOUND_TAG
        )

        @JvmStatic
        private val SKILL_ANIMATION = SynchedEntityData.defineId(
            MagicDragonEntity::class.java,
            EntityDataSerializers.INT
        )

        @JvmStatic
        private val RAM_ATTACKING = SynchedEntityData.defineId(
            MagicDragonEntity::class.java,
            EntityDataSerializers.BOOLEAN
        )

        @JvmStatic
        fun createDefaultMobAttributes(): AttributeSupplier.Builder {
            return createMobAttributes()
                .add(Attributes.MAX_HEALTH, DEFAULT_MAX_HEALTH.toDouble())
                .add(Attributes.MOVEMENT_SPEED, 1.0)
                .add(Attributes.FLYING_SPEED, 1.6)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 128.0)
        }


    }

    val phaseManager = PhaseManager(
        DragonSimpleFlightPhase.HOLDER.get(),
        this,
        onPhaseChanged = ::syncPhaseState,
        collisionTargetPredicate = { !MagicDragonEntity::class.isInstance(it) }
    )

    init {
        isNoGravity = true
        initSkillManager()
    }

    private fun initSkillManager() {
        skillManager.addSkill(DragonBreathSkill())
        skillManager.addSkill(DragonMeteoriteSkill())
        skillManager.addSkill(DragonLaserSkill())
//        skillManager.addSkill(DragonBanMagicSkill())
        skillManager.addSkill(DragonImpactSkill())
        SkillManagerManager.setCache(skillManager)
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(ENTITY_DEATH, false)
        builder.define(ENTITY_MAX_HEALTH, DEFAULT_MAX_HEALTH)
        builder.define(ENTITY_REAL_HEALTH, DEFAULT_MAX_HEALTH)
        builder.define(HAS_SPAWN_POSITION, false)
        builder.define(SPAWN_POS, Vector3f())
        builder.define(PHASE_STATE, CompoundTag())
        builder.define(SKILL_ANIMATION, MagicDragonAnimationState.NONE.ordinal)
        builder.define(RAM_ATTACKING, false)
        builder.define(STUN_PROCESS, 0f)
        builder.define(DIZZINESS_TICK, 0)
        builder.define(IMPACT_CD, 0)
        builder.define(ANIMATE_TICKING, 0)
        builder.define(DAMAGE_REDUCTION, 0f)
    }

    fun syncPhaseState() {
        if (!level().isClientSide) {
            val phaseState = phaseManager.saveAsCompound()
            if (entityData.get(PHASE_STATE) != phaseState) {
                entityData.set(PHASE_STATE, phaseState)
            }
        }
    }

    override fun addAdditionalSaveData(nbt: CompoundTag) {
        nbt.putBoolean("entity_death", entityDeath)
        nbt.putFloat("dragon_max_health", dragonMaxHealth)
        nbt.putFloat("dragon_health", health)
        nbt.putFloat("stun_process", stunProcess)
        nbt.putInt("dizziness_tick", dizzinessTick)
        nbt.putInt("impact_cd", impactCD)
        nbt.putInt("animate_ticking", animateTicking)
        nbt.putUUID("cache_uuid", skillManager.cacheUUID)
        nbt.putBoolean("has_spawn_position", entityData.get(HAS_SPAWN_POSITION))
        if (entityData.get(HAS_SPAWN_POSITION)) {
            nbt.putDouble("spawn_x", spawnPosition.x)
            nbt.putDouble("spawn_y", spawnPosition.y)
            nbt.putDouble("spawn_z", spawnPosition.z)
        }
        nbt.putFloat("damage_reduction", damageReduction)
        val phase = phaseManager.saveAsCompound()
        nbt.put("phase", phase)
        super.addAdditionalSaveData(nbt)
    }

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        if (hasCustomName()) {
            bossBar.name = displayName ?: Component.translatable(type.descriptionId)
        }
        entityData.set(ENTITY_DEATH, nbt.getBoolean("entity_death"))
        dragonMaxHealth =
            if (nbt.contains("dragon_max_health")) nbt.getFloat("dragon_max_health") else DEFAULT_MAX_HEALTH
        health = if (nbt.contains("dragon_health")) nbt.getFloat("dragon_health") else dragonMaxHealth
        stunProcess = if (nbt.contains("stun_process")) nbt.getFloat("stun_process") else 0f
        dizzinessTick = if (nbt.contains("dizziness_tick")) nbt.getInt("dizziness_tick") else 0
        impactCD = if (nbt.contains("impact_cd")) nbt.getInt("impact_cd") else 0
        animateTicking = if (nbt.contains("animate_ticking")) nbt.getInt("animate_ticking") else 0
        damageReduction = if (nbt.contains("damage_reduction")) nbt.getFloat("damage_reduction") else 0f
        if (nbt.getBoolean("has_spawn_position")) {
            spawnPosition = Vec3(
                nbt.getDouble("spawn_x"),
                nbt.getDouble("spawn_y"),
                nbt.getDouble("spawn_z")
            )
        }


        if (nbt.contains("phase")) {
            val compound = nbt.getCompound("phase")
            phaseManager.loadFromCompound(compound)
            syncPhaseState()
        }

        runCatching {
            val cacheUUID = nbt.getUUID("cache_uuid")
            SkillManagerManager.loadFromCache(cacheUUID)?.let { cached ->
                val previousManager = skillManager
                if (previousManager.cacheUUID != cached.cacheUUID) {
                    SkillManagerManager.removeCache(previousManager.cacheUUID)
                }
                // 防御式兜底：如果区块卸载时没有及时清掉技能，这里在重载绑定前强制打断一次。
                cached.interruptActiveSkill(true)
                skillManager = cached
                skillManager.owner = this
            }
        }

        super.readAdditionalSaveData(nbt)
    }

    override fun setCustomName(name: Component?) {
        super.setCustomName(name)
        bossBar.name = name ?: Component.translatable(type.descriptionId)
    }

    override fun registerGoals() {
        super.registerGoals()
        goalSelector.apply {
            addGoal(0, FloatGoal(this@MagicDragonEntity))
            addGoal(0, MagicDragonAttackGoal(this@MagicDragonEntity))
        }
        targetSelector.apply {
            addGoal(0, HurtByTargetGoal(this@MagicDragonEntity))
            addGoal(
                1,
                NearestAttackableTargetGoal(
                    this@MagicDragonEntity,
                    Player::class.java,
                    10,
                    true,
                    false
                ) { candidate ->
                    val player = candidate as? Player ?: return@NearestAttackableTargetGoal false
                    !player.hasInfiniteMaterials() &&
                            !player.isSpectator &&
                            this@MagicDragonEntity.distanceToSqr(player) <= 128.0 * 128.0 &&
                            player.distanceToSqr(this@MagicDragonEntity.spawnPosition) <= 128.0 * 128.0
                }
            )
        }
    }

    override fun tick() {
        clearFire()
        super.tick()
        clearFire()
        if (impactCD > 0) {
            impactCD--
        }
        if (dizzinessTick > 0) {
            dizzinessTick--
            isNoGravity = false
            noPhysics = false
        } else {
            noPhysics = true
            isNoGravity = true
        }
        if (animateTicking > 0) {
            animateTicking--
        }
        if (flySoundCooldown > 0) {
            flySoundCooldown--
        }
        fallDistance = 0f
        captureSpawnPositionIfMissing()
        bossBar.progress = (health / max(dragonMaxHealth, 1f)).coerceIn(0f, 1f)
        phaseManager.tickPhase()
        val dragonIdle =
            target == null && !skillManager.hasActiveSkill() && phaseManager.getCurrentPhaseID() != DragonSimpleFlightPhase.ID
        if (dragonIdle) {
            phaseManager.resetDefaultPhase()
        }

        if (level().isClientSide) {
            return
        }
        skillManager.tick()
    }

    override fun onSyncedDataUpdated(key: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(key)
        if (key == PHASE_STATE && level().isClientSide) {
            phaseManager.loadFromCompound(entityData.get(PHASE_STATE).copy())
        }
    }

    private fun captureSpawnPositionIfMissing() {
        if (!entityData.get(HAS_SPAWN_POSITION)) {
            spawnPosition = position()
        }
    }

    override fun fireImmune(): Boolean {
        return true
    }

    override fun displayFireAnimation(): Boolean {
        return false
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (entityDeath) {
            return false
        }
        if (source.`is`(DamageTypeTags.IS_FIRE)) {
            return false
        }
        if (source.type() == level().damageSources().fall().type()) {
            return false
        }
        if (source.`is`(DamageTypes.GENERIC_KILL)) {
            return super.hurt(source, amount)
        }

        val id = skillManager.active?.getSkillID() ?: ""
        when (id) {
            DragonMeteoriteSkill.ID -> return false
        }

        // 概率增加一点点的眩晕值
        // 如果是暴击（随机） 则增加1.25倍
        if (Random.nextFloat() > 0.5 && amount > 5) {
            stunProcess += 0.01f
        }
        if (Random.nextFloat() < 0.1) {
            stunProcess * 1.25
        }
        if (stunProcess - 1 >= 1e-6) {
            stunProcess = 0f
            dizzinessTick = 20 * (10 + Random.nextInt(5)) // 10 .. 15秒
        }

        val afterReduction = amount * (1 - damageReduction)
        var input = afterReduction * 0.75f
        if (input > 20f) {
            input *= 1e-6f
        }
        return super.hurt(source, input)
    }


    override fun die(damageSource: DamageSource) {
        if (!entityDeath) {
            entityDeath = true
            clearSkillAnimation()
            stopBreathDive()
            skillManager.setEntityDeath()
            bossBar.progress = 0f
            SkillManagerManager.removeCache(skillManager.cacheUUID)
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

    override fun getHurtSound(source: DamageSource): SoundEvent {
        return SoundEvents.ENDER_DRAGON_HURT
    }

    override fun causeFallDamage(fallDistance: Float, multiplier: Float, damageSource: DamageSource): Boolean {
        return false
    }

    fun getCombatTarget(): LivingEntity? {
        val current = target
        return if (current != null && current.isAlive && !current.isRemoved) current else null
    }

    fun playAnimation(state: MagicDragonAnimationState) {
        if (state == MagicDragonAnimationState.FLY) {
            playFlySoundWithCooldown()
        }
        entityData.set(SKILL_ANIMATION, state.ordinal)
    }

    private fun playFlySoundWithCooldown() {
        if (level().isClientSide || flySoundCooldown > 0) {
            return
        }
        level().playSound(
            null,
            x,
            y,
            z,
            SoundEvents.ENDER_DRAGON_FLAP,
            SoundSource.HOSTILE,
            16f,
            0.9f + random.nextFloat() * 0.2f
        )
        flySoundCooldown = MagicDragonAnimationState.FLY.durationTicks
    }

    fun clearSkillAnimation() {
        entityData.set(SKILL_ANIMATION, MagicDragonAnimationState.NONE.ordinal)
    }

    fun getCurrentAnimationState(): MagicDragonAnimationState {
        return MagicDragonAnimationState.fromOrdinal(entityData.get(SKILL_ANIMATION))
    }

    fun isPlayingAnimation(state: MagicDragonAnimationState): Boolean {
        return getCurrentAnimationState() == state
    }

    fun isPlayingAnyAnimation(vararg states: MagicDragonAnimationState): Boolean {
        val currentState = getCurrentAnimationState()
        return states.any { it == currentState }
    }

    fun hasManualAnimation(): Boolean {
        return getCurrentAnimationState() != MagicDragonAnimationState.NONE
    }

    fun guideBreathDive(target: LivingEntity) {
        this.target = target
        ramTarget = target
        entityData.set(RAM_ATTACKING, true)
    }


    fun stopBreathDive() {
        ramTarget = null
        entityData.set(RAM_ATTACKING, false)
    }

    fun isRamAttacking(): Boolean {
        return entityData.get(RAM_ATTACKING)
    }


    fun getBreathAimDirection(): Vec3 {
        val currentTarget = ramTarget ?: getCombatTarget()
        val targetDirection = currentTarget?.eyePosition?.subtract(eyePosition)
        val base = targetDirection ?: lookAngle
        return if (base.lengthSqr() <= 1.0E-6) Vec3(0.0, 0.0, 1.0) else base.normalize()
    }

    fun getMouthPosition(direction: Vec3): Vec3 {
        val headOffset = direction.normalize().scale(bbWidth * 0.85)
        return eyePosition.add(headOffset).add(0.0, 0.2, 0.0)
    }

    fun createImpactVelocity(direction: Vec3, horizontalScale: Double, upwardBoost: Double): Vec3 {
        val push = if (direction.lengthSqr() <= 1.0E-6) Vec3.ZERO else direction.normalize().scale(horizontalScale)
        return Vec3(push.x, upwardBoost, push.z)
    }


    private fun resolveGeoAnimationState(): MagicDragonAnimationState? {
        val manualState = getCurrentAnimationState()
        return manualState.takeIf { it != MagicDragonAnimationState.NONE }
    }

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        controllers.add(
            AnimationController(this, "magic_dragon_main", 0) { state ->
                val animationState = resolveGeoAnimationState() ?: return@AnimationController PlayState.STOP
                state.setAndContinue(animationState.rawAnimation)
            }
        )
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache {
        return cache
    }

    override fun getUnlimitMaxHealth(): Float {
        return dragonMaxHealth
    }

    override fun setHealth(health: Float) {
        entityData.set(ENTITY_REAL_HEALTH, health.coerceIn(0f, dragonMaxHealth))
    }

    override fun getHealth(): Float {
        return entityData.get(ENTITY_REAL_HEALTH)
    }
}
