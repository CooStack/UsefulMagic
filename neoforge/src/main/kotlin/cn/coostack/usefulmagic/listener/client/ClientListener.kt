package cn.coostack.usefulmagic.listener.client

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.UsefulMagicClient
import cn.coostack.usefulmagic.gui.mana.ManaBarCallback
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RenderGuiEvent

@EventBusSubscriber(modid = UsefulMagic.MOD_ID)
object ClientListener {

    @SubscribeEvent
    fun onTickClient(event: ClientTickEvent.Post) {
        UsefulMagicClient.tickClient()
    }

    @SubscribeEvent
    fun hudCallback(event: RenderGuiEvent.Post) {
        ManaBarCallback.onHudRender(event.guiGraphics, event.partialTick.getGameTimeDeltaPartialTick(true))
    }

}