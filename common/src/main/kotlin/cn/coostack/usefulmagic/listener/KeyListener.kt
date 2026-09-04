package cn.coostack.usefulmagic.listener

import cn.coostack.cooparticlesapi.annotations.events.EventHandler
import cn.coostack.cooparticlesapi.annotations.events.EventListener
import cn.coostack.cooparticlesapi.event.events.key.KeyActionEvent
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.UsefulMagicKeys
import cn.coostack.usefulmagic.effects.UsefulMagicEffects
import cn.coostack.usefulmagic.gui.magicexchange.MagicExchangeOpenSignal
import cn.coostack.usefulmagic.items.weapon.wands.MagicWand
import cn.coostack.usefulmagic.utils.ChargeStateAccess
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
                if (ChargeStateAccess.isCharging(player)) {
                    item.stopCharge(player, world, wand, ChargeStateAccess.getChargingTick(player), false)
                }
                ChargeStateAccess.reset(player)
            }
            return
        }
        if (UsefulMagicEffects.isMagicSealed(player)) {
            if (ChargeStateAccess.isCharging(player)) {
                item.stopCharge(player, world, wand, ChargeStateAccess.getChargingTick(player), false)
            }
            ChargeStateAccess.reset(player)
            return
        }
        // 判断玩家是否满足蓄力条件
        if (!MagicHelper.isManaEnough(player, wand)) {
            if (ChargeStateAccess.isCharging(player)) {
                // 先前正在蓄力，但此时魔力不足
                item.stopCharge(player, world, wand, ChargeStateAccess.getChargingTick(player), false)
            }
            ChargeStateAccess.reset(player)
            return
        }

        if (event.isLongPress(UsefulMagicKeys.CHARGE_MAGIC)) {
            if (cancel) {
                val chargingTick = ChargeStateAccess.getChargingTick(player)
                // 释放长按时，取消并结束蓄力
                item.stopCharge(
                    player,
                    world,
                    wand,
                    chargingTick,
                    chargingTick >= MagicHelper.getMaxChargingTick(wand)
                )
                ChargeStateAccess.reset(player)
                return
            }
            val chargingTick = ChargeStateAccess.getChargingTick(player)
            ChargeStateAccess.setCharging(player, true)
            ChargeStateAccess.setChargedItem(player, wand.copy())
            ChargeStateAccess.setChargedItemIdentity(player, wandIdentity)
            item.chargingTick(player, world, wand, chargingTick)
            ChargeStateAccess.setChargingTick(player, chargingTick + 1)
        }
        if (event.isSingleClick(UsefulMagicKeys.CHARGE_MAGIC)) {
            ChargeStateAccess.setChargedItem(player, wand.copy())
            ChargeStateAccess.setChargedItemIdentity(player, wandIdentity)
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
        val chargedWandStack = ChargeStateAccess.getChargedItem(player)
        if (chargedWandStack.isEmpty) {
            if (ChargeStateAccess.isCharging(player) || ChargeStateAccess.getChargingTick(player) > 0) {
                ChargeStateAccess.reset(player)
            }
            return false
        }
        val chargedWandIdentity = ChargeStateAccess.getChargedItemIdentity(player)
        if (chargedWandIdentity != 0) {
            if (chargedWandIdentity == currentWandIdentity) return false
        } else if (ItemStack.isSameItemSameComponents(chargedWandStack, currentWand)) {
            return false
        }

        val chargedItem = chargedWandStack.item
        if (ChargeStateAccess.isCharging(player) && chargedItem is MagicWand) {
            chargedItem.stopCharge(player, world, chargedWandStack, ChargeStateAccess.getChargingTick(player), false)
        }
        ChargeStateAccess.reset(player)
        return true
    }


    @EventHandler
    fun onMagicExchange(event: KeyActionEvent) {
        if (event.serverSide) {
            return
        }
        if (!event.isLongPress(UsefulMagicKeys.EXCHANGE_MAGIC) || event.isReleased(UsefulMagicKeys.EXCHANGE_MAGIC)) {
            return
        }

        MagicExchangeOpenSignal.request()
    }

}
