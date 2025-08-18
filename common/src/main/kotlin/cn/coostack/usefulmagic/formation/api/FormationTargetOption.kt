package cn.coostack.usefulmagic.formation.api

import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID

/**
 * 实体 弹幕 都会成为Formation Target
 */
interface FormationTargetOption {
    /**
     * 获取对应的UniqueID
     */
    fun getUniqueID(): UUID

    /**
     * 如果是弹幕或者Projectile
     * 则返回 shooter的uuid
     * 否则返回自己的uuid
     */
    fun getOwnerUUID(): UUID?

    /**
     * 获取所在的世界
     */
    fun getWorld(): Level

    /**
     * 获取目标位置
     */
    fun pos(): Vec3

    fun movementVec(): Vec3

    /**
     * 攻击目标
     */
    fun damage(amount: Float, source: DamageSource)

    /**
     * 设置移动方向
     */
    fun setVelocity(dir: Vec3)

    fun isValid(): Boolean

}

