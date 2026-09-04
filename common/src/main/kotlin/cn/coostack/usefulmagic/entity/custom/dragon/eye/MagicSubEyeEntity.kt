package cn.coostack.usefulmagic.entity.custom.dragon.eye

import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.dragon.eye.skills.sub.EyeSuicideAttackSkill
import cn.coostack.usefulmagic.entity.custom.dragon.eye.goal.MagicSubEyeAttackGoal
import cn.coostack.usefulmagic.entity.util.FlightMovementUtil
import cn.coostack.usefulmagic.entity.util.phases.PhaseManager
import cn.coostack.usefulmagic.entity.custom.dragon.eye.phases.sub.EyeFollowOwnerPhase
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.managers.server.SkillManagerManager
import cn.coostack.usefulmagic.particles.emitters.entity.eye.MagicEyeHurtEmitter
import cn.coostack.usefulmagic.skill.api.EntitySkillManager
import cn.coostack.usefulmagic.skill.api.EntityRandomSkillManager
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.DamageTypeTags
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import java.util.*
import kotlin.math.PI
import kotlin.random.Random

/**
 * 会尝试去跟随owner（高优先级）， 如果没有owner就不跟
 *
 * 攻击逻辑： 只有几个简单的攻击方式
 * 1. 简单弹幕 （类似MagicBook）
 * 2. 环绕玩家 / 跟随owner
 */
class MagicSubEyeEntity(
    entityType: EntityType<MagicSubEyeEntity>,
    level: Level
) : PathfinderMob(entityType, level) {
    var skillManager: EntitySkillManager = EntityRandomSkillManager(this)
    val phaseManager = PhaseManager(
        EyeFollowOwnerPhase.HOLDER.get(),
        this,
        onPhaseChanged = ::syncPhaseState
    )
    var owner: Mob? = null
        set(value) {
            field = value
            ownerUUID = value?.uuid
        }

    private var ownerUUID: UUID? = null

    companion object {
        private const val OWNER_UUID_KEY = "owner_uuid"
        private const val CACHE_UUID_KEY = "cache_uuid"
        private const val PHASE_KEY = "phase"

        @JvmStatic
        private val PHASE_STATE = SynchedEntityData.defineId(
            MagicSubEyeEntity::class.java,
            EntityDataSerializers.COMPOUND_TAG
        )

        @JvmStatic
        fun createDefaultMobAttributes(): AttributeSupplier.Builder {
            return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.FLYING_SPEED, 0.7)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 128.0)
        }

    }

    constructor(level: Level) : this(UsefulMagicEntityTypes.MAGIC_SUB_EYE_ENTITY_TYPE.get(), level)

    private val followAngleOffset = Random.nextDouble(0.0, PI * 2.0)

    init {
        isNoGravity = true
        xpReward = 0
        SkillManagerManager.setCache(skillManager)
        skillManager.addSkill(EyeSuicideAttackSkill(20.0, 0.5))
    }

    fun positionOnEye() = position().add(0.0, bbHeight.toDouble() / 2, 0.0)

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
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

    override fun tick() {
        clearFire()
        if (!level().isClientSide) {
            resolveOwner()
        }
        phaseManager.tickPhase()
        super.tick()
        clearFire()
        if (!level().isClientSide) {
            skillManager.tick()
        }
        isNoGravity = true
        fallDistance = 0f
        yBodyRot = yRot
        yHeadRot = yRot
    }

    internal fun resolveOwner() {
        val currentOwner = owner
        if (currentOwner != null && (!currentOwner.isRemoved && currentOwner.uuid == ownerUUID)) {
            return
        }
        val uuid = ownerUUID ?: return
        owner = null
        ownerUUID = uuid
        val serverLevel = level() as? ServerLevel ?: return
        owner = serverLevel.getEntity(uuid) as? MagicEyeEntity
    }

    fun isOwnedBy(owner: MagicEyeEntity): Boolean {
        return this.owner?.uuid == owner.uuid || ownerUUID == owner.uuid
    }

    internal fun tickFollowOwnerPhase() {
        resolveOwner()
        if (!tryFollowOwner()) {
            deltaMovement = FlightMovementUtil.brake(deltaMovement, damping = 0.76)
        }
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (source.`is`(DamageTypeTags.IS_FIRE)) {
            return false
        }
        val value = super.hurt(source, amount)
        if (value) {
            MagicEyeHurtEmitter(positionOnEye(), level()).apply {
                this.particleOption.apply {
                    maxCount = 15
                    minCount = 5
                }
                ParticleEmittersManager.spawnEmitters(this)
            }
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
        nbt.putUUID(CACHE_UUID_KEY, skillManager.cacheUUID)
        ownerUUID?.let { nbt.putUUID(OWNER_UUID_KEY, it) }
        nbt.put(PHASE_KEY, phaseManager.saveAsCompound())
        super.addAdditionalSaveData(nbt)
    }

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        ownerUUID = if (nbt.hasUUID(OWNER_UUID_KEY)) nbt.getUUID(OWNER_UUID_KEY) else null
        if (nbt.contains(PHASE_KEY)) {
            phaseManager.loadFromCompound(nbt.getCompound(PHASE_KEY))
            syncPhaseState()
        }
        resolveOwner()
        runCatching {
            val cacheUUID = nbt.getUUID(CACHE_UUID_KEY)
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
        super.readAdditionalSaveData(nbt)
    }

    override fun onSyncedDataUpdated(key: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(key)
        if (key == PHASE_STATE && level().isClientSide) {
            phaseManager.loadFromCompound(entityData.get(PHASE_STATE).copy())
        }
    }

    override fun die(damageSource: DamageSource) {
        skillManager.setEntityDeath()
        SkillManagerManager.removeCache(skillManager.cacheUUID)
        super.die(damageSource)
    }

    override fun remove(reason: RemovalReason) {
        if (reason.shouldDestroy()) {
            skillManager.resetActiveSkill(false)
            SkillManagerManager.removeCache(skillManager.cacheUUID)
        } else {
            skillManager.interruptActiveSkill(true)
            SkillManagerManager.setCache(skillManager)
        }
        super.remove(reason)
    }

    override fun isPersistenceRequired(): Boolean {
        return true
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

    override fun registerGoals() {
        super.registerGoals()
        goalSelector.addGoal(0, MagicSubEyeAttackGoal(this))
        targetSelector.apply {
            addGoal(0, HurtByTargetGoal(this@MagicSubEyeEntity))
            addGoal(
                1,
                NearestAttackableTargetGoal(
                    this@MagicSubEyeEntity,
                    Player::class.java,
                    10,
                    true,
                    false
                ) { candidate ->
                    val player = candidate as? Player ?: return@NearestAttackableTargetGoal false
                    !player.hasInfiniteMaterials() &&
                            !player.isSpectator &&
                            this@MagicSubEyeEntity.distanceToSqr(player) <= 128.0 * 128.0
                }
            )
        }

    }

    fun tryFollowOwner(): Boolean {
        val currentOwner = owner ?: return false
        if (!currentOwner.isAlive || currentOwner.isRemoved) {
            return false
        }
        deltaMovement = FlightMovementUtil.maintainOffset(
            currentPosition = position(),
            currentVelocity = deltaMovement,
            target = currentOwner.boxCenterPosition(),
            acceleration = 0.4,
            damping = 0.82,
            4.0 minRangeTo 8.0,
            -2.0 minRangeTo 2.0
        )
        hasImpulse = true
        return true
    }

}
