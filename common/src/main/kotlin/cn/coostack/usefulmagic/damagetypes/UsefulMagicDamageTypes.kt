package cn.coostack.usefulmagic.damagetypes

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.damagesource.DamageEffects
import net.minecraft.world.damagesource.DamageScaling
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.damagesource.DeathMessageType

object UsefulMagicDamageTypes {

    @JvmField
    val MAGIC = ResourceKey.create(
        Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(
            UsefulMagic.MOD_ID, "magic"
        )
    )

    @JvmStatic
    fun bootstrap(context: BootstrapContext<DamageType>) {
        context.register(
            MAGIC, DamageType(
                MAGIC.location().toLanguageKey(),
                DamageScaling.NEVER,
                0.1f,
                DamageEffects.HURT,
                DeathMessageType.DEFAULT
            )
        )
    }
}