package cn.coostack.usefulmagic.particles.barrages

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.meteorite.impl.OptionMeteorite
import cn.coostack.usefulmagic.particles.group.server.SingleBarrageParticleServer
import cn.coostack.usefulmagic.particles.style.CopperMagicStyle
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.random.Random

class CopperBarrage(
    val damage: Double,
    override var shooter: LivingEntity?,
    loc: Vec3d,
    world: ServerWorld,
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
        return livingEntity.uuid != shooter?.uuid
    }

    override fun tick() {
        super.tick()
        ServerParticleUtil.spawnSingle(
            ParticleTypes.SMOKE, world, loc, Vec3d.ZERO, true, 0.05, 1
        )
    }

    override fun onHit(result: BarrageHitResult) {
        result.entities.forEach {
            it.addStatusEffect(
                StatusEffectInstance(StatusEffects.SLOWNESS, 100, 5), shooter
            )
        }

        // 生成魔法阵
        val style = CopperMagicStyle()
        ParticleStyleManager.spawnStyle(world, loc.add(0.0, 0.5, 0.0), style)
        // 生成小陨石
        CooParticleAPI.scheduler.runTask(30) {
            val meteorite = OptionMeteorite(
                Blocks.NETHERRACK, 1
            ) { loc ->
                val box = Box.of(
                    loc, 20.0, 20.0, 20.0
                )
                val source =
                    if (shooter == null) world.damageSources.flyIntoWall() else world.damageSources.playerAttack(shooter as PlayerEntity)
                world.getEntitiesByClass(
                    LivingEntity::class.java, box
                ) {
                    it != shooter
                }.forEach {
                    it.damage(source, damage.toFloat())
                }
                PointsBuilder()
                    .addCircle(2.0, 60).create().forEach {
                        ServerParticleUtil.spawnSingle(
                            ParticleTypes.CLOUD, this@CopperBarrage.world, loc, it.toVector(), 64.0
                        )
                    }
                world.createExplosion(shooter, source, null, loc, 2f, false, World.ExplosionSourceType.TNT)
            }.withTick {
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.SMOKE, this@CopperBarrage.world, origin, Vec3d.ZERO, true, 0.05, 3, 64.0
                )
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.CLOUD, this@CopperBarrage.world, origin, Vec3d.ZERO, true, 0.05, 3, 64.0
                )
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.FLAME, this@CopperBarrage.world, origin, Vec3d.ZERO, true, 0.05, 3, 64.0
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
                        SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 10F, 1F
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