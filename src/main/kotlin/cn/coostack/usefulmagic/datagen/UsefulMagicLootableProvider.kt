package cn.coostack.usefulmagic.datagen

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class UsefulMagicLootableProvider(
    dataOutput: FabricDataOutput,
    registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricBlockLootTableProvider(dataOutput, registryLookup) {
    override fun generate() {
        addDrop(UsefulMagicBlocks.ALTAR_BLOCK)
        addDrop(UsefulMagicBlocks.ALTAR_BLOCK_CORE)
        addDrop(UsefulMagicBlocks.MAGIC_CORE)
    }
}