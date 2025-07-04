package cn.coostack.usefulmagic.gui

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.gui.widget.BetterTextWidget
import cn.coostack.usefulmagic.gui.widget.button.ItemTextureButton
import net.minecraft.block.Blocks
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AltarTutorialBookScreen(parent: StructureTutorialBookMainScreen) : StructureTutorialBookMainScreen(parent) {
    val pageRender = ArrayList<AltarTutorialBookScreen.(context: DrawContext) -> Unit>()
    val pageWidgets = ArrayList<AltarTutorialBookScreen.() -> Unit>()
    private fun getStepTexture(step: Int): Identifier {
        return Identifier.of(
            UsefulMagic.MOD_ID, "textures/gui/steps/step${step}.png"
        )
    }

    override fun init() {
        initPages()
        initPageWidgets()
        super.init()
    }

    override fun initTypeIcons() {
        val startX = getTypedIconOriginX() + 5
        val startY = getTypedIconOriginY() + 60
        addDrawable(
            TextWidget(
                startX + 20,
                getTypedIconOriginY() + 40,
                200,
                16,
                Text.literal("注魔祭坛支持方块对照表"),
                client!!.textRenderer
            ).setTextColor(0xFFc5b091U.toInt())
                .alignLeft()
        )

        var currentX = startX
        var currentY = startY
        val stepX = 35
        val stepY = 35

        val maxX = width / 2 - 20
        val maxY = height - 20

        val player = client?.player ?: return
        val world = player.world ?: return

        fun nextCurrentX(): Int {
            val old = currentX
            currentX += stepX
            return old
        }

        fun nextCurrentY(): Int {
            val old = currentY
            if (currentX > maxX) {
                currentX = startX
                currentY += stepY
            }
            return old
        }
        addDrawable(
            ItemTextureButton(
                nextCurrentX(), nextCurrentY(), 32, 32, Blocks.COAL_BLOCK.asItem().defaultStack
            ) { btn -> }
        ).apply {
            scale = 2f
            tooltip = Tooltip.of(
                Text.literal(
                    """
                §f煤炭块
                §7 每个方块提供的魔力存储量: 200/方块
                §7 每个方块提供的魔力恢复速度: 1点/秒/方块
            """.trimIndent()
                )
            )
        }
        addDrawable(
            ItemTextureButton(
                nextCurrentX(), nextCurrentY(), 32, 32, Blocks.IRON_BLOCK.asItem().defaultStack
            ) { btn -> }
        ).apply {
            scale = 2f
            tooltip = Tooltip.of(
                Text.literal(
                    """
                §f铁块
                §7 每个方块提供的魔力存储量: 400/方块
                §7 每个方块提供的魔力恢复速度: 1点/秒/方块
            """.trimIndent()
                )
            )
        }
        addDrawable(
            ItemTextureButton(
                nextCurrentX(), nextCurrentY(), 32, 32, Blocks.GOLD_BLOCK.asItem().defaultStack
            ) { btn -> }
        ).apply {
            scale = 2f
            tooltip = Tooltip.of(
                Text.literal(
                    """
                §f金块
                §7 每个方块提供的魔力存储量: 400/方块
                §7 每个方块提供的魔力恢复速度: 1点/秒/方块
            """.trimIndent()
                )
            )
        }
        addDrawable(
            ItemTextureButton(
                nextCurrentX(), nextCurrentY(), 32, 32, Blocks.REDSTONE_BLOCK.asItem().defaultStack
            ) { btn -> }
        ).apply {
            scale = 2f
            tooltip = Tooltip.of(
                Text.literal(
                    """
                §f红石块
                §7 每个方块提供的魔力存储量: 400/方块
                §7 每个方块提供的魔力恢复速度: 1点/秒/方块
            """.trimIndent()
                )
            )
        }
        addDrawable(
            ItemTextureButton(
                nextCurrentX(), nextCurrentY(), 32, 32, Blocks.DIAMOND_BLOCK.asItem().defaultStack
            ) { btn -> }
        ).apply {
            scale = 2f
            tooltip = Tooltip.of(
                Text.literal(
                    """
                §f钻石块
                §7 每个方块提供的魔力存储量: 800/方块
                §7 每个方块提供的魔力恢复速度: 2点/秒/方块
            """.trimIndent()
                )
            )
        }
        addDrawable(
            ItemTextureButton(
                nextCurrentX(), nextCurrentY(), 32, 32, Blocks.EMERALD_BLOCK.asItem().defaultStack
            ) { btn -> }
        ).apply {
            scale = 2f
            tooltip = Tooltip.of(
                Text.literal(
                    """
                §f钻石块
                §7 每个方块提供的魔力存储量: 1000/方块
                §7 每个方块提供的魔力恢复速度: 2点/秒/方块
            """.trimIndent()
                )
            )
        }
        addDrawable(
            ItemTextureButton(
                nextCurrentX(), nextCurrentY(), 32, 32, Blocks.NETHERITE_BLOCK.asItem().defaultStack
            ) { btn -> }
        ).apply {
            scale = 2f
            tooltip = Tooltip.of(
                Text.literal(
                    """
                §f下界合金块
                §7 每个方块提供的魔力存储量: 1500/方块
                §7 每个方块提供的魔力恢复速度: 3点/秒/方块
            """.trimIndent()
                )
            )
        }
    }

    private fun initPages() {
        if (pageRender.isNotEmpty()) return
        val contentX = getContentIconOriginX()
        val contentY = getContentIconOriginY()
        pageRender.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000U.toInt())
            it.drawTexture(getStepTexture(1), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
        pageRender.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000U.toInt())
            it.drawTexture(getStepTexture(2), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
        pageRender.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000U.toInt())
            it.drawTexture(getStepTexture(3), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
        pageRender.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000U.toInt())
            it.drawTexture(getStepTexture(4), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
        pageRender.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000U.toInt())
            it.drawTexture(getStepTexture(5), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
        pageRender.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 77, 0x8F000000U.toInt())
            it.drawTexture(getStepTexture(6), contentX, contentY, 0F, 0F, 150, 75, 150, 75)
        }
    }

    private fun initPageWidgets() {
        if (pageWidgets.isNotEmpty()) return
        val contentX = getContentIconOriginX()
        val contentY = getContentIconOriginY()
        val textRenderer = client!!.textRenderer
        pageWidgets.add {
            addDrawable(
                BetterTextWidget(contentX, contentY + 120, 140, 16 * 6)
                    .apply {
                        shadow = false
                        scaled = 1.2f
                        heightPreLine = 12
                        textColor = 0xFFc5b091u.toInt()
                        texts.add(
                            Text.literal(
                                "首先找个地方,放置注魔输出平台"
                            )
                        )
                    })
        }
        pageWidgets.add {
            addDrawable(
                BetterTextWidget(contentX, contentY + 120, 140, 16 * 6)
                    .apply {
                        shadow = false
                        scaled = 1.2f
                        heightPreLine = 12
                        textColor = 0xFFc5b091u.toInt()
                        texts.add(
                            Text.literal(
                                "在周围放上8个注魔祭坛方块"
                            )
                        )
                    })
        }
        pageWidgets.add {
            addDrawable(
                BetterTextWidget(contentX, contentY + 120, 140, 16 * 6)
                    .apply {
                        shadow = false
                        scaled = 1.2f
                        heightPreLine = 12
                        textColor = 0xFFc5b091u.toInt()
                        texts.add(
                            Text.literal(
                                "在输出平台的上面第3个方块放置"
                            )
                        )
                        texts.add(
                            Text.literal(
                                "注魔祭坛核心"
                            )
                        )
                    })
        }
        pageWidgets.add {
            addDrawable(
                BetterTextWidget(contentX, contentY + 120, 140, 16 * 6)
                    .apply {
                        shadow = false
                        scaled = 1.2f
                        heightPreLine = 12
                        textColor = 0xFFc5b091u.toInt()
                        texts.add(Text.literal("给下面的9个平台放置金属块"))
                        texts.add(Text.literal("支持的金属块可以在左侧查看"))
                        texts.add(Text.literal("每一个平台可以在下面支持"))
                        texts.add(Text.literal("放置最多10个金属块"))
                    }
            )
        }
        pageWidgets.add {
            addDrawable(
                BetterTextWidget(contentX, contentY + 120, 140, 16 * 6)
                    .apply {
                        shadow = false
                        scaled = 1.2f
                        heightPreLine = 12
                        textColor = 0xFFc5b091u.toInt()
                        texts.add(Text.literal("成功激活时会出现粒子"))
                    }
            )
        }
        pageWidgets.add {
            addDrawable(
                BetterTextWidget(contentX, contentY + 80, 140, 16 * 6)
                    .apply {
                        shadow = false
                        scaled = 1.2f
                        heightPreLine = 12
                        textColor = 0xFFc5b091u.toInt()
                        texts.add(Text.literal("右键注魔祭坛核心可以查看信息"))
                    }
            )
        }

    }

    var page = 0
    override fun initContentIcons() {
        pageWidgets.getOrNull(page)?.invoke(this)
    }

    override fun nextPage() {
        if (page == pageRender.size - 1) return
        page++
        clearAndInit()
    }

    override fun prevPage() {
        if (page == 0) return
        page--
        clearAndInit()
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (pageRender.isNotEmpty()) {
            pageRender[page](context)
        }
    }

}