package cn.coostack.usefulmagic.gui.friend.widget

import cn.coostack.usefulmagic.gui.friend.FriendManagerScreen
import cn.coostack.usefulmagic.managers.client.ClientRequestManager
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendRemoveRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendChangeResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.resources.PlayerSkin
import net.minecraft.network.chat.Component
import java.util.function.Consumer

class FriendItemWidget(
    val profile: PacketS2CFriendListResponse.PlayerProfile,
    val skinTexture: PlayerSkin?,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    val fatherScreen: FriendManagerScreen,
) :
    AbstractWidget(x, y, width, height, Component.literal("")) {

    val deleteButtonBuilder = Button.Builder(Component.literal("删除")) {
        // 发包
        val client = Minecraft.getInstance()
        ClientRequestManager.sendRequest(
            PacketC2SFriendRemoveRequest(client.player!!.uuid, profile.uuid),
            PacketS2CFriendChangeResponse.payloadID
        ).recall {
            client.player?.sendSystemMessage(Component.literal("你删除了${profile.name}的朋友气息"))
            fatherScreen.flushWidget()
        }
    }

    lateinit var deleteButton: Button


    override fun renderWidget(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        val matrix = graphics.pose()
        // border
        graphics.fill(
            x, y, x + width, y + height, 0XFF171525U.toInt(),
        )
        val client = Minecraft.getInstance()
        val uiSize = 3f / client.options.guiScale().get()

        if (!::deleteButton.isInitialized) {
            deleteButton = deleteButtonBuilder.bounds(
                x + width - (42 * uiSize).toInt(),
                y + (height - (16 * uiSize).toInt()) / 2,
                (32 * uiSize).toInt(), (16 * uiSize).toInt()
            ).build()
        }

        skinTexture?.let {
            matrix.pushPose()
            val texture = it
            val scale = 0.8f * uiSize
            val px = x + 5f
            val py = y + (height - 32 * uiSize) / 2f
            matrix.scale(scale, scale, 1f)
            matrix.translate(
                px / scale, py / scale, 0f
            )

            graphics.blit(texture.texture, 0, 0, 32, 32, 32, 32)
            matrix.popPose()
        }
        val scale = 1.2f * uiSize
        val infoX = x + 52 * scale
        val infoY = y + (height - 16 * scale) / 2f
        val name = profile.name
        matrix.pushPose()
        matrix.scale(scale, scale, 1f)
        matrix.translate(infoX.toFloat() / scale, infoY.toFloat() / scale, 0f)
        graphics.drawString(
            Minecraft.getInstance().font,
            Component.literal(name),
            0,
            0,
            0xFFFFFFFFu.toInt(),
            false
        )
        matrix.popPose()
        deleteButton.render(graphics, mouseX, mouseY, delta)
    }


    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return deleteButton.mouseClicked(mouseX, mouseY, button)
    }

    override fun updateWidgetNarration(p0: NarrationElementOutput) {
    }

    private fun checkInRange(widget: AbstractWidget, x: Double, y: Double): Boolean {
        val startX = widget.x + 0.0
        val endX = widget.x + widget.width + 0.0
        val startY = widget.y + 0.0
        val endY = widget.y + widget.height + .0
        return x in startX..endX && y in startY..endY
    }


}