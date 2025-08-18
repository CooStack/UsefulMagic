package cn.coostack.usefulmagic.blocks

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredBlock
import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredItem
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.formation.DefendCrystalBlock
import cn.coostack.usefulmagic.blocks.formation.EnergyCrystalsBlock
import cn.coostack.usefulmagic.blocks.formation.FormationCoreBlock
import cn.coostack.usefulmagic.blocks.formation.RecoverCrystalBlock
import cn.coostack.usefulmagic.blocks.formation.SwordAttackCrystalsBlock
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import java.util.function.Supplier

object UsefulMagicBlocks {
    val blocks = ArrayList<CommonDeferredBlock>()
    val blockItems = mutableListOf<CommonDeferredItem>()

    @JvmStatic
    val ALTAR_BLOCK = register("altar_block") {
        AltarBlock(
            BlockBehaviour.Properties.of()
                .noOcclusion()
                .strength(5f, 1200f)
        )
    }


    @JvmStatic
    val ALTAR_BLOCK_CORE = register("altar_block_core") {
        AltarBlockCore(
            BlockBehaviour.Properties.of()
                .noOcclusion()
                .strength(5f, 1200f)

        )
    }

    @JvmStatic
    val MAGIC_CORE = register("magic_core") {
        MagicCoreBlock(
            BlockBehaviour.Properties.of()
                .noOcclusion()
                .strength(5f, 1200f)
        )
    }

    @JvmStatic
    val FORMATION_CORE_BLOCK = register("formation_core") {
        FormationCoreBlock(
            BlockBehaviour.Properties.of()
                .noOcclusion()
                .strength(10f, 1200f)
        )
    }

    @JvmStatic
    val ENERGY_CRYSTAL_BLOCK = register(
        "energy_crystal"
    ) {
        EnergyCrystalsBlock(
            BlockBehaviour.Properties.of()
                .noOcclusion()
                .strength(5f, 1200f)
        )
    }

    @JvmStatic
    val SWORD_ATTACK_CRYSTAL_BLOCK = register(
        "sword_attack_crystal"
    ) {
        SwordAttackCrystalsBlock(
            BlockBehaviour.Properties.of().noOcclusion()
                .strength(5f, 1200f)
        )
    }

    @JvmStatic
    val DEFEND_CRYSTAL_BLOCK = register("defend_crystal") {
        DefendCrystalBlock(
            BlockBehaviour.Properties.of().noOcclusion()
                .strength(5f, 1200f)
        )
    }

    @JvmStatic
    val RECOVER_CRYSTAL_BLOCK = register(
        "recover_crystal"
    ) {
        RecoverCrystalBlock(
            BlockBehaviour.Properties.of().noOcclusion()
                .strength(5f, 1200f)
        )
    }

    private fun registerBlockItem(id: String, block: CommonDeferredBlock): CommonDeferredItem {
//        val blockItem =
//            Registry.register(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, id), BlockItem(block, Item.Properties()))
//        blockItem.appendBlocks(Item.BLOCK_ITEMS, blockItem)

        val blockItem = CommonDeferredItem(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, id)
        ) {
            BlockItem(block.get(), Item.Properties())
        }
        blockItems.add(blockItem)
        return blockItem
    }

    fun register(id: String, block: Supplier<Block>): CommonDeferredBlock {
//        val block = Registry.register(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, id), block)
        val commonBlock = CommonDeferredBlock(ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, id), block)
        blocks.add(commonBlock)
        registerBlockItem(id, commonBlock)
        return commonBlock
    }

    fun init() {
        UsefulMagic.logger.debug("block init")
    }
}