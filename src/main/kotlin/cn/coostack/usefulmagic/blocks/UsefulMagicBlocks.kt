package cn.coostack.usefulmagic.blocks

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.formation.DefendCrystalBlock
import cn.coostack.usefulmagic.blocks.formation.EnergyCrystalsBlock
import cn.coostack.usefulmagic.blocks.formation.FormationCoreBlock
import cn.coostack.usefulmagic.blocks.formation.RecoverCrystalBlock
import cn.coostack.usefulmagic.blocks.formation.SwordAttackCrystalsBlock
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object UsefulMagicBlocks {
    val blocks = ArrayList<Block>()

    @JvmStatic
    val ALTAR_BLOCK = register(
        "altar_block", AltarBlock(
            AbstractBlock.Settings.create()
                .nonOpaque()
                .strength(5f, 1200f)
        )
    )


    @JvmStatic
    val ALTAR_BLOCK_CORE = register(
        "altar_block_core", AltarBlockCore(
            AbstractBlock.Settings.create()
                .nonOpaque()
                .strength(5f, 1200f)

        )
    )

    @JvmStatic
    val MAGIC_CORE = register(
        "magic_core", MagicCoreBlock(
            AbstractBlock.Settings.create()
                .nonOpaque()
                .strength(5f, 1200f)
        )
    )

    @JvmStatic
    val FORMATION_CORE_BLOCK = register(
        "formation_core", FormationCoreBlock(
            AbstractBlock.Settings.create()
                .nonOpaque()
                .strength(10f, 1200f)
        )
    )

    @JvmStatic
    val ENERGY_CRYSTAL_BLOCK = register(
        "energy_crystal", EnergyCrystalsBlock(
            AbstractBlock.Settings.create().nonOpaque()
                .strength(5f, 1200f)
        )
    )

    @JvmStatic
    val SWORD_ATTACK_CRYSTAL_BLOCK = register(
        "sword_attack_crystal", SwordAttackCrystalsBlock(
            AbstractBlock.Settings.create().nonOpaque()
                .strength(5f, 1200f)
        )
    )

    @JvmStatic
    val DEFEND_CRYSTAL_BLOCK = register(
        "defend_crystal", DefendCrystalBlock(
            AbstractBlock.Settings.create().nonOpaque()
                .strength(5f, 1200f)
        )
    )

    @JvmStatic
    val RECOVER_CRYSTAL_BLOCK = register(
        "recover_crystal", RecoverCrystalBlock(
            AbstractBlock.Settings.create().nonOpaque()
                .strength(5f, 1200f)
        )
    )

    private fun registerBlockItem(id: String, block: Block): Item {
        val blockItem =
            Registry.register(Registries.ITEM, Identifier.of(UsefulMagic.MOD_ID, id), BlockItem(block, Item.Settings()))
        blockItem.appendBlocks(Item.BLOCK_ITEMS, blockItem)
        return blockItem
    }

    fun register(id: String, block: Block): Block {
        registerBlockItem(id, block)
        val block = Registry.register(Registries.BLOCK, Identifier.of(UsefulMagic.MOD_ID, id), block)
        blocks.add(block)
        return block
    }

    fun init() {
        UsefulMagic.logger.info("block init")
    }
}