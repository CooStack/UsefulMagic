package cn.coostack.usefulmagic.entity.custom.goal

import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import net.minecraft.world.entity.ai.goal.Goal

class MagicCloseTargetGoal(val entity: MagicBookEntity, val maxRange: Double) : Goal() {
    override fun canUse(): Boolean {
        return entity.target != null
    }

    override fun tick() {
        val target = entity.target ?: let {
            entity.navigation.moveTo(entity.x, entity.y - 1.0, entity.z, 1.0)
            return
        }
        val distance = target.distanceTo(entity)

        if (maxRange > distance) {
            return
        }
        val dir = entity.position().relativize(target.position())
        entity.moveControl.setWantedPosition(entity.x + dir.x / 2, entity.y + dir.y / 2, entity.z + dir.z / 2, 1.0)
    }

}