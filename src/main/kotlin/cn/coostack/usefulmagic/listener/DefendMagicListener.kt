package cn.coostack.usefulmagic.listener

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.items.prop.DefendCoreItem
import cn.coostack.usefulmagic.particles.emitters.CircleEmitters
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.entity.Entity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import kotlin.math.roundToInt

object DefendMagicListener {
    private fun showDefendSuccess(
        direction: Vec3d?,
        damagedEntity: ServerPlayerEntity,
        sourceEntity: Entity?,
        barrage: Boolean
    ) {
        if (direction == null) {
            // direction为null 说明 sourceEntity 也为null
            showDefendPartSuccess(null, damagedEntity, null)
            return
        }
        // 在entity方向显示一个圆环
        val world = damagedEntity.world
        val emitters =
            CircleEmitters(damagedEntity.eyePos.add(direction.normalize().multiply(1.0)), world)

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
        world!!.playSound(
            null,
            damagedEntity.x,
            damagedEntity.y,
            damagedEntity.z,
            UsefulMagicSoundEvents.DEFEND_SHIELD_HIT,
            SoundCategory.PLAYERS,
            2f,
            1f
        )
        if (!barrage) {
            sourceEntity!!.velocityModified = true
            if (sourceEntity is ProjectileEntity) {
                sourceEntity.velocity = direction.normalize().multiply(1.5)
            } else {
                if (sourceEntity.distanceTo(damagedEntity) < 2f) {
                    sourceEntity.velocity = direction.normalize().multiply(0.5)
                }
            }
        }
    }

    private fun showDefendPartSuccess(
        direction: Vec3d?,
        damagedEntity: ServerPlayerEntity,
        sourceEntity: Entity?
    ) {
        // 身上暴粒子
        val emitters = ExplodeMagicEmitters(
            damagedEntity.eyePos.add(0.0, -0.5, 0.0), damagedEntity.world
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

    fun init() {
        ServerLivingEntityEvents
            .ALLOW_DAMAGE.register { e, s, count ->
                if (e !is ServerPlayerEntity) return@register true
                tryDefend(e, s.source, count, s.source?.pos ?: Vec3d.ZERO, s, false)
            }
    }

    /**
     * 尝试防御
     * @return 是否允许伤害
     */
    fun tryDefend(
        entity: ServerPlayerEntity,
        attacker: Entity?,
        count: Float,
        attackPos: Vec3d,
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
            entity.pos.relativize(attackPos)
        }
        if (mana >= manaCost) {
            data.mana -= manaCost.roundToInt()
            // 防御成功
            showDefendSuccess(sourceDirection, entity, sourceEntity, barrage)
            return false
        }
        data.mana = 0
        val actualDamage = count - mana / 5
        entity.damage(source, actualDamage)
        // 成功了一部分
        showDefendPartSuccess(sourceDirection, entity, sourceEntity)
        return false
    }

}