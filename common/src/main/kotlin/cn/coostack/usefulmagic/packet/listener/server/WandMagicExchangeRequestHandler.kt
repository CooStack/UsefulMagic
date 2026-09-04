package cn.coostack.usefulmagic.packet.listener.server

import cn.coostack.cooparticlesapi.platform.network.ServerContext
import cn.coostack.usefulmagic.items.prop.SpellBagItem
import cn.coostack.usefulmagic.items.prop.SpellBagSelector
import cn.coostack.usefulmagic.items.weapon.wands.MagicWand
import cn.coostack.usefulmagic.packet.c2s.PacketC2SWandMagicExchangeRequest
import cn.coostack.usefulmagic.utils.ChargeStateAccess

object WandMagicExchangeRequestHandler {
    fun receive(
        payload: PacketC2SWandMagicExchangeRequest,
        context: ServerContext
    ) {
        val player = context.player()
        if (ChargeStateAccess.isCharging(player) || player.isUsingItem) {
            return
        }
        if (payload.selectedSlot !in 0 until 9) {
            return
        }
        if (player.inventory.selected != payload.selectedSlot) {
            return
        }
        val wandStack = if (payload.offhand) player.offhandItem else player.mainHandItem
        if (wandStack.item !is MagicWand) {
            return
        }
        val selection = SpellBagSelector.findFirstUsable(player) ?: return
        if (selection.inventorySlot != payload.bagSlot) {
            return
        }
        if (payload.magicIndex == SpellBagItem.EMPTY_MAGIC_INDEX && !selection.fallbackEmpty) {
            return
        }
        if (payload.magicIndex != SpellBagItem.EMPTY_MAGIC_INDEX && payload.magicIndex !in selection.contents.indices) {
            return
        }
        val spellBag = selection.stack.item as? SpellBagItem ?: return
        if (!spellBag.exchangeWithWandAt(selection.stack, wandStack, payload.magicIndex, player)) {
            return
        }
        player.inventory.setChanged()
        player.containerMenu.broadcastChanges()
    }
}
