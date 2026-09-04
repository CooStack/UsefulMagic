package cn.coostack.usefulmagic.items.weapon.magic.attack

import cn.coostack.cooparticlesapi.display.DisplayEntityManager
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.usefulmagic.display.magic.attack.GoldenMagicDisplay
import cn.coostack.usefulmagic.extend.charging
import cn.coostack.usefulmagic.items.weapon.magic.MagicItem
import cn.coostack.usefulmagic.particles.composition.magic.attack.GoldenMagicChargingComposition
import cn.coostack.usefulmagic.systems.tick.ControlerStatus
import cn.coostack.usefulmagic.systems.tick.ControlerTickSystem
import cn.coostack.usefulmagic.utils.MagicHelper
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * TODO
 * - 1. 音效设计 （生成，发射弹幕）
 * - 2. 生成设计 （出现的时候太突兀了要有动画）
 * - 3. 强度修复 要么高魔消耗 要么超低伤害
 * @param properties
 */
class GoldenMagic(properties: Properties) : MagicItem(properties) {
    override fun release(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        // 释放 Golden Magic
        val display = GoldenMagicDisplay(shooter.eyePosition.add(-1.0, 0.0, 0.0), world)
        display.setShooter(shooter)
        display.damage = MagicHelper.getMagicDamage(wandStack)
        DisplayEntityManager.spawn(display)
        getComposition(shooter)?.teleportTo(shooter.position())
    }

    override fun usingTick(
        shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    ) {
        val composition = getOrCreateComposition(shooter, world)
        composition.teleportTo(shooter.position())
    }

    override fun stopUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        chargingTick: Int,
        max: Boolean
    ) {
        getComposition(shooter)?.remove()
    }

    override fun startUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack
    ) {
        getOrCreateComposition(shooter, world)
    }

    private fun getOrCreateContainer(shooter: LivingEntity) = ControlerTickSystem.get(shooter.uuid)

    private fun getComposition(shooter: LivingEntity) = getOrCreateContainer(shooter)
        .get<GoldenMagicChargingComposition>()

    private fun getOrCreateComposition(shooter: LivingEntity, world: Level) =
        getOrCreateContainer(shooter).getOrCreate {
            val composition = GoldenMagicChargingComposition(shooter.position(), world)
            ParticleCompositionManager.spawn(composition)
            ControlerStatus(composition) {
                shooter.charging && it.isValid()
            }
        }
}