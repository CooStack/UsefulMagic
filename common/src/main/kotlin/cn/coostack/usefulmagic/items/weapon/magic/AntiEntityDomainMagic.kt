package cn.coostack.usefulmagic.items.weapon.magic

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.extend.minus
import cn.coostack.cooparticlesapi.extend.random
import cn.coostack.cooparticlesapi.extend.times
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.barrages.magic.AntiEntityDomainMagicBarrage
import cn.coostack.usefulmagic.barrages.magic.BarrageMagicBarrage
import cn.coostack.usefulmagic.particles.composition.magic.AntiEntityChargingComposition
import cn.coostack.usefulmagic.particles.composition.magic.AntiEntityDomainComposition
import cn.coostack.usefulmagic.particles.emitters.magic.AntiEntityDomainEmitter
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import cn.coostack.usefulmagic.utils.MagicHelper
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.random.Random
import kotlin.random.nextInt

class AntiEntityDomainMagic(properties: Properties) : ChargingMagic<AntiEntityChargingComposition>(properties) {
    override fun onRelease(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        // 以玩家为半径 48个方块内的所有实体（friend）
        // 都会持续受到伤害, 减速 （向量速度打折)
        // 反实体领域
        val r = 48.0
        val spawnPos = shooter.boxCenterPosition()
        val damage = MagicHelper.getMagicDamage(wandStack)
        val box = AABB.ofSize(spawnPos, r * 2, r * 2, r * 2)
        val composition = AntiEntityDomainComposition(spawnPos.add(0.0, 48.0, 0.0), world)
        val emitter = AntiEntityDomainEmitter(spawnPos.add(0.0, 48.0, 0.0), world)
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(10, 400) {
            val targetEntities = world.getEntitiesOfClass(LivingEntity::class.java, box) {
                it.position().distanceTo(spawnPos) <= r && FriendFilterHelper.filterNotFriend(shooter, it)
            }.take(16)
            targetEntities.forEach { entity ->
                // 每一个实体周围都会召唤Barrage Magic [弹幕法术] 然后高速 / 高伤害 (10)
                repeat(Random.nextInt(2, 4)) {
                    val barrageSpawn =
                        entity.position().add(0.0, 5.0, 0.0)
                            .add(Vec3.ZERO.random() * Random.nextDouble(25.0))
                    val barrage = AntiEntityDomainMagicBarrage(damage, shooter, barrageSpawn, world as ServerLevel)
                    barrage.direction = entity.boxCenterPosition() - barrageSpawn
                    barrage.options.speed(2.0)
                        .acrossBlock(true)
                        .acrossable(true)
                        .maxAcrossCount(1)
                        .maxLivingTick(30)
                    BarrageManager.spawn(barrage)
                }
                entity.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2))
            }
        }.setFinishCallback {
            composition.remove()
            emitter.remove()
        }
        world.playSound(
            null,
            BlockPos.containing(shooter.position()),
            UsefulMagicSoundEvents.MAGIC_EXTEND.get(),
            shooter.soundSource,
            10f,
            2f
        )
        ParticleCompositionManager.spawn(composition)
        ParticleEmittersManager.spawnEmitters(emitter)
    }

    override fun onCompositionTick(
        composition: AntiEntityChargingComposition,
        shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    ) {
        composition.teleportTo(shooter.position())
        if (time % 5 == 0) {
            world.playSound(
                null,
                BlockPos.containing(shooter.position()),
                SoundEvents.SHULKER_TELEPORT,
                shooter.soundSource,
                10f,
                2f
            )
        }
    }

    override fun getComposition(shooter: LivingEntity): AntiEntityChargingComposition? =
        getOrCreateContainer(shooter)
            .get()

    override fun getOrCreateComposition(
        shooter: LivingEntity,
        world: Level
    ): AntiEntityChargingComposition =
        getOrCreateContainer(shooter)
            .getOrCreate {
                controlEntryOf(shooter) {
                    val composition = AntiEntityChargingComposition(shooter.position(), world)
                    ParticleCompositionManager.spawn(composition)
                    composition
                }
            } as AntiEntityChargingComposition
}
