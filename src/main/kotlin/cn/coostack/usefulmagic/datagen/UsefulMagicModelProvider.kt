package cn.coostack.usefulmagic.datagen

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.data.client.BlockStateModelGenerator
import net.minecraft.data.client.ItemModelGenerator
import net.minecraft.data.client.Models

class UsefulMagicModelProvider(output: FabricDataOutput) : FabricModelProvider(output) {
    override fun generateBlockStateModels(gen: BlockStateModelGenerator) {
        gen.registerSimpleState(UsefulMagicBlocks.ALTAR_BLOCK)
        gen.registerSimpleState(UsefulMagicBlocks.ALTAR_BLOCK_CORE)
        gen.registerSimpleState(UsefulMagicBlocks.MAGIC_CORE)
    }

    override fun generateItemModels(gen: ItemModelGenerator) {
        gen.register(UsefulMagicItems.DEBUGGER, Models.GENERATED)
        gen.register(UsefulMagicItems.WOODEN_WAND, Models.HANDHELD)
        gen.register(UsefulMagicItems.STONE_WAND, Models.HANDHELD)
        gen.register(UsefulMagicItems.COPPER_WAND, Models.HANDHELD)
        gen.register(UsefulMagicItems.IRON_WAND, Models.HANDHELD)
        gen.register(UsefulMagicItems.GOLDEN_WAND, Models.HANDHELD)
        gen.register(UsefulMagicItems.DIAMOND_WAND, Models.HANDHELD)
        gen.register(UsefulMagicItems.NETHERITE_WAND, Models.HANDHELD)
        gen.register(UsefulMagicItems.WAND_OF_METEORITE, Models.HANDHELD)
        gen.register(UsefulMagicItems.HEALTH_REVIVE_WAND, Models.HANDHELD)
        gen.register(UsefulMagicItems.ANTI_ENTITY_WAND, Models.HANDHELD)
        gen.register(UsefulMagicItems.SMALL_MANA_REVIVE, Models.GENERATED)
        gen.register(UsefulMagicItems.SMALL_MANA_BOTTLE, Models.GENERATED)
        gen.register(UsefulMagicItems.MANA_REVIVE, Models.GENERATED)
        gen.register(UsefulMagicItems.MANA_BOTTLE, Models.GENERATED)
        gen.register(UsefulMagicItems.LARGE_MANA_BOTTLE, Models.GENERATED)
        gen.register(UsefulMagicItems.MANA_STAR, Models.GENERATED)
        gen.register(UsefulMagicItems.PURPLE_MANA_STAR, Models.GENERATED)
        gen.register(UsefulMagicItems.RED_MANA_STAR, Models.GENERATED)
        gen.register(UsefulMagicItems.MANA_CRYSTAL, Models.GENERATED)
        gen.register(UsefulMagicItems.PURPLE_MANA_CRYSTAL, Models.GENERATED)
        gen.register(UsefulMagicItems.RED_MANA_CRYSTAL, Models.GENERATED)
    }
}