package cn.coostack.usefulmagic.items.weapon.magic

import cn.coostack.cooparticlesapi.network.particle.composition.ParticleComposition
import cn.coostack.usefulmagic.extend.charging
import cn.coostack.usefulmagic.extend.resetChargeState
import cn.coostack.usefulmagic.systems.tick.ControlerStatus
import cn.coostack.usefulmagic.systems.tick.ControlerTickSystem
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import java.util.function.Supplier

abstract class ChargingMagic<T : ParticleComposition>(properties: Properties) : MagicItem(properties) {
    override fun release(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        onRelease(shooter, world, wandStack, ballStack, time)
        getComposition(shooter)?.remove()
        shooter.resetChargeState()
    }

    override fun usingTick(
        shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    ) {
        val composition = getOrCreateComposition(shooter, world)
        onCompositionTick(composition, shooter, wandStack, ballStack, world, time)
    }

    override fun stopUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        chargingTick: Int,
        max: Boolean
    ) {
        getComposition(shooter)?.remove()
    }

    override fun startUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack
    ) {
        getOrCreateComposition(shooter, world)
    }

    fun <T : ParticleComposition> controlEntryOf(
        shooter: LivingEntity,
        sup: Supplier<T>
    ): ControlerStatus<ParticleComposition> {
        return ControlerStatus(sup.get()) {
            shooter.charging && it.isValid()
        }
    }

    protected fun getOrCreateContainer(shooter: LivingEntity) = ControlerTickSystem.get(shooter.uuid)


    abstract fun onRelease(shooter: LivingEntity, world: Level, wandStack: ItemStack, ballStack: ItemStack, time: Int)

    abstract fun onCompositionTick(
        composition: T, shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    )

    abstract fun getComposition(shooter: LivingEntity): T?
    abstract fun getOrCreateComposition(shooter: LivingEntity, world: Level): T

}