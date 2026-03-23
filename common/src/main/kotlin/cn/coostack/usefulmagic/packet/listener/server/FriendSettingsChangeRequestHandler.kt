package cn.coostack.usefulmagic.packet.listener.server

import cn.coostack.cooparticlesapi.platform.network.ServerContext
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendSettingsChangeRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendChangeResponse

/**
 * Handle FriendManager friendly settings updates.
 */
object FriendSettingsChangeRequestHandler {
    fun receive(
        payload: PacketC2SFriendSettingsChangeRequest,
        context: ServerContext
    ) {
        val player = context.player()
        if (player.uuid != payload.owner) {
            context.reply(PacketS2CFriendChangeResponse(payload.owner, false))
            return
        }

        val data = UsefulMagic.state.getDataFromServer(player.uuid)
        data.updateFriendlySettings(
            hostile = payload.treatHostileAsFriend,
            neutral = payload.treatNeutralAsFriend,
            nonFriendPlayer = payload.treatNonFriendPlayerAsFriend,
            friendPlayer = payload.treatFriendPlayerAsFriend,
            animal = payload.treatAnimalAsFriend,
            friendlyMob = payload.treatFriendlyMobAsFriend
        )

        context.reply(PacketS2CFriendChangeResponse(payload.owner, true))
    }
}
