package cn.coostack.usefulmagic.datagen

import cn.coostack.usefulmagic.extend.add
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageTypes
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.core.HolderLookup
import net.minecraft.resources.ResourceKey
import net.minecraft.world.damagesource.DamageType
import java.util.concurrent.CompletableFuture

class UsefulMagicENLangProvider(
    dataOutput: FabricDataOutput,
    registryLookup: CompletableFuture<HolderLookup.Provider>
) : FabricLanguageProvider(dataOutput, "en_us", registryLookup) {
    override fun generateTranslations(
        lookup: HolderLookup.Provider,
        builder: TranslationBuilder
    ) {
        builder.apply {
            // item淇℃伅
            add(UsefulMagicItems.DEBUGGER, "Useful Magic Debugger");
            add(UsefulMagicItems.WOODEN_WAND, "Wooden Wand");
            add(UsefulMagicItems.STONE_WAND, "Stone Ray Wand");
            add(UsefulMagicItems.COPPER_WAND, "Oxidized Copper Wand");
            add(UsefulMagicItems.IRON_WAND, "Iron Magic Wand");
            add(UsefulMagicItems.GOLDEN_WAND, "Golden Magic Wand");
            add(UsefulMagicItems.DIAMOND_WAND, "Diamond Wand");
            add(UsefulMagicItems.NETHERITE_WAND, "Netherite Wand");
            add(UsefulMagicItems.WAND_OF_METEORITE, "Meteorite Wand");
            add(UsefulMagicItems.STARRY_WAND, "Wand of the Stars");
            add(UsefulMagicItems.ANTI_ENTITY_WAND, "Anti-Entity Wand");
            add(UsefulMagicItems.HEALTH_REVIVE_WAND, "Regeneration Wand");
            add(UsefulMagicItems.MAGIC_AXE, "Enchanted Axe");
            add(UsefulMagicItems.LIGHTNING_WAND, "Lightning Wand");
            add(UsefulMagicItems.EXPLOSION_WAND, "Explosion Wand");
            add(UsefulMagicItems.SMALL_MANA_BOTTLE, "Small Mana Bottle");
            add(UsefulMagicItems.SMALL_MANA_REVIVE, "Small Mana Recovery Potion");
            add(UsefulMagicItems.MANA_REVIVE, "Mana Recovery Potion");
            add(UsefulMagicItems.MANA_BOTTLE, "Potion Bottle");
            add(UsefulMagicItems.LARGE_MANA_BOTTLE, "Large Potion Bottle");
            add(UsefulMagicItems.LARGE_MANA_REVIVE, "Large Mana Recovery Potion");
            add(UsefulMagicItems.MANA_STAR, "Tier I Mana Crystal");
            add(UsefulMagicItems.MANA_CRYSTAL, "Tier I Mana Condenser");
            add(UsefulMagicItems.PURPLE_MANA_STAR, "Tier II Mana Crystal");
            add(UsefulMagicItems.PURPLE_MANA_CRYSTAL, "Tier II Mana Condenser");
            add(UsefulMagicItems.RED_MANA_STAR, "Tier III Mana Crystal");
            add(UsefulMagicItems.RED_MANA_CRYSTAL, "Tier III Mana Condenser");
            add(UsefulMagicItems.DEFEND_CORE, "Magic Defense Core");
            add(UsefulMagicItems.FLYING_RUNE, "Flight Rune");
            add(UsefulMagicItems.TUTORIAL_BOOK, "Useful Magic Guide");
            add(UsefulMagicItems.FRIEND_BOARD, "Friend Token");
            add(UsefulMagicItems.SKY_FALLING_RUNE, "Grand Magic: Sky falling Rune");
            add(UsefulMagicItems.MAGIC_EYE_SPAWNER, "Gazing Eye");

            // group
            add("item.useful_magic_main", "Useful Magic");

            // block
            add(UsefulMagicBlocks.ALTAR_BLOCK, "Imbuing Altar");
            add(UsefulMagicBlocks.ALTAR_BLOCK_CORE, "Imbuing Output Platform");
            add(UsefulMagicBlocks.MAGIC_CORE, "Mana Core");
            add(UsefulMagicBlocks.FORMATION_CORE_BLOCK, "Formation Base");
            add(UsefulMagicBlocks.DEFEND_CRYSTAL_BLOCK, "Defense Crystal");
            add(UsefulMagicBlocks.RECOVER_CRYSTAL_BLOCK, "Mana Crystal");
            add(UsefulMagicBlocks.SWORD_ATTACK_CRYSTAL_BLOCK, "Sword Aura Crystal");
            add(UsefulMagicBlocks.ENERGY_CRYSTAL_BLOCK, "Energy Crystal");

            add(UsefulMagicItems.BARRAGE_MAGIC, "Barrage Spell");
            add(UsefulMagicItems.LASER_MAGIC, "Laser Spell");
            add(UsefulMagicItems.LIGHT_BEAM_MAGIC, "Beam Spell");
            add(UsefulMagicItems.SWORD_QI_MAGIC,"Sword Light Spell")
            add(UsefulMagicItems.GOLDEN_MAGIC, "Golden Spell")
            add(UsefulMagicItems.SWORD_FORMATION_MAGIC, "Sword Formation Spell")
            add(UsefulMagicItems.METEORITE_MAGIC, "Meteorite Spell");
            add(UsefulMagicItems.LIGHTNING_MAGIC, "Lightning Spell");
            add(UsefulMagicItems.HEALTH_MAGIC, "Health Spell");
            add(UsefulMagicItems.STARRY_MAGIC, "Starry Meteorite Spell");
            add(UsefulMagicItems.ANTI_ENTITY_DOMAIN_MAGIC, "Anti Entity Domain Spell");
            add(UsefulMagicItems.EXPLOSION_MAGIC, "§c§lExplosion Magic");

            // entity
            add(UsefulMagicEntityTypes.MAGIC_BOOK_ENTITY_TYPE, "Forbidden Grimoire");
            add(UsefulMagicEntityTypes.MAGIC_DRAGON_ENTITY_TYPE, "Abyssal Pupil Dragon");
            add(UsefulMagicEntityTypes.MAGIC_EYE_ENTITY_TYPE, "Eye of the Abyss");
            add(UsefulMagicEntityTypes.MAGIC_SUB_EYE_ENTITY_TYPE, "Abyssal Pearl");
            add(UsefulMagicEntityTypes.MAGIC_HEART_ENTITY_TYPE, "Abyssal Heart");

            // lore / tooltip
            add("screen.shift", "§7Hold Shift for more information");
            add("item.wand.mana", "§aCurrent Mana: §b%mana%");
            add("item.wooden_wand.description", "§7Charge to release an aggressive particle attack");
            add("item.stone_wand.description", "§7Charge to fire a ray that chains to 2 nearby entities");
            add("item.copper_wand.description", "§7Charge to summon small meteorites on impact");
            add(
                "item.iron_wand.description",
                "§7Charge to release a magic orb dealing triple damage, splitting into three after a short time"
            );
            add(
                "item.golden_wand.description",
                "§7Charge to release a magic orb that continuously fires rays at nearby enemies"
            );
            add(
                "item.diamond_wand.description",
                "§7Charge to release tracking sword-like energy that strikes enemies ahead"
            );
            add(
                "item.netherite_wand.description",
                "§7Charge to release more tracking sword energy, striking enemies ahead"
            );
            add(
                "item.wand_of_meteorite.description",
                "§7Charge to summon a falling meteor from the sky - no one knows how this works"
            );
            add(
                "item.anti_entity_wand.description",
                "§7Summons a massive magic circle that continuously fires barrages at enemies"
            );
            add(
                "item.lightning_wand.description",
                "§7Become the lord of lightning, shocking nearby creatures"
            );
            add(
                "item.explosion_wand.description",
                "§7Charge to cast explosive magic. Explosion!"
            );
            add(
                "item.starry_wand.description",
                "§7You will summon the stars and rain them upon the land"
            );
            add(
                "item.health_revive_wand.description",
                "§7Charge to grant regeneration to yourself or heal targets. Extremely effective against undead"
            );

            add(
                "item.mana_star.description",
                "§7Use to increase mana capacity by 20, up to a maximum of 500"
            );
            add(
                "item.mana_crystal.description",
                "§7Use to increase mana regeneration by 1, up to a maximum of 10"
            );
            add(
                "item.purple_mana_star.description",
                "§7Use to increase mana capacity by 50. Requires at least 500 mana. Max 1500"
            );
            add(
                "item.red_mana_star.description",
                "§7Use to increase mana capacity by 100. Requires at least 1500 mana. Max 4500"
            );
            add(
                "item.purple_mana_crystal.description",
                "§7Use to increase mana regeneration by 2. Requires at least 10 regen. Max 30"
            );
            add(
                "item.red_mana_crystal.description",
                "§7Use to increase mana regeneration by 3. Requires at least 30 regen. Max 60"
            );

            add("item.wand.damage", "§7Damage: §c%amount%");
            add("item.health_wand.amount", "§7Health Restored: §a%amount%");
            add("item.wand.cost", "§7Cost: §b%cost% §7Mana");
            add(
                "item.small_mana_glass_bottle.usage",
                "§7Right-click the Mana Core to fill the bottle"
            );
            add(
                "item.small_mana_bottle.revive",
                "§7Drink to restore §b100 §7mana"
            );
            add(
                "item.mana_glass_bottle.usage",
                "§7Right-click the Mana Core to fill the bottle"
            );
            add(
                "item.mana_bottle.revive",
                "§7Drink to restore §b800 §7mana"
            );
            add(
                "item.large_mana_bottle.revive",
                "§7Drink to restore §b1500 §7mana"
            );
            add(
                "item.large_mana_glass_bottle.usage",
                "§7Right-click the Mana Core to fill the bottle"
            );
            add(
                "item.large_mana_bottle.can_use_count",
                "§7Remaining Uses: §b%count%"
            );

            add("item.defend_core_enabled", "§7Current State: %enabled%");
            add("item.flying_rune_enabled", "§7Current State: %enabled%");
            add("item.usefulmagic_enabled", "§aEnabled");
            add("item.usefulmagic_disabled", "§cDisabled");
            add("effect.usefulmagic.freeze", "Freeze");
            add("effect.usefulmagic.magic_sealed", "Mana Dysregulation");

            add("item.usefulmagic.wand.level", "§7Wand Level: %s");
            add("item.usefulmagic.wand.reduction", "§7Mana Cost Reduction: %s");
            add("item.usefulmagic.wand.factor", "§7Casting Speed Multiplier: %s");
            add(
                "item.usefulmagic.wand.cost",
                "§7Mana Cost per Cast: %s %s"
            );
            add(
                "item.usefulmagic.wand.charging_time",
                "§7Charge Time: %s %s"
            );
            add(
                "item.usefulmagic.wand.cd",
                "§7Cooldown After Cast: %s %s"
            );
            add(
                "item.usefulmagic.wand.damage",
                "§7Spell Damage: %s %s"
            );
            add(
                "item.usefulmagic.wand.load_magic",
                "§7Loaded Spell: "
            );

            // prefer info
            add(
                "item.usefulmagic.wand.prefer.info",
                "§7Magic Preference: %s"
            );
            add(
                "item.usefulmagic.wand.usage_install",
                "§7Right-click the wand in your inventory like a bundle to load a spell"
            );
            add(
                "item.usefulmagic.wand.usage_exchange",
                "§7Right-click the wand in your inventory like a bundle to swap or remove the loaded spell"
            );
            add(
                "item.usefulmagic.magic.level",
                "§7Magic Level: %s"
            );
            add("item.usefulmagic.magic.base_cost", "§7Mana Cost: %s")
            add("item.usefulmagic.magic.base_damage", "§7Spell Damage: %s")
            add("item.usefulmagic.magic.base_cd", "§7Base Cooldown: %s")
            add("item.usefulmagic.magic.base_usage", "§7Base Charge Time: %s")

            add("item.usefulmagic.magic.barrage_magic", "§7Charge and release a magic orb")
            // sounds
            add("sounds.usefulmagic.electric_effect", "Electric Shock");
            add("sounds.usefulmagic.magic_activate", "Magic Activation");
            add("sounds.usefulmagic.magic_sword", "Magic Blade");
            add("sounds.usefulmagic.sky_falling_magic_idle", "Grand Magic Charging");
            add("sounds.usefulmagic.sky_falling_magic_start", "Grand Magic Initiation");
            add("sounds.usefulmagic.defend_shield_hit", "Magic Shield Block");
            add("sounds.usefulmagic.magic_explode", "Mana Explosion");
            add("sounds.usefulmagic.star", "Star Sparkle");
            add("sounds.usefulmagic.meteor_fall_far", "Meteor Roar (Far)");
            add("sounds.usefulmagic.meteor_fall_near", "Meteor Roar (Near)");
            add("sounds.usefulmagic.meteor_impact", "Meteor Impact");
            add("sounds.usefulmagic.laser_charge_up", "Cosmic Laser Charging");
            add("sounds.usefulmagic.laser_start", "Cosmic Laser Firing");
            add("sounds.usefulmagic.laser_loop", "Cosmic Laser Beam");
            add("sounds.usefulmagic.laser_obliteration", "Cosmic Laser Obliteration");
            add("sounds.usefulmagic.eye_teleport", "Eye Teleport");
            add("sounds.usefulmagic.eye_active", "Eye Active");
            add("sounds.usefulmagic.eye_spawn", "Eye Spawn");
            add("sounds.usefulmagic.dragon_spawn_magic_laser", "Dragon Spawn Magic Laser");
            add("sounds.usefulmagic.small_laser_shoot", "Small Laser Firing");
            add("sounds.usefulmagic.thorn_hit", "Thorn Hit");
            add("sounds.usefulmagic.liquid_ball", "Liquid Ball");
            add("sounds.usefulmagic.rock_loop", "Rock Loop");

            // gui
            add("screen.title.friend_manager_title", "Friend Manager");
            add("screen.friend_manager.page_prev", "Previous Page");
            add("screen.friend_manager.page_next", "Next Page");
            add("screen.friend_manager.remove", "Remove");
            add("screen.friend_manager.page_info", "Page %s / %s");
            add("screen.friend_manager.empty", "No friends on this page");
            add("screen.friend_manager.removed", "Removed friend: %s");
            add("screen.friend_manager.tab.friend_list", "Friend List");
            add("screen.friend_manager.tab.friendly_settings", "Friendly Settings");
            add("screen.friend_manager.friendly_settings_hint", "Checked entries are treated as friends by FriendManager");
            add("screen.friend_manager.setting.hostile", "Hostile Mobs");
            add("screen.friend_manager.setting.neutral", "Neutral Mobs");
            add("screen.friend_manager.setting.non_friend_player", "Non-Friend Players");
            add("screen.friend_manager.setting.friend_player", "Friend Players");
            add("screen.friend_manager.setting.animal", "Animals");
            add("screen.friend_manager.setting.friendly_mob", "Friendly Mobs");
            add(
                "screen.friend_manager.setting.tooltip.on",
                "Currently enabled: %s is treated as friend.\nDisable it to allow your attacks to hit this type."
            );
            add(
                "screen.friend_manager.setting.tooltip.off",
                "Currently disabled: %s is not treated as friend.\nEnable it to protect this type from friendly fire."
            );

            // tutorial book
            add("screen.usefulmagic.tutorial_book.screen_title", "Useful Magic Tutorial Book")
            add("screen.usefulmagic.tutorial_book.page_info", "%s/%s")
            add("screen.usefulmagic.tutorial_book.raw", "%s")
            add("screen.usefulmagic.tutorial_book.home.type_panel_title", "Tutorial Categories")
            add("screen.usefulmagic.tutorial_book.home.content_panel_title", "Tutorial Content")
            add("screen.usefulmagic.tutorial_book.home.type_panel_hint", "Click a left icon to choose a module")
            add("screen.usefulmagic.tutorial_book.home.content_panel_hint", "Follow the order for faster learning")
            add("screen.usefulmagic.tutorial_book.home.quick_start.title", "Quick Start")
            add("screen.usefulmagic.tutorial_book.home.quick_start.line1", "1. Select a tutorial category on the left")
            add("screen.usefulmagic.tutorial_book.home.quick_start.line2", "2. Open it and learn step by step")
            add("screen.usefulmagic.tutorial_book.home.quick_start.line3", "3. Start with Structure -> Imbuing Altar")
            add("screen.usefulmagic.tutorial_book.home.quick_start.line4", "4. Then use the Recipe page for crafting lookup")
            add("screen.usefulmagic.tutorial_book.home.category.structure.tooltip", "Multiblock structure tutorial")
            add("screen.usefulmagic.tutorial_book.home.category.recipe.tooltip", "Imbuing altar recipes")
            add("screen.usefulmagic.tutorial_book.home.category.bestiary.tooltip", "Bestiary is under development")
            add("screen.usefulmagic.tutorial_book.home.category.structure.label", "Structure")
            add("screen.usefulmagic.tutorial_book.home.category.recipe.label", "Recipe")
            add("screen.usefulmagic.tutorial_book.home.category.bestiary.label", "Bestiary")
            add("screen.usefulmagic.tutorial_book.structure.type_panel_title", "Structure Categories")
            add("screen.usefulmagic.tutorial_book.structure.content_panel_title", "Structure Tutorial")
            add("screen.usefulmagic.tutorial_book.structure.type_panel_hint", "Start with the Imbuing Altar")
            add("screen.usefulmagic.tutorial_book.structure.content_panel_hint", "Select a structure on the left for detailed steps")
            add("screen.usefulmagic.tutorial_book.structure.category.altar.tooltip", "Imbuing Altar")
            add("screen.usefulmagic.tutorial_book.structure.category.formation.tooltip", "Formation")
            add("screen.usefulmagic.tutorial_book.structure.category.altar.label", "Altar")
            add("screen.usefulmagic.tutorial_book.structure.category.formation.label", "Formation")
            add("screen.usefulmagic.tutorial_book.recipe_main.type_panel_title", "Craftable Outputs")
            add("screen.usefulmagic.tutorial_book.recipe_main.content_panel_title", "Recipe Overview")
            add("screen.usefulmagic.tutorial_book.recipe_main.type_panel_hint", "Click an output to open recipe details")
            add("screen.usefulmagic.tutorial_book.recipe_main.content_panel_hint", "Use page buttons to browse all recipes")
            add("screen.usefulmagic.tutorial_book.recipe_main.empty.line1", "No recipes available")
            add("screen.usefulmagic.tutorial_book.recipe_main.empty.line2", "Make sure recipes are loaded in this world")
            add("screen.usefulmagic.tutorial_book.recipe_main.guide.title", "Recipe Guide")
            add("screen.usefulmagic.tutorial_book.recipe_main.guide.line1", "1. Click any output on the left")
            add("screen.usefulmagic.tutorial_book.recipe_main.guide.line2", "2. The right page shows altar layout")
            add("screen.usefulmagic.tutorial_book.recipe_main.guide.line3", "3. Use bottom-right buttons to view more")
            add("screen.usefulmagic.tutorial_book.recipe_main.guide.count", "Recipes available: %s")
            add("screen.usefulmagic.tutorial_book.recipe_detail.content_panel_title", "Recipe Details")
            add("screen.usefulmagic.tutorial_book.recipe_detail.type_panel_hint", "Left panel supports quick recipe switching")
            add("screen.usefulmagic.tutorial_book.recipe_detail.content_panel_hint", "Center ingredient + 8 outer ingredients")
            add("screen.usefulmagic.tutorial_book.recipe_detail.mana_need", "Mana Required: %s")
            add("screen.usefulmagic.tutorial_book.formation.type_panel_title", "Formation Types")
            add("screen.usefulmagic.tutorial_book.formation.content_panel_title", "Formation Preview")
            add("screen.usefulmagic.tutorial_book.formation.type_panel_hint", "Click left crystals to switch formation")
            add("screen.usefulmagic.tutorial_book.formation.content_panel_hint", "The right panel shows the matching layout")
            add("screen.usefulmagic.tutorial_book.formation.tooltip.small", "Small Formation\nRequires at least 1 Energy Crystal\nAnd 1 functional crystal\nEffective radius: about 32 blocks\nClick to view the layout")
            add("screen.usefulmagic.tutorial_book.formation.tooltip.mid", "Medium Formation\nRequires all small-formation conditions\nEffective radius: about 64 blocks\nClick to view the layout")
            add("screen.usefulmagic.tutorial_book.formation.tooltip.large", "Large Formation\nRequires all medium-formation conditions\nEffective radius: about 128 blocks\nClick to view the layout")
            add("screen.usefulmagic.tutorial_book.formation.guide.title", "Formation Learning Flow")
            add("screen.usefulmagic.tutorial_book.formation.guide.line1", "1. Select a formation size on the left")
            add("screen.usefulmagic.tutorial_book.formation.guide.line2", "2. Check the matching layout on the right")
            add("screen.usefulmagic.tutorial_book.formation.guide.line3", "3. Build in world according to the layout and activate")
            add("screen.usefulmagic.tutorial_book.altar.type_panel_title", "Altar Material Support")
            add("screen.usefulmagic.tutorial_book.altar.content_panel_title", "Altar Build Steps")
            add("screen.usefulmagic.tutorial_book.altar.type_panel_hint", "Hover icons to view block attributes")
            add("screen.usefulmagic.tutorial_book.altar.content_panel_hint", "Use bottom-right buttons to switch steps")
            add("screen.usefulmagic.tutorial_book.altar.support.table_title", "Supported Blocks for Imbuing Altar")
            add("screen.usefulmagic.tutorial_book.altar.support.tooltip.coal", "Coal Block\nMana capacity per block: 200\nMana regen per block: 1/sec")
            add("screen.usefulmagic.tutorial_book.altar.support.tooltip.iron", "Iron Block\nMana capacity per block: 400\nMana regen per block: 1/sec")
            add("screen.usefulmagic.tutorial_book.altar.support.tooltip.gold", "Gold Block\nMana capacity per block: 400\nMana regen per block: 1/sec")
            add("screen.usefulmagic.tutorial_book.altar.support.tooltip.redstone", "Redstone Block\nMana capacity per block: 400\nMana regen per block: 1/sec")
            add("screen.usefulmagic.tutorial_book.altar.support.tooltip.diamond", "Diamond Block\nMana capacity per block: 800\nMana regen per block: 2/sec")
            add("screen.usefulmagic.tutorial_book.altar.support.tooltip.emerald", "Emerald Block\nMana capacity per block: 1000\nMana regen per block: 2/sec")
            add("screen.usefulmagic.tutorial_book.altar.support.tooltip.netherite", "Netherite Block\nMana capacity per block: 1500\nMana regen per block: 3/sec")
            add("screen.usefulmagic.tutorial_book.altar.step.1.line1", "Find an open area and place the Imbuing Output Platform")
            add("screen.usefulmagic.tutorial_book.altar.step.2.line1", "Place Imbuing Altar blocks around the platform")
            add("screen.usefulmagic.tutorial_book.altar.step.3.line1", "Place above the output platform:")
            add("screen.usefulmagic.tutorial_book.altar.step.3.line2", "Imbuing Altar Core")
            add("screen.usefulmagic.tutorial_book.altar.step.4.line1", "Place metal blocks under the 9 sub-platforms")
            add("screen.usefulmagic.tutorial_book.altar.step.4.line2", "Supported block types are listed on the left")
            add("screen.usefulmagic.tutorial_book.altar.step.4.line3", "You can keep stacking under each platform")
            add("screen.usefulmagic.tutorial_book.altar.step.4.line4", "Each platform supports up to 10 metal blocks")
            add("screen.usefulmagic.tutorial_book.altar.step.5.line1", "Particles appear when activation succeeds")
            add("screen.usefulmagic.tutorial_book.altar.step.6.line1", "Right-click the Imbuing Altar Core to inspect info")

            // damage type
            add(UsefulMagicDamageTypes.MAGIC, "%1%s was erased by magic cast by %2%s")


            // prefer
            add("item.usefulmagic.prefer.levelr5", "§cDeeply Disliked");
            add("item.usefulmagic.prefer.levelr4", "§cStrongly Disliked");
            add("item.usefulmagic.prefer.levelr3", "§cClearly Disliked");
            add("item.usefulmagic.prefer.levelr2", "§cMildly Disliked");
            add("item.usefulmagic.prefer.levelr1", "§cSlightly Disliked");
            add("item.usefulmagic.prefer.level0", "§7Neutral");
            add("item.usefulmagic.prefer.level1",  "§aSlightly Preferred");
            add("item.usefulmagic.prefer.level2",  "§aMildly Preferred");
            add("item.usefulmagic.prefer.level3",  "§aClearly Preferred");
            add("item.usefulmagic.prefer.level4",  "§aStrongly Preferred");
            add("item.usefulmagic.prefer.level5",  "§aHighly Preferred");

        }
    }

    private fun TranslationBuilder.add(tag: ResourceKey<DamageType>, message: String) {
        add("death.attack." + tag.location().toLanguageKey(), message)
    }
}
