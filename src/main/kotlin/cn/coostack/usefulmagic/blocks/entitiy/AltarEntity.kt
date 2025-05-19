package cn.coostack.usefulmagic.blocks.entitiy

import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack

interface AltarEntity {
    companion object {
        /**
         * level 1 -> 50 1
         * level 2 -> 100 2 铁金红石
         * level 3 -> 150 2
         * level 4 -> 150 3 绿宝石
         * level 5 -> 200 3
         */
        val blockMapper = HashMap<Block, Int>()

        val levelMaxManaMapper = mutableMapOf(
            1 to 200,
            2 to 400,
            3 to 800,
            4 to 1000,
            5 to 1500
        )

        val levelManaReviveSpeedMapper = mutableMapOf(
            1 to 1,
            2 to 1,
            3 to 2,
            4 to 2,
            5 to 3,
        )

        fun getBlockMaxMana(block: Block): Int {
            val level = blockMapper[block] ?: return 0
            val revive = levelMaxManaMapper[level] ?: return 0
            return revive
        }

        fun getBlockRevive(block: Block): Int {
            val level = blockMapper[block] ?: return 0
            val revive = levelManaReviveSpeedMapper[level] ?: return 0
            return revive
        }

        fun registerMapper(block: Block, level: Int) {
            blockMapper[block] = level
        }

        init {
            registerMapper(
                Blocks.COAL_BLOCK, 1
            )
            registerMapper(
                Blocks.IRON_BLOCK, 2
            )
            registerMapper(
                Blocks.GOLD_BLOCK, 2
            )
            registerMapper(
                Blocks.REDSTONE_BLOCK, 2
            )
            registerMapper(
                Blocks.DIAMOND_BLOCK, 3
            )
            registerMapper(
                Blocks.EMERALD_BLOCK, 4
            )
            registerMapper(
                Blocks.NETHERITE_BLOCK, 5
            )
        }
    }
    fun getDownActiveBlocksMaxMana(): Int

    fun getDownActiveBlocksManaReviveSpeed(): Int

    fun getAltarStack(): ItemStack
    fun setAltarStack(stack: ItemStack)

}