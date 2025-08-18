package cn.coostack.usefulmagic.recipe

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderLookup
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.core.NonNullList

class AltarRecipeType(
    val output: ItemStack = ItemStack.EMPTY,
    val round: RoundShapeRecipe,
    val center: Ingredient,
    val manaNeed: Int,
    val tick: Int,
) : Recipe<AltarStackRecipeInput> {

    object Type : RecipeType<AltarRecipeType> {
        const val ID = "altar_recipe"
    }

    object AltarRecipeSerializer : RecipeSerializer<AltarRecipeType> {
        const val ID = "altar_recipe"

        val CODEC: MapCodec<AltarRecipeType> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ItemStack.CODEC.fieldOf("output").forGetter { it.output },
                RoundShapeRecipe.CODEC.fieldOf("data").forGetter { it.round },
                Ingredient.CODEC_NONEMPTY.fieldOf("center").forGetter { it.center },
                Codec.INT.fieldOf("mana_need").forGetter { it.manaNeed },
                Codec.INT.fieldOf("tick").forGetter { it.tick },
            ).apply(instance, ::AltarRecipeType)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AltarRecipeType> =
            StreamCodec.of(
                { buffer, recipe ->
                    buffer.writeInt(recipe.manaNeed)
                    buffer.writeInt(recipe.tick)
                    RoundShapeRecipe.PACKET_CODEC.encode(buffer, recipe.round)
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.center)
                    ItemStack.STREAM_CODEC.encode(buffer, recipe.output)
                },
                { buffer ->
                    val manaNeed = buffer.readInt()
                    val tick = buffer.readInt()
                    val round = RoundShapeRecipe.PACKET_CODEC.decode(buffer)
                    val center = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer)
                    val output = ItemStack.STREAM_CODEC.decode(buffer)
                    AltarRecipeType(output, round, center, manaNeed, tick)
                }
            )

        override fun codec(): MapCodec<AltarRecipeType> {
            return CODEC
        }

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, AltarRecipeType> {
            return STREAM_CODEC
        }
    }

    override fun matches(input: AltarStackRecipeInput, level: Level): Boolean {
        if (level.isClientSide) return false
        if (input.mana < manaNeed) return false
        if (!center.test(input.centerItem)) return false
        return round.matchers(input)
    }

    override fun assemble(input: AltarStackRecipeInput, registries: HolderLookup.Provider?): ItemStack {
        return output.copy()
    }

    override fun getIngredients(): NonNullList<Ingredient> {
        val list = NonNullList.create<Ingredient>()
        list.add(center)
        list.addAll(round.ingredients)
        return list
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean {
        return true
    }

    override fun getResultItem(registries: HolderLookup.Provider): ItemStack {
        return output
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return AltarRecipeSerializer
    }

    override fun getType(): RecipeType<*> {
        return Type
    }
}
