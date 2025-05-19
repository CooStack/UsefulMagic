package cn.coostack.usefulmagic.particles.barrages

import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.style.AntiEntityWandBarrageStyle
import cn.coostack.usefulmagic.particles.style.EndRodLineStyle
import net.fabricmc.loader.impl.lib.sat4j.core.Vec
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d

class AntiEntityWandBarrage(
    val damage: Double,
    loc: Vec3d,
    world: ServerWorld,
    hitBox: HitBox,
) : AbstractBarrage(
    loc, world, hitBox, AntiEntityWandBarrageStyle(),
    BarrageOption().apply {
        enableSpeed = true
        speed = 1.0
        maxLivingTick = 120
        noneHitBoxTick = 10
    }) {
    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return shooter?.uuid != livingEntity.uuid && livingEntity.isAlive
    }

    val traceBox = HitBox.of(32.0, 32.0, 32.0)
    private var tick = 0
    private var lastLoc = loc
    override fun tick() {
        super.tick()
        tick++
        if (tick % 3 == 0) {
            val line = EndRodLineStyle(
                RelativeLocation.of(loc.relativize(lastLoc)), 6, Vec3d(255.0, 255.0, 255.0), 100
            )
            ParticleStyleManager.spawnStyle(world, loc, line)
            lastLoc = loc
//            ServerParticleUtil.spawnSingle(
//                ParticleTypes.END_ROD, world, loc, Vec3d.ZERO, true, 0.0, 1, 256.0
//            )
        }
        if (noclip()) {
            return
        }
        if (tick % 5 == 0) {
            val entities = world.getEntitiesByClass(LivingEntity::class.java, traceBox.ofBox(loc)) {
                filterHitEntity(it)
            }
            val nearest = entities.minByOrNull {
                it.pos.distanceTo(loc)
            }
            if (nearest != null) {
                direction = loc.relativize(nearest.pos)
            }
        }
    }

    override fun onHit(result: BarrageHitResult) {
        val source = world.damageSources.playerAttack(shooter!! as PlayerEntity)
        for (entity in result.entities) {
            entity.damage(source, damage.toFloat())
            entity.resetPortalCooldown()
            entity.timeUntilRegen = 0
            entity.hurtTime = 0
        }

        world.playSound(
            null, loc.x, loc.y, loc.z, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 6f, 1.2f
        )

        PointsBuilder()
            .addBall(1.0, 3)
            .create()
            .forEach {
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.FIREWORK, world, loc, it.toVector().multiply(1 / 2.0), 64.0
                    )
            }
    }
}