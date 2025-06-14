package cn.coostack.usefulmagic.datagen

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.entity.UsefulMagicEntityLayers
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class UsefulMagicLangProvider(
    dataOutput: FabricDataOutput,
    registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricLanguageProvider(dataOutput, "zh_cn", registryLookup) {
    override fun generateTranslations(
        lookup: RegistryWrapper.WrapperLookup,
        builder: TranslationBuilder
    ) {

        builder.apply {
            // item信息
            add(UsefulMagicItems.DEBUGGER, "实用魔法调试器")
            add(UsefulMagicItems.WOODEN_WAND, "木之魔杖")
            add(UsefulMagicItems.STONE_WAND, "石之射线魔杖")
            add(UsefulMagicItems.COPPER_WAND, "铜锈魔杖")
            add(UsefulMagicItems.IRON_WAND, "铁魔法杖")
            add(UsefulMagicItems.GOLDEN_WAND, "金魔法杖")
            add(UsefulMagicItems.DIAMOND_WAND, "钻石法杖")
            add(UsefulMagicItems.NETHERITE_WAND, "下界合金法杖")
            add(UsefulMagicItems.WAND_OF_METEORITE, "陨星法杖")
            add(UsefulMagicItems.ANTI_ENTITY_WAND, "反实体魔杖")
            add(UsefulMagicItems.HEALTH_REVIVE_WAND, "再生法杖")
            add(UsefulMagicItems.MAGIC_AXE, "注魔斧")
            add(UsefulMagicItems.LIGHTNING_WAND, "雷霆法杖")
            add(UsefulMagicItems.EXPLOSION_WAND, "爆裂法杖")
            add(UsefulMagicItems.SMALL_MANA_BOTTLE, "小型小水瓶")
            add(UsefulMagicItems.SMALL_MANA_REVIVE, "小型魔力恢复药水")
            add(UsefulMagicItems.MANA_REVIVE, "魔力恢复药水")
            add(UsefulMagicItems.MANA_BOTTLE, "药水瓶")
            add(UsefulMagicItems.LARGE_MANA_BOTTLE, "大药水瓶")
            add(UsefulMagicItems.LARGE_MANA_REVIVE, "大型魔力恢复药水")
            add(UsefulMagicItems.MANA_STAR, "一级魔力水晶")
            add(UsefulMagicItems.MANA_CRYSTAL, "一级聚魔水晶")
            add(UsefulMagicItems.PURPLE_MANA_STAR, "二级魔力水晶")
            add(UsefulMagicItems.PURPLE_MANA_CRYSTAL, "二级聚魔水晶")
            add(UsefulMagicItems.RED_MANA_STAR, "三级魔力水晶")
            add(UsefulMagicItems.RED_MANA_CRYSTAL, "三级聚魔水晶")
            add(UsefulMagicItems.DEFEND_CORE, "魔法防御核心")
            add(UsefulMagicItems.FLYING_RUNE, "飞行符文")
            // group
            add("item.useful_magic_main", "实用魔法")
            // block 信息
            add(UsefulMagicBlocks.ALTAR_BLOCK, "注魔祭坛")
            add(UsefulMagicBlocks.ALTAR_BLOCK_CORE, "注魔输出平台")
            add(UsefulMagicBlocks.MAGIC_CORE, "聚魔核心")

            // entity 信息
            add(UsefulMagicEntityTypes.MAGIC_BOOK_ENTITY_TYPE,"禁忌魔典")

            // lore信息
            add("screen.shift", "§7按下shift查看更多信息")
            add("item.wand.mana", "§a当前魔力值: §b%mana%")
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
            add("item.defend_core_enabled", "§7当前状态: %enabled%")
            add("item.flying_rune_enabled", "§7当前状态: %enabled%")
            add("item.usefulmagic_enabled", "§a已启用")
            add("item.usefulmagic_disabled", "§c未启用")

            //sounds信息
            add("sounds.usefulmagic.electric_effect","电击")
        }
    }
}