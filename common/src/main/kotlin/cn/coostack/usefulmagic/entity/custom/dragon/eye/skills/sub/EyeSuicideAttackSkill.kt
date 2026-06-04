package cn.coostack.usefulmagic.entity.custom.dragon.eye.skills.sub

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicSubEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.phases.sub.EyeSuicidePhase
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import cn.coostack.usefulmagic.skill.api.SkillCondition
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.pow

class EyeSuicideAttackSkill(val explodeDamage: Double, val falloff: Double) :
    Skill<MagicSubEyeEntity>,
    SkillCondition<MagicSubEyeEntity>, SkillCancelCondition<MagicSubEyeEntity> {
    override var chance: Double = 1.0

    override fun getSkillCountDown(source: MagicSubEyeEntity): Int = 0

    override fun onActive(source: MagicSubEyeEntity) {
        // 设置移动
        source.phaseManager.forceSetPhase(EyeSuicidePhase()) {
            setCurrentTarget(source.target!!.boxCenterPosition())
        }
    }

    override fun onRelease(source: MagicSubEyeEntity, holdingTick: Int) {}

    override fun getMaxHoldingTick(holdingEntity: MagicSubEyeEntity): Int = 72000

    override fun holdingTick(holdingEntity: MagicSubEyeEntity, holdTicks: Int) {
        val target = holdingEntity.target ?: return
        holdingEntity.phaseManager.setCurrentTarget(target.boxCenterPosition())
        // 跟随Entity 如果碰到了就直接开爆

        val box = holdingEntity.boundingBox.inflate(2.0)
        val world = holdingEntity.level()
        val hasCollideEntity = world.getEntitiesOfClass(LivingEntity::class.java, box) {
            it.isAlive && !(it.isSpectator || it.hasInfiniteMaterials()) && it !is MagicSubEyeEntity && it !is MagicEyeEntity && it !is MagicDragonEntity
        }.isNotEmpty()
        if (hasCollideEntity) {
            canceled = true
            // 处理爆炸
            val explodeIncludeBox = holdingEntity.boundingBox.inflate(12.0)
            val includeEntities = world.getEntitiesOfClass(LivingEntity::class.java, explodeIncludeBox) {
                it.isAlive && !(it.isSpectator || it.hasInfiniteMaterials()) && it !is MagicSubEyeEntity && it !is MagicEyeEntity
            }
            val source = UsefulMagicDamageSources.entityMagic(world, holdingEntity, holdingEntity)
            includeEntities.forEach {
                // 首先给实体做设置爆炸的向量 (小)
                val thrustDirection = (it.boxCenterPosition() - holdingEntity.positionOnEye())
                val distance = thrustDirection.length()
                val falloffRatio = distance.pow(falloff)
                val force = 7.0 / falloffRatio
                it.deltaMovement + thrustDirection.normalize() * force
                val finalDamage = (explodeDamage / falloffRatio).coerceAtMost(explodeDamage * 1.5)
                it.hurt(source, finalDamage.toFloat())
            }
            ServerCameraUtil.sendShake(
                world as ServerLevel,
                holdingEntity.positionOnEye(),
                128.0,
                1.0,
                20,
                0.5
            )
            world.playSound(
                null,
                holdingEntity.blockPosition(),
                SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.HOSTILE,
                10f,
                1f
            )
            // 爆炸粒子
            val explosion = ExplodeMagicEmitters(holdingEntity.positionOnEye(), world).apply {
                templateData.apply {
                    effect = ControlableEnchantmentEffect(uuid)
                    color = Math3DUtil.colorOf(210, 90, 255)
                    size = 0.35f
                    alpha = 0.9f
                    light = 15
                    visibleRange = 128.0f
                    speedLimit = 32.0
                }
                randomParticleAgeMin = 18
                randomParticleAgeMax = 45
                precentDrag = 0.97
                maxTick = 1
                ballCountPow = 18
                minSpeed = 0.2
                maxSpeed = 9.0
                randomCountMin = 120
                randomCountMax = 240
            }
            ParticleEmittersManager.spawnEmitters(explosion)

            holdingEntity.kill()
        }
    }

    override fun stopHolding(entity: MagicSubEyeEntity, holdTicks: Int) {}

    override fun getSkillID(): String = "EyeSuicideAttackSkill"

    override fun canTrigger(entity: MagicSubEyeEntity): Boolean =
        (entity.health <= entity.maxHealth / 2 || (entity.owner != null && !entity.owner!!.isAlive)) && entity.target != null

    override fun testCancel(entity: MagicSubEyeEntity): Boolean {
        return entity.target == null
    }

    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = false
}