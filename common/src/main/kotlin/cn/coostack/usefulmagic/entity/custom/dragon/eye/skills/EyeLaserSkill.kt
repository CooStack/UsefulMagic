package cn.coostack.usefulmagic.entity.custom.dragon.eye.skills

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.cooparticlesapi.scheduler.CooScheduler
import cn.coostack.cooparticlesapi.supports.sound.ServerDuckingSoundEffect
import cn.coostack.cooparticlesapi.supports.sound.ServerManagedSoundInstance
import cn.coostack.cooparticlesapi.supports.sound.ServerSoundManager
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.barrages.entity.skill.StraightPointBarrage
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicSubEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.phases.EyeHoverPhase
import cn.coostack.usefulmagic.entity.custom.dragon.playDragonSoundOnce
import cn.coostack.usefulmagic.extend.*
import cn.coostack.usefulmagic.particles.entity.eye.skill.EyeLaserMagicComposition
import cn.coostack.usefulmagic.renderer.StraightLaserRenderEntity
import cn.coostack.usefulmagic.renderer.UsefulMagicPostEffects
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import cn.coostack.usefulmagic.skill.api.SkillCondition
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

/**
 * 先释放一个魔法阵 （类似于无名光神的那种冲击波方案）
 */
class EyeLaserSkill(val damage: Double) : Skill<MagicEyeEntity>, SkillCondition<MagicEyeEntity>,
    SkillCancelCondition<MagicEyeEntity> {
    companion object {
        const val ID = "eye_laser_skill"
        private const val LASER_DISTANCE = 200.0
    }

    private var laser: StraightLaserRenderEntity? = null
    private var composition = EyeLaserMagicComposition(Vec3.ZERO, null)
    private var currentLaserDirection: Vec3? = null
    private var currentLaserTargetPosition: Vec3? = null

    private var loop: ServerManagedSoundInstance? = null
    private var ducking: ServerDuckingSoundEffect? = null
    private var active = false
    private var laserSession = 0
    private var task: CooScheduler.TickRunnable? = null
    override var chance: Double = 12.0


    override fun getSkillCountDown(source: MagicEyeEntity): Int {
        return 0
    }

    override fun onActive(source: MagicEyeEntity) {
        val session = ++laserSession
        active = true
        composition = EyeLaserMagicComposition(source.positionOnEye(), source.level())
        val initialTargetPosition = initialLaserTargetPosition(source)
        currentLaserTargetPosition = initialTargetPosition
        currentLaserDirection = source.targetTowards(initialTargetPosition)
        updateLaserAndComposition(source, initialTargetPosition)
        ParticleCompositionManager.spawn(composition)
        source.phaseManager.forceSetPhase(
            EyeHoverPhase()
        ) {
            setCurrentTarget(source.position() + Vec3(0.0, 5.0, 0.0))
        }


        playDragonSoundOnce(
            source,
            UsefulMagicSoundEvents.LASER_CHARGE_UP.get(),
            SoundSource.HOSTILE,
            0.25F,
            1F,
            256.0,
        )
        task = submitTaskServer(5 * 20) {
            if (!shouldSpawnLaser(source, session)) {
                return@submitTaskServer
            }
            playDragonSoundOnce(
                source,
                UsefulMagicSoundEvents.LASER_START.get(),
                SoundSource.HOSTILE,
                0.9F,
                1F,
                256.0,
            )
            source.deltaMovement += (currentLaserDirection ?: source.currentTowards()).normalize() * -2
            ducking = ServerSoundManager.spawnDucking(
                ServerDuckingSoundEffect(
                    "laser_ducking", source.level() as ServerLevel,
                    source.positionOnEye(),
                    0F,
                    -1.0,
                    setOf(
                        UsefulMagicSoundEvents.LASER_START.id,
                        UsefulMagicSoundEvents.LASER_LOOP.id,
                        UsefulMagicSoundEvents.LASER_OBLITERATION.id
                    )
                )
            ).apply {
                visibleRange = 256.0
                bindToEntity(source)
            }
            loop = ServerSoundManager.instance(
                UsefulMagicSoundEvents.LASER_LOOP.get(),
                SoundSource.HOSTILE
            )
                .volume(0F)
                .pitch(1F)
                .entity(source)
                .layer("laser_loop")
                .looping()
                .visibleRange(256.0)
                .stopWhenBoundEntityMissing(false)
                .spawn().apply {
                    fadeIn(100, 0.5F, 0F)
                }
            laser = StraightLaserRenderEntity(source.level(), source.positionOnEye()).apply {
                this.lifetime = 20 * 30
                this.phaseTicks = 20
                this.color = Math3DUtil.colorOf(255, 100, 240)
                this.maxRadius = 6F
                this.brightness = 1.6f
            }
            val box = source.boundingBox.inflate(256.0)
            source.level().getEntitiesOfClass(Player::class.java, box).forEach {
                UsefulMagicPostEffects.playFlameExplodeFlash(
                    it as ServerPlayer,
                    color = Math3DUtil.colorOf(255, 100, 240).asVec3(),
                )
            }
            ServerRenderEntityManager.spawn(laser!!)
        }
    }

    override fun onRelease(source: MagicEyeEntity, holdingTick: Int) {
        cleanupLaserEffects()
        playDragonSoundOnce(
            source.serverLevel!!,
            source.position(),
            UsefulMagicSoundEvents.LASER_OBLITERATION.get(),
            SoundSource.HOSTILE,
            0.5F,
            1F,
            256.0,
        )
        source.hasLaserSkillActive = true
        source.kill()
    }

    override fun getMaxHoldingTick(holdingEntity: MagicEyeEntity): Int {
        return 20 * (31 + 5)
    }

    override fun holdingTick(
        holdingEntity: MagicEyeEntity,
        holdTicks: Int
    ) {

        // 伤害在打出之后才有 但是魔法阵要一直跟随玩家
        loop?.let {
            val pitch = ((holdTicks.toDouble() - 20) / (getMaxHoldingTick(holdingEntity) / 2))
                .lerpAsProgress(1, 1.3).toFloat()
            it.pitchMultiplier = pitch
        }
        holdingEntity.target?.let {
            val targetPos =
                (it.boxCenterPosition() - holdingEntity.positionOnEye()).normalize() * LASER_DISTANCE + holdingEntity.positionOnEye()
            val laserTargetPos = moveTowards(
                currentLaserTargetPosition ?: initialLaserTargetPosition(holdingEntity),
                targetPos,
                3.7
            )
            currentLaserTargetPosition = laserTargetPos
            currentLaserDirection = holdingEntity.targetTowards(laserTargetPos)
            holdingEntity.lookAtPos(targetPos, 10F, 10F)
            updateLaserAndComposition(holdingEntity)
        }
        if (holdTicks - 5 * 20 <= (laser?.phaseTicks ?: Int.MAX_VALUE)) return

        // 伤害实体
        val direction = currentLaserDirection?.normalize() ?: holdingEntity.currentTowards()
        val laserStart = holdingEntity.positionOnEye()
        val laserEnd = laserStart + direction * LASER_DISTANCE
        val laserDamageRadius = 6.0
        val laserBox = AABB(laserStart, laserEnd).inflate(laserDamageRadius)
        val damageSource = UsefulMagicDamageSources.entityDamage(
            holdingEntity.level(),
            holdingEntity,
            holdingEntity
        )
        holdingEntity.level().getEntitiesOfClass(LivingEntity::class.java, laserBox) {
            it.uuid != holdingEntity.uuid && it.isAlive && it !is MagicEyeEntity && it !is MagicSubEyeEntity
        }.forEach { entity ->
            val entityCenter = entity.boundingBox.center
            val distanceOnLaser = (entityCenter - laserStart).dot(direction)
            if (distanceOnLaser !in 0.0..LASER_DISTANCE) return@forEach

            val closestPoint = laserStart + direction * distanceOnLaser
            if (entityCenter.distanceToSqr(closestPoint) <= laserDamageRadius * laserDamageRadius) {
                if (entity.hurt(damageSource, damage.toFloat())) {
                    entity.invulnerableTime = 5
                }
            }
        }

        holdingEntity.serverLevelApply { world ->
            ServerCameraUtil.sendShake(world, this.positionOnEye(), 256.0, 0.5, 10, 8.0, true)
            if (holdTicks % 20 == 0) {
                val box = boundingBox.inflate(128.0)
                val targets: MutableSet<LivingEntity> = world.getEntitiesOfClass(Player::class.java, box) {
                    it.isAlive && !(it.isSpectator || it.isCreative) && it.uuid != this.target?.uuid
                }.takeLast(3).toMutableSet()
                target?.let {
                    targets.add(it)
                }

                targets.forEach { entity ->
                    repeat(Random.nextInt(5, 8)) {
                        val targetPos = entity.boxCenterPosition()
                        val loc = targetPos.offsetRandomly(Random.nextDouble(25.5, 50.0))
                        StraightPointBarrage(loc, world, 8.0, this).apply {
                            this.direction = targetPos - loc
                            options.speed(3.1)
                            BarrageManager.spawn(this)
                        }
                    }
                }

            }
        }


    }

    override fun stopHolding(entity: MagicEyeEntity, holdTicks: Int) {
        cleanupLaserEffects()
    }

    private fun cleanupLaserEffects() {
        laser?.discard()
        laser = null
        active = false
        if (!composition.status.isDisable()) {
            composition.remove()
        }
        loop?.fadeOut(10)
        loop = null
        ducking?.fadeOut(10)
        ducking = null
        task?.cancel()
    }

    private fun shouldSpawnLaser(source: MagicEyeEntity, session: Int): Boolean {
        return active &&
                session == laserSession &&
                source.isAlive &&
                !source.isRemoved &&
                source.skillManager.active === this
    }

    private fun updateLaserAndComposition(source: MagicEyeEntity, lockedTargetPosition: Vec3? = null) {
        val direction = lockedTargetPosition?.let { source.targetTowards(it) }
            ?: currentLaserDirection ?: return
        val start = source.positionOnEye() + direction * 4
        composition.teleportTo(start)
        laser?.updateBeam(start, start + direction * LASER_DISTANCE)
        composition.direction = currentLaserDirection!!.asRelative()
        if (composition.displayed) {
            composition.markDirty()
        }
    }

    private fun initialLaserTargetPosition(source: MagicEyeEntity): Vec3 {
        source.target?.let {
            return it.position()
        }
        val direction = currentLaserDirection ?: source.currentTowards()
        return source.positionOnEye() + direction * LASER_DISTANCE
    }

    private fun moveTowards(current: Vec3, target: Vec3, maxDistance: Double): Vec3 {
        val delta = target - current
        val distanceSqr = delta.lengthSqr()
        if (distanceSqr <= maxDistance * maxDistance) {
            return target
        }
        return current + delta.normalize() * maxDistance
    }

    override fun getSkillID(): String {
        return ID
    }

    override fun canTrigger(entity: MagicEyeEntity): Boolean {
        return entity.health <= 2 && !entity.hasLaserSkillActive
    }

    override fun testCancel(entity: MagicEyeEntity): Boolean {
        return entity.target == null
    }

    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = false
}
