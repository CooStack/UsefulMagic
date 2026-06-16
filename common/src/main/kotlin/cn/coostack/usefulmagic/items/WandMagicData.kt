package cn.coostack.usefulmagic.items

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

/**
 * 由于十八码的Neoforge 不允许可变对象ItemStack存储
 * 从而让AI设计的这个莫名其妙的data
 *
 * @property item
 * @property count
 * @property components
 * @constructor Create empty Wand magic data
 */
data class WandMagicData(
    val item: Holder<Item>,
    val count: Int,
    val components: DataComponentPatch = DataComponentPatch.EMPTY
) {
    companion object {
        val CODEC: Codec<WandMagicData> = RecordCodecBuilder.create { instance ->
            instance.group(
                ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(WandMagicData::item),
                ExtraCodecs.intRange(1, 99).fieldOf("count").orElse(1).forGetter(WandMagicData::count),
                DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                    .forGetter(WandMagicData::components)
            ).apply(instance, ::WandMagicData)
        }

        fun fromStack(stack: ItemStack): WandMagicData? {
            if (stack.isEmpty) {
                return null
            }
            return WandMagicData(
                stack.itemHolder,
                stack.count,
                stack.componentsPatch
            )
        }
    }

    fun toStack(): ItemStack {
        return ItemStack(item, count, components)
    }

    fun `is`(target: Item): Boolean {
        return item.value() === target
    }
}
