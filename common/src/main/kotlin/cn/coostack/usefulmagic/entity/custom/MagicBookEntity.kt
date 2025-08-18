package cn.coostack.usefulmagic.entity.custom

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.PhysicConstant
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.goal.IllegalFlyingGoal
import cn.coostack.usefulmagic.entity.custom.goal.MagicAttackGoal
import cn.coostack.usefulmagic.entity.custom.goal.MagicCloseTargetGoal
import cn.coostack.usefulmagic.entity.custom.skills.*
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.managers.server.SkillManagerManager
import cn.coostack.usefulmagic.particles.animation.EmittersAnimate
import cn.coostack.usefulmagic.particles.animation.ParticleAnimation
import cn.coostack.usefulmagic.particles.animation.StyleAnimate
import cn.coostack.usefulmagic.particles.barrages.entity.EntityWoodenBarrage
import cn.coostack.usefulmagic.particles.emitters.DirectionShootEmitters
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import cn.coostack.usefulmagic.particles.emitters.ParticleWaveEmitters
import cn.coostack.usefulmagic.particles.style.entitiy.BookEntityDeathStyle
import cn.coostack.usefulmagic.skill.api.EntitySkillManager
import cn.coostack.usefulmagic.skill.api.SkillDamageCancelCondition
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.BossEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.AnimationState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.control.FlyingMoveControl
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.EnderMan
import net.minecraft.world.entity.monster.RangedAttackMob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.*
import kotlin.random.Random

/**
 * BOSS有3个生命阶段
 * 0-1/3*maxHealth, 1/3*maxHealth-2/3*maxHealth, 2/3*maxHealth-maxHealth
 */
class MagicBookEntity(entityType: EntityType<out PathfinderMob>, world: Level) : PathfinderMob(entityType, world),
    RangedAttackMob, UnlimitHealthEntity {
    val bossBar = ServerBossEvent(displayName!!, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS)
    var skillManager = EntitySkillManager(this)
    var bookMaxHealth: Float
        get() = entityData.get(LARGE_MAX_HEALTH)
        set(field) {
            entityData.set(LARGE_MAX_HEALTH, field.coerceAtLeast(0.5f))
        }

    constructor(world: Level) : this(
        UsefulMagicEntityTypes.MAGIC_BOOK_ENTITY_TYPE.get(), world
    )

    companion object {
        @JvmStatic
        private val ENTITY_SPAWNING = SynchedEntityData.defineId(
            MagicBookEntity::class.java, EntityDataSerializers.BOOLEAN
        )

        @JvmStatic
        private val SET_ENTITY_DEATH = SynchedEntityData.defineId(
            MagicBookEntity::class.java, EntityDataSerializers.BOOLEAN
        )

        @JvmStatic
        private val IS_ATTACKING = SynchedEntityData.defineId(
            MagicBookEntity::class.java, EntityDataSerializers.BOOLEAN
        )

        @JvmStatic
        private val LARGE_MAX_HEALTH = SynchedEntityData.defineId(
            MagicBookEntity::class.java, EntityDataSerializers.FLOAT
        )

        @JvmStatic
        private val BOOK_HEALTH = SynchedEntityData.defineId(
            MagicBookEntity::class.java, EntityDataSerializers.FLOAT
        )

        @JvmStatic
        fun createDefaultMobAttributes(): AttributeSupplier.Builder {
            return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.FLYING_SPEED, 0.5)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 20.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
        }
    }

    var entitySpawning: Boolean
        get() = entityData.get(ENTITY_SPAWNING)
        set(value) {
            entityData.set(ENTITY_SPAWNING, value)
        }
    val attackAnimateState = AnimationState()
    val deathAnimation = ParticleAnimation()

    init {
        health = 1f
        initSkillManager()
    }

    private fun initSkillManager() {
        skillManager.addSkill(MagicSwordSkill())
        skillManager.addSkill(HealthReverseSkill())
        skillManager.addSkill(BookShootSkill(2))
        skillManager.addSkill(BookCannonballsSkill(18f))
        skillManager.addSkill(BookSwordSlashSkill(8f))
        SkillManagerManager.setCache(skillManager)
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(IS_ATTACKING, false)
        builder.define(LARGE_MAX_HEALTH, 4096f)
        builder.define(BOOK_HEALTH, 1f)
        builder.define(SET_ENTITY_DEATH, false)
        builder.define(ENTITY_SPAWNING, true)
    }

    fun isAttacking(): Boolean {
        return entityData.get(IS_ATTACKING)
    }

    fun setAttacking(attacking: Boolean) {
        entityData.set(IS_ATTACKING, attacking)
    }

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        if (hasCustomName()) {
            bossBar.name = displayName!!
        }
        entityData.set(SET_ENTITY_DEATH, nbt.getBoolean("entity_death"))
        entityData.set(
            ENTITY_SPAWNING,
            if (nbt.contains("entity_spawning")) nbt.getBoolean("entity_spawning") else true
        )
        runCatching {
            val cache = nbt.getUUID("cache_uuid")
            val fromCache = SkillManagerManager.loadFromCache(cache)
            if (fromCache != null) {
                skillManager = fromCache
                skillManager.owner = this
            }
        }
        super.readAdditionalSaveData(nbt)
    }

    override fun addAdditionalSaveData(nbt: CompoundTag) {
        nbt.putBoolean("entity_death", isEntityDeath())
        nbt.putBoolean("entity_spawning", entitySpawning)
        nbt.putUUID("cache_uuid", skillManager.cacheUUID)
        super.addAdditionalSaveData(nbt)
    }

    override fun setCustomName(name: Component?) {
        super.setCustomName(name)
        this.bossBar.setName(name ?: Component.literal("null"))
    }

    fun setEntityDeath() {
        entityData.set(SET_ENTITY_DEATH, true)
    }

    override fun updateWalkAnimation(posDelta: Float) {
        super.updateWalkAnimation(posDelta)
        val poseAmount = if (this.pose == Pose.STANDING) min(posDelta * 6f, 5f) else 0f
        this.walkAnimation.update(poseAmount, 0.02f)
    }

    /**
     * 是否正在播放死亡动画
     */
    fun isEntityDeath(): Boolean = entityData.get(SET_ENTITY_DEATH)

    override fun registerGoals() {
        super.registerGoals()

        goalSelector.apply {
            addGoal(0, FloatGoal(this@MagicBookEntity))
            addGoal(3, MagicCloseTargetGoal(this@MagicBookEntity, 8.0))
            addGoal(6, LookAtPlayerGoal(this@MagicBookEntity, Player::class.java, 3f))
            addGoal(7, RandomLookAroundGoal(this@MagicBookEntity))
            addGoal(1, MagicAttackGoal(this@MagicBookEntity, 5))
            addGoal(1, IllegalFlyingGoal(this@MagicBookEntity))
        }

        targetSelector.apply {
            addGoal(1, HurtByTargetGoal(this@MagicBookEntity))
            addGoal(1, NearestAttackableTargetGoal(this@MagicBookEntity, Player::class.java, true))
            addGoal(1, NearestAttackableTargetGoal(this@MagicBookEntity, EnderMan::class.java, true))
        }

        moveControl = FlyingMoveControl(this, 10, true)
    }


    fun tickMovement() {
        if (isEntityDeath() || entitySpawning) {
            deltaMovement = Vec3.ZERO
            return
        }
        if (target == null) {
            if (!onGround()) {
                moveControl.setWantedPosition(x, -1.0, z, 0.8)
            }
            return
        }
        if (target!!.y + 6.0 > y) {
            deltaMovement = deltaMovement.add(0.0, 0.05, 0.0)
        } else if (target!!.y + 4 < y) {
            deltaMovement = deltaMovement.add(0.0, -0.05, 0.0)
        }

        // 距离目标水平距离
        val len = sqrt((target!!.x - x).pow(2) + (target!!.z - z).pow(2))
        if (len >= 16 && skillManager.active == null) {
            deltaMovement = deltaMovement.add(Vec3(target!!.x - x, 0.0, target!!.z - z).normalize().scale(0.25))
        }
    }

    override fun startSeenByPlayer(serverPlayer: ServerPlayer) {
        super.startSeenByPlayer(serverPlayer)
        bossBar.addPlayer(serverPlayer)
    }

    override fun stopSeenByPlayer(serverPlayer: ServerPlayer) {
        super.stopSeenByPlayer(serverPlayer)
        // 没有玩家看得到bossBar, 说明他妈的这个区块应该快GG了
//        println("stopped tracking by $player")
//        if (bossBar.players.isEmpty()) {
//            // 移除实体必然需要中断技能使用
//            skillManager.resetActiveSkill(false)
//        }
        bossBar.removePlayer(serverPlayer)
    }


    override fun die(damageSource: DamageSource) {
        if (!isEntityDeath()) {
            setAttacking(false)
            setEntityDeath()
            playDeathAnimation()
            startAge = tickCount
            skillManager.setEntityDeath()
            cancelAllAI()
            health = 1f
            SkillManagerManager.clearCacheIfOwnerDead()
            return
        }
        super.die(damageSource)
    }

    private fun cancelAllAI() {
        goalSelector.removeAllGoals { true }
        targetSelector.removeAllGoals { true }
    }

    private fun playDeathAnimation() {
        if (level().isClientSide) return
        val random = Random(System.currentTimeMillis())
        deathAnimation.addAnimate(
            EmittersAnimate({
                val startRadius = 5.0
                val endRandomRange = 40.0
                val sin = sin(random.nextDouble(-PI, PI)) * startRadius
                val cos = cos(random.nextDouble(-PI, PI)) * startRadius
                val randomPos = Vec3(
                    cos,
                    random.nextDouble(-startRadius, startRadius),
                    sin,
                )
                val spawnPos = eyePosition.add(randomPos)
                LightningParticleEmitters(
                    spawnPos, level()
                ).apply {
                    endPos = RelativeLocation(
                        random.nextDouble(-endRandomRange, endRandomRange),
                        random.nextDouble(-endRandomRange, endRandomRange),
                        random.nextDouble(-endRandomRange, endRandomRange),
                    )
                    maxTick = random.nextInt(1, 5)
                    templateData.also {
                        it.maxAge = 5
                        it.color = Math3DUtil.colorOf(
                            121, 211, 249
                        )
                    }
                }
            }, position(), 2, -1) {
                it as LightningParticleEmitters
                CooParticlesAPI.scheduler.runTaskTimerMaxTick(it.maxTick) {
                    val endRandomRange = 40.0
                    it.endPos = RelativeLocation(
                        random.nextDouble(-endRandomRange, endRandomRange),
                        random.nextDouble(-endRandomRange, endRandomRange),
                        random.nextDouble(-endRandomRange, endRandomRange),
                    )
                }
            }
        ).addAnimate(
            StyleAnimate(BookEntityDeathStyle(id), level() as ServerLevel, position(), -1)
        ).addAnimate(
            EmittersAnimate({
                val emitter = DirectionShootEmitters(position(), level()).apply {
                    templateData.also { it ->
                        it.maxAge = 40
                        it.speed = 1.5
                        it.effect = ControlableCloudEffect(it.uuid)
                    }
                    this.shootDirection = Vec3(0.0, 24.0, 0.0)
                    count = 40
                    randomX = 4.0
                    randomY = 12.0
                    randomZ = 4.0
                    randomSpeedOffset = 0.5
                    gravity = PhysicConstant.EARTH_GRAVITY
                    shootType = EmittersShootTypes.box(HitBox.of(8.0, 1.0, 8.0))
                    maxTick = 20
                }
                emitter
            }, position(), 20, -1) {}
        ).addAnimate(
            EmittersAnimate({
                val emitter = DirectionShootEmitters(position(), level()).apply {
                    templateData.also { it ->
                        it.maxAge = 40
                        it.speed = 1.0
                        it.color = Math3DUtil.colorOf(255, 100, 80)
                    }
                    this.shootDirection = Vec3(0.0, 16.0, 0.0)
                    count = 5
                    randomX = 4.0
                    randomY = 12.0
                    randomZ = 4.0
                    randomSpeedOffset = 0.3
                    gravity = PhysicConstant.EARTH_GRAVITY
                    shootType = EmittersShootTypes.box(HitBox.of(8.0, 1.0, 8.0))
                    maxTick = 20
                }
                emitter
            }, position(), 20, -1) {}
        ).addAnimate(
            EmittersAnimate({
                val emitter = ParticleWaveEmitters(position(), level()).apply {
                    templateData.also { it ->
                        it.maxAge = 40
                        it.color = Math3DUtil.colorOf(255, 100, 80)
                        it.effect = ControlableCloudEffect(it.uuid)
                    }
                    waveCircleCountMin = 240
                    waveCircleCountMax = 480
                    waveSize = 40.0
                    waveSpeed = -1.0
                    maxTick = 1
                }
                emitter
            }, position(), 20, -1) {}
        )
        for (i in 0 until deathAnimation.animations.size) {
            deathAnimation.spawnSingle()
        }
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (isEntityDeath()) return false
        if (entitySpawning) return false
        val sources = level().damageSources()
        val onFire = sources.onFire().type()
        val inFire = sources.inFire().type()
        val campFire = sources.campfire().type()
        var actualDamage = amount
        if (source.type() == sources.fall().type()) {
            return false
        }
        if (source.type() == inFire || source.type() == onFire || campFire == onFire) {
            actualDamage *= 3
        }
        val activeSkill = skillManager.active
        // 设置技能中断条件
        if (activeSkill != null && activeSkill is SkillDamageCancelCondition) {
            activeSkill.damage(actualDamage)
        }
        return super.hurt(source, actualDamage)
    }

    override fun isPersistenceRequired(): Boolean {
        return true
    }

    override fun getUnlimitMaxHealth(): Float {
        return entityData.get(LARGE_MAX_HEALTH)
    }

    override fun setHealth(health: Float) {
        entityData.set(BOOK_HEALTH, health.coerceIn(0f, bookMaxHealth))
    }

    override fun getHealth(): Float {
        return entityData.get(BOOK_HEALTH)
    }

    var tick = 0
    var startAge = 0
    var startPitch = 1f
    var handled = false
    var spawningTick = 80
    override fun getHurtSound(source: DamageSource): SoundEvent? {
        return SoundEvents.BOOK_PAGE_TURN
    }

    override fun dropCustomDeathLoot(level: ServerLevel, damageSource: DamageSource, recentlyHit: Boolean) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit)
        spawnAtLocation { UsefulMagicItems.EXPLOSION_WAND.getItem() }
    }

    override fun remove(reason: RemovalReason) {
        super.remove(reason)
        // 移除实体必然需要中断技能使用
//        println("移除实体: $reason")
        skillManager.resetActiveSkill(false)
    }

    override fun checkDespawn() {
        super.checkDespawn()
    }

    override fun tick() {
        bossBar.progress = health / bookMaxHealth
        if (entitySpawning) {
            val step = bookMaxHealth / spawningTick
            health += step
            if (health >= bookMaxHealth) {
                entitySpawning = false
                if (!level().isClientSide) {
                    ServerCameraUtil.sendShake(
                        level() as ServerLevel, position(), 128.0, 0.6, 20
                    )
                }
                // 正式生成成功
                level().playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 10f, 1.2f)
                val explosion = ExplodeMagicEmitters(position(), level()).apply {
                    this.templateData.also {
                        it.color = Math3DUtil.colorOf(255, 255, 255)
                        it.size = 0.4f
                    }
                    randomParticleAgeMin = 20
                    randomParticleAgeMax = 60
                    precentDrag = 0.95
                    maxTick = 5
                    ballCountPow = 20
                    minSpeed = 0.5
                    maxSpeed = 8.0
                    randomCountMin = 120
                    randomCountMax = 400
                }
                ParticleEmittersManager.spawnEmitters(explosion)
            }
            return
        }

        if (isEntityDeath()) {
            val currentTick = (tickCount - startAge)
            if (currentTick > 20 * 19 && !level().isClientSide) {
                ExperienceOrb.award(this.level() as ServerLevel, this.position(), 250)
            }
            if (currentTick > 20 * 20) {
                health = 0f
                super.tick()
                val source = lastAttacker
                if (source != null) {
                    if (source is Player) {
                        die(damageSources().playerAttack(source))
                    } else {
                        die(damageSources().mobAttack(source))
                    }
                } else {
                    die(damageSources().generic())
                }
                if (!level().isClientSide && !handled) {
                    handled = true
                    val explosion = ExplodeMagicEmitters(position(), level()).apply {
                        this.templateData.also {
                            it.color = Math3DUtil.colorOf(255, 170, 200)
                            it.size = 0.4f
                        }
                        randomParticleAgeMin = 20
                        randomParticleAgeMax = 60
                        precentDrag = 0.95
                        maxTick = 30
                        ballCountPow = 10
                        minSpeed = 0.5
                        maxSpeed = 5.0
                        randomCountMin = 30
                        randomCountMax = 80
                    }
                    ParticleEmittersManager.spawnEmitters(explosion)
                    level().playSound(
                        null,
                        x,
                        y,
                        z,
                        SoundEvents.BEACON_DEACTIVATE,
                        SoundSource.HOSTILE,
                        10f,
                        0.9f
                    )
                    deathAnimation.cancel()
                }
                return
            }
            if (currentTick % 2 == 0) {
                level().playSound(
                    null,
                    x,
                    y,
                    z,
                    SoundEvents.BOOK_PAGE_TURN,
                    SoundSource.HOSTILE,
                    4f,
                    1f
                )
            }
            if (currentTick % 20 == 0) {
                level().playSound(
                    null, x, y, z,
                    SoundEvents.BEACON_ACTIVATE,
                    SoundSource.HOSTILE,
                    10f,
                    startPitch
                )
                startPitch += 0.05f
            }
            if (!level().isClientSide) {
                deathAnimation.doTick()
                if (deathAnimation.animations.isEmpty()) {
                    playDeathAnimation()
                }
                if (currentTick % 10 == 0) {
                    ServerCameraUtil.sendShake(
                        level() as ServerLevel, position(), 64.0, 0.4, 10
                    )
                }
            }
            return
        }
        if (deltaMovement.y > 1.0) {
            deltaMovement = Vec3(deltaMovement.x.coerceIn(-1.0, 1.0), 1.0, deltaMovement.z.coerceIn(-1.0, 1.0))
        }
        super.tick()
        tick++
        setAnimation()
        skillManager.tick()
    }

    internal var attackTick = 0
    private fun setAnimation() {
        if (isAttacking() && attackTick <= 0) {
            attackTick = 5
            attackAnimateState.start(tickCount)
        } else {
            attackTick--
        }
        if (!isAttacking()) {
            attackTick = 0
            attackAnimateState.stop()
        }
    }

    override fun performRangedAttack(target: LivingEntity?, pullProgress: Float) {
        if (level().isClientSide) return
        if (target == null) return
        val direction = position().relativize(target.position())
        val barrage = EntityWoodenBarrage(
            2.0, target, eyePosition, level() as ServerLevel,
            random.nextDouble() > 0.8
        )
        barrage.direction = direction
        barrage.shooter = this
        BarrageManager.spawn(barrage)
    }


    fun getHealthState(): Int {
        val max = getUnlimitMaxHealth()
        return when (health) {
            in 0f..max / 5f -> 1
            in max / 5f..2 * max / 3f -> 2
            in 2 * max / 3f..max -> 3
            else -> 3
        }
    }

}