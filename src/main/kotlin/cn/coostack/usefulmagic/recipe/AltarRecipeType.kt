package cn.coostack.usefulmagic.recipe

import com.mojang.datafixers.kinds.App
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancement.AdvancementRewards.Builder.recipe
import net.minecraft.item.ItemStack
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RawShapedRecipe
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

class AltarRecipeType(
    val output: ItemStack = ItemStack.EMPTY,
    val round: RoundShapeRecipe,
    val center: Ingredient,
    val manaNeed: Int,
    val tick: Int,
) :
    Recipe<AltarStackRecipeInput> {

    object Type : RecipeType<AltarRecipeType> {
        const val ID = "altar_recipe"
    }

    object AltarRecipeSerializer : RecipeSerializer<AltarRecipeType> {
        const val ID = "altar_recipe"

        val CODEC: MapCodec<AltarRecipeType> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ItemStack.CODEC.fieldOf("output").forGetter { it.output },
                RoundShapeRecipe.CODEC.fieldOf("data").forGetter { it.round },
                Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("center").forGetter { it.center },
                Codec.INT.fieldOf("mana_need").forGetter { it.manaNeed },
                Codec.INT.fieldOf("tick").forGetter { it.tick },
            ).apply(instance, ::AltarRecipeType)
        }

        val PACKET_CODEC = PacketCodec.ofStatic<RegistryByteBuf, AltarRecipeType>(
            { buffer, recipe ->
                buffer.writeInt(recipe.manaNeed)
                buffer.writeInt(recipe.tick)
                RoundShapeRecipe.PACKET_CODEC.encode(buffer, recipe.round)
                Ingredient.PACKET_CODEC.encode(buffer, recipe.center)
                ItemStack.PACKET_CODEC.encode(buffer, recipe.output)
            }, {
                val manaNeed = it.readInt()
                val tick = it.readInt()
                val round = RoundShapeRecipe.PACKET_CODEC.decode(it)
                val center = Ingredient.PACKET_CODEC.decode(it)
                val output = ItemStack.PACKET_CODEC.decode(it)
                AltarRecipeType(output, round, center, manaNeed, tick)
            }
        )

        override fun codec(): MapCodec<AltarRecipeType>? {
            return CODEC
        }

        override fun packetCodec(): PacketCodec<RegistryByteBuf?, AltarRecipeType?>? {
            return PACKET_CODEC
        }

    }

    override fun matches(
        input: AltarStackRecipeInput,
        world: World
    ): Boolean {
        if (world.isClient) return false
        if (input.mana < manaNeed) return false
        // 1. 检查中心物品是否匹配
        val centerInput = input.centerItem
        if (!center.test(centerInput)) {
            return false
        }
        return round.matchers(input)
    }

    override fun craft(
        input: AltarStackRecipeInput?,
        lookup: RegistryWrapper.WrapperLookup?
    ): ItemStack? {
        return output.copy()
    }

    override fun getIngredients(): DefaultedList<Ingredient> {
        return DefaultedList.copyOf(center, *round.ingredients.toTypedArray())
    }

    override fun fits(width: Int, height: Int): Boolean {
        return true
    }

    override fun getResult(registriesLookup: RegistryWrapper.WrapperLookup?): ItemStack? {
        return output
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return AltarRecipeSerializer
    }

    override fun getType(): RecipeType<*> {
        return Type
    }
}