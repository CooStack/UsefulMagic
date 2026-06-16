package cn.coostack.usefulmagic.datagen

import com.google.gson.JsonArray
import com.google.gson.JsonObject
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
import java.util.function.Supplier

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
        gen.registerSpellBagBaseModel()
        gen.registerSpellBagOpenModels()
        gen.register(UsefulMagicItems.SKY_FALLING_RUNE, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.MAGIC_EYE_SPAWNER, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.STARRY_WAND, ModelTemplates.FLAT_HANDHELD_ITEM)
        gen.register(UsefulMagicItems.BARRAGE_MAGIC, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.LASER_MAGIC, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.LIGHT_BEAM_MAGIC, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.LIGHTNING_MAGIC, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.HEALTH_MAGIC, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.SWORD_QI_MAGIC, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.GOLDEN_MAGIC, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.SWORD_FORMATION_MAGIC, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.METEORITE_MAGIC, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.STARRY_MAGIC, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.ANTI_ENTITY_DOMAIN_MAGIC, ModelTemplates.FLAT_ITEM)
        gen.register(UsefulMagicItems.EXPLOSION_MAGIC, ModelTemplates.FLAT_ITEM)
    }

    fun ItemModelGenerators.register(item: CommonDeferredItem, model: ModelTemplate) {
        this.generateFlatItem(item.getItem(), model)
    }

    private fun ItemModelGenerators.registerSpellBagOpenModels() {
        generateSpellBagOpenTemplate("template_spell_bag_open_front", 16)
        generateSpellBagOpenTemplate("template_spell_bag_open_back", -16)
        generateSpellBagOpenModel("spell_bag_open_front", "template_spell_bag_open_front")
        generateSpellBagOpenModel("spell_bag_open_back", "template_spell_bag_open_back")
        generateLayeredSpellBagOpenModel()
    }

    private fun ItemModelGenerators.registerSpellBagBaseModel() {
        output.accept(modelLocation("spell_bag"), Supplier {
            JsonObject().apply {
                addProperty("parent", "minecraft:item/generated")
                add("textures", JsonObject().apply {
                    addProperty("layer0", "usefulmagic:item/spell_bag")
                })
                add("overrides", JsonArray().apply {
                    add(JsonObject().apply {
                        add("predicate", JsonObject().apply {
                            addProperty("open", 1.0)
                        })
                        addProperty("model", "usefulmagic:item/spell_bag_open")
                    })
                })
            }
        })
    }

    private fun ItemModelGenerators.generateSpellBagOpenTemplate(path: String, guiTranslationZ: Int) {
        output.accept(modelLocation(path), Supplier {
            JsonObject().apply {
                addProperty("parent", "minecraft:item/generated")
                add("display", JsonObject().apply {
                    add("gui", JsonObject().apply {
                        add("translation", JsonArray().apply {
                            add(0)
                            add(0)
                            add(guiTranslationZ)
                        })
                    })
                })
            }
        })
    }

    private fun ItemModelGenerators.generateSpellBagOpenModel(path: String, templatePath: String) {
        output.accept(modelLocation(path), Supplier {
            JsonObject().apply {
                addProperty("parent", "usefulmagic:item/$templatePath")
                add("textures", JsonObject().apply {
                    addProperty("layer0", "usefulmagic:item/$path")
                })
            }
        })
    }

    private fun ItemModelGenerators.generateLayeredSpellBagOpenModel() {
        output.accept(modelLocation("spell_bag_open"), Supplier {
            JsonObject().apply {
                addProperty("parent", "minecraft:item/generated")
                add("textures", JsonObject().apply {
                    addProperty("layer0", "usefulmagic:item/spell_bag_open_back")
                    addProperty("layer1", "usefulmagic:item/spell_bag_open_front")
                })
            }
        })
    }

    private fun modelLocation(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(UsefulMagicItems.SPELL_BAG.id.namespace, "item/$path")
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
