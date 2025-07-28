package cn.coostack.usefulmagic.particles.barrages

import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.ServerControler
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.formation.api.FormationTargetOption
import cn.coostack.usefulmagic.particles.barrages.api.DamagedBarrage
import cn.coostack.usefulmagic.particles.style.EndRodSwordStyle
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import java.util.UUID

class SwordAttackFormationBarrage(
    loc: Vec3d, world: ServerWorld, hitBox: HitBox, damage: Double, var targetOption: FormationTargetOption
) : DamagedBarrage(
    loc, world, hitBox,
    EndRodSwordStyle().apply {
        this.enableAlpha = true
        this.alphaTick = 10
        this.enableScale = true
    },
    BarrageOption()
        .apply {
            maxLivingTick = 120
            enableSpeed = true
            speed = 2.0
            noneHitBoxTick = 10
            acrossBlock = true
        }, damage
) {
    val trackTime = 60
    var time = 0
    override fun tick() {
        if (noclip()) {
            this.options.speed = 0.0
        } else {
            this.options.speed = 2.0
            if (time++ < trackTime) {
                val d = loc.relativize(targetOption.pos()).normalize()
                direction = direction.normalize().multiply(options.speed).add(d)
            }
        }
        super.tick()
    }

    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        shooter ?: return true
        if (shooter !is PlayerEntity) {
            return true
        }
        if (livingEntity == shooter) return false
        return if (livingEntity is PlayerEntity) {
            val shooterUUID = shooter!!.uuid
            val shooterData = UsefulMagic.state.getDataFromServer(shooterUUID)
            !shooterData.isFriend(livingEntity.uuid)
        } else true
    }

    override fun onHit(result: BarrageHitResult) {
        val damageSource = if (shooter != null) {
            if (shooter is PlayerEntity) {
                world.damageSources.playerAttack(shooter as PlayerEntity)
            } else {
                world.damageSources.mobAttack(shooter)
            }
        } else world.damageSources.magic()
        for (entity in result.entities) {
            // 击中的实体
            entity.damage(damageSource, damage.toFloat())
            entity.timeUntilRegen = 0
            entity.hurtTime = 0
            // 不破坏方块 爆炸音效
            world.playSound(
                null, loc.x, loc.y, loc.z, UsefulMagicSoundEvents.MAGIC_SWORD, SoundCategory.BLOCKS, 6f, 1.2f
            )
        }
        // 爆炸粒子
        PointsBuilder()
            .addBall(1.0, 6)
            .create()
            .forEach {
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.FIREWORK, world, loc, it.toVector().multiply(1 / 2.0), 64.0
                    )
            }
    }

    override fun onHitDamaged(result: BarrageHitResult) {
    }
}