package cn.coostack.usefulmagic.formation.target

import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.formation.api.FormationTargetOption
import cn.coostack.usefulmagic.meteorite.Meteorite
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID

class MeteoriteEntityTargetOption(val meteorite: Meteorite) : FormationTargetOption {
    override fun getUniqueID(): UUID {
        return meteorite.shooter?.uuid ?: UUID.randomUUID()
    }

    override fun getOwnerUUID(): UUID? {
        return meteorite.shooter?.uuid ?: UUID.randomUUID()
    }

    override fun getWorld(): Level {
        return meteorite.world!!
    }

    override fun pos(): Vec3 {
        return meteorite.origin
    }

    override fun movementVec(): Vec3 {
        return meteorite.direction.toVector()
    }

    override fun damage(amount: Float, source: DamageSource) {
        meteorite.onHit(meteorite.origin)
    }

    override fun setVelocity(dir: Vec3) {
        meteorite.direction = RelativeLocation.of(dir)
    }

    override fun isValid(): Boolean {
        return meteorite.valid
    }

}