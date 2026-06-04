package cn.coostack.usefulmagic.skill.api

import net.minecraft.world.entity.LivingEntity
import java.util.LinkedHashMap
import java.util.UUID
import java.util.function.Predicate

abstract class EntitySkillManager(var owner: LivingEntity) {
    val cacheUUID: UUID = UUID.randomUUID()
    protected val skills = LinkedHashMap<String, Skill<LivingEntity>>()

    protected val countdownStorage = LinkedHashMap<String, Int>()
    protected var activeHoldingTick = 0
    var active: Skill<LivingEntity>? = null
        internal set

    fun getSkills(predicate: Predicate<Skill<LivingEntity>>): Map<String, Skill<LivingEntity>> {
        return skills.filter { predicate.test(it.value) }
    }

    open fun addSkill(skill: Skill<out LivingEntity>) {
        val ownerSkill = skill.forOwner()
        skills[ownerSkill.getSkillID()] = ownerSkill
        onSkillAdded(ownerSkill)
    }

    /**
     * 实体死亡时, 需要取消技能释放 (效果)
     */
    fun setEntityDeath() {
        resetActiveSkill(false)
    }

    /**
     * @return true 有可用的技能
     */
    open fun hasAnySkillToChoice(): Boolean {
        return skills.values.any {
            canTriggerSkill(it)
        }
    }

    /**
     * 选择一个技能
     */
    abstract fun choiceSkill(): Skill<LivingEntity>?

    open fun setSkillCountdown(skill: Skill<LivingEntity>) {
        countdownStorage[skill.getSkillID()] = skill.getSkillCountDown(owner)
        onSkillCountdown(skill)
    }


    fun resetActiveSkill(release: Boolean = false) {
        if (!release) {
            active?.stopHolding(owner, activeHoldingTick)
        }
        val activeSkill = active
        (active as? SkillCancelable)?.canceled = true
        active = null
        if (activeSkill != null) {
            onActiveSkillReset(activeSkill, release)
        }
        activeHoldingTick = 0
    }

    fun interruptActiveSkill(setCooldown: Boolean = true) {
        val activeSkill = active ?: return
        if (setCooldown) {
            setSkillCountdown(activeSkill)
        }
        resetActiveSkill(false)
    }

    fun hasCD(id: String): Boolean {
        return countdownStorage.containsKey(id) && (countdownStorage[id] ?: 0) > 0
    }

    /**
     * @param id 技能id
     * @return null 技能不存在或者技能正在冷却
     */
    fun getSkill(id: String): Skill<LivingEntity>? {
        val cd = countdownStorage[id] ?: 0
        if (cd > 0) return null
        return skills[id]
    }

    fun hasActiveSkill(): Boolean {
        return active != null
    }

    fun setActiveSkill(skill: Skill<out LivingEntity>) {

        if (skill is SkillCancelable) {
            skill.canceled = false
        }

        active?.let {
            it.stopHolding(owner, activeHoldingTick)
            onActiveSkillReset(it, false)
        }
        val ownerSkill = skill.forOwner()
        ownerSkill.onActive(owner)
        activeHoldingTick = 0
        active = ownerSkill
        onActiveSkillSet(ownerSkill)
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
        val activeSkill = active ?: return
        if (activeHoldingTick++ >= activeSkill.getMaxHoldingTick(owner)) {
            activeSkill.onRelease(owner, activeHoldingTick)
            setSkillCountdown(activeSkill)
            resetActiveSkill(true)
            return
        }
        activeSkill.holdingTick(owner, activeHoldingTick)

        if (activeSkill is SkillCancelCondition<*>) {
            val condition = activeSkill.asOwnerCancelCondition()
            if (condition.testCancel(owner)) {
                condition.canceled = true
            }
        }

        if (activeSkill is SkillCancelable) {
            val cancelable = activeSkill as SkillCancelable
            val cd = cancelable.cancelSetCD
            if (cancelable.canceled) {
                if (cd) {
                    setSkillCountdown(activeSkill)
                }
                resetActiveSkill(false)
                cancelable.canceled = false
                if (activeSkill is SkillDamageCancelCondition<*>) {
                    activeSkill.asOwnerDamageCancelCondition().damageAmount = 0f
                }
            }
        }
    }

    private fun handleCountDown() {
        val iterator = countdownStorage.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val newCountdown = entry.value - 1
            if (newCountdown <= 0) {
                val skill = skills[entry.key]
                iterator.remove()
                onSkillCooldownFinished(entry.key, skill)
            } else {
                entry.setValue(newCountdown)
            }
        }
    }

    protected fun canTriggerSkill(skill: Skill<LivingEntity>): Boolean {
        if (hasCD(skill.getSkillID())) return false
        return if (skill is SkillCondition<*>) {
            skill.asOwnerCondition().canTrigger(owner)
        } else true
    }

    protected open fun onSkillAdded(skill: Skill<LivingEntity>) {
    }

    protected open fun onSkillCountdown(skill: Skill<LivingEntity>) {
    }

    protected open fun onSkillCooldownFinished(id: String, skill: Skill<LivingEntity>?) {
    }

    protected open fun onActiveSkillSet(skill: Skill<LivingEntity>) {
    }

    protected open fun onActiveSkillReset(skill: Skill<LivingEntity>, release: Boolean) {
    }

    @Suppress("UNCHECKED_CAST")
    private fun Skill<out LivingEntity>.forOwner(): Skill<LivingEntity> {
        return this as Skill<LivingEntity>
    }

    @Suppress("UNCHECKED_CAST")
    private fun SkillCondition<*>.asOwnerCondition(): SkillCondition<LivingEntity> {
        return this as SkillCondition<LivingEntity>
    }

    @Suppress("UNCHECKED_CAST")
    private fun SkillCancelCondition<*>.asOwnerCancelCondition(): SkillCancelCondition<LivingEntity> {
        return this as SkillCancelCondition<LivingEntity>
    }

    @Suppress("UNCHECKED_CAST")
    private fun SkillDamageCancelCondition<*>.asOwnerDamageCancelCondition(): SkillDamageCancelCondition<LivingEntity> {
        return this as SkillDamageCancelCondition<LivingEntity>
    }

}
