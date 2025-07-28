package cn.coostack.usefulmagic.formation.api

interface AttackCrystal : FormationCrystal {
    /**
     * 攻击这个option
     */
    fun shoot(option: FormationTargetOption)

    /**
     * @return 需要消耗的能量值
     */
    fun take(): Int

    /**
     * 进行一次shoot后, 下次shoot的间隔时间 tick
     */
    fun duration(): Int

    /**
     * 初始化设置为0
     */
    var currentTime: Int

    override fun handle(option: FormationTargetOption): FormationTargetOption {
        if (option.getWorld().isClient) return option
        if (!activeFormation!!.hasManaToTransform(take())) return option
        if (duration() > currentTime) return option
        currentTime = 0
        shoot(option)
        transformMana()
        return option
    }

    /**
     * 在进行shoot后, 传入魔力值
     */
    fun transformMana(): Boolean {
        val take = take().coerceAtLeast(0)
        activeFormation ?: return false
        if (!activeFormation!!.isActiveFormation()) return false
        return activeFormation!!.transformMana(this, take)
    }

    override fun tick() {
        currentTime++
    }

}