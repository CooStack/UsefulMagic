package cn.coostack.usefulmagic.items

import net.minecraft.world.food.FoodProperties
import net.minecraft.world.food.Foods

object UsefulMagicFoodComponents {
    @JvmField
    val REVIVE = FoodProperties.Builder()
        .nutrition(0)
        .saturationModifier(0f)
        .build()

}