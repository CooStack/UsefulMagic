package cn.coostack.usefulmagic.skill.api

import net.minecraft.world.entity.LivingEntity
import java.util.UUID
import java.util.function.Predicate
import kotlin.random.Random

class EntitySkillManager(var owner: LivingEntity) {
    val cacheUUID: UUID = UUID.randomUUID()
    private val skills = HashMap<String, Skill<LivingEntity>>()

    private val countdownStorage = HashMap<String, Int>()
    private var activeHoldingTick = 0
    private val random = Random(System.currentTimeMillis())
    var active: Skill<LivingEntity>? = null
        internal set

    fun getSkills(predicate: Predicate<Skill<LivingEntity>>): Map<String, Skill<LivingEntity>> {
        return skills.filter { predicate.test(it.value) }
    }

    fun addSkill(skill: Skill<out LivingEntity>) {
        skills[skill.getSkillID()] = skill.forOwner()
    }

    /**
     * 实体死亡时, 需要取消技能释放 (效果)
     */
    fun setEntityDeath() {
        resetActiveSkill(false)
    }

    /**
     * @return false 没有可用的技能
     */
    fun hasAnySkillToChoice(): Boolean {
        return !skills.keys.any {
            (countdownStorage[it] ?: 0) <= 0
        }
    }

    /**
     * 根据权重随机选择一个技能
     */
    fun choiceSkill(): Skill<LivingEntity>? {
        return skills.asSequence()
            .filter {
                val cd = !hasCD(it.key)
                val value = it.value
                if (value is SkillCondition<*>) {
                    value.asOwnerCondition().canTrigger(owner) && cd
                } else cd
            }
            .maxByOrNull {
                random.nextInt(100) * it.value.chance
            }?.value
    }

    fun setSkillCountdown(skill: Skill<LivingEntity>) {
        countdownStorage[skill.getSkillID()] = skill.getSkillCountDown(owner)
    }


    fun resetActiveSkill(release: Boolean = false) {
        if (!release) {
            active?.stopHolding(owner, activeHoldingTick)
        }
        active = null
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

    fun setActiveSkill(skill: Skill<out LivingEntity>, cancelBefore: Boolean = false) {
        if (cancelBefore) {
            active?.stopHolding(owner, activeHoldingTick)
        }
        val ownerSkill = skill.forOwner()
        ownerSkill.onActive(owner)
        activeHoldingTick = 0
        active = ownerSkill
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
            activeSkill.holdingTick(owner, activeHoldingTick)
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
            countdownStorage[entry.key] = entry.value - 1
            if (entry.value <= 0) {
                iterator.remove()
            }
        }
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
