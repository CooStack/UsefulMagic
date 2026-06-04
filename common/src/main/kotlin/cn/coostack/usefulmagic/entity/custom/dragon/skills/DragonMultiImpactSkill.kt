package cn.coostack.usefulmagic.entity.custom.dragon.skills

import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.effects.UsefulMagicEffects
import cn.coostack.usefulmagic.entity.custom.book.MagicBookEntity
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicSubEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.phases.DragonImpactSkillPhase
import cn.coostack.usefulmagic.entity.custom.dragon.phases.DragonLookAndKeepDistancePhase
import cn.coostack.usefulmagic.entity.custom.dragon.playDragonSoundOnce
import cn.coostack.usefulmagic.entity.custom.dragon.skills.composition.DragonLaserSmallComposition
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.set
import cn.coostack.usefulmagic.renderer.StraightLaserRenderEntity
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

/**
 * 多段冲刺
 *
 * 快速到达玩家附近 （无视朝向）
 *
 * 等待后冲刺， 冲刺时无视Phase的状态， 或者说技能本身就是一个不断切换Phase的作用
 *
 * 当血量小于 最大血量1/2的时候才能使用
 *
 * 冲刺改为： TODO
 *  1. 冲刺1次， 跟随4次激光 （每次2-3个， 随机偏移）
 *  2. 最后一次冲刺 使用大激光 （远离，跟随, 淡入眼睛）
 */
class DragonMultiImpactSkill : DragonSkill() {
    override var chance: Double = 0.25

    companion object {
        const val ID = "dragon_multi_impact_skill"

        /** 最少冲刺次数：未达到时 [getMaxHoldingTick] 持续递增 */
        private const val MIN_IMPACTS = 5

        /** 最多冲刺次数：达到时强制释放 */
        private const val MAX_IMPACTS = 10

        /** 达到最低次数后的默认 holding tick 上限 */
        private const val BASE_HOLD_TICKS = 20 * 3

        /** 判定为"减速到接近0"的速度平方阈值 */
        private const val VELOCITY_NEAR_ZERO_SQ = 0.04 * 0.04

        /** 允许下一段冲刺的目标距离最小值 */
        private const val INSIDE_RANGE_MIN = 14.0

        /** 允许下一段冲刺的目标距离最大值 */
        private const val INSIDE_RANGE_MAX = 26.0

        /** 在 Look 阶段连续等待超过该 tick 数仍未满足冲刺条件时，强制进行下一次冲刺 */
        private const val FORCE_IMPACT_TIMEOUT = 30
    }

    private var impactCount = 0
    private var ticksInLook = 0


    private var laserWaitingTick = 0
    private var laserShootingTick = 0
    private var laserShouldShoot = false

    override fun getSkillCountDown(source: MagicDragonEntity): Int {
        return 20 * 15
    }

    override fun onActive(source: MagicDragonEntity) {
        impactCount = 0
        ticksInLook = 0
        // 进入跟随姿态，等待冲刺时机
        source.phaseManager.forceSetPhase(DragonLookAndKeepDistancePhase()) {
            source.getCombatTarget()?.let { addTarget(it.boxCenterPosition()) }
            params[DragonLookAndKeepDistancePhase.MAX_CATCH_UP] = 24.0
            params[DragonLookAndKeepDistancePhase.MIN_CATCH_UP] = 16.0
        }
    }

    override fun onRelease(source: MagicDragonEntity, holdingTick: Int) {
        clear(source)
    }

    override fun getMaxHoldingTick(holdingEntity: MagicDragonEntity): Int {
        // 达到上限：立即释放
        if (impactCount >= MAX_IMPACTS) {
            return 0
        }
        // 未达到下限：保持持续递增，直到打满 5 次
        if (impactCount < MIN_IMPACTS) {
            return Int.MAX_VALUE
        }
        // 5 ~ 10 次之间：使用默认 holding 上限
        return BASE_HOLD_TICKS
    }

    override fun holdingTick(
        holdingEntity: MagicDragonEntity,
        holdTicks: Int
    ) {
        if (impactCount >= MAX_IMPACTS) return

        val target = holdingEntity.target ?: return
        val phase = holdingEntity.phaseManager
        val currentId = phase.getCurrentPhaseID()

        // 必须回到 Look-And-Keep 才允许下一段冲刺
        if (currentId != DragonLookAndKeepDistancePhase.ID) {
            ticksInLook = 0
            return
        }

        ticksInLook++

        if (laserShouldShoot && laserWaitingTick-- <= 0) {
            // 这里要进行一个判定
            if (laserShootingTick++ > 16) {
                laserShouldShoot = false
            }
            if (laserShootingTick % 2 == 0) {
                // 发射激光， 并且对target进行偏移 （类似laser skill）
                shootSmallBeam(holdingEntity)
            }
        }

        val forceImpact = ticksInLook >= FORCE_IMPACT_TIMEOUT

        if (!forceImpact) {
            // 速度必须减速到接近 0
            if (holdingEntity.deltaMovement.lengthSqr() > VELOCITY_NEAR_ZERO_SQ) {
                return
            }

            // 必须处在保持距离范围内
            val distanceSq = holdingEntity.boxCenterPosition().distanceToSqr(target.boxCenterPosition())
            if (distanceSq < INSIDE_RANGE_MIN * INSIDE_RANGE_MIN ||
                distanceSq > INSIDE_RANGE_MAX * INSIDE_RANGE_MAX
            ) {
                return
            }
        }

        // 这里是进行一次冲刺， 预测大概10tick左右到达停下来的地方， 然后进行射线
        // 也就是在这里设置之后， 要进行等待10tick 然后进行几tick的射线
        phase.forceSetPhase(DragonImpactSkillPhase()) {
            addTarget(target.boxCenterPosition())
            this.params[DragonImpactSkillPhase.IMPACT_SPEED] = 2.8
            this.params[DragonImpactSkillPhase.IMPACT_HURT_DAMAGE] = 12.0
            this.params[DragonImpactSkillPhase.COUNTDOWN_TICK] = 10
        }
        laserWaitingTick = Random.nextInt(8, 15)
        laserShouldShoot = true
        laserShootingTick = 0
        // 这里判定最后一次 （包括tick）
        impactCount++
        ticksInLook = 0
    }

    override fun stopHolding(entity: MagicDragonEntity, holdTicks: Int) {
        clear(entity)
    }

    override fun canTrigger(entity: MagicDragonEntity): Boolean {
        return super.canTrigger(entity) && entity.health <= entity.dragonMaxHealth * 0.7
    }

    override fun testCancel(entity: MagicDragonEntity): Boolean {
        return !(!entity.entityDeath && !entity.isDizzying() && !UsefulMagicEffects.isMagicSealed(entity))
    }

    fun clear(entity: MagicDragonEntity) {
        impactCount = 0
        ticksInLook = 0
        entity.phaseManager.resetDefaultPhase()
    }

    override fun getSkillID(): String {
        return ID
    }

    private fun attackAsLaser(source: MagicDragonEntity, start: Vec3, end: Vec3, r: Double, damage: Float) {
        // 伤害实体
        val direction = end - start
        val laserBox = AABB(start, end).inflate(r)
        val damageSource = UsefulMagicDamageSources.entityDamage(
            source.level(), source, source
        )
        source.level().getEntitiesOfClass(LivingEntity::class.java, laserBox) {
            it.uuid != source.uuid && it.isAlive && it !is MagicEyeEntity && it !is MagicSubEyeEntity
        }.forEach { entity ->
            val entityCenter = entity.boundingBox.center
            val distanceOnLaser = (entityCenter - start).dot(direction.normalize())
            if (distanceOnLaser !in 0.0..direction.length()) return@forEach

            val closestPoint = start.add(direction.normalize().scale(distanceOnLaser))
            if (entityCenter.distanceToSqr(closestPoint) <= r * r) {
                if (entity.hurt(damageSource, damage)) {
                    entity.invulnerableTime = 5
                }
            }
        }
    }

    private fun shootSmallBeam(holdingEntity: MagicDragonEntity) {
        // 找到符合条件的玩家 （范围128）
        var count = 2
        val world = holdingEntity.level()
        val entities = world.getEntitiesOfClass(LivingEntity::class.java, holdingEntity.boundingBox.inflate(256.0)) {
            val valid = it.isAlive && !it.hasInfiniteMaterials()
            (it is Player || it is MagicBookEntity || it.uuid == holdingEntity.target?.uuid) && valid
        }.take(count)


        if (entities.isEmpty()) return

        // 龙周围三个阵法， 朝着周围的实体发射
        val attackedPositions = mutableListOf<Vec3>()
        for (entity in entities) {
            count--
            attackedPositions.add(entity.boxCenterPosition())
        }
        while (count > 0) {
            attackedPositions.add(entities.random().boxCenterPosition().offsetRandomly(Random.nextDouble(4.0, 12.0)))
            count--
        }
        // 先召唤一个阵法
        attackedPositions.forEach {
            val start = holdingEntity.position().offsetRandomly(Random.nextDouble(32.0, 48.0))
            val end = start + (it - start).normalize() * 200
            // 要延申方向
            val formation = DragonLaserSmallComposition(start, world).apply {
                this.direction = (end - start).asRelative()
                ParticleCompositionManager.spawn(this)
            }
            StraightLaserRenderEntity(world, start).apply {
                updateBeam(start, end)
                this.maxRadius = 0.4f
                this.color = Math3DUtil.colorOf(255, 100, 255)
                this.brightness = 0.8f
                this.phaseTicks = 2
                this.lifetime = 2
                ServerRenderEntityManager.spawn(this)
            }
            submitTaskServer(2) {
                // 召唤激光
                playDragonSoundOnce(
                    holdingEntity,
                    UsefulMagicSoundEvents.SMALL_LASER_SHOOT.get(),
                    SoundSource.HOSTILE,
                    1f,
                    Random.nextFloat() * 0.3f + 0.9f,
                    256.0,
                )
                submitTaskTimerMaxTickServer(2) {
                    // 伤害
                    attackAsLaser(holdingEntity, start, end, 0.8, 4f)
                }.setFinishCallback {
                    formation.remove()
                }
            }
        }
    }
}
