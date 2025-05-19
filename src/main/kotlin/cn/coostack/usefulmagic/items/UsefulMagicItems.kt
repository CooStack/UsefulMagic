package cn.coostack.usefulmagic.items

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.items.consumer.LargeManaBottle
import cn.coostack.usefulmagic.items.consumer.LargeManaRevive
import cn.coostack.usefulmagic.items.consumer.ManaCrystal
import cn.coostack.usefulmagic.items.consumer.ManaGlassBottle
import cn.coostack.usefulmagic.items.consumer.ManaRevive
import cn.coostack.usefulmagic.items.consumer.ManaStar
import cn.coostack.usefulmagic.items.consumer.PurpleManaCrystal
import cn.coostack.usefulmagic.items.consumer.PurpleManaStar
import cn.coostack.usefulmagic.items.consumer.RedManaCrystal
import cn.coostack.usefulmagic.items.consumer.RedManaStar
import cn.coostack.usefulmagic.items.consumer.SmallManaRevive
import cn.coostack.usefulmagic.items.consumer.SmallManaGlassBottle
import cn.coostack.usefulmagic.items.wands.AntiEntityWand
import cn.coostack.usefulmagic.items.wands.CopperWand
import cn.coostack.usefulmagic.items.wands.DiamondWand
import cn.coostack.usefulmagic.items.wands.GoldenWand
import cn.coostack.usefulmagic.items.wands.HealthReviveWand
import cn.coostack.usefulmagic.items.wands.IronWand
import cn.coostack.usefulmagic.items.wands.NetheriteWand
import cn.coostack.usefulmagic.items.wands.WandOfMeteorite
import cn.coostack.usefulmagic.items.wands.StoneWand
import cn.coostack.usefulmagic.items.wands.WoodenWand
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity

object UsefulMagicItems {
    val items = ArrayList<Item>()
    val DEBUGGER = register(
        "debugger", DebuggerItem()
    )
    val WOODEN_WAND = register(
        "wooden_wand", WoodenWand(
            Item.Settings()
                .maxCount(1)
                .maxDamage(60)
        )
    )

    val STONE_WAND = register(
        "stone_wand", StoneWand(
            Item.Settings()
                .maxCount(1)
                .maxDamage(140)
        )
    )

    val COPPER_WAND = register(
        "copper_wand", CopperWand(
            Item.Settings()
                .maxCount(1)
                .maxDamage(300)
        )
    )

    val IRON_WAND = register(
        "iron_wand", IronWand(
            Item.Settings()
                .maxCount(1)
                .maxDamage(550)
        )
    )

    val GOLDEN_WAND = register(
        "golden_wand", GoldenWand(
            Item.Settings()
                .maxCount(1)
                .maxDamage(250)
        )
    )

    val DIAMOND_WAND = register(
        "diamond_wand", DiamondWand(
            Item.Settings()
                .maxCount(1)
                .maxDamage(1200)
        )
    )

    val NETHERITE_WAND = register(
        "netherite_wand", NetheriteWand(
            Item.Settings()
                .maxCount(1)
                .fireproof()
                .maxDamage(3600)
        )
    )

    val WAND_OF_METEORITE = register(
        "wand_of_meteorite", WandOfMeteorite(
            Item.Settings()
                .maxCount(1)
                .maxDamage(3600)
                .fireproof()
                .rarity(Rarity.EPIC)
        )
    )

    val HEALTH_REVIVE_WAND = register(
        "health_revive_wand",
        HealthReviveWand(
            Item.Settings()
                .maxCount(1)
                .maxDamage(500)
                .rarity(Rarity.EPIC)
        )
    )
    val ANTI_ENTITY_WAND = register(
        "anti_entity_wand",
        AntiEntityWand(
            Item.Settings()
                .maxCount(1)
                .maxDamage(3600)
                .rarity(Rarity.EPIC)
        )
    )

    val SMALL_MANA_BOTTLE = register(
        "small_mana_bottle", SmallManaGlassBottle()
    )

    val SMALL_MANA_REVIVE = register(
        "small_mana_revive", SmallManaRevive(
            Item.Settings().recipeRemainder(SMALL_MANA_BOTTLE)
                .food(UsefulMagicFoodComponents.REVIVE)
                .maxCount(32)
        )
    )

    val MANA_BOTTLE = register(
        "mana_bottle", ManaGlassBottle()
    )

    val MANA_REVIVE = register(
        "mana_revive", ManaRevive(
            Item.Settings().recipeRemainder(MANA_BOTTLE)
                .food(UsefulMagicFoodComponents.REVIVE)
                .maxCount(16)
        )
    )

    val LARGE_MANA_BOTTLE = register(
        "large_mana_bottle", LargeManaBottle()
    )

    val LARGE_MANA_REVIVE = register(
        "large_mana_revive", LargeManaRevive(
            Item.Settings().recipeRemainder(LARGE_MANA_BOTTLE)
                .food(UsefulMagicFoodComponents.REVIVE)
                .maxCount(4)
        )
    )

    val MANA_STAR = register(
        "mana_star", ManaStar()
    )

    val PURPLE_MANA_STAR = register(
        "purple_mana_star", PurpleManaStar()
    )

    val RED_MANA_STAR = register(
        "red_mana_star", RedManaStar()
    )

    val MANA_CRYSTAL = register(
        "mana_crystal", ManaCrystal()
    )
    val PURPLE_MANA_CRYSTAL = register(
        "purple_mana_crystal", PurpleManaCrystal()
    )
    val RED_MANA_CRYSTAL = register(
        "red_mana_crystal", RedManaCrystal()
    )

    fun register(id: String, item: Item): Item {
        val res = Registry.register(Registries.ITEM, Identifier.of(UsefulMagic.MOD_ID, id), item)
        items.add(res)
        return res
    }

    fun init() {}
}