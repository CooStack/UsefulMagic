package cn.coostack.usefulmagic.gui.guildbook

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.gui.guildbook.widget.button.ItemTextureButton
import net.minecraft.block.Blocks
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class FormationTutorialBookScreen(parent: StructureTutorialBookMainScreen) : StructureTutorialBookMainScreen(parent) {

    private val formationPictureRenders = ArrayList<FormationTutorialBookScreen.(DrawContext) -> Unit>()
    private var current = -1
    override fun init() {
        initPage()
        super.init()
    }

    override fun initTypeIcons() {
        val startX = getTypedIconOriginX() + 5
        val startY = getTypedIconOriginY() + 60
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
        addDrawableChild(
            ItemTextureButton(
                nextCurrentX(), nextCurrentY(), 32, 32, UsefulMagicBlocks.ENERGY_CRYSTAL_BLOCK.asItem().defaultStack
            ) { btn ->
                current = 0
            }
        ).apply {
            scale = 2f
            tooltip = Tooltip.of(
                Text.literal(
                    """
                §f小型阵法
                §7 组成条件如下
                §7 至少有一个能源水晶
                §7 至少有一个功能水晶(攻击或者防御)
                §7 阵法有效半径 32格
                §7 搭建完成时,空手右键阵基
                §f点击查看搭建方式
            """.trimIndent()
                )
            )
        }
        addDrawableChild(
            ItemTextureButton(
                nextCurrentX(), nextCurrentY(), 32, 32, UsefulMagicBlocks.DEFEND_CRYSTAL_BLOCK.asItem().defaultStack
            ) { btn ->
                current = 1
            }
        ).apply {
            scale = 2f
            tooltip = Tooltip.of(
                Text.literal(
                    """
                §f中型阵法
                §7 搭建时 首先要达成小型阵法的条件
                §7 阵法有效半径 64格
                §7 搭建完成时,空手右键阵基
                §f点击查看搭建方式
            """.trimIndent()
                )
            )
        }
        addDrawableChild(
            ItemTextureButton(
                nextCurrentX(),
                nextCurrentY(),
                32,
                32,
                UsefulMagicBlocks.SWORD_ATTACK_CRYSTAL_BLOCK.asItem().defaultStack
            ) { btn ->
                current = 2
            }
        ).apply {
            scale = 2f
            tooltip = Tooltip.of(
                Text.literal(
                    """
                §f大型阵法
                §7 搭建时 首先要达成中型阵法的条件
                §7 阵法有效半径 128格
                §7 搭建完成时,空手右键阵基
                §f点击查看搭建方式
            """.trimIndent()
                )
            )
        }
    }


    private fun initPage() {
        if (formationPictureRenders.isNotEmpty()) return
        val contentX = getContentIconOriginX()
        val contentY = getContentIconOriginY()

        fun texture(name: String): Identifier {
            return Identifier.of(UsefulMagic.MOD_ID, "textures/gui/formation/$name")
        }

        formationPictureRenders.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000U.toInt())
            it.drawTexture(texture("small_formation.png"), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
        formationPictureRenders.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000U.toInt())
            it.drawTexture(texture("mid_formation.png"), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
        formationPictureRenders.add {
            it.fill(contentX - 2, contentY - 2, contentX + 152, contentY + 117, 0x8F000000U.toInt())
            it.drawTexture(texture("large_formation.png"), contentX, contentY, 0F, 0F, 150, 115, 150, 115)
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (current in formationPictureRenders.indices) {
            formationPictureRenders[current](context)
        }

    }
}