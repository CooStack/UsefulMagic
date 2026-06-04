package cn.coostack.usefulmagic.items.weapon.magic

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.extend.minus
import cn.coostack.cooparticlesapi.extend.plus
import cn.coostack.cooparticlesapi.extend.random
import cn.coostack.cooparticlesapi.extend.times
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleShapeComposition
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.canSee
import cn.coostack.usefulmagic.extend.charging
import cn.coostack.usefulmagic.extend.resetChargeState
import cn.coostack.usefulmagic.barrages.magic.SwordFormationBarrage
import cn.coostack.usefulmagic.particles.composition.magic.GoldenMagicChargingComposition
import cn.coostack.usefulmagic.particles.composition.magic.SwordFormationMagicChargingComposition
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.systems.tick.ControlerStatus
import cn.coostack.usefulmagic.systems.tick.ControlerTickSystem
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import cn.coostack.usefulmagic.utils.MagicHelper
import com.mojang.math.Axis
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.random.Random

class SwordFormationMagic(properties: Properties) : ChargingMagic<SwordFormationMagicChargingComposition>(properties) {

    override fun onRelease(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        if (world.isClientSide) return
        // 这里在实体周围设置剑气
        val searchBox = AABB.ofSize(shooter.boxCenterPosition(), 128.0, 128.0, 128.0)
        // 首先是玩家可以看到的（360度） 非友好的 (防止攻击到地下的）
        val entity = world.getEntitiesOfClass(LivingEntity::class.java, searchBox) {
            it != shooter && it.isAlive && shooter.canSee(it) && FriendFilterHelper.filterNotFriend(shooter, it)
        }.minByOrNull { it.distanceTo(shooter) }
        // 给最近的实体上
        if (entity != null) {
            CooParticlesAPI.scheduler.runTaskTimerMaxTick(5, 60) {
                val origin = entity.boxCenterPosition()
                repeat(Random.nextInt(1, 3)) {
                    val spawnPos = origin + (Vec3.ZERO.random() * Random.nextDouble(32.0, 48.0)).with(
                        Direction.Axis.Y,
                        Random.nextDouble(16.0, 24.0)
                    )
                    val dir = origin - spawnPos
                    val barrage =
                        SwordFormationBarrage(
                            spawnPos,
                            world as ServerLevel,
                            MagicHelper.getMagicDamage(wandStack),
                            shooter
                        )
                    barrage.options
                        .acrossLiquid(true)
                        .acrossBlock(true)
                        .maxLivingTick(50)
                    barrage.direction = dir
                    BarrageManager.spawn(barrage)
                }
            }
        }
        world.playSound(
            null,
            BlockPos.containing(shooter.position()),
            UsefulMagicSoundEvents.MAGIC_SWORD.get(),
            shooter.soundSource,
            10f,
            2f
        )

    }

    override fun onCompositionTick(
        composition: SwordFormationMagicChargingComposition,
        shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    ) {
        composition.direction = shooter.forward
        composition.teleportTo(shooter.eyePosition + shooter.forward * 2)
    }


    override fun getComposition(shooter: LivingEntity) = getOrCreateContainer(shooter)
        .get<SwordFormationMagicChargingComposition>()

    override fun getOrCreateComposition(shooter: LivingEntity, world: Level) =
        getOrCreateContainer(shooter).getOrCreate {
            val composition = SwordFormationMagicChargingComposition(shooter.position(), world)
            ParticleCompositionManager.spawn(composition)
            ControlerStatus(composition) {
                shooter.charging && it.isValid()
            }
        } as SwordFormationMagicChargingComposition
}
