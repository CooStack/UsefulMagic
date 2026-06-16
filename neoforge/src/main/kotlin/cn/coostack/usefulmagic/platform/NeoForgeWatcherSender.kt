package cn.coostack.usefulmagic.platform

import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.neoforged.neoforge.network.PacketDistributor


object NeoForgeWatcherSender : WatcherSender {


    override fun trackWith(
        entity: Entity,
        packet: CustomPacketPayload,
        self: Boolean
    ) {
        if (self) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet)
        } else {
            PacketDistributor.sendToPlayersTrackingEntity(entity, packet)
        }
    }
}
