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

class FriendItemWidget(
    val profile: PacketS2CFriendListResponse.PlayerProfile,
    val skinTexture: PlayerSkin?,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    val fatherScreen: FriendManagerScreen,
) : AbstractWidget(x, y, width, height, Component.literal("")) {

    companion object {
        const val DELETE_BUTTON_WIDTH = 58
        const val DELETE_BUTTON_HEIGHT = 18
    }

    private val deleteButtonBuilder = Button.builder(Component.translatable("screen.friend_manager.remove")) {
        val client = Minecraft.getInstance()
        val player = client.player ?: return@builder
        ClientRequestManager.sendRequest(
            PacketC2SFriendRemoveRequest(player.uuid, profile.uuid),
            PacketS2CFriendChangeResponse.payloadID
        ).recall {
            client.player?.sendSystemMessage(
                Component.translatable("screen.friend_manager.removed", profile.name)
            )
            fatherScreen.flushWidget()
        }
    }

    private lateinit var deleteButton: Button

    override fun renderWidget(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        val hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height
        val borderColor = if (hovered) 0xFF5FA188u.toInt() else 0xFF3C646Au.toInt()
        val backgroundColor = if (hovered) 0xE0243539u.toInt() else 0xCC162327u.toInt()
        val innerColor = if (hovered) 0xD01A2A2Eu.toInt() else 0xC3101A1Du.toInt()

        graphics.fill(x, y, x + width, y + height, borderColor)
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, backgroundColor)
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, innerColor)

        if (!::deleteButton.isInitialized) {
            deleteButton = deleteButtonBuilder.bounds(0, 0, DELETE_BUTTON_WIDTH, DELETE_BUTTON_HEIGHT).build()
        }

        val buttonX = x + width - DELETE_BUTTON_WIDTH - 8
        val buttonY = y + (height - DELETE_BUTTON_HEIGHT) / 2
        deleteButton.setPosition(buttonX, buttonY)

        val avatarSize = (height - 8).coerceAtLeast(24)
        val avatarX = x + 6
        val avatarY = y + (height - avatarSize) / 2
        graphics.fill(avatarX - 1, avatarY - 1, avatarX + avatarSize + 1, avatarY + avatarSize + 1, 0xFF3F6A70u.toInt())
        graphics.fill(avatarX, avatarY, avatarX + avatarSize, avatarY + avatarSize, 0xFF122126u.toInt())

        skinTexture?.let {
            val scale = avatarSize / 32f
            val matrix = graphics.pose()
            matrix.pushPose()
            matrix.translate(avatarX.toFloat(), avatarY.toFloat(), 0f)
            matrix.scale(scale, scale, 1f)
            graphics.blit(it.texture, 0, 0, 32, 32, 32, 32)
            matrix.popPose()
        }

        val client = Minecraft.getInstance()
        val font = client.font
        val nameX = avatarX + avatarSize + 8
        val nameY = y + (height - font.lineHeight) / 2
        val availableTextWidth = (buttonX - 8) - nameX
        val nameText = if (availableTextWidth > 8) {
            val raw = profile.name
            if (font.width(raw) > availableTextWidth) {
                font.plainSubstrByWidth(raw, availableTextWidth - font.width("...")) + "..."
            } else {
                raw
            }
        } else {
            ""
        }

        graphics.drawString(
            font,
            Component.literal(nameText),
            nameX,
            nameY,
            if (hovered) 0xFFF0F7F6u.toInt() else 0xFFD3E2E0u.toInt(),
            false
        )

        deleteButton.render(graphics, mouseX, mouseY, delta)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return deleteButton.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button)
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) {
    }
}
