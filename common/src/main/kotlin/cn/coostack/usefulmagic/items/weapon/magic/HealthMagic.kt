package cn.coostack.usefulmagic.items.weapon.magic

import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.charging
import cn.coostack.usefulmagic.particles.composition.magic.HealthMagicComposition
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.systems.tick.ControlerStatus
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import cn.coostack.usefulmagic.utils.MagicHelper
import net.minecraft.core.BlockPos
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import cn.coostack.cooparticlesapi.extend.*

/**
 * 对标原先的生命恢复法杖
 * (Damage = 生命恢复度 所以这里给负数)
 *
 * 治疗光环的效果
 *
 * 每 （冷却时间） 秒 都会对周围生物进行治疗 （任何友好生物， 如果设定敌对生物友好那也会恢复敌对生物）
 *
 * @constructor Create empty Health magic
 */
class HealthMagic(properties: Properties) : ChargingMagic<HealthMagicComposition>(properties) {

    override fun onRelease(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        world.playSound(
            null,
            BlockPos.containing(shooter.position()),
            UsefulMagicSoundEvents.MAGIC_EXTEND.get(),
            shooter.soundSource,
            10f,
            0.7f
        )
        val friends = world.getEntitiesOfClass(
            LivingEntity::class.java,
            AABB.ofSize(shooter.boxCenterPosition(), 24.0, 24.0, 24.0)
        ) {
            FriendFilterHelper.filterFriend(shooter, it)
        }
        val healthRevive = MagicHelper.getMagicDamage(wandStack)
        friends.take(6).forEach {
            // 然后朝实体发射射线， 然后恢复血量 设置生命恢复药水效果
            // 召唤绿色闪电
            it.health += -healthRevive.toFloat()
            it.addEffect(MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1000, 1))
            it.addEffect(MobEffectInstance(MobEffects.REGENERATION, 1000, 2))

            val emitter = LightningParticleEmitters(shooter.eyePosition.add(0.0, 1.0, 0.0), world)
                .apply {
                    targetPos = it.boxCenterPosition() - pos
                    maxTick = 1
                    templateData.apply {
                        setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
                        color = Math3DUtil.colorOf(
                            191, 239, 192
                        )
                    }
                    simpleData.apply {
                        minAge = 3
                        maxAge = 7
                        minCount = 2
                        maxCount = 5
                        minSize = 0.1
                        maxSize = 0.3
                    }
                }
            ParticleEmittersManager.spawnEmitters(emitter)
        }

    }

    override fun onCompositionTick(
        composition: HealthMagicComposition,
        shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    ) {
        composition.teleportTo(shooter.position())
        // 这里可以生成对应的符文 或者Emitter
    }

    override fun getComposition(shooter: LivingEntity) = getOrCreateContainer(shooter)
        .get<HealthMagicComposition>()

    override fun getOrCreateComposition(
        shooter: LivingEntity,
        world: Level
    ): HealthMagicComposition =
        getOrCreateContainer(shooter).getOrCreate {
            val composition = HealthMagicComposition(shooter.position(), world)
            ParticleCompositionManager.spawn(composition)
            ControlerStatus(composition) {
                shooter.charging && it.isValid()
            }
        } as HealthMagicComposition
}