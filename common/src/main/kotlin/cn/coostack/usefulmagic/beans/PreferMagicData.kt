package cn.coostack.usefulmagic.beans

import cn.coostack.usefulmagic.items.weapon.magic.MagicItem
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import kotlin.math.absoluteValue

/**
 * # 设计初衷
 * - 改为了法术分离之后 有些法杖其实没有存在的必要了（因为同级）
 * - 所以做了法术的偏好魔法， 让玩家有制作和使用这些法杖的欲望
 * # 偏好魔法
 * - 如果符合条件 那么就会有额外的加成
 * - 偏好程度 （最差 -5 最好 5） 如果偏好是负数，那就代表排斥
 * - 排斥会有Debuff
 * ## 加成列表
 * - 伤害加成 [damageFactor]
 * - 施法速度加成(需要时间减免) [usageReductionFactor]
 * - cd减免 [cdReductionFactor]
 * - 魔力减免 [manaReductionFactor]
 * 公式如下: final = (1 + level * preferFactor) * (base * wandFactor)
 */
class PreferMagicData(
    val damageFactor: Double,
    val usageReductionFactor: Double,
    val cdReductionFactor: Double,
    val manaReductionFactor: Double,
    preferItemsData: Map<Item, Int> = emptyMap()
) {
    companion object {
        @JvmStatic
        fun getPreferTranslateKey(level: Int): Component {
            return Component.translatable("item.usefulmagic.prefer.level${if (level >= 0) "$level" else "r${level.absoluteValue}"}")
        }

        private fun decodePreferItems(source: Map<ResourceLocation, Int>): DataResult<Map<Item, Int>> {
            val result = HashMap<Item, Int>(source.size)
            source.forEach { (itemId, level) ->
                val item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null)
                    ?: return DataResult.error { "Unknown item id in prefer_items: $itemId" }
                result[item] = level
            }
            return DataResult.success(result)
        }

        private fun encodePreferItems(source: Map<Item, Int>): DataResult<Map<ResourceLocation, Int>> {
            val result = HashMap<ResourceLocation, Int>(source.size)
            source.forEach { (item, level) ->
                val itemId = BuiltInRegistries.ITEM.getKey(item)
                result[itemId] = level
            }
            return DataResult.success(result)
        }

        private val PREFER_ITEMS_CODEC: Codec<Map<Item, Int>> =
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).flatXmap(
                ::decodePreferItems,
                ::encodePreferItems
            )

        val CODEC: Codec<PreferMagicData> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.DOUBLE.fieldOf("damage_factor").forGetter { it.damageFactor },
                Codec.DOUBLE.fieldOf("usage_factor").forGetter { it.usageReductionFactor },
                Codec.DOUBLE.fieldOf("cd_reduction_factor").forGetter { it.cdReductionFactor },
                Codec.DOUBLE.fieldOf("mana_reduction_factor").forGetter { it.manaReductionFactor },
                PREFER_ITEMS_CODEC.optionalFieldOf("prefer_items", emptyMap<Item, Int>())
                    .forGetter { it.preferItems }
            ).apply(instance, ::PreferMagicData)
        }
    }

    private val preferItems = HashMap(preferItemsData)

    fun isPreferItem(item: Item): Boolean {
        return preferItems.containsKey(item)
    }

    fun getPreferLevel(item: Item) = preferItems[item] ?: 0
    fun getPreferLevel(item: ItemStack) = preferItems[item.item] ?: 0

    fun putPrefer(item: Item, level: Int): PreferMagicData {
        preferItems[item] = level
        return this
    }

    fun getDamageFactor(item: ItemStack) = getDamageFactor(item.item)

    fun getUsageFactor(item: ItemStack) = getUsageFactor(item.item)

    fun getCdReductionFactor(item: ItemStack) = getCdReductionFactor(item.item)

    fun getManaReductionFactor(item: ItemStack) = getManaReductionFactor(item.item)

    fun getDamageFactor(item: Item): Double {
        val level = preferItems[item] ?: 0
        return damageFactor * level
    }

    fun getUsageFactor(item: Item): Double {
        val level = preferItems[item] ?: 0
        return usageReductionFactor * level
    }

    fun getCdReductionFactor(item: Item): Double {
        val level = preferItems[item] ?: 0
        return cdReductionFactor * level
    }

    fun getManaReductionFactor(item: Item): Double {
        val level = preferItems[item] ?: 0
        return manaReductionFactor * level
    }
}
