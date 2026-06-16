package cn.coostack.usefulmagic.barrages.magic

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.utils.PhysicsUtil
import cn.coostack.usefulmagic.barrages.api.EntityMagicDamagedBarrage
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.particles.composition.magic.SwordQiComposition
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*

/**
 * 剑气弹幕：短时间追踪目标，并在命中时造成魔法伤害。
 */
class SwordQiBarrage(loc: Vec3, world: ServerLevel, damage: Double, shooter: LivingEntity) :
    EntityMagicDamagedBarrage(
        loc, world, BarrageOption()
            .barrageIgnored(false)
            .acrossEmptyCollectionShape()
            .acrossBlock()
            .maxLivingTick(80)
            .enableSpeedWithOptions(0.0)
            .noneHitBoxTick(20),
        damage, shooter
    ) {
    var age = 0
    var flag = false
    override fun onHitDamaged(result: BarrageHitResult) {
        result.entities.forEach {
            it.invulnerableTime /= 3
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
        if (!noclip() && options.speed <= 1e-6 && !flag) {
            options.speed(2.0)
            flag = true
        }
        if (age < 40 && !noclip()) {
            age++
            val findBox = AABB.ofSize(loc, 64.0, 64.0, 64.0)
            val entity = world.getEntitiesOfClass(LivingEntity::class.java, findBox) {
                it != shooter && it.isAlive && FriendFilterHelper.filterNotFriend(shooter!!, it)
            }.minByOrNull {
                it.distanceTo(shooter!!)
            }
            if (entity != null) {
                val v = PhysicsUtil.nextAttractVelocity(
                    loc, direction.normalize() * options.speed, entity.boxCenterPosition(),
                    1.0,
                    8,
                    3.6,
                    1,
                    maxSpeed = 5.0
                )
                options.speed(v.length())
                direction = v.normalize()
            }

        }
        val composition = bindControl.get() as SwordQiComposition
        composition.movement = direction.asRelative()
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

