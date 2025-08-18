package cn.coostack.usefulmagic.recipe

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredRecipeSerializer
import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredRecipeType
import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import java.util.function.Supplier

object UsefulMagicRecipeTypes {

    val recipeTypes = mutableListOf<CommonDeferredRecipeType<*>>()
    val recipeSerializer = mutableListOf<CommonDeferredRecipeSerializer<*>>()

    fun register() {
        registerSerializer(
            AltarRecipeType.AltarRecipeSerializer.ID
        ) {
            AltarRecipeType.AltarRecipeSerializer
        }
        register(
            AltarRecipeType.Type.ID
        ) {
            AltarRecipeType.Type
        }
    }

    fun <T : Recipe<*>> register(id: String, type: Supplier<RecipeType<T>>): CommonDeferredRecipeType<T> {
        val common = CommonDeferredRecipeType(ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, id), type)
        recipeTypes.add(common)
        return common
    }

    fun <T : Recipe<*>> registerSerializer(
        id: String,
        type: Supplier<RecipeSerializer<T>>
    ): CommonDeferredRecipeSerializer<T> {
        val common = CommonDeferredRecipeSerializer(ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, id), type)
        recipeSerializer.add(common)
        return common
    }


}