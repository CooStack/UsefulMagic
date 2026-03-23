package cn.coostack.usefulmagic.items

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredComponentType
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.beans.PreferMagicData
import com.mojang.serialization.Codec
import net.minecraft.core.component.DataComponentType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import java.util.function.Supplier

object UsefulMagicDataComponentTypes {
    val types = mutableListOf<CommonDeferredComponentType<*>>()

    @JvmField
    val LARGE_REVIVE_USE_COUNT = register("large_revive_usage") {
        DataComponentType.builder<Int>().persistent(Codec.INT).build()
    }

    @JvmField
    val ENABLED = register(
        "enabled"
    ) {
        DataComponentType.builder<Boolean>().persistent(Codec.BOOL).build()
    }


    /**
     * 魔法等级， 法杖必须大于等于这个才能安装
     */
    @JvmField
    val MAGIC_LEVEL = register("magic_level") {
        DataComponentType.builder<Int>().persistent(Codec.INT).build()
    }

    /**
     * 释放魔法基本耗时(单位tick) 会因为速度加成而降低最终需求
     */
    @JvmField
    val MAGIC_BASE_USAGE = register("magic_base_usage") {
        DataComponentType.builder<Int>().persistent(Codec.INT).build()
    }

    /**
     * 最小蓄力时间 (不能小于这个值 防止部分蓄力动画播放不完全)
     */
    @JvmField
    val MAGIC_MIN_USAGE = register("magic_min_usage") {
        DataComponentType.builder<Int>().persistent(Codec.INT).build()
    }

    /**
     * 基本魔力消耗
     */
    @JvmField
    val MAGIC_BASE_MANA_COST = register("magic_base_mana_cost") {
        DataComponentType.builder<Int>().persistent(Codec.INT).build()
    }

    /**
     * 魔力基本伤害
     */
    @JvmField
    val MAGIC_BASE_DAMAGE = register("magic_base_damage") {
        DataComponentType.builder<Double>().persistent(Codec.DOUBLE).build()
    }

    /**
     * 魔法释放后的基础物品冷却（法杖）
     * 会受到速率倍率的影响
     */
    @JvmField
    val MAGIC_RELEASE_CD = register("magic_release_cd") {
        DataComponentType.builder<Int>().persistent(Codec.INT).build()
    }

    @JvmField
    val WAND_PREFER = register("wand_prefer") {
        DataComponentType.builder<PreferMagicData>().persistent(PreferMagicData.CODEC).build()
    }

    /**
     * 法杖等级
     */
    @JvmField
    val WAND_LEVEL = register("wand_level") {
        DataComponentType.builder<Int>().persistent(Codec.INT).build()
    }

    /**
     * 消耗减免 reduction
     * final cost = cost * (1 - effect)
     */
    @JvmField
    val WAND_REDUCTION = register("wand_reduction") {
        DataComponentType.builder<Double>().persistent(Codec.DOUBLE).build()
    }

    /**
     * 法杖安装的法球stack
     */
    @JvmField
    val WAND_MAGIC = register("wand_magic") {
        DataComponentType.builder<ItemStack>().persistent(ItemStack.OPTIONAL_CODEC).build()
    }

    /**
     * 法杖速度倍率
     */
    @JvmField
    val WAND_SPEED_FACTOR = register("wand_speed_factor") {
        DataComponentType.builder<Double>().persistent(Codec.DOUBLE).build()
    }


    fun <T> register(id: String, type: Supplier<DataComponentType<T>>): CommonDeferredComponentType<T> {
        val location = ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, id)
        val common = CommonDeferredComponentType(location, type)
        types.add(common)
        return common
    }

    fun init() {

    }
}