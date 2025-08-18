package cn.coostack.usefulmagic.recipe

import com.mojang.datafixers.kinds.App
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.minecraft.core.NonNullList
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.crafting.Ingredient

/**
 * 配方环
 * 5 6 7
 * 3   4
 * 0 1 2
 */
class RoundShapeRecipe(
    val ingredients: NonNullList<Ingredient> = NonNullList.withSize(
        8,
        Ingredient.EMPTY
    )
) {
    companion object {
        @JvmStatic
        private fun fromData(data: IndexData): DataResult<RoundShapeRecipe> {
            val ingredients = NonNullList.withSize(8, Ingredient.EMPTY)
            data.map.forEach {
                val key = it.key.toIntOrNull() ?: return DataResult.error { "key type is not int" }
                if (key !in 0..7) return DataResult.error { "index must in 0 - 7" }
                ingredients[key] = it.value
            }
            return DataResult.success(RoundShapeRecipe(ingredients))
        }

        @JvmStatic
        val CODEC: Codec<RoundShapeRecipe> = IndexData.CODEC.flatXmap<RoundShapeRecipe>(
            ::fromData
        ) {
            if (it.data.map.isNotEmpty()) DataResult.success(it.data) else DataResult.error { "error" }
        }

        @JvmStatic
        val PACKET_CODEC: StreamCodec<RegistryFriendlyByteBuf, RoundShapeRecipe> = StreamCodec.of(
            { buf, recipe ->
                recipe.ingredients.forEach {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, it)
                }
            }, {
                val ingredients = NonNullList.withSize<Ingredient>(8, Ingredient.EMPTY)
                for (i in 0..7) {
                    ingredients[i] = Ingredient.CONTENTS_STREAM_CODEC.decode(it)
                }
                val res = RoundShapeRecipe(ingredients)
                res
            }
        )
    }

    val data = IndexData(HashMap())
    fun matchers(input: AltarStackRecipeInput): Boolean {
        val rotates = getRotates()
        rotates.forEach {
            it.forEachIndexed { inputIndex, i ->
                // inputIndex -> 输入
                // i -> 对应配方槽位
                val stack = input.getItem(inputIndex)
                if (!ingredients[i].test(stack)) {
                    return@forEach
                }
            }
            return true
        }
        return false
    }

    private fun getRotates(): Set<List<Int>> {
        return setOf(
            // 值代表recipe的索引
            // 索引代表input的索引
            listOf(0, 1, 2, 3, 4, 5, 6, 7),
            listOf(2, 3, 4, 5, 6, 7, 0, 1),
            listOf(4, 5, 6, 7, 0, 1, 2, 3),
            listOf(6, 7, 0, 1, 2, 3, 4, 5),
            listOf(0, 7, 6, 5, 4, 3, 2, 1),
            listOf(6, 5, 4, 3, 2, 1, 0, 7),
            listOf(4, 3, 2, 1, 0, 7, 6, 5),
            listOf(2, 1, 0, 7, 6, 5, 4, 3),
        )
    }

    /**
     * 由int判断ingredients索引
     */
    class IndexData(val map: HashMap<String, Ingredient>) {
        companion object {
            val CODEC: Codec<IndexData> = Codec.unboundedMap(Codec.STRING, Ingredient.CODEC_NONEMPTY).xmap(
                { map -> IndexData(HashMap(map)) },
                { indexData -> indexData.map }
            )
        }
    }
}