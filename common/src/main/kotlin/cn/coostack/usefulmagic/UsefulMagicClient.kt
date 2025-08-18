package cn.coostack.usefulmagic

import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.control.group.ClientParticleGroupManager
import cn.coostack.usefulmagic.gui.friend.FriendManagerScreen
import cn.coostack.usefulmagic.particles.emitters.UsefulMagicEmitters
import cn.coostack.usefulmagic.particles.fall.style.GuildCircleStyle
import cn.coostack.usefulmagic.particles.fall.style.SkyFallingStyle
import cn.coostack.usefulmagic.particles.group.client.EnchantBallBarrageParticleClient
import cn.coostack.usefulmagic.particles.group.client.GoldenBallBarrageParticleClient
import cn.coostack.usefulmagic.particles.group.client.SingleBarrageParticleClient
import cn.coostack.usefulmagic.particles.style.EnchantLineStyle
import cn.coostack.usefulmagic.particles.style.EndRodExplosionStyle
import cn.coostack.usefulmagic.particles.style.EndRodLineStyle
import cn.coostack.usefulmagic.particles.style.EndRodSwordStyle
import cn.coostack.usefulmagic.particles.style.LightStyle
import cn.coostack.usefulmagic.particles.style.TestStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.AntiEntityWandBarrageStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.AntiEntityWandSpellcasterStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.AntiEntityWandStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.CopperMagicStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.DiamondWandStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.GoldenWandStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.HealthReviveStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.NetheriteWandStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.WandMeteoriteSpellcasterStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.WandMeteoriteStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.WandMeteoriteTargetStyle
import cn.coostack.usefulmagic.particles.style.entitiy.BookEntityDeathStyle
import cn.coostack.usefulmagic.particles.style.entitiy.CraftingLevel1Style
import cn.coostack.usefulmagic.particles.style.entitiy.CraftingLevel2Style
import cn.coostack.usefulmagic.particles.style.entitiy.CraftingLevel3Style
import cn.coostack.usefulmagic.particles.style.entitiy.MagicBookSpawnStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionMagicBallStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionMagicStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionStarStyle
import cn.coostack.usefulmagic.particles.style.formation.LargeFormationStyle
import cn.coostack.usefulmagic.particles.style.formation.MidFormationStyle
import cn.coostack.usefulmagic.particles.style.formation.SmallFormationStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.DefendCrystalStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.RecoverCrystalStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.SwordAttackCrystalStyle
import cn.coostack.usefulmagic.particles.style.skill.BookShootSkillStyle
import cn.coostack.usefulmagic.particles.style.skill.GiantSwordStyle
import cn.coostack.usefulmagic.particles.style.skill.SwordLightStyle
import cn.coostack.usefulmagic.particles.style.skill.TaiChiStyle
import com.mojang.authlib.minecraft.client.MinecraftClient
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import java.awt.im.InputContext


object UsefulMagicClient {
    lateinit var friendUIBinding: KeyMapping

    fun init() {
        loadParticleStyles()
    }

    fun loadKeyBindings(key: KeyMapping) {
        friendUIBinding = key
    }

    private fun loadParticleStyles() {
        ClientParticleGroupManager.register(
            SingleBarrageParticleClient::class.java,
            SingleBarrageParticleClient.Provider()
        )
        ClientParticleGroupManager.register(
            EnchantBallBarrageParticleClient::class.java,
            EnchantBallBarrageParticleClient.Provider()
        )
        ClientParticleGroupManager.register(
            GoldenBallBarrageParticleClient::class.java,
            GoldenBallBarrageParticleClient.Provider()
        )

        ParticleStyleManager.register(
            CopperMagicStyle::class.java, CopperMagicStyle.Provider()
        )

        ParticleStyleManager.register(GoldenWandStyle::class.java, GoldenWandStyle.Provider())
        ParticleStyleManager.register(HealthReviveStyle::class.java, HealthReviveStyle.Provider())
        ParticleStyleManager.register(EnchantLineStyle::class.java, EnchantLineStyle.Provider())
        ParticleStyleManager.register(LightStyle::class.java, LightStyle.Provider())
        ParticleStyleManager.register(TestStyle::class.java, TestStyle.Provider())
        ParticleStyleManager.register(EndRodSwordStyle::class.java, EndRodSwordStyle.Provider())
        ParticleStyleManager.register(DiamondWandStyle::class.java, DiamondWandStyle.Provider())
        ParticleStyleManager.register(WandMeteoriteStyle::class.java, WandMeteoriteStyle.Provider())
        ParticleStyleManager.register(WandMeteoriteTargetStyle::class.java, WandMeteoriteTargetStyle.Provider())
        ParticleStyleManager.register(
            WandMeteoriteSpellcasterStyle::class.java,
            WandMeteoriteSpellcasterStyle.Provider()
        )
        ParticleStyleManager.register(EndRodExplosionStyle::class.java, EndRodExplosionStyle.Provider())
        ParticleStyleManager.register(NetheriteWandStyle::class.java, NetheriteWandStyle.Provider())
        ParticleStyleManager.register(
            AntiEntityWandSpellcasterStyle::class.java,
            AntiEntityWandSpellcasterStyle.Provider()
        )
        ParticleStyleManager.register(AntiEntityWandStyle::class.java, AntiEntityWandStyle.Provider())
        ParticleStyleManager.register(AntiEntityWandBarrageStyle::class.java, AntiEntityWandBarrageStyle.Provider())
        ParticleStyleManager.register(EndRodLineStyle::class.java, EndRodLineStyle.Provider())
        ParticleStyleManager.register(CraftingLevel1Style::class.java, CraftingLevel1Style.Provider())
        ParticleStyleManager.register(CraftingLevel2Style::class.java, CraftingLevel2Style.Provider())
        ParticleStyleManager.register(CraftingLevel3Style::class.java, CraftingLevel3Style.Provider())
        ParticleStyleManager.register(ExplosionStarStyle::class.java, ExplosionStarStyle.Provider())
        ParticleStyleManager.register(ExplosionMagicStyle::class.java, ExplosionMagicStyle.Provider())
        ParticleStyleManager.register(ExplosionMagicBallStyle::class.java, ExplosionMagicBallStyle.Provider())
        ParticleStyleManager.register(TaiChiStyle::class.java, TaiChiStyle.Provider())
        ParticleStyleManager.register(BookShootSkillStyle::class.java, BookShootSkillStyle.Provider())
        ParticleStyleManager.register(BookEntityDeathStyle::class.java, BookEntityDeathStyle.Provider())
        ParticleStyleManager.register(MagicBookSpawnStyle::class.java, MagicBookSpawnStyle.Provider())
        ParticleStyleManager.register(SwordLightStyle::class.java, SwordLightStyle.Provider())
        ParticleStyleManager.register(GiantSwordStyle::class.java, GiantSwordStyle.Provider())
        ParticleStyleManager.register(DefendCrystalStyle::class.java, DefendCrystalStyle.Provider())
        ParticleStyleManager.register(RecoverCrystalStyle::class.java, RecoverCrystalStyle.Provider())
        ParticleStyleManager.register(SwordAttackCrystalStyle::class.java, SwordAttackCrystalStyle.Provider())
        ParticleStyleManager.register(SmallFormationStyle::class.java, SmallFormationStyle.Provider())
        ParticleStyleManager.register(MidFormationStyle::class.java, MidFormationStyle.Provider())
        ParticleStyleManager.register(LargeFormationStyle::class.java, LargeFormationStyle.Provider())
        ParticleStyleManager.register(SkyFallingStyle::class.java, SkyFallingStyle.Provider())
        ParticleStyleManager.register(GuildCircleStyle::class.java, GuildCircleStyle.Provider())
        UsefulMagicEmitters.init()
        UsefulMagic.logger.debug("客户端粒子样式注册完成")
    }

    fun tickClient() {
        val minecraft = Minecraft.getInstance()
        if (friendUIBinding.isDown) {
            minecraft.setScreen(FriendManagerScreen())
        }
    }
}