package cn.coostack.usefulmagic.datagen

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredBlock
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.core.HolderLookup
import net.minecraft.tags.BlockTags
import java.util.concurrent.CompletableFuture

class UsefulMagicBlockTagProvider(
    output: FabricDataOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>
) :
    FabricTagProvider.BlockTagProvider(output, registriesFuture) {
    override fun addTags(lookup: HolderLookup.Provider) {
        getOrCreateTagBuilder(
            BlockTags.MINEABLE_WITH_PICKAXE
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

    fun FabricTagBuilder.add(vararg items: CommonDeferredBlock): FabricTagBuilder {
        items.forEach { add(it.get()) }
        return this
    }
}