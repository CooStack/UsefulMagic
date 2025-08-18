package cn.coostack.usefulmagic.formation.api

import cn.coostack.usefulmagic.formation.target.BarrageTargetOption
import net.minecraft.world.phys.Vec3

interface DefendCrystal : FormationCrystal {

    /**
     * 如果在墙的范围内, 则阻挡
     * @return 应该扣除的魔力值
     */
    fun defendEntities(target: FormationTargetOption, wallInner: Boolean): Int

    /**
     * 获取墙的厚度
     */
    fun getWallWidth(): Int

    fun canDefendBarrage(target: BarrageTargetOption): Boolean
    fun canDefendEntities(target: FormationTargetOption): Boolean

    /**
     * @return 应该扣除的魔力值
     */
    fun defendBarrage(target: BarrageTargetOption): Int
    fun displayDeterParticle(deterPos: Vec3, deterDirection: Vec3)

    override fun handle(option: FormationTargetOption): FormationTargetOption {
        if (option.getWorld().isClientSide) return option
        val width = getWallWidth() / 2.0
        val midRange = activeFormation!!.getFormationTriggerRange() - width
        val innerRange = midRange - width
        val outerRange = midRange + width
        // 首先判断在内的
        val distance = option.pos().distanceTo(activeFormation!!.formationCore)
        val isInner = distance in 0.0..innerRange
        if (isInner) return option

        // 如果是弹幕, 直接设置损坏
        if (option is BarrageTargetOption) {
            if (canDefendBarrage(option)) {
                val barrageMana = defendBarrage(option)
                transformMana(barrageMana)
            }
            return option
        }
        //该方法执行时自带条件
        // 自带条件
        // distance in 0 .. outerRange
        val wallInner = distance < midRange
        val can = canDefendEntities(option)
        if (can) {
            val pushMana = defendEntities(option, wallInner)
            transformMana(pushMana)
        }
        return option
    }

    /**
     * 在进行阻止时 消耗的魔力值
     */
    fun transformMana(count: Int): Boolean {
        val take = 1
        activeFormation ?: return false
        if (!activeFormation!!.isActiveFormation()) return false
        return activeFormation!!.transformMana(this, take)
    }
}