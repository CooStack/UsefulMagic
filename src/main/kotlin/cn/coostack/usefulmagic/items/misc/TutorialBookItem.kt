package cn.coostack.usefulmagic.items.misc

import cn.coostack.usefulmagic.gui.TutorialBookScreen
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class TutorialBookItem : Item(Settings().maxCount(1)) {
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val value = super.use(world, user, hand)
        if (!world.isClient) {
            return value
        }
        // client only
        val client = MinecraftClient.getInstance()
        client!!.soundManager.play(
            PositionedSoundInstance.master(
                SoundEvents.ITEM_BOOK_PAGE_TURN, 1f
            )
        )
        val screen = TutorialBookScreen()
        client.setScreen(screen)
        return value
    }
}