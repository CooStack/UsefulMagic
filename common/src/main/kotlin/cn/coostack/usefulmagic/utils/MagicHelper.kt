package cn.coostack.usefulmagic.utils

import cn.coostack.cooparticlesapi.extend.plus
import cn.coostack.cooparticlesapi.extend.times
import cn.coostack.usefulmagic.extend.mana
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import kotlin.math.roundToInt

object MagicHelper {

    fun getMagicDamage(wand: ItemStack): Double {
        val ball = wand.get(UsefulMagicDataComponentTypes.WAND_MAGIC.get()) ?: return 0.0
        val base = ball.get(UsefulMagicDataComponentTypes.MAGIC_BASE_DAMAGE.get()) ?: return 0.0
        val prefer = wand.get(UsefulMagicDataComponentTypes.WAND_PREFER.get()) ?: return 0.0
        val wandReduction = prefer.getDamageFactor(ball)
        // 伤害是加成 所以要用+
        return (1 + wandReduction) * base
    }

    /**
     * 获取最大蓄力时间
     * - 如果法球自动连发， 那么达到后就会自动调用stopCharge， 然后再次调用startCharge
     * - 否则就会一直停留直到手动松开按键（蓄力）
     * @param wand
     * @return 0 不存在法珠 -1 出现了错误 或者等级不够
     */
    fun getMaxChargingTick(wand: ItemStack): Int {
        val factor = wand.get(UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get()) ?: return -1
        val magicBall = wand.get(UsefulMagicDataComponentTypes.WAND_MAGIC.get()) ?: return 0
        if (!isLevelEnough(wand, magicBall)) {
            return 0
        }
        val baseTime = magicBall.get(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get()) ?: return -1
        val minUsage = magicBall.get(UsefulMagicDataComponentTypes.MAGIC_MIN_USAGE.get()) ?: 1
        val wandAddition = (baseTime * factor)
        val prefer = wand.get(UsefulMagicDataComponentTypes.WAND_PREFER.get()) ?: return -1
        val usageReduction = prefer.getUsageFactor(magicBall)
        val finalTime = ((1 - usageReduction) * wandAddition).roundToInt().coerceAtLeast(minUsage)
        return finalTime
    }

    fun getManaCost(wand: ItemStack): Int {
        val ball = wand.get(UsefulMagicDataComponentTypes.WAND_MAGIC.get()) ?: return 0
        val base = ball.get(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get()) ?: return 0
        val effect = wand.get(UsefulMagicDataComponentTypes.WAND_REDUCTION.get()) ?: return 0
        val prefer = wand.get(UsefulMagicDataComponentTypes.WAND_PREFER.get()) ?: return 0
        val wandReduction = prefer.getManaReductionFactor(ball)
        val wandAddition = (base * (1 - effect))
        val final = ((1 - wandReduction) * wandAddition).coerceAtLeast(0.0).roundToInt()
        return final
    }

    fun getFinalCD(wand: ItemStack): Int {
        val ball = wand.get(UsefulMagicDataComponentTypes.WAND_MAGIC.get()) ?: return 0
        val base = ball.get(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get()) ?: return 0
        val effect = wand.get(UsefulMagicDataComponentTypes.WAND_SPEED_FACTOR.get()) ?: return 0
        val wandAddition = effect * base
        val prefer = wand.get(UsefulMagicDataComponentTypes.WAND_PREFER.get()) ?: return 0
        val wandReduction = prefer.getCdReductionFactor(ball)
        val final = ((1 - wandReduction) * wandAddition).roundToInt().coerceAtLeast(0)
        return final
    }

    /**
     * # 判断用户是否拥有足够的魔力
     * 当存在禁魔Effect时 始终返回false
     *
     * @param user
     * @param wand
     * @return
     */
    fun isManaEnough(user: LivingEntity, wand: ItemStack): Boolean {
        // 判断effect TODO Effect开发
        if (user !is Player) {
            return true
        }
        val infinity = user.isCreative || user.isSpectator
        if (infinity) {
            return true
        }
        val userMana = user.mana
        val cost = getManaCost(wand)
        return userMana >= cost
    }

    fun isLevelEnough(wand: ItemStack, ball: ItemStack): Boolean {
        val ballLevel = ball.get(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get()) ?: return false
        val wandLevel = wand.get(UsefulMagicDataComponentTypes.WAND_LEVEL.get()) ?: return false
        return ballLevel <= wandLevel
    }

    /**
     * 通过target的视线方向搜索区间内的方块和实体
     *
     * @param shooter 发射实体
     * @param distance 有效距离
     * @param crossEntity 是否无视实体
     * @param crossBlock 是否无视方块
     * @return 具体点
     */
    fun findClipTarget(
        world: Level,
        shooter: LivingEntity,
        distance: Double = 150.0,
        crossEntity: Boolean = false,
        crossBlock: Boolean = false,
    ): Vec3 {
        val start = shooter.eyePosition
        val look = shooter.lookAngle.normalize()
        val fallbackTarget = start + look * distance
        val blockHit = if (!crossBlock) world.clip(
            ClipContext(
                start,
                fallbackTarget,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                shooter
            )
        ) else null
        val blockCandidate = if (blockHit?.type == HitResult.Type.BLOCK) {
            TargetCandidate(blockHit.location, start.distanceTo(blockHit.location))
        } else {
            null
        }
        val searchBox = AABB(start, fallbackTarget).inflate(2.0)
        val entityCandidate = if (!crossEntity) {
            world.getEntitiesOfClass(LivingEntity::class.java, searchBox) { target ->
                target != shooter && target.isAlive && FriendFilterHelper.filterNotFriend(shooter, target)
            }.mapNotNull { target ->
                val hitPos =
                    target.boundingBox.inflate(0.5).clip(start, fallbackTarget).orElse(null) ?: return@mapNotNull null
                val distance = start.distanceTo(hitPos)
                if (hitPos.subtract(start).dot(look) <= 0.0 || distance > 100.0) {
                    return@mapNotNull null
                }
                TargetCandidate(target.boundingBox.center, distance)
            }.minByOrNull { it.distance }
        } else null

        return when {
            entityCandidate != null && blockCandidate != null -> {
                if (entityCandidate.distance <= blockCandidate.distance) entityCandidate.position else blockCandidate.position
            }

            entityCandidate != null -> entityCandidate.position
            blockCandidate != null -> blockCandidate.position
            else -> fallbackTarget
        }
    }

    private data class TargetCandidate(
        val position: Vec3,
        val distance: Double
    )
}