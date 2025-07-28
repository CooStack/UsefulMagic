package cn.coostack.usefulmagic.gui.formation

import cn.coostack.usefulmagic.formation.api.FormationSettings
import cn.coostack.usefulmagic.managers.client.ClientRequestManager
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFormationSettingChangeRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFormationSettingRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationSettingsResponse
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.PlayerSkinWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class FormationSettingScreen(val clickPos: BlockPos, var settings: FormationSettings) :
    Screen(Text.literal("formation setting")) {
    val elements = ArrayList<Element>()
    lateinit var textField: TextFieldWidget
    override fun init() {
        flush(settings)
        super.init()
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
        elements.onEach(::remove).clear()
        val scaled = 3.0 / client!!.options.guiScale.value
        val originX = width / 2
        elements.add(
            addDrawableChild(
                TextWidget(
                    originX - (64 * scaled).toInt(),
                    (0 * scaled).toInt(),
                    128, 32,
                    Text.literal("阵法设置"),
                    textRenderer
                )
            )
        )
        elements.add(
            addDrawableChild(
                genToggleButton(
                    { settings.hostileEntityAttack = it },
                    { settings.hostileEntityAttack },
                    Text.literal(if (settings.hostileEntityAttack) "§a开启" else "§c关闭")
                ).dimensions(
                    originX + (32 * scaled).toInt(), (38 * scaled).toInt(),
                    (32 * scaled).toInt(), (16 * scaled).toInt()
                ).tooltip(
                    Tooltip.of(
                        Text.of(
                            """
                            开启此选项时, 阵法会隔绝所有敌对生物
                        """.trimIndent()
                        )
                    )
                )
                    .build()
            )
        )

        elements.add(
            addDrawableChild(
                TextWidget(
                    originX - (128 * scaled).toInt(),
                    (30 * scaled).toInt(),
                    (128 * scaled).toInt(), (32 * scaled).toInt(),
                    Text.literal("阵法攻击敌对生物"),
                    textRenderer
                )
            )
        )

        elements.add(
            addDrawableChild(
                genToggleButton(
                    { settings.playerEntityAttack = it },
                    { settings.playerEntityAttack },
                    Text.literal(if (settings.playerEntityAttack) "§a开启" else "§c关闭")
                ).dimensions(
                    originX + (32 * scaled).toInt(), (60 * scaled).toInt(),
                    (32 * scaled).toInt(), (16 * scaled).toInt()
                ).tooltip(
                    Tooltip.of(
                        Text.of(
                            """
                            开启此选项时, 阵法会攻击所有非朋友玩家
                        """.trimIndent()
                        )
                    )
                )
                    .build()
            )
        )

        elements.add(
            addDrawableChild(
                TextWidget(
                    originX - (128 * scaled).toInt(),
                    (54 * scaled).toInt(),
                    (128 * scaled).toInt(), (32 * scaled).toInt(),
                    Text.literal("阵法攻击玩家"),
                    textRenderer
                )
            )
        )

        elements.add(
            addDrawableChild(
                genToggleButton(
                    { settings.animalEntityAttack = it },
                    { settings.animalEntityAttack },
                    Text.literal(if (settings.animalEntityAttack) "§a开启" else "§c关闭")
                ).dimensions(
                    originX + (32 * scaled).toInt(), (82 * scaled).toInt(),
                    (32 * scaled).toInt(), (16 * scaled).toInt()
                ).tooltip(
                    Tooltip.of(
                        Text.of(
                            """
                            开启此选项时, 阵法会隔绝所有动物实体
                        """.trimIndent()
                        )
                    )
                )
                    .build()
            )
        )

        elements.add(
            addDrawableChild(
                TextWidget(
                    originX - (128 * scaled).toInt(),
                    (76 * scaled).toInt(),
                    (128 * scaled).toInt(), (32 * scaled).toInt(),
                    Text.literal("阵法攻击动物"),
                    textRenderer
                )
            )
        )

        elements.add(
            addDrawableChild(
                genToggleButton(
                    { settings.anotherEntityAttack = it },
                    { settings.anotherEntityAttack },
                    Text.literal(if (settings.anotherEntityAttack) "§a开启" else "§c关闭")
                ).dimensions(
                    originX + (32 * scaled).toInt(), (104 * scaled).toInt(),
                    (32 * scaled).toInt(), (16 * scaled).toInt()
                ).tooltip(
                    Tooltip.of(
                        Text.of(
                            """
                            开启此选项时, 如果阵法没有识别出实体的类型具体(前三项),则会攻击
                        """.trimIndent()
                        )
                    )
                )
                    .build()
            )
        )

        elements.add(
            addDrawableChild(
                TextWidget(
                    originX - (128 * scaled).toInt(),
                    (98 * scaled).toInt(),
                    (128 * scaled).toInt(), (32 * scaled).toInt(),
                    Text.literal("阵法攻击其他实体"),
                    textRenderer
                )
            )
        )

        elements.add(
            addDrawableChild(
                genToggleButton(
                    { settings.displayParticleOnlyTrigger = it },
                    { settings.displayParticleOnlyTrigger },
                    Text.literal(if (settings.displayParticleOnlyTrigger) "§c关闭" else "§a开启")
                ).dimensions(
                    originX + (32 * scaled).toInt(), (126 * scaled).toInt(),
                    (32 * scaled).toInt(), (16 * scaled).toInt()
                ).tooltip(
                    Tooltip.of(
                        Text.of(
                            """
                            开启此选项时, 阵法的粒子只会在阵法被激活时展示
                            在阵法没有被激活后6秒则会消失
                        """.trimIndent()
                        )
                    )
                )
                    .build()
            )
        )

        elements.add(
            addDrawableChild(
                TextWidget(
                    originX - (128 * scaled).toInt(),
                    (120 * scaled).toInt(),
                    (128 * scaled).toInt(), (32 * scaled).toInt(),
                    Text.literal("阵法粒子始终显示"),
                    textRenderer
                )
            )
        )
        textField = TextFieldWidget(
            textRenderer, originX + (32 * scaled).toInt(), (148 * scaled).toInt(),
            (32 * scaled).toInt(), (16 * scaled).toInt(), Text.literal("${settings.triggerRange}")
        )
        textField.tooltip = Tooltip.of(
            Text.of(
                """
                            阵法的触发范围
                            在阵法的有效范围内,可以控制阵法触发
                            当实体进入设定的触发范围时,阵法激活
                            直到实体离开阵法的有效范范围为止
                            设定为-1则代表触发范围和有效范围相同
                        """.trimIndent()
            )
        )
        textField.text = "${settings.triggerRange}"
        textField.setTextPredicate {
            it.toDoubleOrNull() != null || it == "-"
        }
        textField.setChangedListener {
            var num = it.toDoubleOrNull() ?: -1.0
            if (it == "-") {
                textField.text = "-1.0"
                settings.triggerRange = -1.0
            } else {
                if (num < 0) num = -1.0
                settings.triggerRange = num
            }
        }

        elements.add(
            addDrawableChild(
                textField
            )
        )

        elements.add(
            addDrawableChild(
                TextWidget(
                    originX - (128 * scaled).toInt(),
                    (142 * scaled).toInt(),
                    (128 * scaled).toInt(), (32 * scaled).toInt(),
                    Text.literal("阵法生效范围"),
                    textRenderer
                )
            )
        )

    }

    private fun genToggleButton(
        toggleMethod: (Boolean) -> Unit,
        getMethod: () -> Boolean,
        text: Text
    ): ButtonWidget.Builder {
        return ButtonWidget.builder(text) {
            val now = getMethod()
            toggleMethod(!now)
            ClientRequestManager.sendRequest(
                PacketC2SFormationSettingChangeRequest(clickPos, settings), PacketS2CFormationSettingsResponse.payloadID
            ).recall {
                flush(settings)
            }
        }
    }

    override fun close() {
        ClientRequestManager.sendRequest(
            PacketC2SFormationSettingChangeRequest(clickPos, settings), PacketS2CFormationSettingsResponse.payloadID
        ).recall {
            flush(settings)
        }
        super.close()
    }

}