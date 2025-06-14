package cn.coostack.usefulmagic.skill.player

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.entity.custom.UnlimitHealthEntity
import cn.coostack.usefulmagic.particles.emitters.LineEmitters
import cn.coostack.usefulmagic.particles.emitters.ShrinkParticleEmitters
import cn.coostack.usefulmagic.particles.style.EnchantLineStyle
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import cn.coostack.usefulmagic.skill.api.SkillCondition
import cn.coostack.usefulmagic.utils.ComboUtil
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Box
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * 在一秒内进行10次攻击
 * 瞬发
 * 拔刀斩
 * 不断向实体发起攻击
 * 攻击会滞后5tick
 * 范围为 Box.of(center,4,4,4)
 */
class PlayerSwordSlashSkill(val damage: Float) : Skill, ComboCondition, SkillCancelCondition {
    override var chance: Double = 0.5
    override fun getMaxHoldingTick(holdingEntity: LivingEntity): Int {
        return 0
    }

    override fun getSkillCountDown(source: LivingEntity): Int {
        return 0
    }

    override fun onActive(source: LivingEntity) {
        canceled = false


        CooParticleAPI.scheduler.runTaskTimerMaxTick(2, 20) {
            handleOnceDamage(source)
        }
    }

    override fun onRelease(source: LivingEntity, holdingTick: Int) {
        val state = ComboUtil.getComboState(source.uuid)
        state.count -= triggerComboMin
        val manaData = UsefulMagic.state.getDataFromServer(source.uuid)
        manaData.mana -= 200
    }

    override fun holdingTick(holdingEntity: LivingEntity, holdTicks: Int) {
    }

    val random = Random(System.currentTimeMillis())
    private fun handleOnceDamage(attacker: LivingEntity) {
        if (attacker !is PlayerEntity) return
        val targetPos = attacker.eyePos.add(attacker.rotationVector.multiply(4.0))
        val spawnPos = targetPos.add(
            random.nextDouble(-5.0, 5.0),
            random.nextDouble(-5.0, 5.0),
            random.nextDouble(-5.0, 5.0),
        )
        val dir = spawnPos.relativize(targetPos)
        val emitter = LineEmitters(spawnPos, attacker.world).apply {
            templateData.apply {
                effect = ControlableCloudEffect(uuid)
                maxAge = 10
                size = 0.1f
            }
            maxTick = 1
            this.endPos = dir.multiply(2.0)
            this.count = (dir.length() * 3).toInt()
        }
        val world = attacker.world
        world.playSound(
            null, attacker.x, attacker.y, attacker.z,
            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 10f, 1.5f
        )
        ParticleEmittersManager.spawnEmitters(emitter)
        world.getEntitiesByClass(LivingEntity::class.java, Box.of(targetPos, 6.0, 6.0, 6.0)) {
            it.uuid != attacker.uuid
        }.forEach {
            val source = it.damageSources.playerAttack(attacker)
            it.timeUntilRegen = 0
            it.hurtTime = 0
            it.damage(source, damage)
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
        if (entity !is ServerPlayerEntity) return false
        val manaData = UsefulMagic.state.getDataFromServer(entity.uuid)
        return super.canTrigger(entity) && manaData.mana >= 200
    }

    override fun testCancel(entity: LivingEntity): Boolean {
        return !canTrigger(entity)
    }

    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = false

}