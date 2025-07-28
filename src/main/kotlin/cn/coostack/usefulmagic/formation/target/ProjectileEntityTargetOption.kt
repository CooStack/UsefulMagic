package cn.coostack.usefulmagic.formation.target

import cn.coostack.usefulmagic.formation.api.FormationTargetOption
import kotlinx.serialization.internal.throwArrayMissingFieldException
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.text.Format
import java.util.UUID

class ProjectileEntityTargetOption(val target: ProjectileEntity) : FormationTargetOption {
    override fun getUniqueID(): UUID {
        return target.uuid
    }

    override fun getOwnerUUID(): UUID {
        return if (target.owner != null) {
            target.owner!!.uuid
        } else {
            target.uuid
        }
    }

    override fun getWorld(): World {
        return target.world
    }

    override fun pos(): Vec3d {
        return target.pos
    }

    override fun movementVec(): Vec3d {
        return target.movement
    }

    override fun damage(amount: Float, source: DamageSource) {
        target.damage(source, amount)
    }

    override fun setVelocity(dir: Vec3d) {
        target.velocity = dir
        target.velocityModified = true
    }

    override fun isValid(): Boolean {
        return target.isAlive
    }
}