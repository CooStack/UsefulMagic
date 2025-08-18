package cn.coostack.usefulmagic.formation.target

import cn.coostack.usefulmagic.formation.api.FormationTargetOption
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MoverType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID

class LivingEntityTargetOption(val target: LivingEntity, val touch: Boolean = true) : FormationTargetOption {
    override fun getUniqueID(): UUID {
        return target.uuid
    }

    override fun getOwnerUUID(): UUID {
        return target.uuid
    }

    override fun getWorld(): Level {
        return target.level()
    }

    override fun pos(): Vec3 {
        return target.position()
    }

    override fun movementVec(): Vec3 {
        return target.knownMovement
    }

    override fun damage(amount: Float, source: DamageSource) {
        target.hurt(source, amount)
    }

    override fun setVelocity(dir: Vec3) {
        target.deltaMovement = dir
        target.hurtMarked = true
        target.move(MoverType.PLAYER, dir)
    }

    override fun isValid(): Boolean {
        return !target.noPhysics && target.isAlive
    }

}