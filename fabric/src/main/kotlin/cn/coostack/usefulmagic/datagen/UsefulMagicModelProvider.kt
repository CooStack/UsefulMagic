package cn.coostack.usefulmagic.datagen

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredBlock
import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredItem
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.models.model.ModelTemplate
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.resources.ResourceLocation

class UsefulMagicModelProvider(output: FabricDataOutput) : FabricModelProvider(output) {
    override fun generateBlockStateModels(gen: BlockModelGenerators) {
        gen.registerSimpleState(UsefulMagicBlocks.ALTAR_BLOCK)
        gen.registerSimpleState(UsefulMagicBlocks.ALTAR_BLOCK_CORE)
        gen.registerSimpleState(UsefulMagicBlocks.MAGIC_CORE)
        gen.registerSimpleState(UsefulMagicBlocks.ENERGY_CRYSTAL_BLOCK)
        gen.registerSimpleState(UsefulMagicBlocks.SWORD_ATTACK_CRYSTAL_BLOCK)
        gen.registerSimpleState(UsefulMagicBlocks.RECOVER_CRYSTAL_BLOCK)
        gen.registerSimpleState(UsefulMagicBlocks.FORMATION_CORE_BLOCK)
        gen.registerSimpleState(UsefulMagicBlocks.DEFEND_CRYSTAL_BLOCK)

    }

    override fun generateItemModels(gen: ItemModelGenerators) {
        gen.register(UsefulMagicItems.DEBUGGER, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.SMALL_MANA_REVIVE, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.SMALL_MANA_BOTTLE, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.MANA_REVIVE, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.MANA_BOTTLE, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.LARGE_MANA_BOTTLE, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.MANA_STAR, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.PURPLE_MANA_STAR, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.RED_MANA_STAR, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.MANA_CRYSTAL, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.PURPLE_MANA_CRYSTAL, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.RED_MANA_CRYSTAL, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.DEFEND_CORE, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.FLYING_RUNE, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.TUTORIAL_BOOK, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.FRIEND_BOARD, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.SKY_FALLING_RUNE, ModelTemplates.FLAT_ITEM)
    }

    fun ItemModelGenerators.register(item: CommonDeferredItem, model: ModelTemplate) {
        this.generateFlatItem(item.getItem(), model)
    }
}

private fun BlockModelGenerators.registerSimpleState(block: CommonDeferredBlock) {
    blockStateOutput.accept(
        BlockModelGenerators.createSimpleBlock(
            block.get(),
            ResourceLocation.fromNamespaceAndPath(block.id.namespace, "block/${block.id.path}")
        )
    )
}
