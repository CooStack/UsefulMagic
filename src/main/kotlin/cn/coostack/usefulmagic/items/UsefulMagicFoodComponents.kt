package cn.coostack.usefulmagic.items

import net.minecraft.component.type.FoodComponent

object UsefulMagicFoodComponents {
    @JvmField
    val REVIVE = FoodComponent.Builder()
        .nutrition(0)
        .saturationModifier(0f)
        .build()

}