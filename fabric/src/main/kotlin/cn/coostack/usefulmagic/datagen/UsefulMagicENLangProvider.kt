package cn.coostack.usefulmagic.datagen

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredItem
import cn.coostack.usefulmagic.extend.add
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.core.HolderLookup
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
            // item信息
            add(UsefulMagicItems.DEBUGGER, "usefulmagic debugger")
            add(UsefulMagicItems.WOODEN_WAND, "wooden wand")
            add(UsefulMagicItems.STONE_WAND, "stone wand")
            add(UsefulMagicItems.COPPER_WAND, "copper wand")
            add(UsefulMagicItems.IRON_WAND, "iron wand")
            add(UsefulMagicItems.GOLDEN_WAND, "golden wand")
            add(UsefulMagicItems.DIAMOND_WAND, "diamond wand")
            add(UsefulMagicItems.NETHERITE_WAND, "netherite wand")
            add(UsefulMagicItems.WAND_OF_METEORITE, "meteorite wand")
            add(UsefulMagicItems.STARRY_WAND, "starry wand")
            add(UsefulMagicItems.ANTI_ENTITY_WAND, "anti entity wand")
            add(UsefulMagicItems.HEALTH_REVIVE_WAND, "health revive wand")
            add(UsefulMagicItems.MAGIC_AXE, "magic axe")
            add(UsefulMagicItems.LIGHTNING_WAND, "lighting wand")
            add(UsefulMagicItems.EXPLOSION_WAND, "explosion wand")
            add(UsefulMagicItems.SMALL_MANA_BOTTLE, "small mana bottle")
            add(UsefulMagicItems.SMALL_MANA_REVIVE, "small mana revive bottle")
            add(UsefulMagicItems.MANA_REVIVE, "mana revive bottle")
            add(UsefulMagicItems.MANA_BOTTLE, "mana bottle")
            add(UsefulMagicItems.LARGE_MANA_BOTTLE, "large mana bottle")
            add(UsefulMagicItems.LARGE_MANA_REVIVE, "large mana revive bottle")
            add(UsefulMagicItems.MANA_STAR, "mana star level 1")
            add(UsefulMagicItems.MANA_CRYSTAL, "mana crystal level 1")
            add(UsefulMagicItems.PURPLE_MANA_STAR, "mana star level 2")
            add(UsefulMagicItems.PURPLE_MANA_CRYSTAL, "mana crystal level 2")
            add(UsefulMagicItems.RED_MANA_STAR, "mana star level 3")
            add(UsefulMagicItems.RED_MANA_CRYSTAL, "mana crystal level 3")
            add(UsefulMagicItems.DEFEND_CORE, "magic defend core")
            add(UsefulMagicItems.FLYING_RUNE, "flying rune")
            add(UsefulMagicItems.TUTORIAL_BOOK, "usefulmagic guild book")
            add(UsefulMagicItems.FRIEND_BOARD, "friend board")
            add(UsefulMagicItems.SKY_FALLING_RUNE, "sky falling rune")
            // group
            add("item.useful_magic_main", "usefulmagic")
            // block 信息
            add(UsefulMagicBlocks.ALTAR_BLOCK, "altar block")
            add(UsefulMagicBlocks.ALTAR_BLOCK_CORE, "altar block core")
            add(UsefulMagicBlocks.MAGIC_CORE, "magic core")
            add(UsefulMagicBlocks.FORMATION_CORE_BLOCK, "formation core")
            add(UsefulMagicBlocks.DEFEND_CRYSTAL_BLOCK, "defend crystal")
            add(UsefulMagicBlocks.RECOVER_CRYSTAL_BLOCK, "recover crystal")
            add(UsefulMagicBlocks.SWORD_ATTACK_CRYSTAL_BLOCK, "sword crystal")
            add(UsefulMagicBlocks.ENERGY_CRYSTAL_BLOCK, "energy crystal")

            // entity 信息
            add(UsefulMagicEntityTypes.MAGIC_BOOK_ENTITY_TYPE, "magic book")

            // lore信息
            add("screen.shift", "§7push shift to look more information")
            add("item.wand.mana", "§acurrent mana: §b%mana%")
            add("item.wooden_wand.description", "§7蓄力释放一个具有攻击性的粒子")
            add("item.stone_wand.description", "§7蓄力释放一条射线,击中实体会自动攻击附近的2个实体")
            add("item.copper_wand.description", "§7蓄力释放 击中物体或者实体时会召唤小型陨石")
            add("item.iron_wand.description", "§7蓄力释放一个魔法球 攻击造成3倍伤害, 一定时间后会分裂成3个小球")
            add("item.golden_wand.description", "§7蓄力释放一个魔法球 不断对周围实体发射射线 造成伤害")
            add("item.diamond_wand.description", "§7蓄力释放粒子剑气,攻击前方的敌人 剑气能追踪实体")
            add("item.netherite_wand.description", "§7蓄力释放更多粒子剑气,攻击前方的敌人 剑气能追踪实体")
            add("item.wand_of_meteorite.description", "§7蓄力从空中召唤一个陨星,没人知道这个魔杖是怎么做到的")
            add("item.anti_entity_wand.description", "§7召唤巨大法阵,不断发射弹幕攻击敌人")
            add("item.lightning_wand.description", "§7化身雷电法王,电击周围的生物")
            add("item.explosion_wand.description", "§7蓄力施放爆裂魔法 Explosion!")
            add("item.starry_wand.description", "§7你将召唤群星，攻向大地")
            add(
                "item.health_revive_wand.description",
                "§7蓄力给自己造成生命恢复效果 对着实体可以让实体恢复生命 对亡灵生物有奇效"
            )
            add("item.mana_star.description", "§7使用可以增加20点魔力上限值 最高可添加到500点")
            add("item.mana_crystal.description", "§7使用可以增加1点魔力恢复速率 最高可添加到10点")
            add(
                "item.purple_mana_star.description",
                "§7使用可以增加50点魔力上限值,只有达到500以上魔力值可以使用 最高可添加到1500点"
            )
            add(
                "item.red_mana_star.description",
                "§7使用可以增加100点魔力上限值,只有达到1500以上魔力值可以使用 最高可添加到4500点"
            )
            add(
                "item.purple_mana_crystal.description",
                "§7使用可以增加2点魔力恢复速率,只有达到10点魔力恢复速率才可以使用 最高可添加到30点"
            )
            add(
                "item.red_mana_crystal.description",
                "§7使用可以增加3点魔力恢复速率,只有达到30以上魔力值可以使用 最高可添加到60点"
            )
            add("item.wand.damage", "§7伤害: §c%amount%§7点")
            add("item.health_wand.amount", "§7恢复生命: §a%amount%§7点")
            add("item.wand.cost", "§7消耗: §b%cost%§7点魔力")
            add("item.small_mana_glass_bottle.usage", "§7右键注聚魔核心装填药水")
            add("item.small_mana_bottle.revive", "§7喝下恢复§b100§7点魔力值")
            add("item.mana_glass_bottle.usage", "§7右键注聚魔核心装填药水")
            add("item.mana_bottle.revive", "§7喝下恢复§b800§7点魔力值")
            add("item.large_mana_bottle.revive", "§7喝下恢复§b1500§7点魔力值")
            add("item.large_mana_glass_bottle.usage", "§7右键注聚魔核心装填药水")
            add("item.large_mana_bottle.can_use_count", "§7剩余使用次数 §b%count%")
            add("item.defend_core_enabled", "§7current status: %enabled%")
            add("item.flying_rune_enabled", "§7current status: %enabled%")
            add("item.usefulmagic_enabled", "§aenable")
            add("item.usefulmagic_disabled", "§cdisable")

            //sounds信息
            add("sounds.usefulmagic.electric_effect", "electric hit")
            add("sounds.usefulmagic.magic_activate", "magic activate")
            add("sounds.usefulmagic.magic_sword", "magic sword hit")
            add("sounds.usefulmagic.sky_falling_magic_idle", "sky falling magic idle")
            add("sounds.usefulmagic.sky_falling_magic_start", "sky falling magic start")
            add("sounds.usefulmagic.defend_shield_hit", "defend magic defended")
            add("sounds.usefulmagic.magic_explode", "magic explode")
            add("sounds.usefulmagic.star", "star shining")
            // gui
            add("screen.title.friend_manager_title", "friend manager")
        }
    }
}