package cn.coostack.usefulmagic.formation.target

import cn.coostack.usefulmagic.formation.api.FormationTargetOption
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.MovementType
import net.minecraft.entity.damage.DamageSource
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.UUID

class LivingEntityTargetOption(val target: LivingEntity, val touch: Boolean = true) : FormationTargetOption {
    override fun getUniqueID(): UUID {
        return target.uuid
    }

    override fun getOwnerUUID(): UUID {
        return target.uuid
    }

    override fun getWorld(): World {
        return target.world
    }

    override fun pos(): Vec3d {
        return target.pos
    }

    override fun movementVec(): Vec3d {
        return target.movement.normalize().multiply(target.movementSpeed.toDouble())
    }

    override fun damage(amount: Float, source: DamageSource) {
        target.damage(source, amount)
    }

    override fun setVelocity(dir: Vec3d) {
        target.velocity = dir
        target.velocityModified = true
        target.move(MovementType.PLAYER, dir)
    }

    override fun isValid(): Boolean {
        return !target.noClip && target.isAlive
    }

}