package cn.coostack.usefulmagic.entity.custom.goal.eye

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.usefulmagic.entity.custom.MagicSubEyeEntity
import cn.coostack.usefulmagic.particles.barrages.entity.skill.StraightPointBarrage
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.goal.Goal
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

class MagicSubEyeAttackGoal(val mob: MagicSubEyeEntity) : Goal() {
    override fun canUse(): Boolean {
        val validTarget = mob.target ?: mob.owner?.target ?: return false
        return validTarget.isAlive
    }

    var tick = 0
    override fun tick() {

        // 判定如果有owner就直接赋值
        mob.owner?.let {
            if (it.target != null && mob.target == null) {
                mob.target = it.target!!
            }
        }

        mob.target ?: let {
            mob.skillManager.resetActiveSkill()
            return
        }
        if (mob.skillManager.hasActiveSkill()) {
            return
        }
        mob.lookAt(mob.target!!, 20f, 20f)
        val skill = mob.skillManager.choiceSkill() ?: let {
            handleSimpleShoot()
            return
        }
        mob.skillManager.setActiveSkill(skill)
        super.tick()
    }

    private fun handleSimpleShoot() {
        if (tick-- > 0) {
            return
        }
        tick = Random.nextInt(40, 70)
        mob.level().runAsIfType<ServerLevel> {
            val barrage = StraightPointBarrage(
                mob.positionOnEye(), this,
                5.0, mob
            )
            barrage.direction = mob.forward
            barrage.options.speed(2.1)
            BarrageManager.spawn(barrage)
        }

    }

}