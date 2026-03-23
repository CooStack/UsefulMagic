package cn.coostack.usefulmagic.platform

import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

interface WatcherSender {
    /**
     *
     * @param entity
     * @param packet
     * @param self 如果entity是玩家，那么也会向entity发包，否则不发包
     */
    fun trackWith(entity: Entity, packet: CustomPacketPayload, self: Boolean = true)
}