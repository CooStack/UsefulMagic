package cn.coostack.usefulmagic

import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.data.tracked.TrackerManager
import cn.coostack.usefulmagic.effects.UsefulMagicEffects
import cn.coostack.usefulmagic.entity.MagicEntityDataInit
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.UsefulMagicItemGroups
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.items.prop.FlyingRuneItem
import cn.coostack.usefulmagic.items.weapon.MagicAxe
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.particles.fall.style.GuildCircleStyle
import cn.coostack.usefulmagic.particles.fall.style.SkyFallingStyle
import cn.coostack.usefulmagic.particles.particle.UsefulMagicParticleTypes
import cn.coostack.usefulmagic.particles.style.formation.LargeFormationStyle
import cn.coostack.usefulmagic.particles.style.formation.MidFormationStyle
import cn.coostack.usefulmagic.particles.style.formation.SmallFormationStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.DefendCrystalStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.RecoverCrystalStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.SwordAttackCrystalStyle
import cn.coostack.usefulmagic.recipe.UsefulMagicRecipeTypes
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.states.ManaServerState
import cn.coostack.usefulmagic.systems.tick.ControlerTickSystem
import cn.coostack.usefulmagic.utils.ComboUtil
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// TODO 死亡魔力属性丢失...
object UsefulMagic {
    @JvmField
    val logger: Logger = LoggerFactory.getLogger("UsefulMagic")
    const val MOD_ID = "usefulmagic"

    lateinit var server: MinecraftServer
    lateinit var state: ManaServerState
    fun init() {
        PhaseRegistries.init()
        logger.info("正在加载 UsefulMagic")
        loadRegistries()
        loadStyles()
        PhaseRegistries.init()
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


    private fun loadStyles() {
        ParticleStyleManager.register(GuildCircleStyle::class.java, GuildCircleStyle.Provider())
        ParticleStyleManager.register(SkyFallingStyle::class.java, SkyFallingStyle.Provider())
        ParticleStyleManager.register(DefendCrystalStyle::class.java, DefendCrystalStyle.Provider())
        ParticleStyleManager.register(RecoverCrystalStyle::class.java, RecoverCrystalStyle.Provider())
        ParticleStyleManager.register(SwordAttackCrystalStyle::class.java, SwordAttackCrystalStyle.Provider())
        ParticleStyleManager.register(LargeFormationStyle::class.java, LargeFormationStyle.Provider())
        ParticleStyleManager.register(MidFormationStyle::class.java, MidFormationStyle.Provider())
        ParticleStyleManager.register(SmallFormationStyle::class.java, SmallFormationStyle.Provider())
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
