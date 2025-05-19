package cn.coostack.usefulmagic.recipe

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object UsefulMagicRecipeTypes {

    fun register() {
        Registry.register(
            Registries.RECIPE_SERIALIZER, Identifier.of(UsefulMagic.MOD_ID, AltarRecipeType.AltarRecipeSerializer.ID),
            AltarRecipeType.AltarRecipeSerializer
        )
        Registry.register(
            Registries.RECIPE_TYPE, Identifier.of(UsefulMagic.MOD_ID, AltarRecipeType.Type.ID),
            AltarRecipeType.Type
        )
    }

}