package cn.coostack.usefulmagic

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.data.tracked.TrackerManager
import cn.coostack.usefulmagic.effects.UsefulMagicEffects
import cn.coostack.usefulmagic.entity.MagicEntityDataInit
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.gamerules.UsefulMagicGameRules
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.UsefulMagicItemGroups
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.items.prop.FlyingRuneItem
import cn.coostack.usefulmagic.items.weapon.MagicAxe
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.particles.particle.UsefulMagicParticleTypes
import cn.coostack.usefulmagic.recipe.UsefulMagicRecipeTypes
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.states.ManaServerState
import cn.coostack.usefulmagic.systems.tick.ControlerTickSystem
import cn.coostack.usefulmagic.test.UsefulMagicBlockTestBuilder
import cn.coostack.usefulmagic.test.UsefulMagicGamingTestBuilder
import cn.coostack.usefulmagic.utils.ComboUtil
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * TODO
 * 1. 龙的动作缺了3个 (忘记补全了)
 * 2. 部分Composition因为转换为GPU粒子的缘故导致不会变化 比如EnchantLine
 * 3. 实体上大部分攻击的拖尾粒子都应该改成GPU模式
 */
object UsefulMagic {
    @JvmField
    val logger: Logger = LoggerFactory.getLogger("UsefulMagic")
    const val MOD_ID = "usefulmagic"

    lateinit var server: MinecraftServer
    lateinit var state: ManaServerState
    fun init() {
        logger.info("正在加载 UsefulMagic")
        UsefulMagicGameRules.init()
        loadRegistries()
        PhaseRegistries.init()
        UsefulMagicGamingTestBuilder.init()
        UsefulMagicBlockTestBuilder.init()
    }

    fun setupServer(server: MinecraftServer) {
        this.server = server
        state = ManaServerState.getFromState(server)
    }

    private fun loadRegistries() {
        MagicEntityDataInit.init()
        UsefulMagicBlocks.init()
        UsefulMagicItems.init()
        UsefulMagicItemGroups.init()
        UsefulMagicEntityTypes.init()
        UsefulMagicDataComponentTypes.init()
        UsefulMagicEffects.init()
        UsefulMagicParticleTypes.init()
        UsefulMagicRecipeTypes.register()
        UsefulMagicSoundEvents.init()
    }



    fun tickServer() {
        state.sendToggle()
        ComboUtil.tick()
        ServerFormationManager.removeNotActiveFormations()
        MagicAxe.postPlayerAxeSkillTick()
        FlyingRuneItem.tickServer()
        TrackerManager.tickOnServer()
        ControlerTickSystem.clearNotValid()
    }


}
