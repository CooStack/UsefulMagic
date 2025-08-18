package cn.coostack.usefulmagic.datagen

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.core.HolderLookup
import net.minecraft.world.level.block.Block
import java.util.concurrent.CompletableFuture

class UsefulMagicLootableProvider(
    dataOutput: FabricDataOutput,
    registryLookup: CompletableFuture<HolderLookup.Provider>
) : FabricBlockLootTableProvider(dataOutput, registryLookup) {
    override fun generate() {
        dropSelf(UsefulMagicBlocks.ALTAR_BLOCK.get())
        dropSelf(UsefulMagicBlocks.ALTAR_BLOCK_CORE.get())
        dropSelf(UsefulMagicBlocks.MAGIC_CORE.get())
        dropSelf(UsefulMagicBlocks.FORMATION_CORE_BLOCK.get())
        dropEmpty(UsefulMagicBlocks.ENERGY_CRYSTAL_BLOCK.get())
        dropEmpty(UsefulMagicBlocks.RECOVER_CRYSTAL_BLOCK.get())
        dropEmpty(UsefulMagicBlocks.SWORD_ATTACK_CRYSTAL_BLOCK.get())
        dropEmpty(UsefulMagicBlocks.DEFEND_CRYSTAL_BLOCK.get())
    }

    fun dropEmpty(block: Block) {
        map[block.lootTable] = noDrop()
    }
}

