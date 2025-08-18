package cn.coostack.usefulmagic.listener.server

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.listener.DefendMagicListener
import net.minecraft.world.entity.player.Player
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.EntityEvent
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent

@EventBusSubscriber(modid = UsefulMagic.MOD_ID)
object PlayerListener {
    @SubscribeEvent
    fun beforeDamaged(event: LivingIncomingDamageEvent) {
        val entity = event.entity
        val canDamage = !DefendMagicListener.call(entity, event.source, event.amount)
        event.isCanceled = canDamage
    }
}