package cn.coostack.usefulmagic.managers.client

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.network.packet.CustomPayload

/**
 * 发起客户端请求时,会通过此等待服务器的发送
 */
object ClientRequestManager {
    class RequestRecall(val request: CustomPayload) {
        private var callable: RequestRecall.(response: CustomPayload) -> Unit = {}
        fun done(response: CustomPayload) {
            callable(response)
        }

        fun recall(callable: RequestRecall.(response: CustomPayload) -> Unit = {}): RequestRecall {
            this.callable = callable
            return this
        }
    }

    /**
     * 多个相同目标的请求会被覆盖
     */
    val requests = HashMap<CustomPayload.Id<*>, RequestRecall>()

    fun sendRequest(packet: CustomPayload, receiverType: CustomPayload.Id<*>): RequestRecall {
        val recall = RequestRecall(packet)
        requests[receiverType] = recall
        ClientPlayNetworking.send(packet)
        return recall
    }

    fun setResponse(packet: CustomPayload) {
        val recall = requests.remove(packet.id) ?: return
        recall.done(packet)
    }
}