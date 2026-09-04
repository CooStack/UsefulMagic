package cn.coostack.usefulmagic.items.weapon.magic

import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.utils.MagicHelper
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

/**
 * 魔球
 * 会有对应魔法 消耗时间 消耗蓝条 然后释放
 * # 设定如下
 * ## 参数列表
 * - 基本耗蓝： 基本魔力消耗
 * - 基本伤害： 直接作用到实体的魔法伤害（不包括爆炸等其他伤害）
 * - 基本释放速度： 完整释放这个魔法需要消耗的时间
 * - 魔法等级： 决定他能装在哪些法杖上
 * @param properties
 */
abstract class MagicItem(properties: Properties) : Item(properties) {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        val level = stack.get(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get()) ?: return
        val baseCost = stack.get(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get()) ?: return
        val baseDamage = stack.get(UsefulMagicDataComponentTypes.MAGIC_BASE_DAMAGE.get()) ?: return
        val baseCD = stack.get(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get()) ?: return
        val baseUsageTime = stack.get(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get()) ?: return
        tooltipComponents.add(Component.translatable("item.usefulmagic.magic.level", Component.literal("$level")))
        tooltipComponents.add(
            Component.translatable(
                "item.usefulmagic.magic.base_cost",
                Component.literal("$baseCost")
            )
        )
        tooltipComponents.add(
            Component.translatable(
                "item.usefulmagic.magic.base_damage",
                Component.literal("$baseDamage")
            )
        )
        tooltipComponents.add(
            Component.translatable(
                "item.usefulmagic.magic.base_usage",
                Component.literal("$baseUsageTime")
            )
        )
        tooltipComponents.add(Component.translatable("item.usefulmagic.magic.base_cd", Component.literal("$baseCD")))
    }


    fun getMaxTick(shooter: LivingEntity, wandStack: ItemStack): Int {
        return MagicHelper.getMaxChargingTick(wandStack)
    }


    /**
     * 蓄力完成时会做
     *
     * @param shooter
     * @param world
     * @param time
     */
    abstract fun release(shooter: LivingEntity, world: Level, wandStack: ItemStack, ballStack: ItemStack, time: Int)

    /**
     * 蓄力时会执行， 到达time会执行release
     *
     * @param shooter
     * @param world
     * @param time
     * @param wandStack 法杖Stack
     * @param ballStack 法球Stack
     */
    abstract fun usingTick(shooter: LivingEntity, wandStack: ItemStack, ballStack: ItemStack, world: Level, time: Int)

    abstract fun stopUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        chargingTick: Int,
        max: Boolean
    )

    abstract fun startUse(shooter: LivingEntity, world: Level, wandStack: ItemStack, ballStack: ItemStack)

}
