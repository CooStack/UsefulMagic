package cn.coostack.usefulmagic

import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageTypes
import cn.coostack.usefulmagic.datagen.UsefulMagicBlockTagProvider
import cn.coostack.usefulmagic.datagen.UsefulMagicDynamicRegistryProvider
import cn.coostack.usefulmagic.datagen.UsefulMagicENLangProvider
import cn.coostack.usefulmagic.datagen.UsefulMagicItemTagProvider
import cn.coostack.usefulmagic.datagen.UsefulMagicLootableProvider
import cn.coostack.usefulmagic.datagen.UsefulMagicModelProvider
import cn.coostack.usefulmagic.datagen.UsefulMagicRecipeProvider
import cn.coostack.usefulmagic.datagen.UsefulMagicZHLangProvider
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries

object UsefulMagicFabricDataGen : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(gen: FabricDataGenerator) {
        val pack = gen.createPack()
        pack.addProvider(::UsefulMagicBlockTagProvider)
        pack.addProvider(::UsefulMagicZHLangProvider)
        pack.addProvider(::UsefulMagicRecipeProvider)
        pack.addProvider(::UsefulMagicModelProvider)
        pack.addProvider(::UsefulMagicItemTagProvider)
        pack.addProvider(::UsefulMagicLootableProvider)
        pack.addProvider(::UsefulMagicENLangProvider)
        pack.addProvider(::UsefulMagicDynamicRegistryProvider)
    }

    override fun buildRegistry(registryBuilder: RegistrySetBuilder) {
        registryBuilder.add(
                Registries.DAMAGE_TYPE, UsefulMagicDamageTypes::bootstrap
        )
    }
}
