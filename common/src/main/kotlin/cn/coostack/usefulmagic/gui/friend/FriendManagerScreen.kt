package cn.coostack.usefulmagic.gui.friend

import cn.coostack.usefulmagic.gui.friend.widget.FriendItemWidget
import cn.coostack.usefulmagic.managers.client.ClientRequestManager
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendListRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.PlayerSkin
import net.minecraft.network.chat.Component
import net.minecraft.realms.RealmsLabel
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

/**
 *
 */
class FriendManagerScreen : Screen(Component.translatable("screen.title.friend_manager_title")) {

    companion object {
        const val itemCardPadding = 5
        const val cardPadding = 30
        const val itemCardHeight = 32
        const val itemCardWidth = 360
    }

    val client: Minecraft
        get() = Minecraft.getInstance()

    //    val pageWidgets = ArrayList<Element>()
    var currentPage = 1
    var maxPage = 1


    fun getOrRequestFriends(recall: (list: MutableList<PacketS2CFriendListResponse.PlayerProfile>) -> Unit) {
        val player = Minecraft.getInstance().player ?: return
        ClientRequestManager.sendRequest(
            PacketC2SFriendListRequest(player.uuid, currentPage),
            PacketS2CFriendListResponse.payloadID
        ).recall {
            val r = this.request as PacketC2SFriendListRequest
            val response = it as PacketS2CFriendListResponse
            this@FriendManagerScreen.maxPage = response.maxPage
            recall(response.friends.toMutableList())
        }
    }


    fun tryFriendsSkinTexture(uuid: UUID): Pair<PlayerSkin?, PlayerSkin.Model?> {
        val networkHandler = client.connection
        val entry = networkHandler?.getPlayerInfo(uuid)
        return entry?.skin to entry?.skin?.model()
    }

    override fun init() {
        var v = client.options.guiScale().get()
        v = if (v == 0) {
            3
        } else {
            v
        }
        val scale = 3.0 / v
        val originX = width / 2
        addRenderableWidget(
            Button.Builder(Component.literal("下一页")) {
                if (maxPage > currentPage) {
                    currentPage++
                }
                flushWidget()
            }.bounds(
                ((itemCardWidth * scale / 2 + originX + itemCardPadding * scale)).toInt(),
                ((itemCardHeight * 10 - itemCardPadding * 2) * scale).toInt(),
                (32 * scale).toInt(),
                (16 * scale).toInt()
            ).build()
        )
        addRenderableWidget(
            Button.Builder(Component.literal("上一页")) {
                if (currentPage > 1) {
                    currentPage--
                }
                flushWidget()
            }.bounds(
                ((itemCardWidth * scale / 2 + originX + itemCardPadding * scale)).toInt(),
                ((itemCardHeight * 10 - itemCardPadding * 3 - 16) * scale).toInt(),
                (32 * scale).toInt(),
                (16 * scale).toInt()
            ).build()
        )
        flushWidget()
        super.init()
    }

    internal fun flushWidget() {
        val scale = client.options.guiScale().get()
        val resetScale = 3.0 / scale
        val originX = width / 2
        clearWidgets()
        getOrRequestFriends {
            addRenderableOnly(
                RealmsLabel(
                    Component.literal("朋友:${currentPage}/${maxPage}"),
                    originX,
                    10,
                    0xFFFFFFFFu.toInt()
                )
            )
            setCurrentPageIcons(it)
        }
    }

    private fun setCurrentPageIcons(currents: MutableList<PacketS2CFriendListResponse.PlayerProfile>) {
        val scale = 3.0 / client.options.guiScale().get()
        val yStart = cardPadding
        val x = (width - (itemCardWidth * scale).toInt()) / 2
        val count = currents.size
        if (count == 0) return
        val yStep = (cardPadding * scale).toInt()
        repeat(count) {
            val profile = currents[it]
            addRenderableWidget(
                FriendItemWidget(
                    profile,
                    tryFriendsSkinTexture(profile.uuid).first,
                    x,
                    (yStart + yStep * it),
                    (itemCardWidth * scale).toInt(),
                    (itemCardHeight * scale).toInt(),
                    this
                )
            )
        }
    }

}