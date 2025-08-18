package cn.coostack.usefulmagic.utils

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3

object EntityUtil {

    fun resetMovement(target: LivingEntity) {
        target.speed = 0f
        target.deltaMovement = Vec3.ZERO
        target.hurtMarked = true

    }

}