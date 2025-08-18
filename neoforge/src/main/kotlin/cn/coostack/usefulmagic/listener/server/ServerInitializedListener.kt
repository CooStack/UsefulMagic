package cn.coostack.usefulmagic.listener.server

import cn.coostack.cooparticlesapi.platform.network.NeoForgeClientContext
import cn.coostack.cooparticlesapi.platform.network.NeoForgeServerContext
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFormationSettingChangeRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFormationSettingRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendAddRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendListRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendRemoveRequest
import cn.coostack.usefulmagic.packet.listener.client.FormationPacketListener
import cn.coostack.usefulmagic.packet.listener.client.FormationSettingsPacketResponseListener
import cn.coostack.usefulmagic.packet.listener.client.FriendChangeResponsePacketListener
import cn.coostack.usefulmagic.packet.listener.client.FriendResponsePacketListener
import cn.coostack.usefulmagic.packet.listener.client.ManaChangePacketListener
import cn.coostack.usefulmagic.packet.listener.server.FormationSettingChangePacketListener
import cn.coostack.usefulmagic.packet.listener.server.FormationSettingRequestPacketListener
import cn.coostack.usefulmagic.packet.listener.server.FriendAddListRequestHandler
import cn.coostack.usefulmagic.packet.listener.server.FriendListRequestHandler
import cn.coostack.usefulmagic.packet.listener.server.FriendRemoveListRequestHandler
import cn.coostack.usefulmagic.packet.s2c.PacketS2CEnergyCrystalChange
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationBreak
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationCreate
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationSettingsResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendChangeResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CManaDataToggle
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

@EventBusSubscriber(modid = UsefulMagic.MOD_ID)
object ServerInitializedListener {


    @SubscribeEvent
    fun onPacketRegister(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar(UsefulMagic.MOD_ID)
        registrar.playToClient(
            PacketS2CEnergyCrystalChange.payloadID,
            PacketS2CEnergyCrystalChange.CODEC
        ) { packet, context ->
            FormationPacketListener.handleEnergyChange(packet, NeoForgeClientContext(context))
        }
        registrar.playToClient(
            PacketS2CFormationBreak.payloadID,
            PacketS2CFormationBreak.CODEC
        ) { packet, context ->
            FormationPacketListener.handleBreak(packet, NeoForgeClientContext(context))
        }
        registrar.playToClient(
            PacketS2CFormationCreate.payloadID,
            PacketS2CFormationCreate.CODEC
        ) { packet, context ->
            FormationPacketListener.handleCreate(packet, NeoForgeClientContext(context))
        }
        registrar.playToClient(
            PacketS2CFormationSettingsResponse.payloadID,
            PacketS2CFormationSettingsResponse.CODEC
        ) { packet, context ->
            FormationSettingsPacketResponseListener.receive(packet, NeoForgeClientContext(context))
        }
        registrar.playToClient(
            PacketS2CFriendChangeResponse.payloadID,
            PacketS2CFriendChangeResponse.CODEC
        ) { packet, context ->
            FriendChangeResponsePacketListener.receive(packet, NeoForgeClientContext(context))
        }
        registrar.playToClient(
            PacketS2CFriendListResponse.payloadID,
            PacketS2CFriendListResponse.CODEC
        ) { packet, context ->
            FriendResponsePacketListener.receive(packet, NeoForgeClientContext(context))
        }
        registrar.playToClient(
            PacketS2CManaDataToggle.payloadID,
            PacketS2CManaDataToggle.CODEC
        ) { packet, context ->
            ManaChangePacketListener.receive(packet, NeoForgeClientContext(context))
        }

        registrar.playToServer(
            PacketC2SFormationSettingChangeRequest.payloadID,
            PacketC2SFormationSettingChangeRequest.CODEC
        ) { packet, context ->
            FormationSettingChangePacketListener.receive(packet, NeoForgeServerContext(context))
        }
        registrar.playToServer(
            PacketC2SFormationSettingRequest.payloadID,
            PacketC2SFormationSettingRequest.CODEC
        ) { packet, context ->
            FormationSettingRequestPacketListener.receive(packet, NeoForgeServerContext(context))
        }

        registrar.playToServer(
            PacketC2SFriendAddRequest.payloadID,
            PacketC2SFriendAddRequest.CODEC
        ) { packet, context ->
            FriendAddListRequestHandler.receive(packet, NeoForgeServerContext(context))
        }
        registrar.playToServer(
            PacketC2SFriendListRequest.payloadID,
            PacketC2SFriendListRequest.CODEC
        ) { packet, context ->
            FriendListRequestHandler.receive(packet, NeoForgeServerContext(context))
        }
        registrar.playToServer(
            PacketC2SFriendRemoveRequest.payloadID,
            PacketC2SFriendRemoveRequest.CODEC
        ) { packet, context ->
            FriendRemoveListRequestHandler.receive(packet, NeoForgeServerContext(context))
        }
    }

}