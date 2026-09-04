package cn.coostack.usefulmagic.barrages.magic

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.extend.asRelative
import cn.coostack.usefulmagic.particles.composition.magic.attack.SwordQiComposition
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3

class SwordFormationBarrage(
    loc: Vec3,
    world: ServerLevel,
    damage: Double,
    shooter: LivingEntity
) : cn.coostack.usefulmagic.barrages.api.EntityMagicDamagedBarrage(
    loc, world, BarrageOption()
        .noneHitBoxTick(10)
        .enableSpeedWithOptions(0.0), damage, shooter
) {
    override fun onHitDamaged(result: BarrageHitResult) {
        result.entities.forEach {
            it.invulnerableTime = 0
        }
        world.playSound(
            null,
            BlockPos.containing(loc),
            UsefulMagicSoundEvents.MAGIC_SWORD.get(),
            SoundSource.PLAYERS,
            3f,
            1f
        )
    }

    override fun tick() {
        val composition = bindControl.get() as SwordQiComposition
        composition.movement = direction.asRelative()
        composition.markDirty()
        if (!noclip() && options.speed <= 1e-6) {
            options.speed(5.0)
        }
        super.tick()
    }

    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity.uuid != shooter?.uuid && FriendFilterHelper.filterNotFriend(shooter!!, livingEntity)
    }

    override fun createHitBox(): HitBox {
        return HitBox.of(6.0, 6.0, 6.0)
    }

    override fun createControler(): ServerControler<*> {
        return SwordQiComposition(loc, world).apply {
            movement = direction.asRelative()
        }
    }
}
