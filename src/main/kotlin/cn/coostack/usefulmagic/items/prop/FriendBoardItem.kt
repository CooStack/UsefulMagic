package cn.coostack.usefulmagic.items.prop

import cn.coostack.usefulmagic.UsefulMagicClient
import cn.coostack.usefulmagic.managers.client.ClientRequestManager
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendAddRequest
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand

class FriendBoardItem : Item(
    Settings()
        .maxCount(1)
) {
    // friend_board

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        tooltip.add(Text.literal("§7右键玩家可以将玩家信息添加到朋友列表"))
        tooltip.add(Text.literal("§7按下§f${UsefulMagicClient.friendUIBinding.boundKeyLocalizedText.string}键§7可以打开添加的朋友UI"))

    }

    override fun useOnEntity(
        stack: ItemStack,
        user: PlayerEntity,
        entity: LivingEntity,
        hand: Hand
    ): ActionResult {
        if (!user.world.isClient) {
            return super.useOnEntity(stack, user, entity, hand)
        }
        if (entity !is PlayerEntity) {
            return super.useOnEntity(stack, user, entity, hand)
        }
        ClientPlayNetworking.send((PacketC2SFriendAddRequest(user.uuid, entity.uuid)))
        user.sendMessage(Text.literal("成功添加玩家: ${entity.name.string} 到好友列表"))

        return super.useOnEntity(stack, user, entity, hand)
    }

}