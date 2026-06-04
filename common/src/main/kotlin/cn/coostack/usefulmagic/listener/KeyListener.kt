package cn.coostack.usefulmagic.listener

import cn.coostack.cooparticlesapi.annotations.events.EventHandler
import cn.coostack.cooparticlesapi.annotations.events.EventListener
import cn.coostack.cooparticlesapi.event.events.key.KeyActionEvent
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.UsefulMagicKeys
import cn.coostack.usefulmagic.effects.UsefulMagicEffects
import cn.coostack.usefulmagic.extend.*
import cn.coostack.usefulmagic.items.weapon.wands.MagicWand
import cn.coostack.usefulmagic.utils.MagicHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

@EventListener(UsefulMagic.MOD_ID)
object KeyListener {

    @EventHandler
    fun onChargingKeyEvents(event: KeyActionEvent) {
        if (!event.serverSide) {
            return
        }
        val actions = event.getAction(UsefulMagicKeys.CHARGE_MAGIC)
        if (actions.isEmpty()) {
            return
        }
        val player = event.player
        val world = player.level()
        val wand = if (player.mainHandItem.item is MagicWand) {
            player.mainHandItem
        } else if (player.offhandItem.item is MagicWand) {
            player.offhandItem
        } else {

            handleChargingItemExchange(player, ItemStack.EMPTY, 0, world)
            return
        }
        val item = wand.item as MagicWand
        val cancel = event.isReleased(UsefulMagicKeys.CHARGE_MAGIC)
        val wandIdentity = System.identityHashCode(wand)

        if (handleChargingItemExchange(player, wand, wandIdentity, world)) {
            return
        }
        // 冷却时仍然要允许松键清理服务端蓄力状态，不能直接吞掉 release 事件
        if (player.cooldowns.isOnCooldown(item)) {
            if (cancel) {
                if (player.charging) {
                    item.stopCharge(player, world, wand, player.chargingTick, false)
                }
                player.resetChargeState()
            }
            return
        }
        if (UsefulMagicEffects.isMagicSealed(player)) {
            if (player.charging) {
                item.stopCharge(player, world, wand, player.chargingTick, false)
            }
            player.resetChargeState()
            return
        }
        // 判断玩家是否满足蓄力条件
        if (!MagicHelper.isManaEnough(player, wand)) {
            if (player.charging) {
                // 先前正在蓄力，但此时魔力不足
                item.stopCharge(player, world, wand, player.chargingTick, false)
            }
            player.resetChargeState()
            return
        }

        if (event.isLongPress(UsefulMagicKeys.CHARGE_MAGIC)) {
            if (cancel) {
                // 释放长按时，取消并结束蓄力
                item.stopCharge(
                    player,
                    world,
                    wand,
                    player.chargingTick,
                    player.chargingTick >= MagicHelper.getMaxChargingTick(wand)
                )
                player.resetChargeState()
                return
            }
            player.charging = true
            player.chargedItem = wand.copy()
            player.chargedItemIdentity = wandIdentity
            item.chargingTick(player, world, wand, player.chargingTick++)
        }
        if (event.isSingleClick(UsefulMagicKeys.CHARGE_MAGIC)) {
            player.chargedItem = wand.copy()
            player.chargedItemIdentity = wandIdentity
            // 长按前会先触发一次单击事件
            item.startCharge(player, world, wand)
        }
    }

    private fun handleChargingItemExchange(
        player: Player,
        currentWand: ItemStack,
        currentWandIdentity: Int,
        world: Level
    ): Boolean {
        val chargedWandStack = player.chargedItem
        if (chargedWandStack.isEmpty) {
            if (player.charging || player.chargingTick > 0) {
                player.resetChargeState()
            }
            return false
        }
        val chargedWandIdentity = player.chargedItemIdentity
        if (chargedWandIdentity != 0) {
            if (chargedWandIdentity == currentWandIdentity) return false
        } else if (ItemStack.isSameItemSameComponents(chargedWandStack, currentWand)) {
            return false
        }

        val chargedItem = chargedWandStack.item
        if (player.charging && chargedItem is MagicWand) {
            chargedItem.stopCharge(player, world, chargedWandStack, player.chargingTick, false)
        }
        player.resetChargeState()
        return true
    }

}
