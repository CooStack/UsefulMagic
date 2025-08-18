package cn.coostack.usefulmagic.formation.api

import cn.coostack.usefulmagic.entity.custom.formation.FormationCoreEntity
import it.unimi.dsi.fastutil.booleans.BooleanIntMutablePair
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID

/**
 * 破坏阵法只有2种方法
 * 1. 直接破坏核心方块
 * 2. 使用弹幕攻击核心方块 (只对攻击阵法有用)
 * 3. 使用弹幕攻击防御阵法, 使其收容魔力值<0 (此时核心会爆炸)
 */
interface BlockFormation {
    var world: Level?
    val uuid: UUID

    /**
     * 判断阵法的主人 (玩家UUID)
     * 设置为null代表这是野生的阵法, 或者主人信息丢失
     */
    var owner: UUID?

    var activeCrystals: MutableList<FormationCrystal>

    /**
     * 阵法核心位置
     */
    var formationCore: Vec3

    /**
     * 对阵法进行攻击
     *
     * 防御阵法应该将所有的伤害全都通过此方法转移
     * @param attackerOption 造成伤害的项目
     */
    fun breakFormation(damage: Float, attackerOption: FormationTargetOption?)

    /**
     * 判断这个阵法是否拥有某个水晶类型
     * 攻击, 防护, 储存, 收集(魔力值) 隐藏
     */
    fun hasCrystalType(type: Class<out FormationCrystal>): Boolean

    /**
     * 获取阵法的触发范围
     * 只要在这个范围内, 就会触发阵法BUFF
     */
    fun getFormationTriggerRange(): Double

    /**
     * 获取阵法的规模
     */
    fun getFormationScale(): FormationScale

    /**
     * 判断option是否为友方内容
     */
    fun isFriendly(option: FormationTargetOption): Boolean

    /**
     * 是否符合阵法的成阵条件
     */
    fun canBeFormation(): Boolean

    fun chunkLoaded(): Boolean

    /**
     * 尝试构建阵法
     * 此时才会更新规模
     */
    fun tryBuildFormation(): Boolean

    /**
     * 阵法在激活后, 返回true
     */
    fun isActiveFormation(): Boolean

    fun createFormationEntity(): FormationCoreEntity

    /**
     * 处理阵法的所有功能 (包括防御, 攻击, 储存 等)
     */
    fun tick()

    fun transformMana(requestCrystal: FormationCrystal, count: Int): Boolean

    fun hasManaToTransform(count: Int): Boolean

    /**
     * 可能存在某些非 barrage LivingEntity Projectile 的攻击
     * 为了让防御水晶能够正常防御, 则出此方法
     *
     * @return 攻击是否成功
     */
    fun attack(damage: Float, who: FormationTargetOption?, attackedPos: Vec3): Boolean

}