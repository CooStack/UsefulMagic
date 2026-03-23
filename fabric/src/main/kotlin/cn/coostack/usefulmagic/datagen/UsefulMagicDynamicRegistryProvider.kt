package cn.coostack.usefulmagic.datagen

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import java.util.concurrent.CompletableFuture

class UsefulMagicDynamicRegistryProvider(
    output: FabricDataOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>
) : FabricDynamicRegistryProvider(output, registriesFuture) {
    override fun configure(
        lookup: HolderLookup.Provider,
        entries: Entries
    ) {
        entries.addAll(
            lookup.lookupOrThrow(Registries.DAMAGE_TYPE)
        )
    }

    override fun getName(): String {
        return "UsefulMagic Dynamic Registry"
    }
}