package cn.coostack.usefulmagic.entity.custom.skills.dragon

import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import cn.coostack.usefulmagic.entity.util.phases.dragon.DragonCrossFlightPhase
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

class DragonImpactSkill : DragonSkill() {
    companion object {
        const val ID = "dragon_impact_skill"
    }

    override var chance: Double = 0.4

    override fun getSkillCountDown(source: MagicDragonEntity): Int {
        return 20 * 20 + Random.nextInt(5) * 20
    }

    override fun onActive(source: MagicDragonEntity) {
        val target = source.getCombatTarget() ?: return
        val selectTarget = target.position() ?: return
        source.phaseManager.trySetPhase(
            DragonCrossFlightPhase.HOLDER.get()
        ) {
            addTarget(selectTarget)
        }
    }

    override fun onRelease(source: MagicDragonEntity, holdingTick: Int) {
    }

    override fun getMaxHoldingTick(holdingEntity: MagicDragonEntity): Int {
        return 20 * 10
    }

    var arrive = false
    override fun holdingTick(
        holdingEntity: MagicDragonEntity,
        holdTicks: Int
    ) {
        holdingEntity.target ?: return
        val phase = holdingEntity.phaseManager
        val target = phase.peekTargetOrNull() ?: return
        // 在到达之前 （ 8格 ） 都会一直跟随
        val velocity = holdingEntity.deltaMovement
        val box = holdingEntity.boundingBox.inflate(8.0).asHitBox()
        if (!arrive) {
            arrive = Math3DUtil.isPointCrossByBox(
                holdingEntity.position(), velocity,
                box, target
            )
        }
        if (!arrive) {
            phase.setCurrentTarget(holdingEntity.target!!.boxCenterPosition())
        }
    }

    override fun stopHolding(entity: MagicDragonEntity, holdTicks: Int) {
    }

    override fun getSkillID(): String {
        return ID
    }

    override fun canTrigger(entity: MagicDragonEntity): Boolean {
        return !entity.entityDeath && entity.dizzinessTick <= 0
    }

}
