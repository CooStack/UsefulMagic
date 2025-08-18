package cn.coostack.usefulmagic.gui.formation

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.formation.api.FormationSettings
import cn.coostack.usefulmagic.managers.client.ClientRequestManager
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFormationSettingChangeRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFormationSettingRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationSettingsResponse
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.realms.RealmsLabel

class FormationSettingScreen(val clickPos: BlockPos, var settings: FormationSettings) :
    Screen(Component.literal("formation-setting")) {
    lateinit var textField: EditBox
    val client: Minecraft
        get() = Minecraft.getInstance()

    override fun init() {
        UsefulMagic.logger.debug("try flushing buttons")
        flush(settings)
        super.init()
        UsefulMagic.logger.debug("init finished")
    }


    private fun request() {
        ClientRequestManager.sendRequest(
            PacketC2SFormationSettingRequest(clickPos), PacketS2CFormationSettingsResponse.payloadID
        ).recall {
            it as PacketS2CFormationSettingsResponse
            flush(it.settings)
        }
    }

    private fun flush(data: FormationSettings) {
        this.settings = data
        clearWidgets()
        var v = client.options.guiScale().get()
        v = if (v == 0) {
            3
        } else {
            v
        }
        val font = Minecraft.getInstance().font
        // 基本位置然后转移到居中的Y轴偏移
        // 16 是按钮的高
        val alignCenterOffset = (16 + font.lineHeight) / 2
        val scaled = 3.0 / v
        val originX = width / 2
        addRenderableOnly(
            RealmsLabel(
                Component.literal("阵法设置"),
//                originX - (64 * scaled).toInt(),
                originX,
                (alignCenterOffset * scaled).toInt(),
                0xFFFFFFFFU.toInt()
            )
        )
        addRenderableWidget(
            genToggleButton(
                { settings.hostileEntityAttack = it },
                { settings.hostileEntityAttack },
                Component.literal(if (settings.hostileEntityAttack) "§a开启" else "§c关闭")
            ).bounds(
                originX + (64 * scaled).toInt(),
                (38 * scaled).toInt(),
                (32 * scaled).toInt(), (16 * scaled).toInt()
            ).tooltip(
                Tooltip.create(
                    Component.literal(
                        """
                            开启此选项时, 阵法会隔绝所有敌对生物
                        """.trimIndent()
                    )
                )
            )
                .build()
        )

        addRenderableOnly(
            RealmsLabel(
                Component.literal("阵法攻击敌对生物"),
//                originX - (128 * scaled).toInt(),
                originX - (64 * scaled).toInt(),
                ((30 + alignCenterOffset) * scaled).toInt(),
                0xFFFFFFFFU.toInt()
//                    (128 * scaled).toInt(), (32 * scaled).toInt(),
//                    textRenderer
            )
        )

        addRenderableWidget(
            genToggleButton(
                { settings.playerEntityAttack = it },
                { settings.playerEntityAttack },
                Component.literal(if (settings.playerEntityAttack) "§a开启" else "§c关闭")
            ).bounds(
                originX + (64 * scaled).toInt(),
//                originX,
                (60 * scaled).toInt(),
                (32 * scaled).toInt(), (16 * scaled).toInt()
            ).tooltip(
                Tooltip.create(
                    Component.literal(
                        """
                            开启此选项时, 阵法会攻击所有非朋友玩家
                        """.trimIndent()
                    )
                )
            )
                .build()
        )

        addRenderableOnly(
            RealmsLabel(
                Component.literal("阵法攻击玩家"),
//                originX - (128 * scaled).toInt(),
                originX - (64 * scaled).toInt(),
                ((54 + alignCenterOffset) * scaled).toInt(),
                0xFFFFFFFFU.toInt()
//                    (128 * scaled).toInt(), (32 * scaled).toInt(),
//                    textRenderer
            )
        )

        addRenderableWidget(
            genToggleButton(
                { settings.animalEntityAttack = it },
                { settings.animalEntityAttack },
                Component.literal(if (settings.animalEntityAttack) "§a开启" else "§c关闭")
            ).bounds(
                originX + (64 * scaled).toInt(),
//                originX,
                (82 * scaled).toInt(),
                (32 * scaled).toInt(), (16 * scaled).toInt()
            ).tooltip(
                Tooltip.create(
                    Component.literal(
                        """
                            开启此选项时, 阵法会隔绝所有动物实体
                        """.trimIndent()
                    )
                )
            )
                .build()
        )

        addRenderableOnly(
            RealmsLabel(
                Component.literal("阵法攻击动物"),
                originX - (64 * scaled).toInt(),
                ((76 + alignCenterOffset) * scaled).toInt(),
                0xFFFFFFFFU.toInt()
//                    (128 * scaled).toInt(), (32 * scaled).toInt(),
//                    textRenderer
            )
        )
        addRenderableWidget(
            genToggleButton(
                { settings.anotherEntityAttack = it },
                { settings.anotherEntityAttack },
                Component.literal(if (settings.anotherEntityAttack) "§a开启" else "§c关闭")
            ).bounds(
                originX + (64 * scaled).toInt(),
                ((104) * scaled).toInt(),
                (32 * scaled).toInt(), (16 * scaled).toInt()
            ).tooltip(
                Tooltip.create(
                    Component.literal(
                        """
                            开启此选项时, 如果阵法没有识别出实体的类型具体(前三项),则会攻击
                        """.trimIndent()
                    )
                )
            )
                .build()
        )

        addRenderableOnly(
            RealmsLabel(
                Component.literal("阵法攻击其他实体"),
                originX - (64 * scaled).toInt(),
                ((98 + alignCenterOffset) * scaled).toInt(),
                0xFFFFFFFFu.toInt()
//                    (128 * scaled).toInt(), (32 * scaled).toInt(),
//                            textRenderer
            )
        )

        addRenderableWidget(
            genToggleButton(
                { settings.displayParticleOnlyTrigger = it },
                { settings.displayParticleOnlyTrigger },
                Component.literal(if (settings.displayParticleOnlyTrigger) "§c关闭" else "§a开启")
            ).bounds(
                originX + (64 * scaled).toInt(),
                (126 * scaled).toInt(),
                (32 * scaled).toInt(), (16 * scaled).toInt()
            ).tooltip(
                Tooltip.create(
                    Component.literal(
                        """
                            开启此选项时, 阵法的粒子只会在阵法被激活时展示
                            在阵法没有被激活后6秒则会消失
                        """.trimIndent()
                    )
                )
            )
                .build()
        )

        addRenderableOnly(
            RealmsLabel(
                Component.literal("阵法粒子始终显示"),
                originX - (64 * scaled).toInt(),
                ((120 + alignCenterOffset) * scaled).toInt(),
                0xFFFFFFFFU.toInt()
//                    (128 * scaled).toInt(), (32 * scaled).toInt(),
            )
        )
        textField = EditBox(
            font, originX + (64 * scaled).toInt(),
            (148 * scaled).toInt(),
            (32 * scaled).toInt(), (16 * scaled).toInt(), Component.literal("${settings.triggerRange}")
        )
        textField.tooltip = Tooltip.create(
            Component.literal(
                """
                            阵法的触发范围
                            在阵法的有效范围内,可以控制阵法触发
                            当实体进入设定的触发范围时,阵法激活
                            直到实体离开阵法的有效范范围为止
                            设定为-1则代表触发范围和有效范围相同
                        """.trimIndent()
            )
        )
        textField.value = "${settings.triggerRange}"
        textField.setFilter { it.toDoubleOrNull() != null || it == "-" }
        textField.setResponder { text ->
            var num = text.toDoubleOrNull() ?: -1.0
            if (text == "-") {
                textField.value = "-1.0"      // NeoForge 用 .value 替代 .text
                settings.triggerRange = -1.0
            } else {
                if (num < 0) num = -1.0
                settings.triggerRange = num
            }
        }

        addRenderableWidget(
            textField
        )

        addRenderableOnly(
            RealmsLabel(
                Component.literal("阵法生效范围"),
                originX - (64 * scaled).toInt(),
                ((142 + alignCenterOffset) * scaled).toInt(),
                0xFFFFFFFFU.toInt()
//                    (128 * scaled).toInt(), (32 * scaled).toInt(),
//                    textRenderer
            )
        )
        UsefulMagic.logger.debug("FLUSH FINISHED")
    }

    private fun genToggleButton(
        toggleMethod: (Boolean) -> Unit,
        getMethod: () -> Boolean,
        text: Component
    ): Button.Builder {
        return Button.builder(text) {
            val now = getMethod()
            toggleMethod(!now)
            ClientRequestManager.sendRequest(
                PacketC2SFormationSettingChangeRequest(clickPos, settings), PacketS2CFormationSettingsResponse.payloadID
            ).recall {
                Minecraft.getInstance().execute {
                    flush(settings)
                }
            }
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun onClose() {
        ClientRequestManager.sendRequest(
            PacketC2SFormationSettingChangeRequest(clickPos, settings), PacketS2CFormationSettingsResponse.payloadID
        ).recall {
            Minecraft.getInstance().execute {
                flush(settings)
            }
        }
        super.onClose()
    }


}