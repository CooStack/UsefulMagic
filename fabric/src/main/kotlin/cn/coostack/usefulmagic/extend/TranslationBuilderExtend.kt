package cn.coostack.usefulmagic.extend

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredBlock
import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredEntityType
import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredItem
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider

fun FabricLanguageProvider.TranslationBuilder.add(item: CommonDeferredItem, value: String) {
    this.add(item.getItem(), value)
}

fun FabricLanguageProvider.TranslationBuilder.add(item: CommonDeferredBlock, value: String) {
    this.add(item.get(), value)
}

fun FabricLanguageProvider.TranslationBuilder.add(item: CommonDeferredEntityType<*>, value: String) {
    this.add(item.get(), value)
}