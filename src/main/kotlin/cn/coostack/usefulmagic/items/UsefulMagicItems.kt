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
import cn.coostack.usefulmagic.items.prop.DefendCoreItem
import cn.coostack.usefulmagic.items.prop.FlyingRuneItem
import cn.coostack.usefulmagic.items.misc.TutorialBookItem
import cn.coostack.usefulmagic.items.prop.FriendBoardItem
import cn.coostack.usefulmagic.items.prop.SkyFallingRuneItem
import cn.coostack.usefulmagic.items.weapon.MagicAxe
import cn.coostack.usefulmagic.items.weapon.wands.AntiEntityWand
import cn.coostack.usefulmagic.items.weapon.wands.CopperWand
import cn.coostack.usefulmagic.items.weapon.wands.DiamondWand
import cn.coostack.usefulmagic.items.weapon.wands.ExplosionWand
import cn.coostack.usefulmagic.items.weapon.wands.GoldenWand
import cn.coostack.usefulmagic.items.weapon.wands.HealthReviveWand
import cn.coostack.usefulmagic.items.weapon.wands.IronWand
import cn.coostack.usefulmagic.items.weapon.wands.LightningWand
import cn.coostack.usefulmagic.items.weapon.wands.NetheriteWand
import cn.coostack.usefulmagic.items.weapon.wands.WandOfMeteorite
import cn.coostack.usefulmagic.items.weapon.wands.StoneWand
import cn.coostack.usefulmagic.items.weapon.wands.WoodenWand
import net.minecraft.item.AxeItem
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

    val MAGIC_AXE = register(
        "magic_axe", MagicAxe(
            Item.Settings()
                .attributeModifiers(
                    AxeItem.createAttributeModifiers(
                        UsefulMagicToolMaterials.MAGIC,
                        2.0F, -2.2F
                    )
                ).fireproof()
                .rarity(Rarity.EPIC)
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
                .maxDamage(1500)
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

    val LIGHTNING_WAND = register(
        "lightning_wand", LightningWand(
            Item.Settings()
                .maxCount(1)
                .maxDamage(1200)
                .rarity(Rarity.EPIC)
        )
    )

    val EXPLOSION_WAND = register(
        "explosion_wand", ExplosionWand(
            Item.Settings()
                .maxCount(1)
                .maxDamage(7200)
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
    val DEFEND_CORE = register(
        "defend_core", DefendCoreItem()
    )

    val FLYING_RUNE = register(
        "flying_rune", FlyingRuneItem()
    )

    val TUTORIAL_BOOK = register(
        "tutorial_book", TutorialBookItem()
    )

    val FRIEND_BOARD = register(
        "friend_board", FriendBoardItem()
    )

    val SKY_FALLING_RUNE = register(
        "sky_falling_rune", SkyFallingRuneItem()
    )

    fun register(id: String, item: Item): Item {
        val res = Registry.register(Registries.ITEM, Identifier.of(UsefulMagic.MOD_ID, id), item)
        items.add(res)
        return res
    }

    fun init() {}
}