package cn.coostack.usefulmagic.entity.custom.dragon.phases

import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonAnimationState
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.util.EntityPoseUtil
import cn.coostack.usefulmagic.entity.util.phases.*
import cn.coostack.usefulmagic.extend.asDouble
import cn.coostack.usefulmagic.extend.asInt
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.set
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*

/**
 * 朝着目标冲刺一段时间
 *
 * 冲刺结束后切换为 [DragonLookAndKeepDistancePhase]
 */
class DragonImpactSkillPhase : PhaseDefinition<MagicDragonEntity>, PhaseCollisionEntity<MagicDragonEntity> {

    companion object {
        /** 撞击速度 (Double) */
        const val IMPACT_SPEED = "impact_speed"

        /** 冲刺持续 tick (Int) */
        const val COUNTDOWN_TICK = "countdown_tick"

        /** 撞击伤害 (Double) */
        const val IMPACT_HURT_DAMAGE = "impact_hurt_damage"

        const val ID = "dragon_skill_impact"
        val HOLDER = PhaseRegistries.register(ID) { DragonImpactSkillPhase() }
    }

    private var tick = 0
    private var impactDirection: Vec3 = Vec3.ZERO
    private var impactTarget: Vec3 = Vec3.ZERO

    override fun canBegin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): Boolean {
        return runtime.hasTarget()
    }

    override fun begin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
        tick = 0
        val target = runtime.peekTargetOrNull() ?: instance.getCombatTarget()?.boxCenterPosition()
        if (target == null) {
            impactDirection = instance.forward.normalize()
            impactTarget = instance.boxCenterPosition()
            return
        }
        impactTarget = target
        val raw = target - instance.boxCenterPosition()
        impactDirection = if (raw.lengthSqr() <= 1.0E-6) instance.forward.normalize() else raw.normalize()
    }

    override fun step(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): PhaseResult {
        val cdTick = runtime.params[COUNTDOWN_TICK].asInt ?: 40
        val speed = runtime.params[IMPACT_SPEED].asDouble ?: 1.8

        instance.deltaMovement = impactDirection * speed
        EntityPoseUtil.rotationFix(instance.forward, impactDirection, instance, 30f)
        instance.playAnimation(MagicDragonAnimationState.FLY)

        if (tick++ < cdTick) {
            return PhaseResult.Continue
        }

        val followTarget = instance.getCombatTarget()?.boxCenterPosition() ?: impactTarget
        return PhaseResult.Next(DragonLookAndKeepDistancePhase()) {
            addTarget(followTarget)
            params[DragonLookAndKeepDistancePhase.MAX_CATCH_UP] = 24.0
            params[DragonLookAndKeepDistancePhase.MIN_CATCH_UP] = 16.0
            params[DragonLookAndKeepDistancePhase.LIMIT_HEIGHT] = true
            params[DragonLookAndKeepDistancePhase.HEIGHT_RELATIVE_LEAST] = 3.0
            params[DragonLookAndKeepDistancePhase.HEIGHT_RELATIVE_MOST] = 8.0


        }
    }

    override fun end(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
        runtime.clearTargets()
    }

    override fun id(): String = ID

    override fun collisionEntity(
        targets: Set<LivingEntity>,
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
        targets.forEach {
            val damage = runtime.params[IMPACT_HURT_DAMAGE].asDouble ?: 8.0
            val source = UsefulMagicDamageSources.entityDamage(instance.level(), instance, instance)
            it.deltaMovement = instance.deltaMovement.normalize() * 0.15
            it.hurt(source, damage.toFloat())
        }
    }
}
