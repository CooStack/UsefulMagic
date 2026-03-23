package cn.coostack.usefulmagic.entity.custom.skills.dragon

import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.cooparticlesapi.sound.ServerSoundManager
import cn.coostack.cooparticlesapi.sound.SoundVolumeFalloff
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.MagicSubEyeEntity
import cn.coostack.usefulmagic.entity.util.phases.dragon.DragonOrbitFlightPhase
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.set
import cn.coostack.usefulmagic.particles.entity.dragon.composition.lifecycle.spawn.MagicDragonSpawnLaserComposition
import cn.coostack.usefulmagic.particles.entity.dragon.composition.skills.DragonLaserSmallComposition
import cn.coostack.usefulmagic.renderer.StraightLaserRenderEntity
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import com.ibm.icu.text.PluralRules
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import java.util.*
import kotlin.random.Random

/**
 *  在龙的周围朝着target发射的激光
 *  TODO 技能释放方式改为
 *  前3秒释放大量小激光 （1tick一次， 伤害1tick)
 *
 *  然后等待3秒（第一秒释放魔法阵）
 *  释放一个大激光， 大激光持续3秒
 * @constructor Create empty Dragon laser skill
 */
class DragonLaserSkill : DragonSkill() {
    companion object {
        const val ID = "DragonLaserSkill"
    }

    override var chance: Double = 0.1
    override fun getSkillCountDown(source: MagicDragonEntity): Int {
        return 20 * 4
    }

    override fun onActive(source: MagicDragonEntity) {
        hugeLaserPlaced = false
        hugeLaserShoot = false
        source.phaseManager.forceSetPhase(
            DragonOrbitFlightPhase()
        ) {
            this.params.apply {
                this[DragonOrbitFlightPhase.RADIUS] = 32.0
                this[DragonOrbitFlightPhase.MIN_RADIUS] = 16.0
            }
            setCurrentTarget(source.spawnPosition)
        }

    }

    override fun onRelease(source: MagicDragonEntity, holdingTick: Int) {
        clearStatus(source)
    }

    override fun getMaxHoldingTick(holdingEntity: MagicDragonEntity): Int {
        return 20 * 9
    }

    private var hugeLaserPlaced = false
    private var hugeLaserShoot = false
    private var hugeComposition: MagicDragonSpawnLaserComposition? = null
    private var hugeLaserEntity: StraightLaserRenderEntity? = null

    private var hugeBeamDirection = Vec3.ZERO
    private var hugeBeamMagicStart = Vec3.ZERO

    override fun holdingTick(
        holdingEntity: MagicDragonEntity, holdTicks: Int
    ) {
        if (holdTicks < 40) {
            return
        }

        if (holdTicks in 40..(20 * 4 + 10)) {
            if (holdTicks % 2 != 0) {
                return
            }
            shootSmallBeam(holdingEntity)
            return
        }
        if (!hugeLaserPlaced) {
            placeHugeBeam(holdingEntity)
            hugeLaserPlaced = true
        }
        if (hugeLaserShoot) {
            attackAsLaser(holdingEntity, hugeBeamMagicStart, hugeBeamMagicStart + hugeBeamDirection * 200, 5.0, 10f)
        }
    }

    override fun stopHolding(entity: MagicDragonEntity, holdTicks: Int) {
        clearStatus(entity)
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


    private fun placeHugeBeam(holdingEntity: MagicDragonEntity) {
        // 直接射target
        val target = holdingEntity.target ?: return
        val start = holdingEntity.getMouthPosition(holdingEntity.forward)
        val world = holdingEntity.level()
        val end = target.boxCenterPosition()
        hugeBeamDirection = (end - start).normalize()
        // 初始化大激光 (4秒)
        hugeComposition = MagicDragonSpawnLaserComposition(start, world).apply {
            this.direction = hugeBeamDirection.asRelative()
            ParticleCompositionManager.spawn(this)
        }
        hugeBeamMagicStart = start
        ServerSoundManager.instance(
            UsefulMagicSoundEvents.DRAGON_HUGE_LASER_CHARGE_UP.get(),
            SoundSource.HOSTILE
        ).volume(1f)
            .pitch(0.5f)
            .entity(holdingEntity)
            .stopWhenBoundEntityMissing(false)
            .layer("huge_laser_charge_up")
            .visibleRange(256.0)
            .syncEveryTick(true)
            .volumeFalloff(SoundVolumeFalloff.LINEAR)
            .spawn()
        submitTaskTimerMaxTickServer(40) {
            // 跟随
            hugeComposition?.direction = hugeBeamDirection.asRelative()
            hugeBeamDirection = (target.boxCenterPosition() - start).normalize()
        }.setCancelPredicate {
            hugeComposition?.status?.isDisable() ?: true
        }
        submitTaskServer(40) {
            ServerSoundManager.instance(
                UsefulMagicSoundEvents.DRAGON_HUGE_LASER_SHOOT.get(),
                SoundSource.HOSTILE
            ).volume(1f)
                .pitch(1f)
                .entity(holdingEntity)
                .stopWhenBoundEntityMissing(false)
                .layer("huge_laser_charge_shoot")
                .visibleRange(256.0)
                .syncEveryTick(true)
                .volumeFalloff(SoundVolumeFalloff.LINEAR)
                .spawn()
            hugeLaserEntity = StraightLaserRenderEntity(world, start).apply {
                maxRadius = 5f
                brightness = 0.9f
                phaseTicks = 10
                lifetime = 20 * 120
                updateBeam(start, start + hugeBeamDirection * 200)
                ServerRenderEntityManager.spawn(this)
            }
            hugeLaserShoot = true
        }
    }

    private fun shootSmallBeam(holdingEntity: MagicDragonEntity) {
        // 找到符合条件的玩家 （范围128）
        var count = 2
        val world = holdingEntity.level()
        val entities = world.getEntitiesOfClass(LivingEntity::class.java, holdingEntity.boundingBox.inflate(128.0)) {
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
            attackedPositions.add(entities.random().boxCenterPosition().offsetRandomly(Random.nextDouble(3.0, 8.0)))
            count--
        }
        // 先召唤一个阵法
        attackedPositions.forEach {
            val start = holdingEntity.position().offsetRandomly(Random.nextDouble(24.0, 36.0))
            val end = start + (it - start).normalize() * 200
            // 要延申方向
            val formation = DragonLaserSmallComposition(start, world).apply {
                this.direction = (end - start).asRelative()
                ParticleCompositionManager.spawn(this)
            }
            StraightLaserRenderEntity(world, start).apply {
                updateBeam(start, end)
                this.maxRadius = 0.5f
                this.color = Math3DUtil.colorOf(255, 100, 255)
                this.brightness = 0.7f
                this.phaseTicks = 2
                this.lifetime = 2
                ServerRenderEntityManager.spawn(this)
            }
            submitTaskServer(2) {
                // 召唤激光
                ServerSoundManager.instance(
                    UsefulMagicSoundEvents.SMALL_LASER_SHOOT.get(), SoundSource.HOSTILE
                ).volume(1f)
                    .pitch(Random.nextFloat() * 0.3f + 0.9f)
                    .layer("small_laser")
                    .volume(128f)
                    .syncEveryTick(true).entity(holdingEntity).uniqueKey(UUID.randomUUID().toString())
                    .volumeFalloff(SoundVolumeFalloff.LINEAR).stopWhenBoundEntityMissing(false).spawn()
                submitTaskTimerMaxTickServer(2) {
                    // 伤害
                    attackAsLaser(holdingEntity, start, end, 0.8, 8f)
                }.setFinishCallback {
                    formation.remove()
                }
            }
        }
    }

    private fun clearStatus(source: MagicDragonEntity) {
        source.phaseManager.resetDefaultPhase()
        hugeComposition?.remove()
        hugeLaserEntity?.discard()
        PluralRules.Operand.w
    }
}