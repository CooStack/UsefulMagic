package cn.coostack.usefulmagic.items.weapon.magic

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleComposition
import cn.coostack.usefulmagic.extend.charging
import cn.coostack.usefulmagic.extend.resetChargeState
import cn.coostack.usefulmagic.systems.tick.ControlerStatus
import cn.coostack.usefulmagic.systems.tick.ControlerTickSystem
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import java.util.function.Supplier

abstract class ChargingMagic<T : ServerControler<*>>(properties: Properties) : MagicItem(properties) {
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

    /**
     * ```kotlin
     * 一般实现返回为
     * getOrCreateContainer(shooter)
     *             .get()
     * ```
     */
    abstract fun getComposition(shooter: LivingEntity): T?

    /**
     * 获取或者构建一个Composition
     *
     * ```kotlin
     * getOrCreateContainer(shooter)
     *         .getOrCreate {
     *             controlEntryOf(shooter) {
     *                 val composition = XXXComposition(shooter.position(), world)
     *                     .apply {
     *                         XXX
     *                     }
     *                 ParticleCompositionManager.spawn(composition)
     *                 composition
     *             }
     *         } as XXXComposition
     * ```
     *
     * @param shooter
     * @param world
     * @return the t
     */
    abstract fun getOrCreateComposition(shooter: LivingEntity, world: Level): T

}
