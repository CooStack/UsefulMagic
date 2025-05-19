package cn.coostack.usefulmagic

import cn.coostack.usefulmagic.datagen.UsefulMagicItemTagProvider
import cn.coostack.usefulmagic.datagen.UsefulMagicModelProvider
import cn.coostack.usefulmagic.datagen.UsefulMagicLangProvider
import cn.coostack.usefulmagic.datagen.UsefulMagicBlockTagProvider
import cn.coostack.usefulmagic.datagen.UsefulMagicLootableProvider
import cn.coostack.usefulmagic.datagen.UsefulMagicRecipeProvider
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object UsefulMagicDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider(::UsefulMagicBlockTagProvider)
        pack.addProvider(::UsefulMagicLangProvider)
        pack.addProvider(::UsefulMagicRecipeProvider)
        pack.addProvider(::UsefulMagicModelProvider)
        pack.addProvider(::UsefulMagicItemTagProvider)
        pack.addProvider(::UsefulMagicLootableProvider)
    }
}