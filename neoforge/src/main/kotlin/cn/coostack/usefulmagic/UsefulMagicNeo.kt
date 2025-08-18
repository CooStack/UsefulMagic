package cn.coostack.usefulmagic

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.UsefulMagicItemGroups
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.platform.FuelHelper
import cn.coostack.usefulmagic.recipe.UsefulMagicRecipeTypes
import net.minecraft.core.registries.BuiltInRegistries
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.registries.RegisterEvent
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

/**
 * 没有找到能够直接注入 object作为 event的方法
 * 如果注解了 EventBusSubscriber 则一定要是class 而不是 object
 * 要不然就会报大错 :)
 */
@Mod(UsefulMagic.MOD_ID)
object UsefulMagicNeo {
    init {
        MOD_BUS.addListener(::onClientSetup)
        MOD_BUS.addListener(::onCommonSetup)
        MOD_BUS.addListener(::onServerRegistry)
        MOD_BUS.addListener(::loadEntityAttributes)
        UsefulMagic.init()
        loadFuel()

    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        UsefulMagicNeoClient.onClientSetup(event)
    }

    private fun loadFuel() {
        FuelHelper.fuels[UsefulMagicItems.WOODEN_WAND] = 200
    }

    private fun loadEntityAttributes(event: EntityAttributeCreationEvent) {
        UsefulMagicEntityTypes.init()
        event.put(
            UsefulMagicEntityTypes.MAGIC_BOOK_ENTITY_TYPE.get(),
            MagicBookEntity.createDefaultMobAttributes().build()
        )
    }

    private fun onServerRegistry(event: RegisterEvent) {
        event.register(BuiltInRegistries.BLOCK.key()) {
            UsefulMagicBlocks.blocks.forEach { item ->
                it.register(item.id, item.get())
            }
        }
        event.register(BuiltInRegistries.ITEM.key()) {
            UsefulMagicItems.items.forEach { item ->
                it.register(item.id, item.getItem())
            }
            UsefulMagicBlocks.blockItems.forEach { item ->
                it.register(item.id, item.getItem())
            }
        }
        event.register(BuiltInRegistries.CREATIVE_MODE_TAB.key()) {
            val group = UsefulMagicItemGroups.usefulMagicMainGroup
            it.register(group.id, group.get())
        }

        event.register(BuiltInRegistries.BLOCK_ENTITY_TYPE.key()) {
            UsefulMagicBlockEntities.blockEntityTypes.forEach { entityType ->
                it.register(entityType.id, entityType.get())
            }
        }

        event.register(BuiltInRegistries.ENTITY_TYPE.key()) {
            UsefulMagicEntityTypes.entityTypes.forEach { entityType ->
                it.register(entityType.id, entityType.get())
            }
        }

        event.register(BuiltInRegistries.DATA_COMPONENT_TYPE.key()) {
            UsefulMagicDataComponentTypes.types.forEach { type ->
                it.register(type.id, type.get())
            }
        }

        event.register(BuiltInRegistries.RECIPE_TYPE.key()) {
            UsefulMagicRecipeTypes.recipeTypes.forEach { type ->
                it.register(type.id, type.get())
            }
        }
        event.register(BuiltInRegistries.RECIPE_SERIALIZER.key()) {
            UsefulMagicRecipeTypes.recipeSerializer.forEach { type ->
                it.register(type.id, type.get())
            }
        }
    }

    private fun onCommonSetup(event: FMLCommonSetupEvent) {
    }
}