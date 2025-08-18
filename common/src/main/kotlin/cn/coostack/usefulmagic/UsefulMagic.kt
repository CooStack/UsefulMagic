package cn.coostack.usefulmagic

import cn.coostack.usefulmagic.beans.MagicPlayerData
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.UsefulMagicItemGroups
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.items.weapon.MagicAxe
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.meteorite.MeteoriteManager
import cn.coostack.usefulmagic.recipe.UsefulMagicRecipeTypes
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.states.ManaServerState
import cn.coostack.usefulmagic.utils.ComboUtil
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * TODO
 * 兼容BUGS (Fabric测试)
 * 1. 所有方块材质无法显示
 * 2. 爆烈魔法的魔力条聚集方向错误
 * 3. Usefulmagic的 创造模式物品栏位置错误
 * 4. 陨石法杖无法正常显示魔法 对标报错 ArrayIndexOutOfBoundsException ParticleDisplayer$ParticleStyleDisplayer.display(ParticleDisplayer.kt:36)
 * 5. 陨石法杖的陨石方块碰撞判定错误
 * 6. 朋友GUI错位
 */
object UsefulMagic {
    @JvmField
    val logger: Logger = LoggerFactory.getLogger("UsefulMagic")
    const val MOD_ID = "usefulmagic"

    lateinit var server: MinecraftServer
    lateinit var state: ManaServerState
    fun init() {
        loadRegistries()
    }

    fun setupServer(server: MinecraftServer) {
        this.server = server
        state = ManaServerState.getFromState(server)
    }

    private fun loadRegistries() {
        UsefulMagicBlocks.init()
        UsefulMagicItems.init()
        UsefulMagicItemGroups.init()
        UsefulMagicEntityTypes.init()
        UsefulMagicDataComponentTypes.init()
        UsefulMagicRecipeTypes.register()
        UsefulMagicSoundEvents.init()
    }

    fun tickServer() {
        state.magicPlayerData.values.forEach(MagicPlayerData::tick)
        state.sendToggle()
        MeteoriteManager.doTick()
        ComboUtil.tick()
        ServerFormationManager.removeNotActiveFormations()
        MagicAxe.postPlayerAxeSkillTick()
    }


}