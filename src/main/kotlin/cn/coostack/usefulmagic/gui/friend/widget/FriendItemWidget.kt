package cn.coostack.usefulmagic.gui.friend.widget

import cn.coostack.usefulmagic.gui.friend.FriendManagerScreen
import cn.coostack.usefulmagic.managers.client.ClientRequestManager
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendRemoveRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendChangeResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.Widget
import net.minecraft.client.util.SkinTextures
import net.minecraft.text.Text
import java.util.function.Consumer

class FriendItemWidget(
    val profile: PacketS2CFriendListResponse.PlayerProfile,
    val skinTexture: SkinTextures?,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    val fatherScreen: FriendManagerScreen,
) :
    ClickableWidget(x, y, width, height, Text.literal("")) {

    val deleteButtonBuilder = ButtonWidget.Builder(Text.literal("删除")) {
        // 发包
        val client = MinecraftClient.getInstance()
        ClientRequestManager.sendRequest(
            PacketC2SFriendRemoveRequest(client.player!!.uuid, profile.uuid),
            PacketS2CFriendChangeResponse.payloadID
        ).recall {
            client.player?.sendMessage(Text.literal("你删除了${profile.name}的朋友气息"))
            fatherScreen.flushWidget()
        }
    }

    lateinit var deleteButton: ButtonWidget


    override fun renderWidget(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        val matrix = context.matrices
        // border
        context.fill(
            x, y, x + width, y + height, 0XFF171525U.toInt(),
        )
        val client = MinecraftClient.getInstance()
        val uiSize = 3f / client.options.guiScale.value

        if (!::deleteButton.isInitialized) {
            deleteButton = deleteButtonBuilder.dimensions(
                x + width - (42 * uiSize).toInt(),
                y + (height - (16 * uiSize).toInt()) / 2,
                (32 * uiSize).toInt(), (16 * uiSize).toInt()
            ).build()
        }

        skinTexture?.let {
            matrix.push()
            val texture = it.texture()
            val scale = 0.8f * uiSize
            val px = x + 5f
            val py = y + (height - 32 * uiSize) / 2f
            matrix.scale(scale, scale, 1f)
            matrix.translate(
                px / scale, py / scale, 0f
            )
            context.drawTexture(texture, 0, 0, 32, 32, 32, 32)
            matrix.pop()
        }
        val scale = 1.2f * uiSize
        val infoX = x + 52 * scale
        val infoY = y + (height - 16 * scale) / 2f
        val name = profile.name
        matrix.push()
        matrix.scale(scale, scale, 1f)
        matrix.translate(infoX.toFloat() / scale, infoY.toFloat() / scale, 0f)
        context.drawText(
            MinecraftClient.getInstance().textRenderer,
            Text.literal(name),
            0,
            0,
            0xFFFFFFFFu.toInt(),
            false
        )
        matrix.pop()
        deleteButton.render(context, mouseX, mouseY, delta)
    }


    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return deleteButton.mouseClicked(mouseX, mouseY, button)
    }

    private fun checkInRange(widget: Widget, x: Double, y: Double): Boolean {
        val startX = widget.x + 0.0
        val endX = widget.x + widget.width + 0.0
        val startY = widget.y + 0.0
        val endY = widget.y + widget.height + .0
        return x in startX..endX && y in startY..endY
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
    }


}