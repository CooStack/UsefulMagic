package cn.coostack.usefulmagic.items

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredItem
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.items.consumer.*
import cn.coostack.usefulmagic.items.misc.TutorialBookItem
import cn.coostack.usefulmagic.items.prop.*
import cn.coostack.usefulmagic.items.weapon.MagicAxe
import cn.coostack.usefulmagic.items.weapon.magic.*
import cn.coostack.usefulmagic.items.weapon.wands.MagicWand
import cn.coostack.usefulmagic.profile.PreferMagicData
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.AxeItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import java.util.function.Supplier

object UsefulMagicItems {
    val items = ArrayList<CommonDeferredItem>()

    // Keep the wand progression intact while lowering the top-end ceiling.
    private const val MAX_WAND_CHARGE_MULTIPLIER = 6.0
    private const val PREVIOUS_MAX_WAND_SPEED_FACTOR = 0.122
    private const val MAX_WAND_REDUCTION = 0.35
    private const val PREVIOUS_MAX_WAND_REDUCTION = 0.45
    private const val PREFER_FACTOR_SCALE = 0.85

    private fun scaledWandSpeedFactor(baseFactor: Double): Double {
        return baseFactor * ((1.0 / MAX_WAND_CHARGE_MULTIPLIER) / PREVIOUS_MAX_WAND_SPEED_FACTOR)
    }

    private fun scaledWandReduction(baseReduction: Double): Double {
        return if (baseReduction > 0.0) {
            baseReduction * (MAX_WAND_REDUCTION / PREVIOUS_MAX_WAND_REDUCTION)
        } else {
            baseReduction
        }
    }

    private fun scaledPrefer(
        damageFactor: Double,
        usageReductionFactor: Double,
        cdReductionFactor: Double,
        manaReductionFactor: Double,
    ): PreferMagicData {
        return PreferMagicData(
            damageFactor * PREFER_FACTOR_SCALE,
            usageReductionFactor * PREFER_FACTOR_SCALE,
            cdReductionFactor * PREFER_FACTOR_SCALE,
            manaReductionFactor * PREFER_FACTOR_SCALE,
        )
    }

    @JvmField
    val DEBUGGER = register(
        "debugger"
    ) {
        DebuggerItem()
    }

    @JvmField
    val BARRAGE_MAGIC = register(
        "barrage_magic"
    ) {
        BarrageMagic(
            Item.Properties()
                .stacksTo(1)
                .component(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get(), 1)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get(), 5)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get(), 12)
                .component(UsefulMagicDataComponentTypes.MAGIC_MIN_USAGE.get(), 2)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_DAMAGE.get(), 4.0)
                .component(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get(), 0)
        )
    }

    @JvmField
    val LASER_MAGIC = register(
        "laser_magic"
    ) {
        BeamMagic(
            Item.Properties()
                .stacksTo(1)
                .component(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get(), 1)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get(), 10)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get(), 16)
                .component(UsefulMagicDataComponentTypes.MAGIC_MIN_USAGE.get(), 2)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_DAMAGE.get(), 5.0)
                .component(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get(), 5)
        )
    }

    @JvmField
    val LIGHT_BEAM_MAGIC = register(
        "light_beam_magic"
    ) {
        LightBeamMagic(
            Item.Properties()
                .stacksTo(1)
                .component(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get(), 6)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get(), 50) // 50 pre-tick
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get(), 60)
                .component(UsefulMagicDataComponentTypes.MAGIC_MIN_USAGE.get(), 60)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_DAMAGE.get(), 3.0)
                .component(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get(), 50)
        )
    }


    @JvmField
    val SWORD_QI_MAGIC = register(
        "sword_qi_magic"
    ) {
        SwordQiMagic(
            Item.Properties()
                .stacksTo(1)
                .component(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get(), 3)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get(), 60)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get(), 64)
                .component(UsefulMagicDataComponentTypes.MAGIC_MIN_USAGE.get(), 15)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_DAMAGE.get(), 6.0)
                .component(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get(), 20)
        )
    }

    @JvmField
    val GOLDEN_MAGIC = register(
        "golden_magic"
    ) {
        GoldenMagic(
            Item.Properties()
                .stacksTo(1)
                .component(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get(), 3)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get(), 80)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get(), 120)
                .component(UsefulMagicDataComponentTypes.MAGIC_MIN_USAGE.get(), 10)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_DAMAGE.get(), 4.0)
                .component(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get(), 20)
        )
    }

    @JvmField
    val SWORD_FORMATION_MAGIC = register(
        "sword_formation_magic"
    ) {
        SwordFormationMagic(
            Item.Properties()
                .stacksTo(1)
                .component(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get(), 4)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get(), 350)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get(), 320)
                .component(UsefulMagicDataComponentTypes.MAGIC_MIN_USAGE.get(), 40)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_DAMAGE.get(), 8.0)
                .component(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get(), 500)
        )
    }

    @JvmField
    val METEORITE_MAGIC = register(
        "meteorite_magic"
    ) {
        MeteoriteMagic(
            Item.Properties()
                .stacksTo(1)
                .component(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get(), 5)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get(), 1200)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get(), 600)
                .component(UsefulMagicDataComponentTypes.MAGIC_MIN_USAGE.get(), 100)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_DAMAGE.get(), 200.0)
                .component(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get(), 1000)
        )
    }

    @JvmField
    val LIGHTNING_MAGIC = register("lightning_magic") {
        LightningMagic(
            Item.Properties()
                .stacksTo(1)
                .component(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get(), 5)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get(), 30)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get(), 60)
                .component(UsefulMagicDataComponentTypes.MAGIC_MIN_USAGE.get(), 4)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_DAMAGE.get(), 3.2)
                .component(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get(), 0)
        )
    }

    @JvmField
    val HEALTH_MAGIC = register("health_magic") {
        HealthMagic(
            Item.Properties()
                .stacksTo(1)
                .component(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get(), 5)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get(), 500)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get(), 600)
                .component(UsefulMagicDataComponentTypes.MAGIC_MIN_USAGE.get(), 100)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_DAMAGE.get(), -32.0)
                .component(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get(), 100)
        )
    }

    @JvmField
    val STARRY_MAGIC = register("starry_magic") {
        StarryMagic(
            Item.Properties()
                .stacksTo(1)
                .component(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get(), 6)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get(), 1800)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get(), 1200)
                .component(UsefulMagicDataComponentTypes.MAGIC_MIN_USAGE.get(), 120)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_DAMAGE.get(), 500.0)
                .component(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get(), 1500)
        )
    }

    @JvmField

    val ANTI_ENTITY_DOMAIN_MAGIC = register("anti_entity_domain_magic") {
        AntiEntityDomainMagic(
            Item.Properties()
                .stacksTo(1)
                .component(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get(), 6)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get(), 3000)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get(), 1200)
                .component(UsefulMagicDataComponentTypes.MAGIC_MIN_USAGE.get(), 180)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_DAMAGE.get(), 4.0)
                .component(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get(), 3000)
        )
    }

    @JvmField
    val EXPLOSION_MAGIC = register("explosion_magic") {
        ExplosionMagic(
            Item.Properties()
                .stacksTo(1)
                .component(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get(), 8)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get(), 5000)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get(), 5000)
                .component(UsefulMagicDataComponentTypes.MAGIC_MIN_USAGE.get(), 300)
                .component(UsefulMagicDataComponentTypes.MAGIC_BASE_DAMAGE.get(), 1024.0)
                .component(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get(), 4000)
        )
    }

    @JvmField
    val WOODEN_WAND = register(
        "wooden_wand"
    ) {
        MagicWand(
            Item.Properties()
                .stacksTo(1)
                .component(
                    UsefulMagicDataComponentTypes.WAND_LEVEL.get(), 1
                ).component(
                    UsefulMagicDataComponentTypes.WAND_REDUCTION.get(), scaledWandReduction(0.0)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get(), scaledWandSpeedFactor(1.25)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_MAGIC.get(), ItemStack.EMPTY
                )
                .component(
                    UsefulMagicDataComponentTypes.WAND_PREFER.get(),
                    scaledPrefer(0.0, 0.0, 0.0, 0.0)
                )
        )
    }

    @JvmField
    val STONE_WAND = register(
        "stone_wand"
    ) {
        MagicWand(
            Item.Properties()
                .stacksTo(1)
                .component(
                    UsefulMagicDataComponentTypes.WAND_LEVEL.get(), 1
                ).component(
                    UsefulMagicDataComponentTypes.WAND_REDUCTION.get(), scaledWandReduction(0.05)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get(), scaledWandSpeedFactor(0.91)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_MAGIC.get(), ItemStack.EMPTY
                )
                .component(
                    UsefulMagicDataComponentTypes.WAND_PREFER.get(),
                    scaledPrefer(0.02, 0.01, 0.01, 0.01)
                        .putPrefer(LASER_MAGIC.getItem(), 5)
                )
        )
    }

    @JvmField
    val COPPER_WAND = register(
        "copper_wand"
    ) {
        MagicWand(
            Item.Properties()
                .stacksTo(1)
                .component(
                    UsefulMagicDataComponentTypes.WAND_LEVEL.get(), 2
                ).component(
                    UsefulMagicDataComponentTypes.WAND_REDUCTION.get(), scaledWandReduction(0.1)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get(), scaledWandSpeedFactor(0.67)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_MAGIC.get(), ItemStack.EMPTY
                )
                .component(
                    UsefulMagicDataComponentTypes.WAND_PREFER.get(),
                    scaledPrefer(0.05, 0.02, 0.1, 0.1)
                )
        )
    }

    @JvmField
    val IRON_WAND = register(
        "iron_wand"
    ) {
        MagicWand(
            Item.Properties()
                .stacksTo(1)
                .component(
                    UsefulMagicDataComponentTypes.WAND_LEVEL.get(), 3
                ).component(
                    UsefulMagicDataComponentTypes.WAND_REDUCTION.get(), scaledWandReduction(0.18)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get(), scaledWandSpeedFactor(0.45)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_MAGIC.get(), ItemStack.EMPTY
                )
                .component(
                    UsefulMagicDataComponentTypes.WAND_PREFER.get(),
                    scaledPrefer(0.1, 0.05, 0.1, 0.1)
                        .putPrefer(SWORD_QI_MAGIC.getItem(), 5)
                )
        )
    }

    @JvmField
    val GOLDEN_WAND = register(
        "golden_wand"
    ) {
        MagicWand(
            Item.Properties()
                .stacksTo(1)
                .component(
                    UsefulMagicDataComponentTypes.WAND_LEVEL.get(), 3
                ).component(
                    UsefulMagicDataComponentTypes.WAND_REDUCTION.get(), scaledWandReduction(-0.1)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get(), scaledWandSpeedFactor(0.238)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_MAGIC.get(), ItemStack.EMPTY
                )
                .component(
                    UsefulMagicDataComponentTypes.WAND_PREFER.get(),
                    scaledPrefer(0.1, 0.1, 0.1, 0.1)
                        .putPrefer(GOLDEN_MAGIC.getItem(), 5)
                )
        )
    }

    @JvmField
    val DIAMOND_WAND = register(
        "diamond_wand"
    ) {
        MagicWand(
            Item.Properties()
                .stacksTo(1)
                .component(
                    UsefulMagicDataComponentTypes.WAND_LEVEL.get(), 4
                ).component(
                    UsefulMagicDataComponentTypes.WAND_REDUCTION.get(), scaledWandReduction(0.3)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get(), scaledWandSpeedFactor(0.333)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_MAGIC.get(), ItemStack.EMPTY
                )
                .component(
                    UsefulMagicDataComponentTypes.WAND_PREFER.get(),
                    scaledPrefer(0.1, 0.1, 0.1, 0.1)
                        .putPrefer(BARRAGE_MAGIC.getItem(), 2)
                        .putPrefer(LASER_MAGIC.getItem(), 3)
                        .putPrefer(SWORD_FORMATION_MAGIC.getItem(), 3)
                )
        )
    }

    @JvmField
    val NETHERITE_WAND = register(
        "netherite_wand"
    ) {
        MagicWand(
            Item.Properties()
                .stacksTo(1)
                .fireResistant()
                .component(
                    UsefulMagicDataComponentTypes.WAND_LEVEL.get(), 5
                ).component(
                    UsefulMagicDataComponentTypes.WAND_REDUCTION.get(), scaledWandReduction(0.4)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get(), scaledWandSpeedFactor(0.125)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_MAGIC.get(), ItemStack.EMPTY
                )
                .component(
                    UsefulMagicDataComponentTypes.WAND_PREFER.get(),
                    scaledPrefer(0.1, 0.12, 0.1, 0.1)
                        .putPrefer(BARRAGE_MAGIC.getItem(), 2)
                        .putPrefer(LASER_MAGIC.getItem(), 3)
                        .putPrefer(LIGHTNING_MAGIC.getItem(), 1)
                        .putPrefer(SWORD_FORMATION_MAGIC.getItem(), 5)
                )
        )
    }

    @JvmField
    val MAGIC_AXE = register(
        "magic_axe"
    ) {
        MagicAxe(
            Item.Properties()
                .attributes(
                    AxeItem.createAttributes(
                        UsefulMagicToolMaterials.MAGIC,
                        2.0F, -2.2F
                    )
                ).fireResistant()
                .rarity(Rarity.EPIC)
        )
    }

    @JvmField
    val WAND_OF_METEORITE = register(
        "wand_of_meteorite"
    ) {
        MagicWand(
            Item.Properties()
                .stacksTo(1)
                .fireResistant()
                .component(
                    UsefulMagicDataComponentTypes.WAND_LEVEL.get(), 6
                ).component(
                    UsefulMagicDataComponentTypes.WAND_REDUCTION.get(), scaledWandReduction(0.4)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get(), scaledWandSpeedFactor(0.125)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_MAGIC.get(), ItemStack.EMPTY
                )
                .component(
                    UsefulMagicDataComponentTypes.WAND_PREFER.get(),
                    scaledPrefer(0.1, 0.1, 0.1, 0.1)
                        .putPrefer(SWORD_FORMATION_MAGIC.getItem(), 2)
                        .putPrefer(METEORITE_MAGIC.getItem(), 5)
                        .putPrefer(STARRY_MAGIC.getItem(), 3)
                )
        )
    }

    @JvmField
    val STARRY_WAND = register(
        "starry_wand"
    ) {
        MagicWand(
            Item.Properties()
                .stacksTo(1)
                .fireResistant()
                .component(
                    UsefulMagicDataComponentTypes.WAND_LEVEL.get(), 7
                ).component(
                    UsefulMagicDataComponentTypes.WAND_REDUCTION.get(), scaledWandReduction(0.42)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get(), scaledWandSpeedFactor(0.124)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_MAGIC.get(), ItemStack.EMPTY
                )
                .component(
                    UsefulMagicDataComponentTypes.WAND_PREFER.get(),
                    scaledPrefer(0.1, 0.1, 0.1, 0.1)
                        .putPrefer(SWORD_FORMATION_MAGIC.getItem(), 4)
                        .putPrefer(METEORITE_MAGIC.getItem(), 3)
                        .putPrefer(STARRY_MAGIC.getItem(), 5)
                        .putPrefer(ANTI_ENTITY_DOMAIN_MAGIC.getItem(), 5)
                )
        )
    }

    @JvmField
    val HEALTH_REVIVE_WAND = register(
        "health_revive_wand"
    ) {
        MagicWand(
            Item.Properties()
                .stacksTo(1)
                .fireResistant()
                .component(
                    UsefulMagicDataComponentTypes.WAND_LEVEL.get(), 5
                ).component(
                    UsefulMagicDataComponentTypes.WAND_REDUCTION.get(), scaledWandReduction(0.4)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get(), scaledWandSpeedFactor(0.125)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_MAGIC.get(), ItemStack.EMPTY
                )
                .component(
                    UsefulMagicDataComponentTypes.WAND_PREFER.get(),
                    scaledPrefer(0.06, 0.05, 0.04, 0.06)
                        .putPrefer(LIGHTNING_MAGIC.getItem(), -5)
                        .putPrefer(LASER_MAGIC.getItem(), -5)
                        .putPrefer(BARRAGE_MAGIC.getItem(), -5)
                        .putPrefer(SWORD_FORMATION_MAGIC.getItem(), -5)
                        .putPrefer(HEALTH_MAGIC.getItem(), 5)
                )
        )
    }

    @JvmField
    val ANTI_ENTITY_WAND = register(
        "anti_entity_wand"
    ) {
        MagicWand(
            Item.Properties()
                .stacksTo(1)
                .fireResistant()
                .component(
                    UsefulMagicDataComponentTypes.WAND_LEVEL.get(), 6
                ).component(
                    UsefulMagicDataComponentTypes.WAND_REDUCTION.get(), scaledWandReduction(0.4)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get(), scaledWandSpeedFactor(0.125)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_MAGIC.get(), ItemStack.EMPTY
                )
                .component(
                    UsefulMagicDataComponentTypes.WAND_PREFER.get(),
                    scaledPrefer(0.1, 0.15, 0.1, 0.15)
                        .putPrefer(LASER_MAGIC.getItem(), -3)
                        .putPrefer(BARRAGE_MAGIC.getItem(), 3)
                        .putPrefer(SWORD_FORMATION_MAGIC.getItem(), 3)
                        .putPrefer(HEALTH_MAGIC.getItem(), -5)
                        .putPrefer(ANTI_ENTITY_DOMAIN_MAGIC.getItem(), 5)
                )
        )
    }

    @JvmField
    val LIGHTNING_WAND = register(
        "lightning_wand"
    ) {
        MagicWand(
            Item.Properties()
                .stacksTo(1)
                .fireResistant()
                .component(
                    UsefulMagicDataComponentTypes.WAND_LEVEL.get(), 6
                ).component(
                    UsefulMagicDataComponentTypes.WAND_REDUCTION.get(), scaledWandReduction(0.4)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get(), scaledWandSpeedFactor(0.125)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_MAGIC.get(), ItemStack.EMPTY
                )
                .component(
                    UsefulMagicDataComponentTypes.WAND_PREFER.get(),
                    scaledPrefer(0.1, 0.15, 0.1, 0.15)
                        .putPrefer(LASER_MAGIC.getItem(), 3)
                        .putPrefer(BARRAGE_MAGIC.getItem(), -5)
                        .putPrefer(LIGHTNING_MAGIC.getItem(), 5)
                        .putPrefer(HEALTH_MAGIC.getItem(), 2)
                        .putPrefer(LIGHT_BEAM_MAGIC.getItem(), 5)
                )
        )
    }

    @JvmField
    val EXPLOSION_WAND = register(
        "explosion_wand"
    ) {
        MagicWand(
            Item.Properties()
                .stacksTo(1)
                .fireResistant()
                .component(
                    UsefulMagicDataComponentTypes.WAND_LEVEL.get(), 8
                ).component(
                    UsefulMagicDataComponentTypes.WAND_REDUCTION.get(), scaledWandReduction(0.45)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get(), scaledWandSpeedFactor(0.122)
                ).component(
                    UsefulMagicDataComponentTypes.WAND_MAGIC.get(), ItemStack.EMPTY
                )
                .component(
                    UsefulMagicDataComponentTypes.WAND_PREFER.get(),
                    scaledPrefer(0.12, 0.15, 0.13, 0.15)
                        .putPrefer(LASER_MAGIC.getItem(), -5)
                        .putPrefer(BARRAGE_MAGIC.getItem(), -5)
                        .putPrefer(LIGHTNING_MAGIC.getItem(), 2)
                        .putPrefer(SWORD_FORMATION_MAGIC.getItem(), 3)
                        .putPrefer(HEALTH_MAGIC.getItem(), 3)
                        .putPrefer(ANTI_ENTITY_DOMAIN_MAGIC.getItem(), 3)
                        .putPrefer(STARRY_MAGIC.getItem(), 3)
                        .putPrefer(METEORITE_MAGIC.getItem(), 4)
                        .putPrefer(EXPLOSION_MAGIC.getItem(), 5)
                        .putPrefer(LIGHT_BEAM_MAGIC.getItem(), 5)
                )
        )
    }

    @JvmField
    val SMALL_MANA_BOTTLE = register(
        "small_mana_bottle"
    ) { SmallManaGlassBottle() }

    @JvmField
    val SMALL_MANA_REVIVE = register(
        "small_mana_revive"
    ) {
        SmallManaRevive(
            Item.Properties()
                .craftRemainder(SMALL_MANA_BOTTLE.getItem())
                .food(UsefulMagicFoodComponents.REVIVE)
                .stacksTo(32)
        )
    }

    @JvmField
    val MANA_BOTTLE = register(
        "mana_bottle"
    ) { ManaGlassBottle() }

    @JvmField
    val MANA_REVIVE = register(
        "mana_revive", {
            ManaRevive(
                Item.Properties().craftRemainder(MANA_BOTTLE.getItem())
                    .food(UsefulMagicFoodComponents.REVIVE)
                    .stacksTo(16)
            )
        }
    )

    @JvmField
    val LARGE_MANA_BOTTLE = register(
        "large_mana_bottle"
    ) { LargeManaBottle() }

    @JvmField
    val LARGE_MANA_REVIVE = register(
        "large_mana_revive"
    ) {
        LargeManaRevive(
            Item.Properties().craftRemainder(LARGE_MANA_BOTTLE.getItem())
                .food(UsefulMagicFoodComponents.REVIVE)
                .stacksTo(4)
        )
    }

    @JvmField
    val MANA_STAR = register(
        "mana_star"
    ) { ManaStar() }

    @JvmField
    val PURPLE_MANA_STAR = register(
        "purple_mana_star"
    ) { PurpleManaStar() }

    @JvmField
    val RED_MANA_STAR = register(
        "red_mana_star"
    ) { RedManaStar() }

    @JvmField
    val MANA_CRYSTAL = register(
        "mana_crystal"
    ) { ManaCrystal() }

    @JvmField
    val PURPLE_MANA_CRYSTAL = register(
        "purple_mana_crystal"
    ) { PurpleManaCrystal() }

    @JvmField
    val RED_MANA_CRYSTAL = register(
        "red_mana_crystal"
    ) { RedManaCrystal() }

    @JvmField
    val DEFEND_CORE = register(
        "defend_core"
    ) { DefendCoreItem() }

    @JvmField
    val FLYING_RUNE = register(
        "flying_rune"
    ) { FlyingRuneItem() }

    @JvmField
    val TUTORIAL_BOOK = register(
        "tutorial_book"
    ) { TutorialBookItem() }

    @JvmField
    val FRIEND_BOARD = register(
        "friend_board"
    ) { FriendBoardItem() }

    @JvmField
    val SKY_FALLING_RUNE = register(
        "sky_falling_rune", { SkyFallingRuneItem() }
    )

    @JvmField
    val MAGIC_EYE_SPAWNER = register(
        "magic_eye_spawner"
    ) { MagicEyeSpawner(Item.Properties().stacksTo(16)) }

    fun register(id: String, item: Supplier<Item>): CommonDeferredItem {
        val common = CommonDeferredItem(ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, id), item)
        items.add(common)
        return common
    }

    fun init() {}
}
