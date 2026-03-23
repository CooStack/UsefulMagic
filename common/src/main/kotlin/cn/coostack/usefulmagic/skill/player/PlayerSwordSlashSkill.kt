package cn.coostack.usefulmagic.skill.player

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.extend.mana
import cn.coostack.usefulmagic.extend.multiply
import cn.coostack.usefulmagic.particles.emitters.LineEmitters
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import cn.coostack.usefulmagic.utils.ComboUtil
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

/**
 * 在一秒内进行10次攻击
 * 瞬发
 * 拔刀斩
 * 不断向实体发起攻击
 * 攻击会滞后 tick
 * 范围：AABB.ofSize(center,4,4,4)
 */
class PlayerSwordSlashSkill(val damage: Float) : Skill<LivingEntity>, ComboCondition,
    SkillCancelCondition<LivingEntity> {
    override var chance: Double = 0.5
    override fun getMaxHoldingTick(holdingEntity: LivingEntity): Int {
        return 0
    }

    override fun getSkillCountDown(source: LivingEntity): Int {
        return 0
    }

    override fun onActive(source: LivingEntity) {
        canceled = false


        CooParticlesAPI.scheduler.runTaskTimerMaxTick(2, 20) {
            handleOnceDamage(source)
        }
    }

    override fun onRelease(source: LivingEntity, holdingTick: Int) {
        val state = ComboUtil.getComboState(source.uuid)
        state.count -= triggerComboMin
        if (source is Player) {
            source.mana -= 200
        }
    }

    override fun holdingTick(holdingEntity: LivingEntity, holdTicks: Int) {
    }

    val random = Random(System.currentTimeMillis())
    private fun handleOnceDamage(attacker: LivingEntity) {
        if (attacker !is Player) return
        val targetPos = attacker.eyePosition.add(attacker.forward.multiply(4.0))
        val spawnPos = targetPos.add(
            random.nextDouble(-5.0, 5.0),
            random.nextDouble(-5.0, 5.0),
            random.nextDouble(-5.0, 5.0),
        )
        val dir = spawnPos.relativize(targetPos)
        val emitter = LineEmitters(spawnPos, attacker.level()).apply {
            templateData.apply {
                effect = ControlableCloudEffect(uuid)
                maxAge = 10
                size = 0.1f
            }
            maxTick = 1
            this.endPos = dir.multiply(2.0)
            this.count = (dir.length() * 3).toInt()
        }
        val world = attacker.level()
        world.playSound(
            null, attacker.x, attacker.y, attacker.z,
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 10f, 1.5f
        )
        ParticleEmittersManager.spawnEmitters(emitter)
        world.getEntitiesOfClass(LivingEntity::class.java, AABB.ofSize(targetPos, 6.0, 6.0, 6.0)) {
            it.uuid != attacker.uuid
        }.forEach {
            val source = it.damageSources().playerAttack(attacker)
            it.invulnerableTime = 0
            it.hurt(source, damage)
        }
    }

    override fun stopHolding(entity: LivingEntity, holdTicks: Int) {
        onRelease(entity, holdTicks)
    }

    override fun getSkillID(): String {
        return "player-sword-slash-skill"
    }

    override val triggerComboMin: Int
        get() = 7

    override fun canTrigger(entity: LivingEntity): Boolean {
        if (entity !is ServerPlayer) return false
        val manaData = UsefulMagic.state.getDataFromServer(entity.uuid)
        return super.canTrigger(entity) && entity.mana >= 200
    }

    override fun testCancel(entity: LivingEntity): Boolean {
        return !canTrigger(entity)
    }

    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = false

}
