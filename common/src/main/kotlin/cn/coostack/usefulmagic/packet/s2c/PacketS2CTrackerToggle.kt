package cn.coostack.usefulmagic.packet.s2c

import cn.coostack.cooparticlesapi.annotations.codec.CodecHelper
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.data.tracked.CooDataTracker
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

/**
 * - 发送方提供完整tracker
 * - 接收方接受发生变更的data
 */
class PacketS2CTrackerToggle private constructor(
    val tracker: CooDataTracker,
    val targetID: Int,
    private val dirtyData: Map<String, Any>
) :
    CustomPacketPayload {
    constructor(tracker: CooDataTracker, targetID: Int) : this(
        tracker,
        targetID,
        tracker.getDirtiesDataAndClean()
    )

    companion object {
        val payloadID = CustomPacketPayload.Type<PacketS2CTrackerToggle>(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "tracker_toggle")
        )

        val CODEC = CustomPacketPayload.codec<FriendlyByteBuf, PacketS2CTrackerToggle>(
            { data, buf ->
                val dirties = data.dirtyData
                buf.writeInt(dirties.size)
                buf.writeInt(data.targetID)
                dirties.forEach { (string, any) ->
                    val type = data.tracker.trackedTypes[string] ?: return@forEach
                    val codec = CodecHelper.supposedTypes[type.type.name]!! as StreamCodec<FriendlyByteBuf, Any>
                    buf.writeUtf(string)
                    codec.encode(buf, any)
                }
            }, {
                val count = it.readInt()
                val id = it.readInt()
                val tracker = CooDataTracker()
                // 这里一定是client环境
                // 这里要找到entity 或者他的父类 (不要子类的注册）
                repeat(count) { i ->
                    val key = it.readUtf()
                    val type = CooDataTracker.registerEntityTypes[key]!!
                    val codec = CodecHelper.supposedTypes[type.type.name]!! as StreamCodec<FriendlyByteBuf, Any>
                    val value = codec.decode(it)
                    tracker.trackedData[key] = value
                    tracker.trackedDirties[key] = false
                    tracker.trackedTypes[key] = type
                }
                PacketS2CTrackerToggle(tracker, id, tracker.trackedData.toMap())
            }
        )
    }


    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return payloadID
    }
}
