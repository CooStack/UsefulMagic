package cn.coostack.usefulmagic.items.weapon.magic.attack

import cn.coostack.cooparticlesapi.animation.Animate
import cn.coostack.cooparticlesapi.animation.AnimateManager
import cn.coostack.cooparticlesapi.animation.AnimateNode
import cn.coostack.cooparticlesapi.extend.random
import cn.coostack.cooparticlesapi.extend.times
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.usefulmagic.extend.charging
import cn.coostack.usefulmagic.items.weapon.magic.ChargingMagic
import cn.coostack.usefulmagic.meteorite.MeteoriteAnimateAction
import cn.coostack.usefulmagic.particles.composition.magic.attack.MeteoriteChargingComposition
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.systems.tick.ControlerStatus
import cn.coostack.usefulmagic.utils.MagicHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 对标原先的陨星法杖
 * damage 决定他最终大小 (power)
 * @constructor
 *
 * @param properties
 */
class MeteoriteMagic(properties: Properties) : ChargingMagic<MeteoriteChargingComposition>(properties) {
    override fun onRelease(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        if (world !is ServerLevel) return
        val spellDamage = MagicHelper.getMagicDamage(wandStack).toFloat()
        val size = spellDamage / 20f
        val targetPos = findMeteoriteTarget(world, shooter)
        val spawnPos = shooter.eyePosition
            .add(0.0, 60.0, 0.0)
            .add(
                (Vec3.ZERO.random() * Random.nextDouble(-30.0, 30.0)).with(
                    Direction.Axis.Y,
                    Random.nextDouble(-10.0, 10.0)
                )
            )
        val action = MeteoriteAnimateAction(
            spawnPos,
            targetPos,
            world,
            size,
            spellDamage,
            shooter
        )
        action.explosionPower = calculateExplosionPower(spellDamage)
        action.summingCount = 1 minRangeTo 2
        val animate = Animate()
            .addNode(
                AnimateNode()
                    .addAction(action)
            )
        AnimateManager.displayAnimateServer(animate)
        world.playSound(
            null,
            BlockPos.containing(shooter.position()),
            UsefulMagicSoundEvents.MAGIC_ACTIVATE.get(),
            shooter.soundSource,
            5f,
            0.9f
        )
        world.playSound(
            null,
            BlockPos.containing(shooter.position()),
            UsefulMagicSoundEvents.MAGIC_EXTEND.get(),
            shooter.soundSource,
            5f,
            1.7f
        )
    }

    override fun onCompositionTick(
        composition: MeteoriteChargingComposition,
        shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    ) {
        composition.teleportTo(shooter.position().add(0.0, 0.02, 0.0))
        if (time % 5 == 0) {
            world.playSound(
                null,
                BlockPos.containing(shooter.position()),
                SoundEvents.SHULKER_TELEPORT,
                shooter.soundSource,
                10f,
                2f
            )
        }
    }

    private fun findMeteoriteTarget(world: ServerLevel, shooter: LivingEntity): Vec3 {
        return MagicHelper.findClipTarget(world, shooter, 150.0)
    }

    private fun calculateExplosionPower(spellDamage: Float): Float {
        val safeDamage = spellDamage.coerceAtLeast(1f).toDouble()
        val targetRadius = (2.5 + sqrt(safeDamage) * 0.55).coerceIn(3.0, 12.0)
        return ln(targetRadius).toFloat() * 1.4f
    }

    override fun getComposition(shooter: LivingEntity) = getOrCreateContainer(shooter)
        .get<MeteoriteChargingComposition>()

    override fun getOrCreateComposition(shooter: LivingEntity, world: Level) =
        getOrCreateContainer(shooter).getOrCreate {
            val composition = MeteoriteChargingComposition(shooter.position(), world)
            ParticleCompositionManager.spawn(composition)
            ControlerStatus(composition) {
                shooter.charging && it.isValid()
            }
        } as MeteoriteChargingComposition


}
