package cn.coostack.usefulmagic.managers.client

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.platform.CooParticlesServices
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

/**
 * 发起客户端请求时,会通过此等待服务器的发送
 */
object ClientRequestManager {
    class RequestRecall(val request: CustomPacketPayload) {
        private var callable: RequestRecall.(response: CustomPacketPayload) -> Unit = {}
        fun done(response: CustomPacketPayload) {
            callable(response)
        }

        fun recall(callable: RequestRecall.(response: CustomPacketPayload) -> Unit = {}): RequestRecall {
            this.callable = callable
            return this
        }
    }

    /**
     * 多个相同目标的请求会被覆盖
     */
    val requests = HashMap<CustomPacketPayload.Type<*>, RequestRecall>()

    fun sendRequest(packet: CustomPacketPayload, receiverType: CustomPacketPayload.Type<*>): RequestRecall {
        val recall = RequestRecall(packet)
        requests[receiverType] = recall
        CooParticlesServices.CLIENT_NETWORK.send(packet)
        return recall
    }

    fun setResponse(packet: CustomPacketPayload) {
        val recall = requests.remove(packet.type()) ?: return
        recall.done(packet)
    }
}