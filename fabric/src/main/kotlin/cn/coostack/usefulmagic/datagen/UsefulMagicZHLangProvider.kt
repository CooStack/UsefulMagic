package cn.coostack.usefulmagic.datagen

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageTypes
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.extend.add
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.core.HolderLookup
import net.minecraft.resources.ResourceKey
import net.minecraft.world.damagesource.DamageType
import java.util.concurrent.CompletableFuture

class UsefulMagicZHLangProvider(
    dataOutput: FabricDataOutput,
    registryLookup: CompletableFuture<HolderLookup.Provider>
) : FabricLanguageProvider(dataOutput, "zh_cn", registryLookup) {
    override fun generateTranslations(
        lookup: HolderLookup.Provider,
        builder: TranslationBuilder
    ) {

        builder.apply {
            // item信息
            add(UsefulMagicItems.DEBUGGER, "实用魔法调试器")
            add(UsefulMagicItems.WOODEN_WAND, "木魔杖")
            add(UsefulMagicItems.STONE_WAND, "石魔杖")
            add(UsefulMagicItems.COPPER_WAND, "铜锈魔杖")
            add(UsefulMagicItems.IRON_WAND, "铁魔法杖")
            add(UsefulMagicItems.GOLDEN_WAND, "金魔法杖")
            add(UsefulMagicItems.DIAMOND_WAND, "钻石法杖")
            add(UsefulMagicItems.NETHERITE_WAND, "下界合金法杖")
            add(UsefulMagicItems.WAND_OF_METEORITE, "陨星法杖")
            add(UsefulMagicItems.STARRY_WAND, "群星法杖")
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
            add(UsefulMagicItems.TUTORIAL_BOOK, "实用魔法手册")
            add(UsefulMagicItems.FRIEND_BOARD, "好友令牌")
            add(UsefulMagicItems.SKY_FALLING_RUNE, "超位魔法「天空坠落」 符文")
            add(UsefulMagicItems.MAGIC_EYE_SPAWNER, "凝视之眼")
            // group
            add("item.useful_magic_main", "实用魔法")
            // block 信息
            add(UsefulMagicBlocks.ALTAR_BLOCK, "注魔祭坛")
            add(UsefulMagicBlocks.ALTAR_BLOCK_CORE, "注魔输出平台")
            add(UsefulMagicBlocks.MAGIC_CORE, "聚魔核心")
            add(UsefulMagicBlocks.FORMATION_CORE_BLOCK, "阵基")
            add(UsefulMagicBlocks.DEFEND_CRYSTAL_BLOCK, "防御水晶")
            add(UsefulMagicBlocks.RECOVER_CRYSTAL_BLOCK, "聚魔水晶")
            add(UsefulMagicBlocks.SWORD_ATTACK_CRYSTAL_BLOCK, "剑气水晶")
            add(UsefulMagicBlocks.ENERGY_CRYSTAL_BLOCK, "能源水晶")

            add(UsefulMagicItems.BARRAGE_MAGIC, "魔法飞弹法术")
            add(UsefulMagicItems.LASER_MAGIC, "激光法术")
            add(UsefulMagicItems.LIGHT_BEAM_MAGIC, "光束法术")
            add(UsefulMagicItems.SWORD_QI_MAGIC, "剑气法术")
            add(UsefulMagicItems.GOLDEN_MAGIC, "黄金魔法")
            add(UsefulMagicItems.SWORD_FORMATION_MAGIC, "剑阵魔法")
            add(UsefulMagicItems.METEORITE_MAGIC, "陨星魔法")
            add(UsefulMagicItems.LIGHTNING_MAGIC, "雷电法术")
            add(UsefulMagicItems.HEALTH_MAGIC, "生命法术")
            add(UsefulMagicItems.STARRY_MAGIC, "群星魔法")
            add(UsefulMagicItems.ANTI_ENTITY_DOMAIN_MAGIC, "反实体领域魔法")
            add(UsefulMagicItems.EXPLOSION_MAGIC, "§c§l爆裂魔法")

            // entity 信息
            add(UsefulMagicEntityTypes.MAGIC_BOOK_ENTITY_TYPE, "禁忌魔典")
            add(UsefulMagicEntityTypes.MAGIC_DRAGON_ENTITY_TYPE, "渊瞳魔龙")
            add(UsefulMagicEntityTypes.MAGIC_EYE_ENTITY_TYPE, "渊瞳之眼")
            add(UsefulMagicEntityTypes.MAGIC_SUB_EYE_ENTITY_TYPE, "渊瞳珍珠")
            add(UsefulMagicEntityTypes.MAGIC_HEART_ENTITY_TYPE, "渊瞳之心")

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
            add("item.defend_core_enabled", "§7当前状态: %enabled%")
            add("item.flying_rune_enabled", "§7当前状态: %enabled%")
            add("item.usefulmagic_enabled", "§a已启用")
            add("item.usefulmagic_disabled", "§c未启用")
            add("effect.usefulmagic.freeze", "冻结")
            add("effect.usefulmagic.magic_sealed", "魔能失调")

            add("item.usefulmagic.wand.level", "§7法杖等级: %s")
            add("item.usefulmagic.wand.reduction", "§7魔力消耗减免: %s")
            add("item.usefulmagic.wand.factor", "§7释放速度倍率: %s")
            add("item.usefulmagic.wand.cost", "§7释放一次魔法需要的魔力值: %s %s")
            add("item.usefulmagic.wand.charging_time", "§7蓄力时间: %s %s")
            add("item.usefulmagic.wand.cd", "§7释放后冷却时间: %s %s")
            add("item.usefulmagic.wand.damage", "§7法术伤害: %s %s")
            add("item.usefulmagic.wand.load_magic", "§7装载的法术: ")

            // prefer info
            add("item.usefulmagic.wand.prefer.info", "§7魔法偏好程度: %s")

            add("item.usefulmagic.wand.usage_install", "§7在背包中像收纳袋一样右键法杖，可将法术装填进去")
            add("item.usefulmagic.wand.usage_exchange", "§7在背包中像收纳袋一样右键法杖，可切换或移出已装填的法术")
            add("item.usefulmagic.magic.level", "§7魔法等级: %s")
            add("item.usefulmagic.magic.base_cost", "§7魔力消耗: %s")
            add("item.usefulmagic.magic.base_damage", "§7法术伤害: %s")
            add("item.usefulmagic.magic.base_cd", "§7法术基本冷却: %s")
            add("item.usefulmagic.magic.base_usage", "§7法术基本蓄力: %s")

            add("item.usefulmagic.magic.barrage_magic", "§7蓄力释放一个魔法球")

            //sounds信息
            add("sounds.usefulmagic.electric_effect", "电击")
            add("sounds.usefulmagic.magic_activate", "魔力激活")
            add("sounds.usefulmagic.magic_sword", "魔法剑")
            add("sounds.usefulmagic.sky_falling_magic_idle", "超位魔法充能")
            add("sounds.usefulmagic.sky_falling_magic_start", "超位魔法起手")
            add("sounds.usefulmagic.defend_shield_hit", "防御魔法防御")
            add("sounds.usefulmagic.magic_explode", "魔力爆炸")
            add("sounds.usefulmagic.star", "星星闪耀")
            add("sounds.usefulmagic.meteor_fall_far", "陨石远处破空")
            add("sounds.usefulmagic.meteor_fall_near", "陨石近地破空")
            add("sounds.usefulmagic.meteor_impact", "陨石冲击")
            add("sounds.usefulmagic.laser_charge_up", "宇宙激光：充能")
            add("sounds.usefulmagic.laser_start", "宇宙激光：发射")
            add("sounds.usefulmagic.laser_loop", "宇宙激光：持续照射")
            add("sounds.usefulmagic.laser_obliteration", "宇宙激光：湮灭爆发")
            add("sounds.usefulmagic.eye_teleport", "魔眼传送")
            add("sounds.usefulmagic.eye_active", "魔眼激活")
            add("sounds.usefulmagic.eye_spawn", "魔眼生成")
            add("sounds.usefulmagic.dragon_spawn_magic_laser", "魔龙召唤激光")
            add("sounds.usefulmagic.small_laser_shoot", "小型激光发射")
            add("sounds.usefulmagic.thorn_hit", "荆棘命中")
            add("sounds.usefulmagic.liquid_ball", "液球")
            add("sounds.usefulmagic.rock_loop", "岩石回旋")

            // gui
            add("screen.title.friend_manager_title", "朋友管理器")
            add("screen.friend_manager.page_prev", "上一页")
            add("screen.friend_manager.page_next", "下一页")
            add("screen.friend_manager.remove", "删除")
            add("screen.friend_manager.page_info", "第 %s / %s 页")
            add("screen.friend_manager.empty", "本页暂无好友")
            add("screen.friend_manager.removed", "已删除好友: %s")
            add("screen.friend_manager.tab.friend_list", "好友列表")
            add("screen.friend_manager.tab.friendly_settings", "友好设置")
            add("screen.friend_manager.friendly_settings_hint", "勾选后将被 FriendManager 视为朋友")
            add("screen.friend_manager.setting.hostile", "敌对生物")
            add("screen.friend_manager.setting.neutral", "中立生物")
            add("screen.friend_manager.setting.non_friend_player", "非好友玩家")
            add("screen.friend_manager.setting.friend_player", "好友玩家")
            add("screen.friend_manager.setting.animal", "动物")
            add("screen.friend_manager.setting.friendly_mob", "友好生物")
            add(
                "screen.friend_manager.setting.tooltip.on",
                "当前已开启：%s 会被视为朋友。\n关闭后：该类型将不再受友好过滤保护，可能被误伤。"
            )
            add(
                "screen.friend_manager.setting.tooltip.off",
                "当前已关闭：%s 不会被视为朋友。\n开启后：该类型会被友好过滤保护，减少误伤。"
            )
            // tutorial book
            add("screen.usefulmagic.tutorial_book.screen_title", "实用魔法手册")
            add("screen.usefulmagic.tutorial_book.page_info", "%s/%s")
            add("screen.usefulmagic.tutorial_book.raw", "%s")
            add("screen.usefulmagic.tutorial_book.home.type_panel_title", "教学分类")
            add("screen.usefulmagic.tutorial_book.home.content_panel_title", "教学内容")
            add("screen.usefulmagic.tutorial_book.home.type_panel_hint", "点击左侧图标查看模块")
            add("screen.usefulmagic.tutorial_book.home.content_panel_hint", "按顺序学习会更快上手")
            add("screen.usefulmagic.tutorial_book.home.quick_start.title", "快速开始")
            add("screen.usefulmagic.tutorial_book.home.quick_start.line1", "1. 左侧选择教学分类")
            add("screen.usefulmagic.tutorial_book.home.quick_start.line2", "2. 打开后按步骤翻页学习")
            add("screen.usefulmagic.tutorial_book.home.quick_start.line3", "3. 先看 结构 -> 注魔祭坛")
            add("screen.usefulmagic.tutorial_book.home.quick_start.line4", "4. 再到 配方 页面查询合成")
            add("screen.usefulmagic.tutorial_book.home.category.structure.tooltip", "多方块结构教学")
            add("screen.usefulmagic.tutorial_book.home.category.recipe.tooltip", "注魔祭坛合成配方")
            add("screen.usefulmagic.tutorial_book.home.category.bestiary.tooltip", "实体图鉴开发中")
            add("screen.usefulmagic.tutorial_book.home.category.structure.label", "结构")
            add("screen.usefulmagic.tutorial_book.home.category.recipe.label", "配方")
            add("screen.usefulmagic.tutorial_book.home.category.bestiary.label", "图鉴")
            add("screen.usefulmagic.tutorial_book.structure.type_panel_title", "结构分类")
            add("screen.usefulmagic.tutorial_book.structure.content_panel_title", "结构教学")
            add("screen.usefulmagic.tutorial_book.structure.type_panel_hint", "建议先看注魔祭坛")
            add("screen.usefulmagic.tutorial_book.structure.content_panel_hint", "选择左侧结构查看详细步骤")
            add("screen.usefulmagic.tutorial_book.structure.category.altar.tooltip", "注魔祭坛")
            add("screen.usefulmagic.tutorial_book.structure.category.formation.tooltip", "阵法")
            add("screen.usefulmagic.tutorial_book.structure.category.altar.label", "祭坛")
            add("screen.usefulmagic.tutorial_book.structure.category.formation.label", "阵法")
            add("screen.usefulmagic.tutorial_book.recipe_main.type_panel_title", "可合成物品")
            add("screen.usefulmagic.tutorial_book.recipe_main.content_panel_title", "配方总览")
            add("screen.usefulmagic.tutorial_book.recipe_main.type_panel_hint", "点击物品进入配方详情")
            add("screen.usefulmagic.tutorial_book.recipe_main.content_panel_hint", "翻页查看全部可用合成")
            add("screen.usefulmagic.tutorial_book.recipe_main.empty.line1", "暂无可显示配方")
            add("screen.usefulmagic.tutorial_book.recipe_main.empty.line2", "请确认世界中已加载配方")
            add("screen.usefulmagic.tutorial_book.recipe_main.guide.title", "配方浏览说明")
            add("screen.usefulmagic.tutorial_book.recipe_main.guide.line1", "1. 左侧点击任意产物")
            add("screen.usefulmagic.tutorial_book.recipe_main.guide.line2", "2. 右页显示祭坛摆放方式")
            add("screen.usefulmagic.tutorial_book.recipe_main.guide.line3", "3. 右下角可翻页查看更多")
            add("screen.usefulmagic.tutorial_book.recipe_main.guide.count", "当前配方数: %s")
            add("screen.usefulmagic.tutorial_book.recipe_detail.content_panel_title", "配方详情")
            add("screen.usefulmagic.tutorial_book.recipe_detail.type_panel_hint", "左侧支持快速切换配方")
            add("screen.usefulmagic.tutorial_book.recipe_detail.content_panel_hint", "中心材料 + 外圈 8 材料")
            add("screen.usefulmagic.tutorial_book.recipe_detail.mana_need", "所需魔力: %s")
            add("screen.usefulmagic.tutorial_book.formation.type_panel_title", "阵法类型")
            add("screen.usefulmagic.tutorial_book.formation.content_panel_title", "阵法示意")
            add("screen.usefulmagic.tutorial_book.formation.type_panel_hint", "点击左侧晶体切换阵法")
            add("screen.usefulmagic.tutorial_book.formation.content_panel_hint", "右侧会显示对应搭建图")
            add(
                "screen.usefulmagic.tutorial_book.formation.tooltip.small",
                "小型阵法\n至少需要 1 个能源水晶\n以及 1 个功能水晶\n有效半径约 32 格\n点击后查看搭建图"
            )
            add(
                "screen.usefulmagic.tutorial_book.formation.tooltip.mid",
                "中型阵法\n需先满足小型阵法条件\n有效半径约 64 格\n点击后查看搭建图"
            )
            add(
                "screen.usefulmagic.tutorial_book.formation.tooltip.large",
                "大型阵法\n需先满足中型阵法条件\n有效半径约 128 格\n点击后查看搭建图"
            )
            add("screen.usefulmagic.tutorial_book.formation.guide.title", "阵法学习流程")
            add("screen.usefulmagic.tutorial_book.formation.guide.line1", "1. 左侧选择阵法规模")
            add("screen.usefulmagic.tutorial_book.formation.guide.line2", "2. 右侧查看对应搭建图")
            add("screen.usefulmagic.tutorial_book.formation.guide.line3", "3. 回到世界按图搭建并激活")
            add("screen.usefulmagic.tutorial_book.altar.type_panel_title", "祭坛材料支持")
            add("screen.usefulmagic.tutorial_book.altar.content_panel_title", "祭坛搭建步骤")
            add("screen.usefulmagic.tutorial_book.altar.type_panel_hint", "悬浮图标可查看方块属性")
            add("screen.usefulmagic.tutorial_book.altar.content_panel_hint", "使用右下角按钮切换步骤")
            add("screen.usefulmagic.tutorial_book.altar.support.table_title", "注魔祭坛支持方块对照表")
            add(
                "screen.usefulmagic.tutorial_book.altar.support.tooltip.coal",
                "煤炭块\n每个方块提供魔力容量: 200\n每个方块提供魔力恢复速度: 1/秒"
            )
            add(
                "screen.usefulmagic.tutorial_book.altar.support.tooltip.iron",
                "铁块\n每个方块提供魔力容量: 400\n每个方块提供魔力恢复速度: 1/秒"
            )
            add(
                "screen.usefulmagic.tutorial_book.altar.support.tooltip.gold",
                "金块\n每个方块提供魔力容量: 400\n每个方块提供魔力恢复速度: 1/秒"
            )
            add(
                "screen.usefulmagic.tutorial_book.altar.support.tooltip.redstone",
                "红石块\n每个方块提供魔力容量: 400\n每个方块提供魔力恢复速度: 1/秒"
            )
            add(
                "screen.usefulmagic.tutorial_book.altar.support.tooltip.diamond",
                "钻石块\n每个方块提供魔力容量: 800\n每个方块提供魔力恢复速度: 2/秒"
            )
            add(
                "screen.usefulmagic.tutorial_book.altar.support.tooltip.emerald",
                "绿宝石块\n每个方块提供魔力容量: 1000\n每个方块提供魔力恢复速度: 2/秒"
            )
            add(
                "screen.usefulmagic.tutorial_book.altar.support.tooltip.netherite",
                "下界合金块\n每个方块提供魔力容量: 1500\n每个方块提供魔力恢复速度: 3/秒"
            )
            add("screen.usefulmagic.tutorial_book.altar.step.1.line1", "先找一个空地，放置注魔输出平台")
            add("screen.usefulmagic.tutorial_book.altar.step.2.line1", "在平台四周放置注魔祭坛方块")
            add("screen.usefulmagic.tutorial_book.altar.step.3.line1", "在输出平台上方放置")
            add("screen.usefulmagic.tutorial_book.altar.step.3.line2", "注魔祭坛核心")
            add("screen.usefulmagic.tutorial_book.altar.step.4.line1", "给下面的 9 个平台放置金属块")
            add("screen.usefulmagic.tutorial_book.altar.step.4.line2", "支持的金属块可以在左侧查看")
            add("screen.usefulmagic.tutorial_book.altar.step.4.line3", "每个平台下方都可以继续堆叠")
            add("screen.usefulmagic.tutorial_book.altar.step.4.line4", "单个平台最多支持 10 个金属块")
            add("screen.usefulmagic.tutorial_book.altar.step.5.line1", "成功激活时会出现粒子效果")
            add("screen.usefulmagic.tutorial_book.altar.step.6.line1", "右键注魔祭坛核心可以查看信息")


            // damage type
            add(
                UsefulMagicDamageTypes.MAGIC,
                $$"%1$s被%2$s释放的魔法抹去了存在"
            )


            // prefer
            add("item.usefulmagic.prefer.levelr5", "§c深度排斥");
            add("item.usefulmagic.prefer.levelr4", "§c强烈排斥");
            add("item.usefulmagic.prefer.levelr3", "§c明显排斥");
            add("item.usefulmagic.prefer.levelr2", "§c轻度排斥");
            add("item.usefulmagic.prefer.levelr1", "§c略微排斥");
            add("item.usefulmagic.prefer.level0", "§7无偏好");
            add("item.usefulmagic.prefer.level1", "§a略微偏好");
            add("item.usefulmagic.prefer.level2", "§a轻度偏好");
            add("item.usefulmagic.prefer.level3", "§a明显偏好");
            add("item.usefulmagic.prefer.level4", "§a强烈偏好");
            add("item.usefulmagic.prefer.level5", "§a极度偏好");

        }
    }

    private fun TranslationBuilder.add(tag: ResourceKey<DamageType>, message: String) {
        add("death.attack." + tag.location().toLanguageKey(), message)
    }
}
