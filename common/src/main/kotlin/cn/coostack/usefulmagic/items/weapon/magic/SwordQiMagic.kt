package cn.coostack.usefulmagic.items.weapon.magic

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.extend.plus
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.barrages.magic.SwordQiBarrage
import cn.coostack.usefulmagic.utils.MagicHelper
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class SwordQiMagic(properties: Properties) : MagicItem(properties) {
    override fun release(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        if (world.isClientSide) return
        // 分别释放三个剑（在玩家周围）
        val direction = shooter.forward
        val center = shooter.eyePosition
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(6, 18) {
            val offsetXZ = RelativeLocation(
                cos(Random.nextDouble(-PI, PI)),
                Random.nextDouble(-0.5, 0.5),
                sin(Random.nextDouble(-PI, PI)),
            ) * Random.nextDouble(2.0, 4.0)
            val finalPos = offsetXZ.toVector() + center
            val barrage =
                SwordQiBarrage(finalPos, world as ServerLevel, MagicHelper.getMagicDamage(wandStack), shooter)
            barrage.direction = direction
            BarrageManager.spawn(barrage)
        }
    }

    override fun usingTick(
        shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    ) {
    }

    override fun stopUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        chargingTick: Int,
        max: Boolean
    ) {
    }

    override fun startUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack
    ) {
    }
}
