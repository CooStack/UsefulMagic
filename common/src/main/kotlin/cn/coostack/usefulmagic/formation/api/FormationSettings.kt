package cn.coostack.usefulmagic.formation.api

class FormationSettings {
    /**
     * 阻止敌对生物
     */
    var hostileEntityAttack = true

    /**
     * 阻止动物 (攻击动物)
     */
    var animalEntityAttack = false

    /**
     * 阻止非好友玩家
     */
    var playerEntityAttack = true

    /**
     * 阻止不符合上面两项的 LivingEntity
     */
    var anotherEntityAttack = true

    /**
     * 只在阵法被激活时显示阵法粒子
     * 设置为false则始终显示
     */
    var displayParticleOnlyTrigger = true

    /**
     * 设置为-1.0 则和有效范围相同
     * 触发阵法时的范围
     *
     * 触发阵法时, 在0 - 有效范围内均会持续生效
     * 直到离开了有效范围为止, 继续隐藏
     * @see FormationScale
     */
    var triggerRange = -1.0


}