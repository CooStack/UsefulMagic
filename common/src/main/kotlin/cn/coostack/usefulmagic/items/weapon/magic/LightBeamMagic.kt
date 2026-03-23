package cn.coostack.usefulmagic.items.weapon.magic

import cn.coostack.cooparticlesapi.data.cache.CacheKey
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.cooparticlesapi.sound.ServerManagedSoundInstance
import cn.coostack.cooparticlesapi.sound.ServerSoundManager
import cn.coostack.cooparticlesapi.sound.SoundVolumeFalloff
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.extend.charging
import cn.coostack.usefulmagic.particles.composition.magic.MagicBeamComposition
import cn.coostack.usefulmagic.renderer.StraightLaserRenderEntity
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.systems.tick.ControlerStatus
import cn.coostack.usefulmagic.systems.tick.ControlerTickSystem
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import cn.coostack.usefulmagic.utils.MagicHelper
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import java.util.function.Supplier

/**
 * 这个场景不适合使用charging magic
 *
 * 因为是阶段性usingTick
 *
 * 这个技能是打死2阶段BOSS的战利品（所以理论上是没有的）
 *
 * @constructor
 *
 * @param properties
 */
class LightBeamMagic(properties: Properties) : MagicItem(properties) {
    companion object {
        @JvmField
        val chargeSoundTag = CacheKey.of<ServerManagedSoundInstance>(
            ResourceLocation.fromNamespaceAndPath(
                UsefulMagic.MOD_ID,
                "chargeSound"
            )
        )

        @JvmField
        val shootingSoundTag = CacheKey.of<ServerManagedSoundInstance>(
            ResourceLocation.fromNamespaceAndPath(
                UsefulMagic.MOD_ID,
                "shootingSound"
            )
        )

        @JvmField
        val beamTag = CacheKey.of<StraightLaserRenderEntity>(
            ResourceLocation.fromNamespaceAndPath(
                UsefulMagic.MOD_ID,
                "beam"
            )
        )

    }

    override fun release(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        clear(shooter)
    }

    override fun usingTick(
        shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    ) {
        val composition = getOrCreateComposition(shooter, world)
        composition.teleportTo(shooter.eyePosition.add(shooter.forward * 5))
        composition.direction = shooter.forward.asRelative()
        // 这里进行step操作

        step(shooter, wandStack, ballStack, time)
    }


    fun step(shooter: LivingEntity, wandStack: ItemStack, ballStack: ItemStack, time: Int) {
        if (time < getMaxTick(shooter, wandStack) / 2) {
            // 蓄力阶段
            return
        }
        val beamStart = shooter.eyePosition.add(shooter.forward * 1.5)
        val beamEnd = beamStart + shooter.forward * 150

        shooter.cacher.getOrCreate(beamTag) {
            StraightLaserRenderEntity(
                shooter.level(),
            ).apply {
                maxRadius = 4f
                color = Math3DUtil.colorOf(255, 100, 200)
                phaseTicks = 10
                lifetime = 100000
                ServerRenderEntityManager.spawn(this)
            }
        }.updateBeam(beamStart, beamEnd)
        // 放下音效
        shooter.cacher.getOrCreate(shootingSoundTag) {
            ServerSoundManager.instance(
                UsefulMagicSoundEvents.DRAGON_HUGE_LASER_SHOOT.get(),
                SoundSource.PLAYERS
            )
                .entity(shooter)
                .visibleRange(256.0)
                .volumeFalloff(SoundVolumeFalloff.QUADRATIC)
                .pitch(1f)
                .volume(0.7f)
                .syncEveryTick(true)
                .layer("huge_beam")
                .stopWhenBoundEntityMissing(false)
                .spawn()
        }
        // 伤害阶段
        attackAsLaser(shooter, beamStart, beamEnd, 4.0, MagicHelper.getMagicDamage(wandStack))
    }


    override fun stopUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        chargingTick: Int,
        max: Boolean
    ) {
        clear(shooter)
    }


    fun clear(shooter: LivingEntity) {
        val cache = shooter.cacher
        cache.remove(chargeSoundTag)?.let {
            if (!it.isStopped) {
                it.fadeOut(20)
            }
        }
        cache.remove(shootingSoundTag)?.let {
            if (!it.isStopped) {
                it.fadeOut(20)
            }
        }
        cache.remove(beamTag)?.discard()
        getOrCreateComposition(shooter, shooter.level())
            .remove()
    }

    override fun startUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack
    ) {
        shooter.cacher.getOrCreate(chargeSoundTag) {
            ServerSoundManager.instance(
                UsefulMagicSoundEvents.DRAGON_HUGE_LASER_CHARGE_UP.get(),
                SoundSource.PLAYERS
            )
                .entity(shooter)
                .visibleRange(256.0)
                .volumeFalloff(SoundVolumeFalloff.QUADRATIC)
                .pitch(1f)
                .volume(0.7f)
                .syncEveryTick(true)
                .layer("huge_beam")
                .stopWhenBoundEntityMissing(false)
                .spawn()
        }
    }

    private fun getOrCreateContainer(shooter: LivingEntity) = ControlerTickSystem.get(shooter.uuid)
    private fun getOrCreateComposition(shooter: LivingEntity, world: Level): MagicBeamComposition {
        return getOrCreateContainer(shooter).getOrCreate {
            controlEntryOf(shooter) {
                MagicBeamComposition(shooter.eyePosition.add(shooter.forward * 5), world).apply {
                    this.direction = shooter.forward.asRelative()
                    ParticleCompositionManager.spawn(this)
                }
            }
        } as MagicBeamComposition
    }

    fun <T : ParticleComposition> controlEntryOf(
        shooter: LivingEntity,
        sup: Supplier<T>
    ): ControlerStatus<ParticleComposition> {
        return ControlerStatus(sup.get()) {
            shooter.charging && it.isValid()
        }
    }

    private fun attackAsLaser(source: LivingEntity, start: Vec3, end: Vec3, r: Double, damage: Double) {
        // 伤害实体
        val direction = end - start
        val laserBox = AABB(start, end).inflate(r)
        val damageSource = UsefulMagicDamageSources.entityDamage(
            source.level(), source, source
        )
        source.level().getEntitiesOfClass(LivingEntity::class.java, laserBox) {
            it.uuid != source.uuid && it.isAlive && FriendFilterHelper.filterNotFriend(source, it)
        }.forEach { entity ->
            val entityCenter = entity.boundingBox.center
            val distanceOnLaser = (entityCenter - start).dot(direction.normalize())
            if (distanceOnLaser !in 0.0..direction.length()) return@forEach

            val closestPoint = start.add(direction.normalize().scale(distanceOnLaser))
            if (entityCenter.distanceToSqr(closestPoint) <= r * r) {
                if (entity.hurt(damageSource, damage.toFloat())) {
                    entity.invulnerableTime = 5
                }
            }
        }
    }
}