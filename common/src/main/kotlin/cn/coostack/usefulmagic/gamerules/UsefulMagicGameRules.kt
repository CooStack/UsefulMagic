package cn.coostack.usefulmagic.gamerules

import cn.coostack.usefulmagic.mixin.GameRulesBooleanValueInvoker
import cn.coostack.usefulmagic.mixin.GameRulesInvoker
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level

object UsefulMagicGameRules {
    /**
     * 是否允许本模组造成地形破坏。默认 true (开启)。
     */
    @JvmField
    val MAGIC_TERRAIN_DESTRUCTION: GameRules.Key<GameRules.BooleanValue> =
        GameRulesInvoker.`usefulmagic$register`(
            "magicTerrainDestruction",
            GameRules.Category.MISC,
            GameRulesBooleanValueInvoker.`usefulmagic$create`(true)
        )

    fun init() {
    }

    /**
     * 当前世界是否允许本模组造成地形破坏。
     *
     * 客户端世界无法可靠读取服务端游戏规则, 所有破坏逻辑都应在服务端调用, 这里默认按 true 处理客户端。
     */
    fun canDestroyTerrain(level: Level): Boolean {
        return level.gameRules.getBoolean(MAGIC_TERRAIN_DESTRUCTION)
    }

    /**
     * 便捷重载: 通过实体所在世界判断。
     */
    fun canDestroyTerrain(entity: Entity): Boolean {
        return canDestroyTerrain(entity.level())
    }
}
