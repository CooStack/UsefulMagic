package cn.coostack.usefulmagic.skill.api

import net.minecraft.entity.LivingEntity

/**
 * @param cd 技能冷却
 * 冷却管理器位于 Skills类
 */
interface Skill {
    /**
     * 存入EntitySkillManager中 执行 EntitySkillManager.choiceSkill()方法时被选中的权重, 值越高 选中的概率越大
     * @see EntitySkillManager.choiceSkill
     */
    var chance: Double

    /**
     * cd的冷却时间
     */

    fun getSkillCountDown(source: LivingEntity): Int

    /**
     * 当此技能被设定为激活时 会执行一次
     * 用于初始化技能属性
     * 注意 所有的属性都需要初始化 因为技能施放时 使用的对象都是同一个
     */
    fun onActive(source: LivingEntity)

    /**
     * 蓄力结束时 执行此代码
     */
    fun onRelease(source: LivingEntity, holdingTick: Int)

    /**
     * 设置最大蓄力上限
     */
    fun getMaxHoldingTick(holdingEntity: LivingEntity): Int

    /**
     * 实体进行蓄力时 执行此代码
     */
    fun holdingTick(holdingEntity: LivingEntity, holdTicks: Int)

    /**
     * 实体蓄力到一半 中断蓄力
     */
    fun stopHolding(entity: LivingEntity, holdTicks: Int)

    /**
     * 获取技能ID 用于施放指定技能
     */
    fun getSkillID(): String

}