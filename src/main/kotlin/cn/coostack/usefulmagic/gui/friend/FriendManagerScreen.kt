package cn.coostack.usefulmagic.gui.friend

import cn.coostack.usefulmagic.gui.friend.widget.FriendItemWidget
import cn.coostack.usefulmagic.managers.client.ClientRequestManager
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendListRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ElementListWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.client.gui.widget.Widget
import net.minecraft.client.util.SkinTextures
import net.minecraft.text.Text
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

/**
 *
 */
class FriendManagerScreen : Screen(Text.translatable("screen.title.friend_manager_title")) {

    companion object {
        const val itemCardPadding = 5
        const val cardPadding = 30
        const val itemCardHeight = 32
        const val itemCardWidth = 360
    }

    val pageWidgets = ArrayList<Element>()
    var currentPage = 1
    var maxPage = 1


    fun getOrRequestFriends(recall: (list: MutableList<PacketS2CFriendListResponse.PlayerProfile>) -> Unit) {
        val player = MinecraftClient.getInstance().player ?: return
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


    fun tryFriendsSkinTexture(uuid: UUID): Pair<SkinTextures?, SkinTextures.Model?> {
        val client = client ?: return null to null
        val networkHandler = client.networkHandler
        val entry = networkHandler?.getPlayerListEntry(uuid)
        return entry?.skinTextures to entry?.skinTextures?.model()
    }

    override fun init() {
        val scale = 3.0 / client!!.options.guiScale.value
        val originX = width / 2
        addDrawableChild(
            ButtonWidget.Builder(Text.literal("下一页")) {
                if (maxPage > currentPage) {
                    currentPage++
                }
                flushWidget()
            }.dimensions(
                ((itemCardWidth * scale / 2 + originX + itemCardPadding * scale)).toInt(),
                ((itemCardHeight * 10 - itemCardPadding * 2) * scale).toInt(),
                (32 * scale).toInt(),
                (16 * scale).toInt()
            ).build()
        )
        addDrawableChild(
            ButtonWidget.Builder(Text.literal("上一页")) {
                if (currentPage > 1) {
                    currentPage--
                }
                flushWidget()
            }.dimensions(
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
        val scale = client!!.options.guiScale.value
        val resetScale = 3.0 / scale
        val originX = width / 2
        pageWidgets.onEach(::remove).clear()
        getOrRequestFriends {
            pageWidgets.add(
                addDrawableChild(
                    TextWidget(
                        (100 * resetScale).toInt(),
                        (20 * resetScale).toInt(),
                        Text.literal("朋友:${currentPage}/${maxPage}"),
                        client?.textRenderer
                    )
                        .apply {
                            this.x = ((originX - (50 * resetScale).roundToInt()))
                            this.y = (10)
                        }
                ))
            setCurrentPageIcons(it)
        }
    }

    private fun setCurrentPageIcons(currents: MutableList<PacketS2CFriendListResponse.PlayerProfile>) {
        val scale = 3.0 / client!!.options.guiScale.value
        val yStart = cardPadding
        val x = (width - (itemCardWidth * scale).toInt()) / 2
        val count = currents.size
        if (count == 0) return
        val yStep = (cardPadding * scale).toInt()
        repeat(count) {
            val profile = currents[it]
            pageWidgets.add(
                addDrawableChild(
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
            )
        }
    }

}