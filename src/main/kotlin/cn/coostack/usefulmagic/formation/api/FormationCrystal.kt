package cn.coostack.usefulmagic.formation.api

import net.minecraft.util.math.Vec3d

interface FormationCrystal {
    /**
     * 激活的阵法
     */
    var activeFormation: BlockFormation?

    /**
     * 水晶方块所在位置
     */
    var crystalPos: Vec3d

    /**
     * 阵法通过tick方法调用crystal的功能
     */
    fun handle(option: FormationTargetOption): FormationTargetOption

    fun onFormationActive(formation: BlockFormation)

    fun tick() {}
}