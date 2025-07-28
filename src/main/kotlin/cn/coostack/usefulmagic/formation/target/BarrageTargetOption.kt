package cn.coostack.usefulmagic.formation.target

import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.Barrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.usefulmagic.formation.api.FormationTargetOption
import cn.coostack.usefulmagic.particles.barrages.api.DamagedBarrage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.text.Format
import java.util.UUID

class BarrageTargetOption(val target: Barrage) : FormationTargetOption {
    override fun getUniqueID(): UUID {
        return target.uuid
    }

    override fun getOwnerUUID(): UUID? {
        return target.shooter?.uuid ?: let {
            if (target is DamagedBarrage) target.offlineShooter else null
        }
    }

    override fun getWorld(): World {
        return target.world
    }

    override fun pos(): Vec3d {
        return target.loc
    }

    override fun movementVec(): Vec3d {
        return if (target.options.enableSpeed) {
            target.direction.normalize().multiply(target.options.speed)
        } else {
            target.direction
        }
    }

    override fun damage(amount: Float, source: DamageSource) {
        target.hit(BarrageHitResult())
    }


    fun hit() {
        target.hit(BarrageHitResult())
    }

    override fun setVelocity(dir: Vec3d) {
        target.direction = dir
    }

    override fun isValid(): Boolean {
        return target.valid
    }
}