package cn.coostack.usefulmagic.extend

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredItem
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

fun ItemStack.isOf(another: Item): Boolean {
    return this.`is`(another)
}

fun ItemStack.isOf(item: CommonDeferredItem): Boolean = this.isOf(item.getItem())
fun ItemStack.isIn(key: TagKey<Item>): Boolean = this.`is`(key)