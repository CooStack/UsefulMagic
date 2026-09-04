package cn.coostack.usefulmagic

import cn.coostack.usefulmagic.UsefulMagic.MOD_ID
import net.minecraft.resources.ResourceLocation

object UsefulMagicKeys {
    val CHARGE_MAGIC = ResourceLocation.fromNamespaceAndPath(MOD_ID, "charge_magic")

    /**
     * 更改法杖中的法术时按下的按键
     * 长按按键会有一个圆环形的槽位进行选择
     * （手持法杖，背包内要有一个法术收纳袋）
     * 可能会有多个收纳袋的情况， 只识别第一个
     */
    val EXCHANGE_MAGIC = ResourceLocation.fromNamespaceAndPath(MOD_ID, "exchange_magic")
}