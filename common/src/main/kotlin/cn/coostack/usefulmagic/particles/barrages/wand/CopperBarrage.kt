package cn.coostack.usefulmagic.particles.barrages.wand

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.meteorite.impl.OptionMeteorite
import cn.coostack.usefulmagic.particles.group.server.SingleBarrageParticleServer
import cn.coostack.usefulmagic.particles.style.barrage.wand.CopperMagicStyle
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import kotlin.random.Random

class CopperBarrage(
    val damage: Double,
    override var shooter: LivingEntity?,
    loc: Vec3,
    world: ServerLevel,
) : AbstractBarrage(
    loc, world, HitBox.of(1.0, 1.0, 1.0), SingleBarrageParticleServer(), BarrageOption()
        .apply {
            acrossBlock = false
            acrossLiquid = true
            enableSpeed = true
            speed = 1.5
            noneHitBoxTick = 0
        }) {
    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity.uuid != shooter?.uuid && FriendFilterHelper.filterNotFriend(shooter!!, livingEntity.uuid)
    }

    override fun tick() {
        super.tick()
        ServerParticleUtil.spawnSingle(
            ParticleTypes.SMOKE, world, loc, Vec3.ZERO, true, 0.05, 1
        )
    }

    override fun onHit(result: BarrageHitResult) {
        result.entities.forEach {
            it.addEffect(
                MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 5), shooter
            )
        }

        // 生成魔法阵
        val style = CopperMagicStyle()
        ParticleStyleManager.spawnStyle(world, loc.add(0.0, 0.5, 0.0), style)
        // 生成小陨石
        CooParticlesAPI.scheduler.runTask(30) {
            val meteorite = OptionMeteorite(
                Blocks.NETHERRACK, 1
            ) { loc ->
                val box = AABB.ofSize(
                    loc, 20.0, 20.0, 20.0
                )
                val source =
                    if (shooter == null) world.damageSources().flyIntoWall() else world.damageSources().playerAttack(shooter as Player)
                world.getEntitiesOfClass(
                    LivingEntity::class.java, box
                ) {
                    it != shooter
                }.forEach {
                    it.hurt(source, damage.toFloat())
                }
                PointsBuilder()
                    .addCircle(2.0, 60).create().forEach {
                        ServerParticleUtil.spawnSingle(
                            ParticleTypes.CLOUD, this@CopperBarrage.world, loc, it.toVector(), 64.0
                        )
                    }
                world.explode(shooter, source, null, loc, 2f, false, Level.ExplosionInteraction.TNT)
            }.withTick {
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.SMOKE, this@CopperBarrage.world, origin, Vec3.ZERO, true, 0.05, 3, 64.0
                )
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.CLOUD, this@CopperBarrage.world, origin, Vec3.ZERO, true, 0.05, 3, 64.0
                )
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.FLAME, this@CopperBarrage.world, origin, Vec3.ZERO, true, 0.05, 3, 64.0
                )
                if (time % 10 == 0) {
                    PointsBuilder()
                        .addCircle(0.5, 60).create().forEach {
                            ServerParticleUtil.spawnSingle(
                                ParticleTypes.FLAME, this@CopperBarrage.world, origin, it.toVector(), 64.0
                            )
                        }
                }

                if (time % 5 == 0) {
                    world?.playSound(
                        null, origin.x, origin.y, origin.z,
                        SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 10F, 1F
                    )
                }

            }

            val random = Random(System.currentTimeMillis())
            val spawnPos = loc.add(random.nextDouble(-16.0, 16.0), 50.0, random.nextDouble(-16.0, 16.0))
            meteorite.direction = RelativeLocation.of(spawnPos.relativize(loc))
            meteorite.speed = 18.0 / 20
            meteorite.spawn(spawnPos, world)
        }
    }
}