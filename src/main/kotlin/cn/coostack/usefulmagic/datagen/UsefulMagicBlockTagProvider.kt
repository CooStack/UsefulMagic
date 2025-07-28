package cn.coostack.usefulmagic.datagen

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
import java.util.concurrent.CompletableFuture

class UsefulMagicBlockTagProvider(output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>) :
    FabricTagProvider.BlockTagProvider(output, registriesFuture) {
    override fun configure(lookup: RegistryWrapper.WrapperLookup) {
        getOrCreateTagBuilder(
            BlockTags.PICKAXE_MINEABLE
        ).add(
            UsefulMagicBlocks.MAGIC_CORE,
            UsefulMagicBlocks.ALTAR_BLOCK_CORE,
            UsefulMagicBlocks.ALTAR_BLOCK,
            UsefulMagicBlocks.RECOVER_CRYSTAL_BLOCK,
            UsefulMagicBlocks.DEFEND_CRYSTAL_BLOCK,
            UsefulMagicBlocks.SWORD_ATTACK_CRYSTAL_BLOCK,
            UsefulMagicBlocks.ENERGY_CRYSTAL_BLOCK,
            UsefulMagicBlocks.FORMATION_CORE_BLOCK,
        )
    }
}