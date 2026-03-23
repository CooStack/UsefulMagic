package cn.coostack.usefulmagic.platform

import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

object FabricWatcherSender : WatcherSender {

    override fun trackWith(
        entity: Entity,
        packet: CustomPacketPayload,
        self: Boolean
    ) {
        PlayerLookup.tracking(entity).forEach { player ->
            ServerPlayNetworking.send(player, packet)
        }

        if (self && entity is ServerPlayer) {
            ServerPlayNetworking.send(entity, packet)
        }
    }
}