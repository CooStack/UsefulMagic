package cn.coostack.usefulmagic.entity.custom

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.HitBox
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
import cn.coostack.usefulmagic.particles.animation.EmittersAnimate
import cn.coostack.usefulmagic.particles.animation.ParticleAnimation
import cn.coostack.usefulmagic.particles.animation.StyleAnimate
import cn.coostack.usefulmagic.particles.barrages.EntityWoodenBarrage
import cn.coostack.usefulmagic.particles.emitters.DirectionShootEmitters
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import cn.coostack.usefulmagic.particles.emitters.ParticleWaveEmitters
import cn.coostack.usefulmagic.particles.style.entitiy.BookEntityDeathStyle
import cn.coostack.usefulmagic.skill.api.EntitySkillManager
import cn.coostack.usefulmagic.skill.api.SkillDamageCancelCondition
import net.minecraft.entity.AnimationState
import net.minecraft.entity.EntityPose
import net.minecraft.entity.EntityType
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.RangedAttackMob
import net.minecraft.entity.ai.control.FlightMoveControl
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.EndermanEntity
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.*
import kotlin.random.Random

/**
 * BOSS有3个生命阶段
 * 0-1/3*maxHealth, 1/3*maxHealth-2/3*maxHealth, 2/3*maxHealth-maxHealth
 */
class MagicBookEntity(entityType: EntityType<out PathAwareEntity>, world: World) : PathAwareEntity(entityType, world),
    RangedAttackMob, UnlimitHealthEntity {
    val bossBar = ServerBossBar(displayName, BossBar.Color.RED, BossBar.Style.PROGRESS)
    val skillManager = EntitySkillManager(this)
    var bookMaxHealth: Float
        get() = dataTracker.get(LARGE_MAX_HEALTH)
        set(field) {
            dataTracker.set(LARGE_MAX_HEALTH, field.coerceAtLeast(0.5f))
        }

    constructor(world: World) : this(
        UsefulMagicEntityTypes.MAGIC_BOOK_ENTITY_TYPE, world
    )

    companion object {
        @JvmStatic
        private val ENTITY_SPAWNING = DataTracker.registerData(
            MagicBookEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN
        )

        @JvmStatic
        private val SET_ENTITY_DEATH = DataTracker.registerData(
            MagicBookEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN
        )

        @JvmStatic
        private val IS_ATTACKING = DataTracker.registerData(
            MagicBookEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN
        )

        @JvmStatic
        private val LARGE_MAX_HEALTH = DataTracker.registerData(
            MagicBookEntity::class.java, TrackedDataHandlerRegistry.FLOAT
        )

        @JvmStatic
        private val BOOK_HEALTH = DataTracker.registerData(
            MagicBookEntity::class.java, TrackedDataHandlerRegistry.FLOAT
        )

        @JvmStatic
        fun createDefaultMobAttributes(): DefaultAttributeContainer.Builder {
            return createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.5)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 20.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
        }
    }

    var entitySpawning: Boolean
        get() = dataTracker.get(ENTITY_SPAWNING)
        set(value) {
            dataTracker.set(ENTITY_SPAWNING, value)
        }
    val attackAnimateState = AnimationState()
    val deathAnimation = ParticleAnimation()

    init {
        skillManager.addSkill(MagicSwordSkill())
        skillManager.addSkill(HealthReverseSkill())
        skillManager.addSkill(BookShootSkill(2))
        skillManager.addSkill(BookCannonballsSkill(18f))
        skillManager.addSkill(BookSwordSlashSkill(8f))
        health = 1f
    }

    override fun initDataTracker(builder: DataTracker.Builder?) {
        super.initDataTracker(builder)
        builder?.add(IS_ATTACKING, false)
        builder?.add(LARGE_MAX_HEALTH, 4096f)
        builder?.add(BOOK_HEALTH, 1f)
        builder?.add(SET_ENTITY_DEATH, false)
        builder?.add(ENTITY_SPAWNING, true)
    }

    override fun isAttacking(): Boolean {
        return dataTracker.get(IS_ATTACKING)
    }

    override fun setAttacking(attacking: Boolean) {
        super.setAttacking(attacking)
        dataTracker.set(IS_ATTACKING, attacking)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        if (this.hasCustomName()) {
            this.bossBar.setName(this.displayName)
        }
        dataTracker.set(SET_ENTITY_DEATH, nbt.getBoolean("entity_death"))
        dataTracker.set(
            ENTITY_SPAWNING,
            if (nbt.contains("entity_spawning")) nbt.getBoolean("entity_spawning") else true
        )
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        nbt.putBoolean("entity_death", isEntityDeath())
        nbt.putBoolean("entity_spawning", entitySpawning)
    }

    override fun setCustomName(name: Text?) {
        super.setCustomName(name)
        this.bossBar.setName(this.displayName)
    }

    fun setEntityDeath() {
        dataTracker.set(SET_ENTITY_DEATH, true)
    }

    override fun updateLimbs(posDelta: Float) {
        super.updateLimbs(posDelta)
        val pose = if (this.pose == EntityPose.STANDING) min(posDelta * 6f, 5f) else 0f
        this.limbAnimator.updateLimbs(pose, 0.02f)
    }

    /**
     * 是否正在播放死亡动画
     */
    fun isEntityDeath(): Boolean = dataTracker.get(SET_ENTITY_DEATH)

    override fun initGoals() {
        super.initGoals()
        goalSelector.apply {
            add(0, SwimGoal(this@MagicBookEntity))
            add(3, FlyGoal(this@MagicBookEntity, 1.0))
            add(3, MagicCloseTargetGoal(this@MagicBookEntity, 8.0))
            add(6, LookAtEntityGoal(this@MagicBookEntity, PlayerEntity::class.java, 3f))
            add(7, LookAroundGoal(this@MagicBookEntity))
            add(1, MagicAttackGoal(this@MagicBookEntity, 5))
            add(1, IllegalFlyingGoal(this@MagicBookEntity))
        }
        targetSelector.add(1, RevengeGoal(this))
        targetSelector.add(1, ActiveTargetGoal(this, PlayerEntity::class.java, true))
        targetSelector.add(1, ActiveTargetGoal(this, EndermanEntity::class.java, true))
        moveControl = FlightMoveControl(this, 10, true)
    }


    override fun tickMovement() {
        if (isEntityDeath() || entitySpawning) {
            velocity = Vec3d.ZERO
            return
        }
        if (target == null) {
            if (!isOnGround) {
                moveControl.moveTo(x, -1.0, z, 0.8)
            }
            super.tickMovement()
            return
        }

        if (target!!.y + 6.0 > y) {
            velocity = velocity.add(0.0, 0.05, 0.0)
        } else if (target!!.y + 4 < y) {
            velocity = velocity.add(0.0, -0.05, 0.0)
        }

        // 距离目标水平距离
        val len = sqrt((target!!.x - x).pow(2) + (target!!.z - z).pow(2))
        if (len >= 16 && skillManager.active == null) {
            velocity = velocity.add(Vec3d(target!!.x - x, 0.0, target!!.z - z).normalize().multiply(0.25))
        }
        super.tickMovement()
    }

    override fun onStartedTrackingBy(player: ServerPlayerEntity?) {
        super.onStartedTrackingBy(player)
        bossBar.addPlayer(player)
    }

    override fun onStoppedTrackingBy(player: ServerPlayerEntity?) {
        super.onStoppedTrackingBy(player)
        bossBar.removePlayer(player)
        // 没有玩家看得到bossBar, 说明他妈的这个区块应该快GG了
//        println("stopped tracking by $player")
//        if (bossBar.players.isEmpty()) {
//            // 移除实体必然需要中断技能使用
//            skillManager.resetActiveSkill(false)
//        }
    }

    override fun onDeath(damageSource: DamageSource?) {
        if (!isEntityDeath()) {
            isAttacking = false
            setEntityDeath()
            playDeathAnimation()
            startAge = age
            skillManager.setEntityDeath()
            cancelAllAI()
            health = 1f
            return
        }
        super.onDeath(damageSource)
    }

    private fun cancelAllAI() {
        goalSelector.clear { true }
        targetSelector.clear { true }
    }

    private fun playDeathAnimation() {
        if (world.isClient) return
        val random = Random(System.currentTimeMillis())
        deathAnimation.addAnimate(
            EmittersAnimate({
                val startRadius = 5.0
                val endRandomRange = 40.0
                val sin = sin(random.nextDouble(-PI, PI)) * startRadius
                val cos = cos(random.nextDouble(-PI, PI)) * startRadius
                val randomPos = Vec3d(
                    cos,
                    random.nextDouble(-startRadius, startRadius),
                    sin,
                )
                val spawnPos = eyePos.add(randomPos)
                LightningParticleEmitters(
                    spawnPos, world
                ).apply {
                    endPos = RelativeLocation(
                        random.nextDouble(-endRandomRange, endRandomRange),
                        random.nextDouble(-endRandomRange, endRandomRange),
                        random.nextDouble(-endRandomRange, endRandomRange),
                    )
                    maxTick = random.nextInt(1, 5)
                    templateData.also {
                        it.color = Math3DUtil.colorOf(
                            121, 211, 249
                        )
                    }
                }
            }, pos, 2, -1) {
                it as LightningParticleEmitters
                CooParticleAPI.scheduler.runTaskTimerMaxTick(it.maxTick) {
                    val endRandomRange = 40.0
                    it.endPos = RelativeLocation(
                        random.nextDouble(-endRandomRange, endRandomRange),
                        random.nextDouble(-endRandomRange, endRandomRange),
                        random.nextDouble(-endRandomRange, endRandomRange),
                    )
                }
            }
        ).addAnimate(
            StyleAnimate(BookEntityDeathStyle(id), world as ServerWorld, pos, -1)
        ).addAnimate(
            EmittersAnimate({
                val emitter = DirectionShootEmitters(pos, world).apply {
                    templateData.also { it ->
                        it.maxAge = 40
                        it.speed = 1.5
                        it.effect = ControlableCloudEffect(it.uuid)
                    }
                    this.shootDirection = Vec3d(0.0, 24.0, 0.0)
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
            }, pos, 20, -1) {}
        ).addAnimate(
            EmittersAnimate({
                val emitter = DirectionShootEmitters(pos, world).apply {
                    templateData.also { it ->
                        it.maxAge = 40
                        it.speed = 1.0
                        it.color = Math3DUtil.colorOf(255, 100, 80)
                    }
                    this.shootDirection = Vec3d(0.0, 16.0, 0.0)
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
            }, pos, 20, -1) {}
        ).addAnimate(
            EmittersAnimate({
                val emitter = ParticleWaveEmitters(pos, world).apply {
                    templateData.also { it ->
                        it.maxAge = 40
                        it.color = Math3DUtil.colorOf(255, 100, 80)
                        it.effect = ControlableCloudEffect(it.uuid)
                    }
                    waveCircleCountMin = 120
                    waveCircleCountMax = 240
                    waveSize = 40.0
                    waveSpeed = -1.0
                    maxTick = 1
                }
                emitter
            }, pos, 20, -1) {}
        )
        for (i in 0 until deathAnimation.animations.size) {
            deathAnimation.spawnSingle()
        }
    }

    override fun damage(source: DamageSource, amount: Float): Boolean {
        if (isEntityDeath()) return false
        if (entitySpawning) return false
        val sources = world.damageSources
        val onFire = sources.onFire().type
        val inFire = sources.inFire().type
        val campFire = sources.campfire().type
        var actualDamage = amount
        if (source.type == sources.fall().type) {
            return false
        }
        if (source.type == inFire || source.type == onFire || campFire == onFire) {
            actualDamage *= 3
        }
        val activeSkill = skillManager.active
        // 设置技能中断条件
        if (activeSkill != null && activeSkill is SkillDamageCancelCondition) {
            activeSkill.damage(actualDamage)
        }
        return super.damage(source, actualDamage)
    }

    override fun isPersistent(): Boolean {
        return true
    }

    override fun getUnlimitMaxHealth(): Float {
        return dataTracker.get(LARGE_MAX_HEALTH)
    }

    override fun setHealth(health: Float) {
        dataTracker.set(BOOK_HEALTH, health.coerceIn(0f, bookMaxHealth))
    }

    override fun getHealth(): Float {
        return dataTracker.get(BOOK_HEALTH)
    }

    var tick = 0
    var startAge = 0
    var startPitch = 1f
    var handled = false
    var spawningTick = 80
    override fun getHurtSound(source: DamageSource?): SoundEvent? {
        return SoundEvents.ITEM_BOOK_PAGE_TURN
    }

    override fun dropLoot(damageSource: DamageSource?, causedByPlayer: Boolean) {
        super.dropLoot(damageSource, causedByPlayer)
        dropStack(UsefulMagicItems.EXPLOSION_WAND.defaultStack)
    }

    override fun remove(reason: RemovalReason?) {
        super.remove(reason)
        // 移除实体必然需要中断技能使用
//        println("移除实体: $reason")
        skillManager.resetActiveSkill(false)
    }

    override fun checkDespawn() {
        super.checkDespawn()
    }

    override fun tick() {
        if (entitySpawning) {
            val step = bookMaxHealth / spawningTick
            health += step
            if (health >= bookMaxHealth) {
                entitySpawning = false
                if (!world.isClient) {
                    ServerCameraUtil.sendShake(
                        world as ServerWorld, pos, 128.0, 0.6, 20
                    )
                }
                // 正式生成成功
                world.playSound(null, x, y, z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 10f, 1.2f)
                val explosion = ExplodeMagicEmitters(pos, world).apply {
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
        }

        if (isEntityDeath()) {
            val currentTick = (age - startAge)
            if (currentTick > 20 * 19 && !world.isClient) {
                ExperienceOrbEntity.spawn(this.world as ServerWorld, this.pos, 250)
            }
            if (currentTick > 20 * 20) {
                health = 0f
                super.tick()
                val source = lastAttacker
                if (source != null) {
                    if (source is PlayerEntity) {
                        onDeath(damageSources.playerAttack(source))
                    } else {
                        onDeath(damageSources.mobAttack(source))
                    }
                } else {
                    onDeath(damageSources.generic())
                }
                if (!world.isClient && !handled) {
                    handled = true
                    val explosion = ExplodeMagicEmitters(pos, world).apply {
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
                    world.playSound(
                        null,
                        x,
                        y,
                        z,
                        SoundEvents.BLOCK_BEACON_DEACTIVATE,
                        SoundCategory.HOSTILE,
                        10f,
                        0.9f
                    )
                    deathAnimation.cancel()
                }
                return
            }
            if (currentTick % 2 == 0) {
                world.playSound(
                    null,
                    x,
                    y,
                    z,
                    SoundEvents.ITEM_BOOK_PAGE_TURN,
                    SoundCategory.HOSTILE,
                    4f,
                    1f
                )
            }
            if (currentTick % 20 == 0) {
                world.playSound(
                    null, x, y, z,
                    SoundEvents.BLOCK_BEACON_ACTIVATE,
                    SoundCategory.HOSTILE,
                    10f,
                    startPitch
                )
                startPitch += 0.05f
            }
            if (!world.isClient) {
                deathAnimation.doTick()
                if (deathAnimation.animations.isEmpty()) {
                    playDeathAnimation()
                }
                if (currentTick % 10 == 0) {
                    ServerCameraUtil.sendShake(
                        world as ServerWorld, pos, 64.0, 0.4, 10
                    )
                }
            }
        }
        if (velocity.y > 1.0) {
            velocity = Vec3d(velocity.x.coerceIn(-1.0, 1.0), 1.0, velocity.z.coerceIn(-1.0, 1.0))
        }
        super.tick()
        tick++
        setAnimation()
        bossBar.percent = health / bookMaxHealth
        skillManager.tick()
    }

    internal var attackTick = 0
    private fun setAnimation() {
        if (isAttacking && attackTick <= 0) {
            attackTick = 5
            attackAnimateState.start(age)
        } else {
            attackTick--
        }
        if (!isAttacking) {
            attackTick = 0
            attackAnimateState.stop()
        }
    }

    override fun shootAt(target: LivingEntity?, pullProgress: Float) {
        if (world.isClient) return
        if (target == null) return
        val direction = pos.relativize(target.pos)
        val barrage = EntityWoodenBarrage(
            2.0, target, eyePos, world as ServerWorld,
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