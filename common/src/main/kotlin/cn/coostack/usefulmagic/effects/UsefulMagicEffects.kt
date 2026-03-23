package cn.coostack.usefulmagic.effects

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredRegistry
import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import java.util.function.Supplier

object UsefulMagicEffects {
    val mobEffects = mutableListOf<CommonDeferredRegistry<MobEffect>>()

    @JvmField
    val FREEZE = register("freeze") { FreezeEffect() }

    @JvmField
    val MAGIC_SEALED = register("magic_sealed") { MagicSealedEffect() }

    fun isFrozen(entity: Entity?): Boolean {
        return entity is LivingEntity && entity.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(FREEZE.get()))
    }

    fun isMagicSealed(entity: LivingEntity?): Boolean {
        return entity?.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MAGIC_SEALED.get())) == true
    }

    @JvmStatic
    fun asHolder(registry: CommonDeferredRegistry<MobEffect>): Holder<MobEffect> {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(registry.get())
    }

    private fun register(id: String, effect: Supplier<MobEffect>): CommonDeferredRegistry<MobEffect> {
        val common = CommonDeferredRegistry(
            BuiltInRegistries.MOB_EFFECT,
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, id),
            effect
        )
        mobEffects.add(common)
        return common
    }

    fun init() {}
}


fun CommonDeferredRegistry<MobEffect>.asHolder() = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(get())