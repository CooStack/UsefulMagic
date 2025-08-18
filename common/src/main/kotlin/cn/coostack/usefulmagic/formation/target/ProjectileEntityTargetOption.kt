package cn.coostack.usefulmagic.formation.target

import cn.coostack.usefulmagic.formation.api.FormationTargetOption
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.text.Format
import java.util.UUID

class ProjectileEntityTargetOption(val target: Projectile) : FormationTargetOption {
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
    }

    override fun isValid(): Boolean {
        return target.isAlive
    }
}