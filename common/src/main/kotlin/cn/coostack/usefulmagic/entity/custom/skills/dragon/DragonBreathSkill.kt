package cn.coostack.usefulmagic.entity.custom.skills.dragon

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.effects.UsefulMagicEffects
import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import cn.coostack.usefulmagic.entity.util.phases.dragon.DragonCrossFlightPhase
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.particles.entity.dragon.emitter.skills.DragonBreathEmitter
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import cn.coostack.cooparticlesapi.extend.*

/**
 * 需要替换成其他的， 因为这个技能太傻逼了
 *
 * @constructor Create empty Dragon breath skill
 */
class DragonBreathSkill : DragonSkill() {
    companion object {
        const val ID = "dragon_breath_skill"
    }

    override var chance: Double = 1.4

    private var breathEmitter: DragonBreathEmitter? = null

    override fun getSkillCountDown(source: MagicDragonEntity): Int {
        return 48 * 20
    }


    override fun onActive(source: MagicDragonEntity) {
        // 初始化breath
        // 初始化target
        // 修改phase为cross
        val target = source.getCombatTarget() ?: return
        val selectTarget = target.position() ?: return
        source.phaseManager.trySetPhase(
            DragonCrossFlightPhase.HOLDER.get().apply {
                this.runAsIfType<DragonCrossFlightPhase> {
                    impactDamage = 5f
                }
            }
        ) {
            addTarget(selectTarget)
        }
    }

    override fun onRelease(source: MagicDragonEntity, holdingTick: Int) {
        stopHolding(source, holdingTick)
    }

    override fun getMaxHoldingTick(holdingEntity: MagicDragonEntity): Int {
        return 20 * 52
    }

    var arrive = false
    override fun holdingTick(
        holdingEntity: MagicDragonEntity,
        holdTicks: Int
    ) {

        if (holdTicks < 50) {
            return
        }

        val phase = holdingEntity.phaseManager
        breathEmitter ?: let {
            breathEmitter =
                DragonBreathEmitter(holdingEntity.getMouthPosition(holdingEntity.forward), holdingEntity.level())
            breathEmitter!!.apply {
                direction = holdingEntity.forward
                maxTick = -1
            }
            ParticleEmittersManager.spawnEmitters(breathEmitter!!)
        }
        // 喷射
        breathEmitter!!.pos = holdingEntity.getMouthPosition(holdingEntity.forward)
        breathEmitter!!.direction = holdingEntity.forward
        // 范围检测
        // 检测长度 （半径6， 长度7）
        val end = holdingEntity.forward.add(0.0, -0.2, 0.0) * 7
        val aabb = holdingEntity.boundingBox.expandTowards(end)
        val searchedEntities = holdingEntity.level()
            .getEntitiesOfClass(
                LivingEntity::class.java, aabb
            ) {
                it !is MagicDragonEntity && it.isAlive
            }
        searchedEntities.forEach {
            // 进行攻击
            val source = it.damageSources()
                .dragonBreath()
            it.hurt(source, 8f)
            it.addEffect(
                MobEffectInstance(
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(UsefulMagicEffects.MAGIC_SEALED.get()),
                    20 * 8
                )
            )
        }

        val target = phase.peekTargetOrNull() ?: return
        // 在到达之前 （ 8格 ） 都会一直跟随
        val velocity = holdingEntity.deltaMovement
        val box = holdingEntity.boundingBox.inflate(8.0).asHitBox()
        if (!arrive) {
            arrive = Math3DUtil.isPointCrossByBox(
                holdingEntity.position(), velocity,
                box, target
            )
        }
        if (!arrive) {
            phase.setCurrentTarget(holdingEntity.target!!.boxCenterPosition())
        }
    }

    override fun stopHolding(entity: MagicDragonEntity, holdTicks: Int) {
        // RESET （不用修改PHASE)
        breathEmitter?.cancelled = true
        breathEmitter = null
    }

    override fun getSkillID(): String {
        return ID
    }


    override fun testCancel(entity: MagicDragonEntity): Boolean {
        return entity.phaseManager.getCurrentPhaseID() != DragonCrossFlightPhase.ID
    }

    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = true


}
