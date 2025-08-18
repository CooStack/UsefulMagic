package cn.coostack.usefulmagic.skill.api

/**
 * 是否可以强制中断技能施放
 */
interface SkillCancelable {
    var canceled: Boolean

    /**
     * 是否在强制中断后设置技能CD
     */
    var cancelSetCD: Boolean
}