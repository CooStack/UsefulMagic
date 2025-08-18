package cn.coostack.usefulmagic.items.misc

import cn.coostack.usefulmagic.gui.guildbook.TutorialBookScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

class TutorialBookItem : Item(Properties().stacksTo(1)) {
    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val value = super.use(world, user, hand)
        if (!world.isClientSide) {
            return value
        }
        // client only
        val client = Minecraft.getInstance()
        client.soundManager.play(
            SimpleSoundInstance.forUI(
                SoundEvents.BOOK_PAGE_TURN, 1f
            )
        )
        val screen = TutorialBookScreen()
        client.setScreen(screen)

        return value
    }
}