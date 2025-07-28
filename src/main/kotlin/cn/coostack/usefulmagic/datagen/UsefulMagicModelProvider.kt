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
        gen.registerSimpleState(UsefulMagicBlocks.ENERGY_CRYSTAL_BLOCK)
        gen.registerSimpleState(UsefulMagicBlocks.SWORD_ATTACK_CRYSTAL_BLOCK)
        gen.registerSimpleState(UsefulMagicBlocks.RECOVER_CRYSTAL_BLOCK)
        gen.registerSimpleState(UsefulMagicBlocks.FORMATION_CORE_BLOCK)
        gen.registerSimpleState(UsefulMagicBlocks.DEFEND_CRYSTAL_BLOCK)
    }

    override fun generateItemModels(gen: ItemModelGenerator) {

        gen.register(UsefulMagicItems.DEBUGGER, Models.GENERATED)
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
        gen.register(UsefulMagicItems.DEFEND_CORE, Models.GENERATED)
        gen.register(UsefulMagicItems.FLYING_RUNE, Models.GENERATED)
        gen.register(UsefulMagicItems.TUTORIAL_BOOK, Models.GENERATED)
        gen.register(UsefulMagicItems.FRIEND_BOARD, Models.GENERATED)
        gen.register(UsefulMagicItems.SKY_FALLING_RUNE, Models.GENERATED)

    }
}