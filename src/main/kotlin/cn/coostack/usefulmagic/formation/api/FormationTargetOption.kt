package cn.coostack.usefulmagic.formation.api

import net.minecraft.entity.damage.DamageSource
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
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
    fun getWorld(): World

    /**
     * 获取目标位置
     */
    fun pos(): Vec3d

    fun movementVec(): Vec3d

    /**
     * 攻击目标
     */
    fun damage(amount: Float, source: DamageSource)

    /**
     * 设置移动方向
     */
    fun setVelocity(dir: Vec3d)

    fun isValid(): Boolean

}

