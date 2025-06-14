package cn.coostack.usefulmagic.skill.api

import net.minecraft.entity.LivingEntity
import java.util.function.Predicate
import kotlin.random.Random

class EntitySkillManager(var owner: LivingEntity) {
    private val skills = HashMap<String, Skill>()

    private val countdownStorage = HashMap<String, Int>()
    private var activeHoldingTick = 0
    private val random = Random(System.currentTimeMillis())
    var active: Skill? = null
        internal set

    fun getSkills(predicate: Predicate<Skill>): Map<String, Skill> {
        return skills.filter { predicate.test(it.value) }
    }

    fun addSkill(skill: Skill) {
        skills[skill.getSkillID()] = skill
    }

    /**
     * 实体死亡时, 需要取消技能释放 (效果)
     */
    fun setEntityDeath() {
        resetActiveSkill(false)
    }

    /**
     * 根据权重随机选择一个技能
     */
    fun choiceSkill(): Skill? {
        return skills.asSequence()
            .filter {
                val cd = !hasCD(it.key)
                val value = it.value
                if (value is SkillCondition) {
                    value.canTrigger(owner) && cd
                } else cd
            }
            .maxByOrNull {
                random.nextInt(100) * it.value.chance
            }?.value
    }

    fun setSkillCountdown(skill: Skill) {
        countdownStorage[skill.getSkillID()] = skill.getSkillCountDown(owner)
    }


    fun resetActiveSkill(release: Boolean = false) {
        if (!release) {
            active?.stopHolding(owner, activeHoldingTick)
        }
        active = null
        activeHoldingTick = 0
    }

    fun hasCD(id: String): Boolean {
        return countdownStorage.containsKey(id) && (countdownStorage[id] ?: 0) > 0
    }

    /**
     * @param id 技能id
     * @return null 技能不存在或者技能正在冷却
     */
    fun getSkill(id: String): Skill? {
        val cd = countdownStorage[id] ?: 0
        if (cd > 0) return null
        return skills[id]
    }

    fun hasActiveSkill(): Boolean {
        return active != null
    }

    fun setActiveSkill(skill: Skill, cancelBefore: Boolean = false) {
        if (cancelBefore) {
            active?.stopHolding(owner, activeHoldingTick)
        }
        skill.onActive(owner)
        activeHoldingTick = 0
        active = skill
    }

    /**
     * 在LivingEntity 的tick方法中执行
     */
    fun tick() {
        handleCountDown()
        if (!hasActiveSkill()) return
        handleActiveSkill()
    }

    private fun handleActiveSkill() {
        active ?: return
        if (activeHoldingTick++ >= active!!.getMaxHoldingTick(owner)) {
            active!!.onRelease(owner, activeHoldingTick)
            setSkillCountdown(active!!)
            active!!.holdingTick(owner, activeHoldingTick)
            resetActiveSkill(true)
            return
        }
        active!!.holdingTick(owner, activeHoldingTick)

        if (active is SkillCancelCondition) {
            val condition = active as SkillCancelCondition
            if (condition.testCancel(owner)) {
                condition.canceled = true
            }
        }

        if (active is SkillCancelable) {
            val cancelable = active as SkillCancelable
            val cd = cancelable.cancelSetCD
            if (cancelable.canceled) {
                if (cd) {
                    setSkillCountdown(active!!)
                }
                resetActiveSkill(false)
                cancelable.canceled = false
                if (active is SkillDamageCancelCondition) {
                    (active as SkillDamageCancelCondition).damageAmount = 0f
                }
            }
        }
    }

    private fun handleCountDown() {
        val iterator = countdownStorage.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            countdownStorage[entry.key] = entry.value - 1
            if (entry.value <= 0) {
                iterator.remove()
            }
        }
    }

}