package cn.coostack.usefulmagic.test

import cn.coostack.cooparticlesapi.test.GamingTestGroup
import cn.coostack.cooparticlesapi.test.SimpleDisplayEntityOption
import cn.coostack.cooparticlesapi.test.TestManager
import cn.coostack.cooparticlesapi.test.api.TestGroup
import cn.coostack.cooparticlesapi.test.api.TestGroupBuilder
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.display.magic.useful.FlowerDisplay
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

/**
 * 构建 UsefulMagic 的普通游戏测试组。
 *
 * @property player 执行测试的玩家
 */
class UsefulMagicGamingTestBuilder(val player: Player) : TestGroupBuilder {
    /** 普通游戏测试组的注册入口。 */
    companion object {
        /** 普通游戏测试组的注册路径。 */
        private const val ID_PATH = "usefulmagic-test"

        /** 普通游戏测试组的稳定注册 ID。 */
        val ID: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, ID_PATH)

        /** 向 CooParticlesAPI 测试管理器注册普通游戏测试组。 */
        fun init() {
            TestManager.register(ID) {
                UsefulMagicGamingTestBuilder(it)
            }
        }
    }

    override fun groupID(): ResourceLocation {
        return ID
    }

    /**
     * 创建包含花朵展示实体的普通游戏测试组。
     *
     * @return 可由测试控制器执行的普通游戏测试组
     */
    override fun build(): TestGroup {
        val group = GamingTestGroup(player, ID)
            .appendOption {
                SimpleDisplayEntityOption(
                    FlowerDisplay(player.position(), player.level()), -1
                )
            }

        return group
    }
}
