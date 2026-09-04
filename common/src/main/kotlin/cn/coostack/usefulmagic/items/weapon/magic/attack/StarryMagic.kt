package cn.coostack.usefulmagic.items.weapon.magic.attack

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.extend.asRelative
import cn.coostack.cooparticlesapi.extend.minus
import cn.coostack.cooparticlesapi.extend.random
import cn.coostack.cooparticlesapi.extend.times
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.usefulmagic.extend.charging
import cn.coostack.usefulmagic.items.weapon.magic.ChargingMagic
import cn.coostack.usefulmagic.meteorite.starry.StarryMainBarrage
import cn.coostack.usefulmagic.meteorite.starry.StarrySubBarrage
import cn.coostack.usefulmagic.particles.composition.magic.attack.StarryChargingComposition
import cn.coostack.usefulmagic.particles.composition.magic.attack.StarryMagicComposition
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.systems.tick.ControlerStatus
import cn.coostack.usefulmagic.utils.MagicHelper
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.random.Random

/**
 * 对标原先的群星法杖 (星辰法术)
 */
class StarryMagic(properties: Properties) : ChargingMagic<StarryChargingComposition>(properties) {
    override fun onRelease(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        // 释放一个法阵
        // 和MeteoriteComposition 一样
        val summonPosition =
            shooter.eyePosition.add(0.0, 100.0, 0.0).add(Vec3.ZERO.random() * Random.nextDouble(-30.0, 30.0))
        val target = MagicHelper.findClipTarget(world, shooter, 150.0)

        // 生成
        val composition = StarryMagicComposition(summonPosition, world)
            .apply {
                this.directon = (target - summonPosition).asRelative()
            }
        world.playSound(
            null,
            BlockPos.containing(summonPosition),
            UsefulMagicSoundEvents.MAGIC_EXTEND.get(),
            shooter.soundSource,
            10f,
            1f
        )
        CooParticlesAPI.scheduler.runTask(70) {
            world.playSound(
                null,
                BlockPos.containing(summonPosition),
                UsefulMagicSoundEvents.MAGIC_EXTEND.get(),
                shooter.soundSource,
                10f,
                2f
            )
            CooParticlesAPI.scheduler.runTaskTimerMaxTick(
                5, 400
            ) {
                // sub
                if (Random.nextDouble() <= 0.3) {
                    val pos =
                        composition.position.add(Vec3.ZERO.random() * Random.nextDouble(-60.0, 60.0))
                    val barrage = StarryMainBarrage(
                        pos,
                        world as ServerLevel,
                        BarrageOption(),
                        MagicHelper.getMagicDamage(wandStack) / 7
                    )
                    barrage.options.speed(1.5)
                    barrage.shooter = shooter
                    barrage.direction = composition.directon.toVector()
                    BarrageManager.spawn(barrage)
                }
                repeat(Random.nextInt(2, 5)) {
                    val pos =
                        composition.position.add(Vec3.ZERO.random() * Random.nextDouble(-40.0, 40.0))
                    val barrage = StarrySubBarrage(
                        pos,
                        world as ServerLevel,
                        BarrageOption(),
                        MagicHelper.getMagicDamage(wandStack) / 12
                    )
                    barrage.shooter = shooter
                    barrage.options.speed(2.0)
                    barrage.direction = composition.directon.toVector()
                    BarrageManager.spawn(barrage)
                }
            }.setFinishCallback {
                composition.remove()
            }
        }
        ParticleCompositionManager.spawn(composition)

    }

    override fun onCompositionTick(
        composition: StarryChargingComposition,
        shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    ) {
        composition.teleportTo(shooter.position())
        if (time == 1) {
            world.playSound(
                null,
                BlockPos.containing(shooter.position()),
                UsefulMagicSoundEvents.MAGIC_EXTEND.get(),
                shooter.soundSource,
                5f,
                1.5f
            )
        }

        if (time % 5 == 0) {
            world.playSound(
                null,
                BlockPos.containing(shooter.position()),
                UsefulMagicSoundEvents.STAR.get(),
                shooter.soundSource,
                10f,
                1f + Random.nextFloat()
            )
        }

    }

    override fun getComposition(shooter: LivingEntity) = getOrCreateContainer(shooter)
        .get<StarryChargingComposition>()

    override fun getOrCreateComposition(
        shooter: LivingEntity,
        world: Level
    ) = getOrCreateContainer(shooter)
        .getOrCreate {
            val composition = StarryChargingComposition(shooter.position(), world)
            ParticleCompositionManager.spawn(composition)
            ControlerStatus(composition) {
                shooter.charging && it.isValid()
            }
        } as StarryChargingComposition
}