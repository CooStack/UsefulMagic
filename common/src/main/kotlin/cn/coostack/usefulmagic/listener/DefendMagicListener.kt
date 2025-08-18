package cn.coostack.usefulmagic.listener

import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.items.prop.DefendCoreItem
import cn.coostack.usefulmagic.particles.emitters.CircleEmitters
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.world.entity.Entity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.phys.Vec3
import kotlin.math.roundToInt

object DefendMagicListener {
    private fun showDefendSuccess(
        direction: Vec3?,
        damagedEntity: ServerPlayer,
        sourceEntity: Entity?,
        barrage: Boolean
    ) {
        if (direction == null) {
            // direction为null 说明 sourceEntity 也为null
            showDefendPartSuccess(null, damagedEntity, null)
            return
        }
        // 在entity方向显示一个圆环
        val world = damagedEntity.level()
        val emitters =
            CircleEmitters(damagedEntity.eyePosition.add(direction.normalize().scale(1.0)), world)

        emitters.apply {
            maxTick = 1
            templateData.also {
                it.effect = ControlableCloudEffect(uuid)
                it.size = 0.1f
                it.color = Math3DUtil.colorOf(147, 242, 255)
            }
            circleSpeed = 0.5
            precentDrag = 0.7
            circleDirection = direction
        }
        ParticleEmittersManager.spawnEmitters(emitters)
        world.playSound(
            null,
            damagedEntity.x,
            damagedEntity.y,
            damagedEntity.z,
            UsefulMagicSoundEvents.DEFEND_SHIELD_HIT.get(),
            SoundSource.PLAYERS,
            2f,
            1f
        )
        if (!barrage) {
            sourceEntity!!.hurtMarked = true
            sourceEntity ?: return
            if (sourceEntity is Projectile) {
                sourceEntity.deltaMovement = direction.normalize().scale(1.5)
            } else {
                if (sourceEntity.distanceTo(damagedEntity) < 2f) {
                    sourceEntity.deltaMovement = direction.normalize().scale(0.5)
                }
            }
        }
    }

    private fun showDefendPartSuccess(
        direction: Vec3?,
        damagedEntity: ServerPlayer,
        sourceEntity: Entity?
    ) {
        // 身上暴粒子
        val emitters = ExplodeMagicEmitters(
            damagedEntity.eyePosition.add(0.0, -0.5, 0.0), damagedEntity.level()
        )
        emitters.apply {
            maxTick = 1
            precentDrag = 0.5
            randomParticleAgeMin = 10
            randomParticleAgeMax = 30
            randomCountMin = 10
            randomCountMax = 20
            ballCountPow = 6
            maxSpeed = 3.0
            minSpeed = 0.5
            templateData.also {
                it.effect = ControlableCloudEffect(uuid)
                it.color = Math3DUtil.colorOf(
                    255, 255, 255
                )
            }
        }

        ParticleEmittersManager.spawnEmitters(emitters)
    }

    /**
     * fabric 使用ServerLivingEntityEvents.ALLOW_DAMAGE 注册
     * neoforge还不知道 TODO
     */
    fun call(entity: LivingEntity, source: DamageSource, amount: Float): Boolean {
        if (entity !is ServerPlayer) return true
        return tryDefend(entity, source.entity, amount, source.sourcePosition ?: Vec3.ZERO, source, false)
    }


    /**
     * 尝试防御
     * @return 是否允许伤害
     */
    fun tryDefend(
        entity: ServerPlayer,
        attacker: Entity?,
        count: Float,
        attackPos: Vec3,
        source: DamageSource,
        barrage: Boolean
    ): Boolean {
        // 判断玩家背包内是否有对应的物品
        if (!DefendCoreItem.checkEnabled(entity)) {
            return true
        }
        val data = UsefulMagic.state.getDataFromServer(entity.uuid)
        val mana = data.mana
        if (mana == 0) {
            return true
        }
        val manaCost = count * 5
        val sourceEntity = attacker
        val sourceDirection = attacker?.let {
            entity.position().relativize(attackPos)
        }
        if (mana >= manaCost) {
            data.mana -= manaCost.roundToInt()
            // 防御成功
            showDefendSuccess(sourceDirection, entity, sourceEntity, barrage)
            return false
        }
        data.mana = 0
        val actualDamage = count - mana / 5
        entity.hurt(source, actualDamage)
        // 成功了一部分
        showDefendPartSuccess(sourceDirection, entity, sourceEntity)
        return false
    }

}