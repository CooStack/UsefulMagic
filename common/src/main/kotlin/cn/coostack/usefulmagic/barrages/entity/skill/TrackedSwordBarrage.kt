package cn.coostack.usefulmagic.barrages.entity.skill

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.utils.PhysicsUtil
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicSubEyeEntity
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.particles.composition.skill.SwordComposition
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*

class TrackedSwordBarrage(
    val trackedTarget: LivingEntity,
    loc: Vec3,
    world: ServerLevel,
    damage: Double,
    shooter: LivingEntity
) :
    cn.coostack.usefulmagic.barrages.api.EntityMagicDamagedBarrage(
        loc,
        world,
        BarrageOption()
            .enableSpeedWithOptions(0.8)
            .maxLivingTick(20 * 4)
            .acrossBlock(true),
        damage,
        shooter
    ) {
    companion object {
        const val TRACKING_TICK = 20
    }

    override fun onHitDamaged(result: BarrageHitResult) {
    }

    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity.uuid != shooter?.uuid && FriendFilterHelper.filterNotFriend(
            shooter!!,
            livingEntity
        ) && livingEntity !is MagicEyeEntity && livingEntity !is MagicSubEyeEntity && livingEntity !is MagicDragonEntity
    }

    override fun createHitBox(): HitBox {
        return HitBox.of(2.0, 2.0, 2.0)
    }

    override fun createControler(): ServerControler<*> {
        return SwordComposition(loc, world)
    }

    var tick = 0
    override fun tick() {
        if (tick++ < TRACKING_TICK) {
            // 玩家吸引力
            val nextV = PhysicsUtil.nextAttractVelocity(
                loc,
                direction.normalize() * options.speed,
                trackedTarget.boxCenterPosition(), 1.0,
                16,
                0.4,
                2
            )
            direction = nextV
            options.speed = nextV.length()
        } else {
            options.acrossBlock(false)
        }
        getBindControlerInstance<SwordComposition>().apply {
            direction = this@TrackedSwordBarrage.direction.asRelative()
        }
        super.tick()
    }
}
